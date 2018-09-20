package tw.moze.imsi.report;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import tw.moze.imsi.redis.RedisUtil;
import tw.moze.imsi.util.ReportUtil;
import tw.moze.util.concurrent.CountUpDownLatch;
import tw.moze.util.concurrent.ExecutorBuilder;
import tw.moze.util.dev.XXX;
import tw.moze.util.fileutil.DirLister;
import tw.moze.util.fileutil.FileExtUtils;

public class DMSReport implements Closeable {
	// src file mapping 完後會移到此處
	private String[] dirForFiltered = new String[] {
		"C:/Users/User/Downloads/data/imsi_mapping/filtered/dms/10.108.61.155",
		"C:/Users/User/Downloads/data/imsi_mapping/filtered/dms/10.108.61.167",
//		"/data/imsi_mapping/filtered/dms/10.108.61.155",
//		"/data/imsi_mapping/filtered/dms/10.108.61.167",
	};

	private ThreadPoolExecutor tp;

	private String filePattern = "*.stat.json";

	public DMSReport() {
		tp = ExecutorBuilder.newCachedThreadPool(6);
		RedisUtil.initPool();
	}

	public void process() {
		List<String> files = new LinkedList<>();
		List<String> dirs = new LinkedList<>();
		for (String srcPath: dirForFiltered) {
			DirLister.deepListFiles(srcPath, filePattern, files, dirs);
		}

		List<Map<String, String>> list = Collections.synchronizedList(new ArrayList<Map<String, String>>());
		processFiles(files, list);
		if (list.isEmpty()) {
			XXX.out("No " + filePattern + " found!");
			return;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String reportPath  = "C:/Users/User/Downloads/data/imsi_mapping/filtered/dms/dmsreport." + sdf.format(new Date())+ ".csv";

		XXX.out("Sorting report items: " + list.size());
		Collections.sort(list, comp);
		ReportUtil.writeReport(list, reportPath);
		genDailyReport(list);
	}

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private void genDailyReport(List<Map<String, String>> list) {
		String today = sdf.format(new Date());
		String currentDate = today;
		List<Map<String, String>> ret = new LinkedList<>();
		LinkedHashMap<String, Double> drow = new LinkedHashMap<>();

		for (Map<String, String> row : list) {
			String rowDate = row.get("File Time").substring(0, 10);
			int comp = rowDate.compareTo(currentDate);

			if (comp > 0) {
				continue;
			}
			else if (comp == 0) {
				sumDRow(drow, row);
			}
			else { //
				if (!drow.isEmpty()) {
					ret.add(genDRowMap(drow, currentDate));
				}
				currentDate = rowDate;
				drow = new LinkedHashMap<>();
				sumDRow(drow, row);
			}
		}

		if (!drow.isEmpty()) {
			ret.add(genDRowMap(drow, currentDate));
		}

		if (ret.isEmpty())
			return;
		////
//		String reportPath  = "/data/imsi_mapping/filtered/dms/dmsreport_daily." + sdf.format(new Date())+ ".csv";
		String reportPath  = "C:/Users/User/Downloads/data/imsi_mapping/filtered/dms/dmsreport_daily." + sdf.format(new Date())+ ".csv";
		ReportUtil.writeReport(ret, reportPath);
	}

	private Double getStat(Map<String, Double> stat, String field, Double def) {
		Double v = stat.get(field);
		return v == null ? def : v;
	}


	private Map<String, String> genDRowMap(Map<String, Double> drow, String currentDate) {
		Map<String, String> ret = new LinkedHashMap<>();
		for (Map.Entry<String, Double> entry: drow.entrySet()) {
			ret.put(entry.getKey(), s(entry.getValue()));
		}
		double matchRate = 0d;
		double imsiRate = 0d;
		double keyRate = 0d;
		double imeiRate = 0d;

		double key = getStat(drow, "Key", 0d);
		double matched = getStat(drow, "Matched", 0d);
		double total = getStat(drow, "Total", 0d);
		double imsi = getStat(drow, "IMSI(string)", 0d);
		double imei = getStat(drow, "IMEI(string)", 0d);

		if (total != 0) {
			matchRate = matched/total;
			imsiRate = imsi/total;
			keyRate = key/total;
			imeiRate = imei/total;
		}

		ret.put("IMSI Rate", Math.round(imsiRate*10000)/100d + "%");
		ret.put("IMEI Rate", Math.round(imeiRate*10000)/100d + "%");
		ret.put("Key Rate", Math.round(keyRate*10000)/100d + "%");
		ret.put("Match Rate", Math.round(matchRate*10000)/100d + "%");
		ret.put("File Date", currentDate);
		return ret;
	}

	private String s(double d) {
		return String.format("%.0f", d);
	}

	private void sumDRow(Map<String, Double> drow, Map<String, String> row) {
		String[] fieldToSum = {
				"IMSI(string)", "M-TMSI (hex.)(string)","IMEI(string)", "GUMMEI(string)", "MME UE S1AP ID(int64)",
				"eNB UE S1AP ID(int64)", "Start Time(uint64-nsec)", "End Time(uint64-nsec)", "Total", "Key",
				"Matched" //, "Session Error"
		};

		for (String field: fieldToSum) {
			double d = getMapValue2(row, field, 0d);
			Double sum = drow.get(field);
			if (sum == null) {
				drow.put(field, d);
			}
			else {
				drow.put(field, sum + d);
			}
		}
		double fileError = getMapValue2(row, "Session Error", 0d) > 0 ? 1 : 0;
		Double sum = drow.get("File Error");
		if (sum == null) {
			drow.put("File Error", fileError);
		}
		else {
			drow.put("File Error", sum + fileError);
		}

		double fc = getStat(drow, "File Count", 0d);
		drow.put("File Count", fc+1);
	}


	private double getMapValue2(Map<String, String> map, String key, double defValue) {
		String ret = map.get(key);
		return ret == null? defValue : Double.valueOf(ret);
	}

	@Override
	public void close() throws IOException {
		XXX.out("Closing Redis pool.");
		RedisUtil.close();
		XXX.out("Closing Thread pool.");
		if (tp != null)
			tp.shutdown();
		XXX.out("Done!");
	}

	private void processFiles(List<String> files, List<Map<String, String>> list) {
	    CountUpDownLatch latch = new CountUpDownLatch();
	    List<Runnable> jobs = new LinkedList<>();
		for (String file: files) {
			File statFile = new File(file);
			File csvFile = FileExtUtils.replaceExtension(statFile, ".stat.json", ".csv");
			if (!csvFile.exists()) {
				continue;
			}
			Runnable job = new DMSFileReporter(latch, statFile, csvFile, list);
			jobs.add(job);
		}

		latch.setCount(jobs.size());
		XXX.out("Checker job count: " + jobs.size());

		for (Runnable job: jobs) {
			tp.execute(job);
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static Comparator<Map<String, String>> comp = new Comparator<Map<String, String>>() {
		@Override
		public int compare(Map<String, String> r1, Map<String, String> r2) {
			String v1 = r1.get("File Time");
			String v2 = r2.get("File Time");

			int value1 = v2.compareTo(v1);
	        if (value1 == 0) {
				v1 = r1.get("File Path");
				v2 = r2.get("File Path");
	        	return v1.compareTo(v2);
	        }
	        return value1;
		}
	};

	public static void main(String[] args) {
		final DMSReport report = new DMSReport();
		report.process();
		try {
    		report.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}