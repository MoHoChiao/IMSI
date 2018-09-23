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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tw.moze.imsi.util.ReportUtil;
import tw.moze.util.dev.XXX;
import tw.moze.util.fileutil.DirLister;
import tw.moze.util.fileutil.FileExtUtils;
import tw.moze.util.fileutil.FileUtils;
import tw.moze.util.json.JsonUtil;

public class DMSKeyFlagReport implements Closeable {
	// src file mapping 完後會移到此處
	private String[] dirForFiltered = new String[] {
		"/data/imsi_mapping_cluster_v1/filtered/dms/10.108.61.155",
		"/data/imsi_mapping_cluster_v1/filtered/dms/10.108.61.167",
	};

	public DMSKeyFlagReport() {
	}

	public void process() {
		String[] keys = {"EE", "EN", "EX", "NN", "NE", "NX", "XE", "XN", "XX"};
		for (String key : keys) {
			processKeyFlag(key);
		}
	}

	public void processKeyFlag(String keyFlag) {
		String filePattern = "*.stat." + keyFlag + ".json";
		List<String> files = new LinkedList<>();
		List<String> dirs = new LinkedList<>();
		for (String srcPath: dirForFiltered) {
			DirLister.deepListFiles(srcPath, filePattern, files, dirs);
		}

		List<Map<String, String>> list = new ArrayList<>();

		for (String file: files) {
			File statFile = new File(file);
			File csvFile = FileExtUtils.replaceExtension(statFile, ".stat.XX.json", ".csv");
			if (!csvFile.exists()) {
				continue;
			}
			try {
				Map<String, Integer> stat = readStat(file);
				Map<String, String> stat2 = computeStat(stat, csvFile);
				stat2.put("Key Flag", keyFlag);
				list.add(stat2);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (list.isEmpty()) {
			XXX.out("No " + filePattern + " found!");
			return;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String reportPath  = "/data/imsi_mapping_cluster_v1/filtered/dms/dmsreport." + sdf.format(new Date())+ "." + keyFlag + ".csv";

		XXX.out("Sorting report items: " + list.size());
		Collections.sort(list, comp);
		ReportUtil.writeReport(list, reportPath);
	}


	private Map<String, String> computeStat(Map<String, Integer> stat, File csvFile) {
		Map<String, String> map = new LinkedHashMap<>();
		for(Map.Entry<String, Integer> entry: stat.entrySet()) {
			Integer value = entry.getValue();
			if (value == null)
				value = 0;

			map.put(entry.getKey(), String.valueOf(value));
		}
		String filename = csvFile.getAbsolutePath();

		map.put("File Path", filename);
		map.put("File Time", getFileTime(filename));
		map.put("Sync Time", getSyncTime(csvFile));

		return map;
	}

	@SuppressWarnings("unchecked")
	private static  Map<String, Integer> readStat(String statFile) throws IOException {
		String jsonString = FileUtils.readFile(statFile);
		return JsonUtil.fromJsonString(jsonString, LinkedHashMap.class);
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

	@Override
	public void close() throws IOException {
		XXX.out("Done!");
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



//	public static void main(String[] args) throws IOException {
//		Map<String, Long> stat = readStat("D:\\imsi\\s1ap_1_prb_MSP6000-12249_ipx_reports_data_1506273000_60_0_9.stat.XE.json");
//		System.out.println(stat);
//		if (stat.get("IMSI(string)") == null) {
//			System.out.println("NULL");
//		}
//	}

	public static void main(String[] args) {
		final DMSKeyFlagReport report = new DMSKeyFlagReport();
		report.process();
		try {
    		report.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}