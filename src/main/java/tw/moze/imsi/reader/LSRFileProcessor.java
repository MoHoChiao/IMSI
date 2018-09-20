package tw.moze.imsi.reader;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.opencsv.CSVWriter;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Response;
import tw.moze.imsi.redis.RedisUtil;
import tw.moze.util.collection.CountableMap;
import tw.moze.util.concurrent.CountUpDownLatch;
import tw.moze.util.dev.XXX;
import tw.moze.util.fileformat.FieldCSVReader;
import tw.moze.util.fileutil.FileExtUtils;
import tw.moze.util.fileutil.FileUtils;
import tw.moze.util.json.JsonUtil;
import tw.moze.imsi.redis.JedisClusterPipeline;
import tw.moze.imsi.redis.RedisBatchRunner2;

class LSRFileProcessor implements Runnable {
	File srcFile, outFile, statFile;
	CountUpDownLatch latch;
	Map<String, Long> fileSizeMap;
	Long mappedCount = 0L;
	Long blankCount = 0L;
	Long nokeyCount = 0L;
	Long dmsNotMatchCount = 0L;
	Long noImeiCount = 0L;
	Long keyNoImeiCount = 0L;
	Long ImeiNoKeyCount = 0L;
	Long keyNoImsiCount = 0L;
	Long ImeiNoImsiCount = 0L;
	Long ImeiNoNewImsiCount = 0L;
	Long ImsiNoKeyCount = 0L;
	Long ImsiNoImeiCount = 0L;
	Long NewImsiNoImeiCount = 0L;
	Long NewImeiCount = 0L;
	Long NewImsiNoNewImeiCount = 0L;
	Long KeyMappingCount = 0L;
	Long ImeiMappingCount = 0L;
	Long DmsMappinCount = 0L;
	Long KeyNoImeiMappinfCount = 0L;
	Long ImeiNoKeyMappinfCount = 0L;
	Long ErrorCount = 0L;

	FieldCSVReader reader = null;
	CSVWriter writer = null;
	CSVWriter missingWriter = null;
	CSVWriter mappingWriter = null;
	IMSI_querier imSiQuerier;
	IMEI_querier imEiQuerier;
	LSRMatchWriter matchWriter;

	private static String[] fieldsToRead = {
			"IMSI",
			"IMEI",
//			"Start m-TMSI",
//			"End m-TMSI",
			"Start GUMMEI",
			"End GUMMEI",
			"Start Time",
			"End Time",
			"Start Type",
			"End Type",
			"Start MME UE S1AP ID",
			"End MME UE S1AP ID",
			"Start eNB UE S1AP ID",
			"End eNB UE S1AP ID",
			};

	private static String[] mappingFileFields = {
			"Orig IMSI",
			"IMEI",
			"Start GUMMEI",
			"End GUMMEI",
			"Start Time",
			"End Time",
			"Start Type",
			"End Type",
			"Start MME UE S1AP ID",
			"End MME UE S1AP ID",
			"Start eNB UE S1AP ID",
			"End eNB UE S1AP ID",
			"IMSI from MME_eNB",
			"IMSI from IMEI",
			"Mapped IMSI"
	};

	private long[] valueCount;
	CountableMap startTypeCount = new CountableMap();
	CountableMap endTypeCount = new CountableMap();

	public LSRFileProcessor(CountUpDownLatch latch, Map<String, Long> fileSizeMap, File srcFile, File outFile) {
		this.latch = latch;
		this.fileSizeMap = fileSizeMap;
		this.srcFile = srcFile;
		this.outFile = outFile;
		this.statFile = FileExtUtils.newExtension(outFile, ".stat.json");
	}

	private void init() {
		mappedCount = 0L;
		blankCount = 0L;
		nokeyCount = 0L;
		dmsNotMatchCount = 0L;
		noImeiCount = 0L;
		keyNoImeiCount = 0L;
		ImeiNoKeyCount = 0L;
		keyNoImsiCount = 0L;
		ImeiNoImsiCount = 0L;
		ImeiNoNewImsiCount = 0L;
		ImsiNoKeyCount = 0L;
		ImsiNoImeiCount = 0L;
		NewImsiNoImeiCount = 0L;
		NewImeiCount = 0L;
		NewImsiNoNewImeiCount = 0L;
		KeyMappingCount = 0L;
		ImeiMappingCount = 0L;
		DmsMappinCount = 0L;
		KeyNoImeiMappinfCount = 0L;
		ImeiNoKeyMappinfCount = 0L;
		ErrorCount = 0L;
		this.valueCount = new long[fieldsToRead.length];
		startTypeCount.put("Connection Reestablishment", 0L);
		startTypeCount.put("New Connection", 0L);
		startTypeCount.put("Unknown", 0L);
		startTypeCount.put("X2 Handover", 0L);
		endTypeCount.put("Connection Release", 0L);
		endTypeCount.put("Failed Attempt", 0L);
		endTypeCount.put("Unknown", 0L);
		endTypeCount.put("Outgoing S1 Handover", 0L);
		endTypeCount.put("Outgoing X2 Handover", 0L);
	}

