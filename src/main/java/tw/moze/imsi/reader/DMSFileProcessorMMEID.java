package tw.moze.imsi.reader;
//package tw.moze.imsi.reader;
//
//import java.io.Closeable;
//import java.io.File;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//import com.opencsv.CSVWriter;
//
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.Pipeline;
//import tw.moze.imsi.redis.RedisUtil;
//import tw.moze.imsi.util.StatFile;
//import tw.moze.util.collection.CountableMap;
//import tw.moze.util.concurrent.CountUpDownLatch;
//import tw.moze.util.dev.XXX;
//import tw.moze.util.fileformat.FieldCSVReader;
//import tw.moze.util.fileutil.FileExtUtils;
//import tw.moze.util.redis.RedisBatchRunner2;
//import tw.moze.util.string.StringUtil;
//
//class DMSFileProcessor implements Runnable {
//	File srcFile, filteredFile;
//	Map<String, Long> fileSizeMap;
//	CountUpDownLatch latch;
//	private CountableMap statMap;
//	private Map<String, CountableMap> keyFlagStatMap;
//
//	FieldCSVReader reader = null;
//	CSVWriter writer = null;
//
//	private static String[] fieldsToRead = new String[] {
//			"IMSI(string)",
//			"M-TMSI (hex.)(string)",
//			"IMEI(string)",
//			"GUMMEI(string)",
//			"MME UE S1AP ID(int64)",
//			"eNB UE S1AP ID(int64)",
//			"Start Time(uint64-nsec)",
//			"End Time(uint64-nsec)",
//			};
//
//	public DMSFileProcessor(CountUpDownLatch latch, Map<String, Long> fileSizeMap, File srcFile, File filteredFile) {
//		this.latch = latch;
//		this.fileSizeMap = fileSizeMap;
//		this.srcFile = srcFile;
//		this.filteredFile = filteredFile;
////		this.valueCount = new int[fieldsToRead.length];
//		this.statMap = new CountableMap();
//		this.keyFlagStatMap = new HashMap<>();
//	}
//
//	@Override
//	public void run() {
//		long t1 = System.currentTimeMillis();
//		try {
//			String name = srcFile.getAbsolutePath();
//			Long lastSize = fileSizeMap.get(name);
//			if (lastSize == null) { // 第一次處理, 不真的處理，只寫檔案大小
//				XXX.out("keep "+ name + ", size =" + srcFile.length());
//				fileSizeMap.put(name, srcFile.length());
//			}
//			else {
//				// 第二次處理到同一個檔案時，檔案一定是已經完整寫入了
//				XXX.out("process " + name);
//				fileSizeMap.remove(name);
//				try {
//					readCSV();
//				} catch (Throwable e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		finally {
//			latch.countDown();
//			long t2 = System.currentTimeMillis();
//			XXX.out("Job done. took " + timeSpan(t2-t1)+ "; remains: " + latch.getCount());
//		}
//	}
//
//	private String timeSpan(long ms) {
//		long sec = ms /1000;
//		long minutes = sec/60;
//		sec %= 60;
//		ms %= 1000;
//		StringBuffer sb = new StringBuffer();
//		if (minutes > 0)
//			sb.append(minutes + "m ");
//
//		sb.append(String.format("%02d.%03ds", sec, ms));
//		return sb.toString();
//	}
//
//	private void readCSV() throws IOException {
//		Jedis jedis = null;
//		Map<String, Long> stat = new LinkedHashMap<>();
//		String statFile = FileExtUtils.newExtension(filteredFile, ".stat.json").getAbsolutePath();
//		long sessionError = 0L;
//		try {
//			reader = new FieldCSVReader(new FileReader(srcFile), ',', '\01');
//			reader.readHeader();
//
//			jedis = RedisUtil.getResource();
//
//			writer = new CSVWriter(new FileWriter(filteredFile));
//			String[] writerField = reader.getHeaderNames();
//			writerField = Arrays.copyOf(writerField, writerField.length + 1);
//			writerField[writerField.length - 1] = "Key Flag";
//			writer.writeNext(writerField);
//
//			DMSDataWriter redisWriter = new DMSDataWriter(jedis, 1000);
//
//			int colCount = reader.getHeaderNames().length;
//			while (reader.next()) {
//				if (reader.getValueCount() != colCount) {
//					XXX.out("file " + srcFile.getPath() +" line error >>> " + Arrays.toString(reader.getValues()));
//					sessionError++;
//					continue;
//				}
//
//				// 寫到 filteredFile
//				String[] values = reader.getValues();
//				values = Arrays.copyOf(values, values.length + 1);
//				String keyFlag = getKeyFlag(reader);
//				values[values.length - 1] = keyFlag;
//				writer.writeNext(values);
//
//				CountableMap keyFlagStat = getKeyFlagStat(keyFlag);
//				for (int i = 0; i < fieldsToRead.length; i++) {
//					String field = fieldsToRead[i];
//					String value = reader.get(field);
//					if (!isEmpty(value)) {
//						statMap.add(field);
//						keyFlagStat.add(field);
//					}
//				}
//
//				// 塞進 Redis
//				redisWriter.send(values);
//				keyFlagStat.add("Total");
//				statMap.add("Total");
//			}
//
//			redisWriter.flush();
//
//			convertStatMap(statMap, stat);
//			stat.put("Session Error", sessionError);
//			StatFile.save(statFile, stat);
//			XXX.out("total line = " + statMap.get("Total"));
//
//			for (String keyFlag: keyFlagStatMap.keySet()) {
//				CountableMap keyFlagStat = getKeyFlagStat(keyFlag);
//				Map<String, Long> map = new LinkedHashMap<>();
//				convertStatMap(keyFlagStat, map);
//				String keyFlagstatFile = statFile.substring(0, statFile.length() - ".stat.json".length());
//				keyFlagstatFile += ".stat." + keyFlag + ".json";
//				StatFile.save(keyFlagstatFile, map);
//			}
//		}
//		finally {
//			safeClose(reader);
//			safeClose(writer);
//			safeClose(jedis);
//		}
//	}
//
//	private void convertStatMap(CountableMap statMap, Map<String, Long> ret) {
//		for (int i = 0; i < fieldsToRead.length; i++) {
//			String field = fieldsToRead[i];
//			Long count = statMap.get(field);
//			ret.put(field, count);
//		}
//		ret.put("Key", statMap.get("Key"));
//		ret.put("Total", statMap.get("Total"));
//	}
//
//	private CountableMap getKeyFlagStat(String keyFlag) {
//		CountableMap keyFlagStat = keyFlagStatMap.get(keyFlag);
//		if (keyFlagStat == null) {
//			keyFlagStat = new CountableMap();
//			keyFlagStatMap.put(keyFlag, keyFlagStat);
//		}
//		return keyFlagStat;
//	}
//
//	private String getKeyFlag(FieldCSVReader reader) {
//		String eNB_IP = reader.get("eNB IP(string)");
//		String MME_IP = reader.get("MME IP(string)");
//		String eNB_Flag = "X";
//		String MME_Flag = "X";
//
//		if (eNB_IP == null)
//			eNB_IP = "";
//
//		if (MME_IP == null)
//			MME_IP = "";
//
//
//		if (eNB_IP.startsWith("10.11")) {
//			eNB_Flag = "E";
//		}
//		else if (eNB_IP.startsWith("10.10")) {
//			eNB_Flag = "N";
//		}
//
//		if (MME_IP.startsWith("10.")) {
//			MME_Flag = "E";
//		}
//		else if (MME_IP.startsWith("172.")) {
//			MME_Flag = "N";
//		}
//		return eNB_Flag + MME_Flag;
//	}
//
//	private void safeClose(Closeable res) {
//		if (res != null) {
//			try {
//				res.close();
//			} catch (IOException e) {
//				;
//			}
//		}
//	}
//
//	private boolean isEmpty(String val) {
//		return (val == null || val.trim().isEmpty());
//	}
//
//	class DMSDataWriter extends RedisBatchRunner2<Void> {
//
//		public DMSDataWriter(Jedis jedis, int batchSize) {
//			super(jedis, batchSize);
//		}
//
//		@Override
//		public void invoke(Pipeline pipeline, String[] vals, List<Void> results) {
//
//			String mme_id = reader.get("MME UE S1AP ID(int64)", vals);
//			String enb_id = reader.get("eNB UE S1AP ID(int64)", vals);
//			String imsi = reader.get("IMSI(string)", vals);
//			String imei = reader.get("IMEI(string)", vals);
//
////			String key1 = (isEmpty(mme_id) || isEmpty(enb_id))
////					? null
////					: String.format("mme-enb:%s-%s", mme_id, enb_id);
//
//			String key1 = (isEmpty(mme_id))
//					? null
//					: String.format("mme:%s", mme_id);
//
//			String key2 = null;
//			if (!isEmpty(imei)) {
//				String mzIMEI = imei;
//				if (mzIMEI.length() > 14)
//					mzIMEI= mzIMEI.substring(0, 14);
//				key2 = String.format("imei:%s", mzIMEI);
//			}
//
//			String key3 = null;
//			if (isEmpty(imsi))
//				imsi = "NA";
//			else if (!isEmpty(imei)) // imsi && imei not empty
//				key3 = String.format("imsi:%s", imsi);
//
//			String m_tmsi = reader.get("M-TMSI (hex.)(string)", vals);
//			String gummei = reader.get("GUMMEI(string)", vals);
//			String start_time = reader.get("Start Time(uint64-nsec)", vals);
//			String end_time = reader.get("End Time(uint64-nsec)", vals);
//
//			String val = StringUtil.join("\t", imsi, m_tmsi, gummei, start_time, end_time);
//
//			if (key1 != null) {
//				statMap.add("Key");
//				String keyFlag = vals[vals.length - 1];
//				CountableMap keyFlagStat = getKeyFlagStat(keyFlag);
//				keyFlagStat.add("Key");
//				setKeyValue(pipeline, results, key1, imsi, val, 5, 6);
//			}
//
//			if (key2 != null)
//				setKeyValue(pipeline, results, key2, imsi, val, 4, 24*14);
//
//			if (key3 != null) {		// 同時有 imsi 及 imei 值
//				String val2 = StringUtil.join("\t", imei, m_tmsi, gummei, start_time, end_time);
//				setKeyValue(pipeline, results, key3, imei, val2, 4, 24*14);
//			}
//
//		}
//
//		private void setKeyValue(Pipeline pipeline, List<Void> results, String key,
//				String imsi, String val,
//				int listSize, int expireHours) {
//			if ("NA".equals(imsi)) {	// 如果是 NA, 放到最後
//				pipeline.rpush(key, val);
//			}
//			else {
//				pipeline.lpush(key, val);
//			}
//
//			pipeline.ltrim(key, 0, listSize - 1); // keep first five elements
//			pipeline.expire(key, expireHours*60*60);	// keep entire list for N hours
//
//		}
//
//		@Override
//		public void result(Void result) {
//			; // do nothing
//		}
//
//	}
//
//}