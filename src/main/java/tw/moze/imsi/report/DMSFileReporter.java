package tw.moze.imsi.report;

import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Response;
import tw.moze.imsi.redis.RedisUtil;
import tw.moze.imsi.util.StatFile;
import tw.moze.util.concurrent.CountUpDownLatch;
import tw.moze.util.dev.XXX;
import tw.moze.util.fileformat.FieldCSVReader;
import tw.moze.util.fileutil.FileUtils;
import tw.moze.util.json.JsonUtil;
import tw.moze.imsi.redis.JedisClusterPipeline;
import tw.moze.imsi.redis.RedisBatchRunner2;

public class DMSFileReporter implements Runnable {
	private CountUpDownLatch latch;
	private File statFile;
	private File csvFile;
	private List<Map<String, String>> list;

	private int matched;
	private FieldCSVReader reader;

	public DMSFileReporter (CountUpDownLatch latch, File statFile, File csvFile, List<Map<String, String>> list) {
		this.latch = latch;
		this.statFile = statFile;
		this.csvFile = csvFile;
		this.list = list;
	}

	@Override
	public void run() {
		try {
			Map<String, Integer> stat = readStat();
			// 如果檔案時間在 80 分鐘以內，會重新計算 stat.json
			if (System.currentTimeMillis() - csvFile.lastModified() < 80 * 60 * 1000) {
				checkCSV(stat);
			}
			list.add(computeStat(stat));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			latch.countDown();
			XXX.out("Job done. remains: " + latch.getCount());
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Integer> readStat() throws IOException {
		String jsonString = FileUtils.readFile(statFile.getAbsolutePath());
		return JsonUtil.fromJsonString(jsonString, LinkedHashMap.class);
	}

	private Map<String, String> computeStat(Map<String, Integer> stat) {
		Map<String, String> map = new LinkedHashMap<>();
		for(Map.Entry<String, Integer> entry: stat.entrySet()) {
			if ("File Error".equals(entry.getKey()))
				map.put("Session Error", String.valueOf(entry.getValue()));
			else
				map.put(entry.getKey(), String.valueOf(entry.getValue()));
		}

		String filename = csvFile.getAbsolutePath();

		double matchRate = 0d;
		double imsiRate = 0d;
		double keyRate = 0d;
		double imeiRate = 0d;

		Integer m = getStat(stat, "Matched", 0);
		Integer i = getStat(stat, "IMSI(string)", 0);
		Integer k = getStat(stat, "Key", 0);
		Integer e = getStat(stat, "IMEI(string)", 0);
		Integer t = getStat(stat, "Total", 0);
		if (t != 0) {
			imsiRate = i.doubleValue()/t.doubleValue();
			matchRate = m.doubleValue()/t.doubleValue();
			keyRate = k.doubleValue()/t.doubleValue();
			imeiRate = e.doubleValue()/t.doubleValue();
		}

		map.put("IMSI Rate", Math.round(imsiRate*10000)/100d + "%");
		map.put("Key Rate", Math.round(keyRate*10000)/100d + "%");
		map.put("IMEI Rate", Math.round(imeiRate*10000)/100d + "%");
		map.put("Match Rate", Math.round(matchRate*10000)/100d + "%");
		map.put("File Path", filename);
		map.put("File Time", getFileTime(filename));
		map.put("Sync Time", getSyncTime(csvFile));
		return map;
	}

	private Integer getStat(Map<String, Integer> stat, String field, Integer def) {
		Integer v = stat.get(field);
		return v == null ? def : v;
	}

	private void checkCSV(Map<String, Integer> stat) {
		JedisCluster jedis = null;
		matched = 0;
		try {
			reader = new FieldCSVReader(new FileReader(csvFile));
			reader.readHeader();

			jedis = RedisUtil.getResource();

			Map<String, Integer> headerMap = reader.getHeaderMap();
			DMSRecallRunner recallRunner = new DMSRecallRunner(jedis, 500);

			while (reader.next()) {
				if (reader.getValueCount() != headerMap.size()) {
					XXX.out("error line >>> " + Arrays.toString(reader.getValues()));
					continue;
				}
				String[] cells = reader.getValues();
				recallRunner.send(cells);
			}

			recallRunner.flush();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		finally {
			stat.put("Matched", matched);
			StatFile.save(statFile.getAbsolutePath(), stat);

			safeClose(reader);
//			safeClose(jedis);
		}
	}

	private void safeClose(Closeable res) {
		if (res != null) {
			try {
				res.close();
			} catch (IOException e) {
				;
			}
		}
	}

	private boolean isEmpty(String val) {
		return (val == null || val.trim().isEmpty());
	}

	private Pattern p = Pattern.compile("data_(\\d+)_");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private String getFileTime(String path) {
		Matcher m = p.matcher(path);
		if (m.find()) {
			String v = m.group(1);
			long l = Long.valueOf(v);
			return sdf2.format(new Date(l * 1000));
		}
		return "";
	}

	private String getSyncTime(File f) {
		long l = f.lastModified();
		return sdf2.format(new Date(l));
	}

	class DMSRecallRunner extends RedisBatchRunner2<Response<String>> {
		public DMSRecallRunner(JedisCluster jedis, int batchSize) {
			super(jedis, batchSize);
		}

		@Override
		public void invoke(JedisClusterPipeline pipeline, String[] vals, List<Response<String>> results) {
			String mme_id    = reader.get("MME UE S1AP ID(int64)", vals);
			String enb_id    = reader.get("eNB UE S1AP ID(int64)", vals);

			if (isEmpty(mme_id) || isEmpty(enb_id))
				return;

			String key = String.format("imsi-found:%s-%s", mme_id, enb_id);
			results.add(pipeline.get(key));
		}

		@Override
		public void result(Response<String> res) {
			try {
				String imsi = res.get();
				if (!isEmpty(imsi)) {
					matched++;
				}
			}
			catch (Throwable ex) {
				XXX.out("Error query redis:");
				ex.printStackTrace();
			}
		}
	}
}