	@Override
	public void run() {
		try {
			String absSrcPath = srcFile.getAbsolutePath();
			Long lastSize = fileSizeMap.get(absSrcPath);
			if (lastSize == null) { // 第一次處理, 不真的處理，只寫檔案大小
				XXX.out("keep "+ absSrcPath + ", size =" + srcFile.length());
				fileSizeMap.put(absSrcPath, srcFile.length());
			}
			else {
				fileSizeMap.remove(absSrcPath);
				for (int i = 0; i < 3; i++) {
					try {
						init();
						processCSV();
					} catch (Throwable e) {
						if (i == 2) {
							XXX.out("Failed to process " + srcFile.getAbsolutePath() + " for 3 times.");
							e.printStackTrace();
						}
					}
					if (statFile.exists())
						break;
				}
			}
		}
		finally {
			latch.countDown();
			XXX.out("Job done. remains: " + latch.getCount());
		}
	}

	private void processCSV() throws IOException {

		JedisCluster jedis = null;
//		boolean fileError = false;
		try {
			reader = new FieldCSVReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(srcFile))), ',', '\01');
			reader.readHeader();

			jedis = RedisUtil.getResource();
			
			Map<String, Integer> headerMap = reader.getHeaderMap();
			Integer imsiFieldIndex = headerMap.get("IMSI");
			if (imsiFieldIndex == null) {
				throw new IOException("Invalid csv file. Missing IMSI field! " + srcFile.getAbsolutePath());
			}
			writer = new CSVWriter(new FileWriter(outFile));
			writer.writeNext(reader.getHeaderNames());

			String missingName = outFile.getName();
			missingName = missingName.substring(0, missingName.length()-4) + ".missing.csv";

			File missingFile = new File(outFile.getParentFile(), missingName);
			missingWriter = new CSVWriter(new FileWriter(missingFile));
			missingWriter.writeNext(reader.getHeaderNames());

			String mappingName = outFile.getName();
			mappingName = mappingName.substring(0, mappingName.length()-4) + ".mapping.csv";

			File mappingFile = new File(outFile.getParentFile(), mappingName);
			mappingFile.getParentFile().mkdirs(); // 確保父目錄存在
			mappingWriter = new CSVWriter(new FileWriter(mappingFile));
			mappingWriter.writeNext(mappingFileFields);

			imSiQuerier = new IMSI_querier(jedis, 500);
			imEiQuerier = new IMEI_querier(jedis, 500);

			matchWriter = new LSRMatchWriter(jedis, 500);

			Long totalCount = 0L;
			int colCount = reader.getHeaderNames().length;

			while (reader.next()) {
				if (reader.getValueCount() != colCount) {
					XXX.out("file " + srcFile.getPath() +" line error >>> " + Arrays.toString(reader.getValues()));
					ErrorCount++;
					continue;
				}

				String[] item = reader.getValues();
				imSiQuerier.send(item);
				totalCount++;
			}
			imSiQuerier.flush();
            matchWriter.flush();
			imEiQuerier.flush();

			Map<String, Long> stat = new LinkedHashMap<>();
			for (int i = 0; i < fieldsToRead.length; i++) {
				String field = fieldsToRead[i];
				Long count = valueCount[i];
				stat.put(field, count);
			}
			stat.put("Total", totalCount);
			stat.put("IMSI Blank", blankCount);
			stat.put("LSR No Key", nokeyCount);
			stat.put("LSR No IMEI", noImeiCount);
			stat.put("IMSI Mapped", mappedCount);
			stat.put("DMS Not Match", dmsNotMatchCount);
			stat.put("KEY NO IMEI", keyNoImeiCount);
			stat.put("KEY NO IMSI", keyNoImsiCount);
			stat.put("IMEI NO KEY", ImeiNoKeyCount);
			stat.put("IMEI NO IMSI", ImeiNoImsiCount);
			stat.put("IMEI NO NewIMSI", ImeiNoNewImsiCount);
			stat.put("IMSI NO KEY", ImsiNoKeyCount);
			stat.put("IMSI NO IMEI", ImsiNoImeiCount);
			stat.put("NewIMSI NO IMEI", NewImsiNoImeiCount);
			stat.put("IMEI Mapped", NewImeiCount);
			stat.put("NewIMSI NO NewIMEI", NewImsiNoNewImeiCount);
			stat.put("KEY IMSI Mapping", KeyMappingCount);
			stat.put("IMEI IMSI Mapping", ImeiMappingCount);
			stat.put("DMS IMSI Mapping", DmsMappinCount);
			stat.put("KEY NO IMEI Mapping", KeyNoImeiMappinfCount);
			stat.put("IMEI NO KEY Mapping", ImeiNoKeyMappinfCount);
			stat.put("Session Error", ErrorCount);

			for (Map.Entry<String, Long> entry: startTypeCount.entrySet()) {
				stat.put("Start Type:" + entry.getKey(), entry.getValue());
			}
			for (Map.Entry<String, Long> entry: endTypeCount.entrySet()) {
				stat.put("End Type:" + entry.getKey(), entry.getValue());
			}

			FileUtils.writeFile(statFile.getAbsolutePath(), JsonUtil.toJsonString(stat));
			XXX.out("file [" + outFile.getPath() + "], total = " + totalCount
					+ ", mapped = " + mappedCount + ", blank = " + blankCount+ ", nokey = " + nokeyCount
					+ ", no imei = " + noImeiCount);
		} finally {
			safeClose(reader);
			safeClose(writer);
			safeClose(missingWriter);
			safeClose(mappingWriter);
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

	class IMSIQueryVO {
		String[] vals;
		String imsi;
		Response<List<String>> res1;
		Response<List<String>> res2;
	}

	class IMEIQueryVO {
        String[] vals;
        String imsi;
        String imei;
        Response<List<String>> res;
    }
	/**
	 * 向 Redis 查詢 IMSI 值
	 * @author edward
	 *
	 */
	class IMSI_querier extends RedisBatchRunner2<IMSIQueryVO> {

		public IMSI_querier(JedisCluster jedis, int batchSize) {
			super(jedis, batchSize);
		}

		@Override
		public void invoke(JedisClusterPipeline pipeline, String[] vals, List<IMSIQueryVO> results) {

			for (int i = 0; i < fieldsToRead.length; i++) {
				String field = fieldsToRead[i];
				String value = null;
				value = reader.get(field, vals);
				if (!isEmpty(value))
					valueCount[i]++;
				if ("Start Type".equals(field)) {
					startTypeCount.add(value);
				}
				else if ("End Type".equals(field)) {
					endTypeCount.add(value);
				}
			}
			String imsi = reader.get("IMSI", vals);

			IMSIQueryVO result = new IMSIQueryVO();
			result.vals = vals;

			String[] keyPair = getKeyPair(vals);
			String imei = reader.get("IMEI", vals);

			boolean hasKey  = (keyPair != null);
			boolean hasIMEI = !isEmpty(imei);
			boolean hasIMSI = !isEmpty(imsi);

			if (!hasKey)
				nokeyCount++;

			if (!hasIMEI)
				noImeiCount++;

			if (hasKey && !hasIMEI)
				keyNoImeiCount++;

			if (hasKey && !hasIMSI)
				keyNoImsiCount++;

			if (hasIMEI && !hasKey)
				ImeiNoKeyCount++;

			if (hasIMEI && !hasIMSI)
				ImeiNoImsiCount++;

			if (hasIMSI && !hasKey)
				ImsiNoKeyCount++;

			if (hasIMSI && !hasIMEI)
				ImsiNoImeiCount++;

			if (hasIMSI) {
				// 無論原本是否有 IMSI, 都呼叫 Mapping
				result.imsi = imsi;
//				results.add(vo);
//				return;
			}

			if (hasKey) {
				String key = String.format("mme-enb:%s-%s", keyPair[0], keyPair[1]);
				result.res1 = pipeline.lrange(key, 0, 4);
			}

			if (hasIMEI) {
				String mzIMEI = imei;
				if (mzIMEI.length() > 14)
					mzIMEI= mzIMEI.substring(0, 14);
				String key = String.format("imei:%s", mzIMEI);
				result.res2 = pipeline.lrange(key, 0, 3);
			}
			results.add(result);
		}

		private String[] getKeyPair(String[] vals) {
			String start_mme_id  = reader.get("Start MME UE S1AP ID", vals);
			String end_mme_id    = reader.get("End MME UE S1AP ID", vals);
			String start_enb_id  = reader.get("Start eNB UE S1AP ID", vals);
			String end_enb_id    = reader.get("End eNB UE S1AP ID", vals);

			String[] keyPair = null;

			if (!isEmpty(start_mme_id) && !isEmpty(start_enb_id))  //XXXX
				keyPair = new String[]{start_mme_id, start_enb_id};
			else if (!isEmpty(end_mme_id) && !isEmpty(end_enb_id))
				keyPair = new String[]{end_mme_id, end_enb_id};

			return keyPair;
		}

		@Override
		public void result(IMSIQueryVO result) {
			ImsiMapped imsis = getImsi(result);
			String[] vals = result.vals;
			String[] mappingImsiValues = new String[mappingFileFields.length];

			if (isMatched(imsis.imsiFromKey))
				KeyMappingCount++;

			if (isMatched(imsis.imsiFromIMEI))
				ImeiMappingCount++;

			if (isMatched(imsis.imsiFromKey) || isMatched(imsis.imsiFromIMEI))
				DmsMappinCount++;

			if (isMatched(imsis.imsiFromKey) && !isMatched(imsis.imsiFromIMEI))
				KeyNoImeiMappinfCount++;

			if (isMatched(imsis.imsiFromIMEI) && !isMatched(imsis.imsiFromKey))
				ImeiNoKeyMappinfCount = 0L;

			String imei = reader.get("IMEI", vals);
			boolean hasIMEI = !isEmpty(imei);

			if (isMatched(imsis.mappedIMSI) && !hasIMEI)
				NewImsiNoImeiCount++;

			if (hasIMEI && !isMatched(imsis.mappedIMSI))
				ImeiNoNewImsiCount++;

			for (int i = 0; i < fieldsToRead.length; i++) {
				String field = fieldsToRead[i];
				mappingImsiValues[i] = reader.get(field, vals);
			}

			mappingImsiValues[fieldsToRead.length + 0] = imsis.imsiFromKey;  // "IMSI from MME_eNB",
			mappingImsiValues[fieldsToRead.length + 1] = imsis.imsiFromIMEI;  // "IMSI from IMEI",
			mappingImsiValues[fieldsToRead.length + 2] = imsis.mappedIMSI;	// "Mapped IMSI"

			if ("NA".equals(imsis.mappedIMSI)) {	// IMSI Mapping 成功，但 DMS 中的 IMSI 是空值
				blankCount++;
				missingWriter.writeNext(vals);
			}
			else if ("NK".equals(imsis.mappedIMSI)) {	// IMSI Mapping 失敗
				dmsNotMatchCount++;
				missingWriter.writeNext(vals);
			}
			else if (isEmpty(imsis.mappedIMSI)) {
				missingWriter.writeNext(vals);
			}
			else {
				reader.set("IMSI", vals, imsis.mappedIMSI);
				mappedCount++;
			}

			String[] keyPair = getKeyPair(vals);
			if (keyPair != null) {
				String val = imsis.mappedIMSI == null? "NA" : imsis.mappedIMSI;
				String[] item = new String[]{keyPair[0], keyPair[1], val};
				matchWriter.send(item);
			}

//			writer.writeNext(vals); // 不管有沒有 missing, 都要寫到 mapped file
            imEiQuerier.send(vals);
			mappingWriter.writeNext(mappingImsiValues);
		}
	}

	private ImsiMapped getImsi(IMSIQueryVO result) {
		ImsiMapped ret = new ImsiMapped();

		ret.origIMSI = result.imsi;
		ret.mappedIMSI = result.imsi;

		if (result.res1 != null) {		// MME/eNB-IMSI mapping
			List<String> list = result.res1.get();
			ret.imsiFromKey = getIMXI(list, result.vals);
			if (isEmpty(ret.mappedIMSI))
				ret.mappedIMSI = ret.imsiFromKey;
		}

		if (result.res2 != null ) { // IMEI-IMSI mapping
			List<String> list = result.res2.get();
			ret.imsiFromIMEI = getIMXI(list, result.vals);
			if (isEmpty(ret.mappedIMSI))
				ret.mappedIMSI = ret.imsiFromIMEI;
		}
		return ret;
	}

	private boolean isMatched(String val) {
		return !isEmpty(val) &&  !"NA".equals(val) && !"NK".equals(val);
	}

	// 取得 IMSI 或 IMEI 值，其結果視 list string tab 分割字串中第一個值決定
	private String getIMXI(List<String> list, String[] vals) {
		if (list.isEmpty())
			return "NK";

		Map<String, String[]> map = new LinkedHashMap<>();
		for (String val : list) {
			String[] ary = val.split("\t");
			String imxi = ary[0];
			if (map.get(imxi) == null)
				map.put(imxi, ary);
		}

		if (map.size() == 1)
			return map.keySet().toArray(new String[map.size()])[0];

		String start_gummei    = reader.get("Start GUMMEI", vals);
		String end_gummei      = reader.get("End GUMMEI", vals);
		String start_time  	= reader.get("Start Time", vals);
		String end_time  		= reader.get("End Time", vals);

		long min_time_gap = Long.MAX_VALUE;
		String ret = null;
		for (Map.Entry<String, String[]> entry: map.entrySet()) {
			String[] ary = entry.getValue();
			String imxi = ary[0];
//					String d_m_tmsi = ary[1];
			String d_gummei = ary[2];
			String d_start_time = ary[3];
			String d_end_time = ary[4];
			if (!isEmpty(d_gummei)) { // DMS 的 gummi 幾乎都有值, 不過 LSR 的 start_gummei, end_gummei 經常沒值
				if (d_gummei.equals(start_gummei) || d_gummei.equals(end_gummei))
					return imxi;

				long time_gap = getTimeGap(d_start_time, d_end_time, start_time, end_time);
				if (time_gap < min_time_gap) {
					ret = imxi;
					min_time_gap = time_gap;
				}
			}
		}
		if (ret != null)
			return ret;
		return map.keySet().toArray(new String[map.size()])[0];
	}

	private long getTimeGap(String d_start_time, String d_end_time, String start_time, String end_time) {
		// d_start_time & d_end_time 格式 1499263099425730000
		// start_time & end_time 格式 2017-07-05 17:26:13
		// Java time long 格式 1483262773000
		// Long.MAX_VALUE 格式 9223372036854775807
		long d_start_timel = Long.valueOf(d_start_time) / 1000000;
		long d_end_timel = Long.valueOf(d_end_time) / 1000000;
		long start_timel = parseTime(start_time);
		long end_timel   = parseTime(end_time);
		return Math.abs(d_start_timel - start_timel) + Math.abs(d_end_timel - end_timel);
	}

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //  2017-07-05 17:26:13
	private long parseTime(String time) {
		 try {
			return sdf.parse(time).getTime();
		} catch (ParseException e) {
			return 0;
		}
	}

	/**
	 *
	 */
	class ImsiMapped {
		String origIMSI = "";
		String imsiFromKey = "";
		String imsiFromIMEI = "";
		String mappedIMSI = "";
	}

	/**
	 * 用來寫回有查詢到的 IMSI
	 * @author edward
	 */
	class LSRMatchWriter extends RedisBatchRunner2<Void> {
		public LSRMatchWriter(JedisCluster jedis, int batchSize) {
			super(jedis, batchSize);
		}

		@Override
		public void invoke(JedisClusterPipeline pipeline, String[] vals, List<Void> results) {
			String mme_id = vals[0];
			String enb_id = vals[1];
			String imsi = vals[2];
			String key = String.format("imsi-found:%s-%s", mme_id, enb_id);
			pipeline.setex(key, 2*60*60, imsi); // keep entire list for 2 hours
		}

		@Override
		public void result(Void result) {
		}
	}

	   /**
     * 用來寫回有查詢到的 IMSI
     * @author edward
     */
    class IMEI_querier extends RedisBatchRunner2<IMEIQueryVO> {
        public IMEI_querier(JedisCluster jedis, int batchSize) {
            super(jedis, batchSize);
        }

        @Override
        public void invoke(JedisClusterPipeline pipeline, String[] vals, List<IMEIQueryVO> results) {
            String imsi = reader.get("IMSI", vals);
            String imei = reader.get("IMEI", vals);
            IMEIQueryVO vo = new IMEIQueryVO();
            vo.imsi = imsi;
            vo.imei = imei;
            vo.vals = vals;
            if (isEmpty(vo.imei) && isMatched(imsi)) {
                String key = String.format("imsi:%s", imsi);
                vo.res = pipeline.lrange(key, 0, 3);
            }
            results.add(vo);
        }

        @Override
        public void result(IMEIQueryVO vo) {
            String newIMEI = vo.imei;
            if (vo.res != null) {
                List<String> list = vo.res.get();
                newIMEI = getIMXI(list, vo.vals);
                reader.set("IMEI", vo.vals, newIMEI);
            }

			boolean hasNewIMEI = !isEmpty(newIMEI);
            if (hasNewIMEI)
            	NewImeiCount++;

            if (isMatched(vo.imsi) && !hasNewIMEI)
                NewImsiNoNewImeiCount++;

            writer.writeNext(vo.vals);
        }
    }

}