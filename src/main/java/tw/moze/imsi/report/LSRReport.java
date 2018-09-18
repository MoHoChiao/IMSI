package tw.moze.imsi.report;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
import tw.moze.util.fileutil.FileUtils;
import tw.moze.util.json.JsonUtil;

public class LSRReport {
	// src file mapping 完後會移到此處
	private String[] dirForMapped = new String[] {
			"/data/imsi_mapping/mapped/lsr/10.108.200.141/",
			"/data/imsi_mapping/mapped/lsr/10.108.200.142/",
			"/data/imsi_mapping/mapped/lsr/10.108.200.143/",
	};

	private String filePattern = "*.stat.json";
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	public LSRReport() {

	}

	public void process() {
		List<String> files = new LinkedList<>();
		List<String> dirs = new LinkedList<>();
		for (String srcPath: dirForMapped) {
			DirLister.deepListFiles(srcPath, filePattern, files, dirs);
		}
		List<Map<String, String>> list = new LinkedList<>();
		for (String file: files) {
			try {
				String jsonString = FileUtils.readFile(file);
				@SuppressWarnings("unchecked")
				Map<String, Integer> stat = JsonUtil.fromJsonString(jsonString, LinkedHashMap.class);
				Map<String, String> map = new LinkedHashMap<>();
				for(Map.Entry<String, Integer> entry: stat.entrySet()) {
					if ("File Error".equals(entry.getKey()))
						map.put("Session Error", String.valueOf(entry.getValue()));
					else
						map.put(entry.getKey(), String.valueOf(entry.getValue()));
				}

				double total = getMapValue(stat, "Total", 1d);
				double blank = getMapValue(stat, "IMSI Blank", 0d);
				double orig  = getMapValue(stat, "IMSI", 0d);
				double nokey = getMapValue(stat, "LSR No Key", 0d);
				double noimei = getMapValue(stat, "LSR No IMEI", 0d);
				double mapped = getMapValue(stat, "IMSI Mapped", 0d);
				double dmsNotMatch = getMapValue(stat, "DMS Not Match", 0d);
				double dmsMatch = total - dmsNotMatch - nokey;

				double keyNoImeiCount = getMapValue(stat, "KEY NO IMEI", 0d);
				double keyNoImsiCount = getMapValue(stat, "KEY NO IMSI", 0d);
				double ImeiNoKeyCount = getMapValue(stat, "IMEI NO KEY", 0d);
				double ImeiNoImsiCount = getMapValue(stat, "IMEI NO IMSI", 0d);
				double ImeiNoNewImsiCount = getMapValue(stat, "IMEI NO NewIMSI", 0d);
				double ImsiNoKeyCount = getMapValue(stat, "IMSI NO KEY", 0d);
				double ImsiNoImeiCount = getMapValue(stat, "IMSI NO IMEI", 0d);
				double NewImsiNoImeiCount = getMapValue(stat, "NewIMSI NO IMEI", 0d);

				double NewImeiCount = getMapValue(stat, "IMEI Mapped", 0d);
				double NewImsiNoNewImeiCount = getMapValue(stat, "NewIMSI NO NewIMEI", 0d);

				double KeyMappingCount = getMapValue(stat, "KEY IMSI Mapping", 0d);
				double ImeiMappingCount = getMapValue(stat, "IMEI IMSI Mapping", 0d);
				double DmsMappinCount = getMapValue(stat, "DMS IMSI Mapping", 0d);
				double KeyNoImeiMappinfCount = getMapValue(stat, "KEY NO IMEI Mapping", 0d);
				double ImeiNoKeyMappinfCount = getMapValue(stat, "IMEI NO KEY Mapping", 0d);
				map.put("DMS Match", String.valueOf(dmsMatch));
				map.put("Key Rate", Math.round(((total-nokey)/total)*10000)/100d + "%");
				map.put("IMEI Rate", Math.round(((total-noimei)/total)*10000)/100d + "%");
				map.put("Orig IMSI Rate", Math.round((orig/total)*10000)/100d + "%");

				map.put("KEY NO IMEI Rate", Math.round((keyNoImeiCount/total)*10000)/100d + "%");
				map.put("KEY NO IMSI Rate", Math.round((keyNoImsiCount/total)*10000)/100d + "%");
				map.put("IMEI NO KEY Rate", Math.round((ImeiNoKeyCount/total)*10000)/100d + "%");
				map.put("IMEI NO IMSI Rate", Math.round((ImeiNoImsiCount/total)*10000)/100d + "%");
				map.put("IMEI NO NewIMSI Rate", Math.round((ImeiNoNewImsiCount/total)*10000)/100d + "%");
				map.put("IMSI NO KEY Rate", Math.round((ImsiNoKeyCount/total)*10000)/100d + "%");
				map.put("IMSI NO IMEI Rate", Math.round((ImsiNoImeiCount/total)*10000)/100d + "%");
				map.put("NewIMSI NO IMEI Rate", Math.round((NewImsiNoImeiCount/total)*10000)/100d + "%");
				map.put("NewIMEI Rate", Math.round((NewImeiCount/total)*10000)/100d + "%");
				map.put("NewIMSI NO NewIMEI Rate", Math.round((NewImsiNoNewImeiCount/total)*10000)/100d + "%");

				map.put("DMS Not Match Rate", Math.round((dmsNotMatch/total)*10000)/100d + "%");
				map.put("Keyed DMS Match Rate",  Math.round((dmsMatch/(total-nokey))*10000)/100d + "%");
				map.put("DMS Match Rate",  Math.round((dmsMatch/total)*10000)/100d + "%");

				map.put("Mapped Rate", Math.round((mapped/total)*10000)/100d + "%");
				map.put("Keyed Mapped Rate", Math.round((mapped/(total-nokey))*10000)/100d + "%");
				map.put("Keyed Mapped Rate (with Blank)", Math.round(((mapped+blank)/(total-nokey))*10000)/100d + "%");

				map.put("KEY IMSI Mapping Rate", Math.round((KeyMappingCount/total)*10000)/100d + "%");
				map.put("IMEI IMSI Mapping Rate", Math.round((ImeiMappingCount/total)*10000)/100d + "%");
				map.put("DMS IMSI Mapping Rate", Math.round((DmsMappinCount/total)*10000)/100d + "%");
				map.put("KEY NO IMEI Mapping Rate", Math.round((KeyNoImeiMappinfCount/total)*10000)/100d + "%");
				map.put("IMEI NO KEY Mapping Rate", Math.round((ImeiNoKeyMappinfCount/total)*10000)/100d + "%");

				String filepath = file.substring("/data/imsi_mapping/mapped/lsr/".length(), file.length()-"stat.json".length()) + "csv";
				map.put("File Path", filepath);
				map.put("File Time", getFileTime(filepath));
				map.put("Sync Time", getSyncTime("/data/imsi_mapping/src/lsr/" + filepath + ".gz"));

				list.add(map);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (list.isEmpty()) {
			XXX.out("No " + filePattern + " found!");
			return;
		}

		String reportPath  = "/data/imsi_mapping/mapped/lsr/lsrreport." + sdf.format(new Date())+ ".csv";
		XXX.out("Sorting report items: " + list.size());
		Collections.sort(list, comp);

		ReportUtil.writeReport(list, reportPath);
		genDailyReport(list);
	}


	private String getSyncTime(String filePath) {
		File f = new File(filePath);
		long l = f.lastModified();
		return sdf2.format(new Date(l));
	}

	private void genDailyReport(List<Map<String, String>> list) {
		String today = sdf.format(new Date());
		String currentDate = today;
		DailyReportRow drow = new DailyReportRow();
		List<Map<String, String>> ret = new LinkedList<>();

		for (Map<String, String> row : list) {
			String rowDate = row.get("File Time").substring(0, 10);
			int comp = rowDate.compareTo(currentDate);

			if (comp > 0) {
				continue;
			}
			else if (comp == 0) {
				drow.add(row);
			}
			else { //
				if (drow.Total > 0d) {
					ret.add(drow.toMap(currentDate));
				}
				currentDate = rowDate;
				drow = new DailyReportRow();
				drow.add(row);
			}
		}

		if (drow.Total > 0d) {
			ret.add(drow.toMap(currentDate));
		}

		if (ret.isEmpty())
			return;
		////
		String reportPath  = "/data/imsi_mapping/mapped/lsr/lsrreport_daily." + sdf.format(new Date())+ ".csv";
		ReportUtil.writeReport(ret, reportPath);
	}



	Pattern p = Pattern.compile("\\/\\d{10}\\.(\\d{10})\\.");
	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private String getFileTime(String path) {
		Matcher m = p.matcher(path);
		if (m.find()) {
			String v = m.group(1);
			long l = Long.valueOf(v);
			return sdf2.format(new Date(l * 1000));
		}
		return "";
	}

	private double getMapValue(Map<String, Integer> map, String key, double defValue) {
		Integer ret = map.get(key);
		return ret == null? defValue : ret.doubleValue();
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
		LSRReport report = new LSRReport();
		report.process();
	}

	static class DailyReportRow {
		double IMSI;
		double IMEI;
		double Start_GUMMEI;
		double End_GUMMEI;
		double Start_Time;
		double End_Time;
		double Start_MME_UE_1AP_ID;
		double End_MME_UE_S1AP_ID;
		double Start_eNB_UE_S1AP_ID;
		double End_eNB_UE_S1AP_ID;
		double Total;
		double IMSI_Blank;
		double LSR_No_Key;
		double LSR_No_IMEI;
		double IMSI_Mapped;
		double DMS_Not_Match;
		double DMS_Match;
		double keyNoImeiCount;
		double keyNoImsiCount;
		double ImeiNoKeyCount;
		double ImeiNoImsiCount;
		double ImeiNoNewImsiCount;
		double ImsiNoKeyCount;
		double ImsiNoImeiCount;
		double NewImsiNoImeiCount;

		double NewImeiCount;
		double NewImsiNoNewImeiCount;

		double KeyMappingCount;
		double ImeiMappingCount;
		double DmsMappinCount;
		double KeyNoImeiMappinfCount;
		double ImeiNoKeyMappinfCount;
		double sessionErrorCount;
		double fileErrorCount;
		double fileCount;

		private void add(Map<String, String> row) {
			IMSI += getMapValue2(row, "IMSI", 0d);
			IMEI += getMapValue2(row, "IMEI", 0d);
			Start_GUMMEI += getMapValue2(row, "Start GUMMEI", 0d);
			End_GUMMEI += getMapValue2(row, "End GUMMEI", 0d);
			Start_Time += getMapValue2(row, "Start Time", 0d);
			End_Time += getMapValue2(row, "End Time", 0d);
			Start_MME_UE_1AP_ID += getMapValue2(row, "Start MME UE S1AP ID", 0d);
			End_MME_UE_S1AP_ID += getMapValue2(row, "End MME UE S1AP ID", 0d);
			Start_eNB_UE_S1AP_ID += getMapValue2(row, "Start eNB UE S1AP ID", 0d);
			End_eNB_UE_S1AP_ID += getMapValue2(row, "End eNB UE S1AP ID", 0d);
			Total += getMapValue2(row, "Total", 0d);
			IMSI_Blank += getMapValue2(row, "IMSI Blank", 0d);
			LSR_No_Key += getMapValue2(row, "LSR No Key", 0d);
			LSR_No_IMEI += getMapValue2(row, "LSR No IMEI", 0d);
			IMSI_Mapped += getMapValue2(row, "IMSI Mapped", 0d);
			DMS_Not_Match += getMapValue2(row, "DMS Not Match", 0d);
			DMS_Match += getMapValue2(row, "DMS Match", 0d);
			keyNoImeiCount += getMapValue2(row, "KEY NO IMEI", 0d);
			keyNoImsiCount += getMapValue2(row, "KEY NO IMSI", 0d);
			ImeiNoKeyCount += getMapValue2(row, "IMEI NO KEY", 0d);
			ImeiNoImsiCount += getMapValue2(row, "IMEI NO IMSI", 0d);
			ImeiNoNewImsiCount += getMapValue2(row, "IMEI NO NewIMSI", 0d);
			ImsiNoKeyCount += getMapValue2(row, "IMSI NO KEY", 0d);
			ImsiNoImeiCount += getMapValue2(row, "IMSI NO IMEI", 0d);
			NewImsiNoImeiCount += getMapValue2(row, "NewIMSI NO IMEI", 0d);

			NewImeiCount += getMapValue2(row, "IMEI Mapped", 0d);
			NewImsiNoNewImeiCount += getMapValue2(row, "NewIMSI NO NewIMEI", 0d);

			KeyMappingCount += getMapValue2(row, "KEY IMSI Mapping", 0d);
			ImeiMappingCount += getMapValue2(row, "IMEI IMSI Mapping", 0d);
			DmsMappinCount += getMapValue2(row, "DMS IMSI Mapping", 0d);
			KeyNoImeiMappinfCount += getMapValue2(row, "KEY NO IMEI Mapping", 0d);
			ImeiNoKeyMappinfCount += getMapValue2(row, "IMEI NO KEY Mapping", 0d);
			double sessionError = getMapValue2(row, "Session Error", 0d);
			if (sessionError > 0) {
				sessionErrorCount += sessionError;
				fileErrorCount++;
			}
			fileCount++;
		}

		private Map<String, String> toMap(String currentDate) {
			Map<String, String> ret = new LinkedHashMap<>();
			ret.put("IMSI", s(IMSI));
			ret.put("IMEI", s(IMEI));
			ret.put("Start GUMMEI", s(Start_GUMMEI));
			ret.put("End GUMMEI", s(End_GUMMEI));
			ret.put("Start Time", s(Start_Time));
			ret.put("End Time", s(End_Time));
			ret.put("Start MME UE S1AP ID", s(Start_MME_UE_1AP_ID));
			ret.put("End MME UE S1AP ID", s(End_MME_UE_S1AP_ID));
			ret.put("Start eNB UE S1AP ID", s(Start_eNB_UE_S1AP_ID));
			ret.put("End eNB UE S1AP ID", s(End_eNB_UE_S1AP_ID));
			ret.put("Total", s(Total));
			ret.put("IMSI Blank", s(IMSI_Blank));
			ret.put("LSR No Key", s(LSR_No_Key));
			ret.put("LSR No IMEI", s(LSR_No_IMEI));
			ret.put("IMSI Mapped", s(IMSI_Mapped));
			ret.put("DMS Not Match", s(DMS_Not_Match));

			ret.put("KEY NO IMEI", s(keyNoImeiCount));
			ret.put("KEY NO IMSI", s(keyNoImsiCount));
			ret.put("IMEI NO KEY", s(ImeiNoKeyCount));
			ret.put("IMEI NO IMSI", s(ImeiNoImsiCount));
			ret.put("IMEI NO NewIMSI", s(ImeiNoNewImsiCount));
			ret.put("IMSI NO KEY", s(ImsiNoKeyCount));
			ret.put("IMSI NO IMEI", s(ImsiNoImeiCount));

			ret.put("DMS Match", s(DMS_Match));
			ret.put("IMEI Mapped", s(NewImeiCount));
			ret.put("NewIMSI NO IMEI", s(NewImsiNoImeiCount));

			ret.put("KEY IMSI Mapping", s(KeyMappingCount));
			ret.put("IMEI IMSI Mapping", s(ImeiMappingCount));
			ret.put("DMS IMSI Mapping", s(DmsMappinCount));
			ret.put("KEY NO IMEI Mapping", s(KeyNoImeiMappinfCount));
			ret.put("IMEI NO KEY Mapping", s(ImeiNoKeyMappinfCount));

			ret.put("Key Rate", Math.round(((Total - LSR_No_Key)/Total)*10000)/100d + "%");
			ret.put("IMEI Rate", Math.round(((Total - LSR_No_IMEI)/Total)*10000)/100d + "%");
			ret.put("Orig IMSI Rate", Math.round((IMSI/Total)*10000)/100d + "%");

			ret.put("KEY NO IMEI Rate", Math.round((keyNoImeiCount/Total)*10000)/100d + "%");
			ret.put("KEY NO IMSI Rate", Math.round((keyNoImsiCount/Total)*10000)/100d + "%");
			ret.put("IMEI NO KEY Rate", Math.round((ImeiNoKeyCount/Total)*10000)/100d + "%");
			ret.put("IMEI NO IMSI Rate", Math.round((ImeiNoImsiCount/Total)*10000)/100d + "%");
			ret.put("IMEI NO NewIMSI Rate", Math.round((ImeiNoNewImsiCount/Total)*10000)/100d + "%");
			ret.put("IMSI NO KEY Rate", Math.round((ImsiNoKeyCount/Total)*10000)/100d + "%");
			ret.put("IMSI NO IMEI Rate", Math.round((ImsiNoImeiCount/Total)*10000)/100d + "%");
			ret.put("NewIMSI NO IMEI Rate", Math.round((NewImsiNoImeiCount/Total)*10000)/100d + "%");
			ret.put("NewIMEI Rate", Math.round((NewImeiCount/Total)*10000)/100d + "%");
			ret.put("NewIMSI NO NewIMEI Rate", Math.round((NewImsiNoNewImeiCount/Total)*10000)/100d + "%");

			ret.put("DMS Not Match Rate", Math.round((DMS_Not_Match/Total)*10000)/100d + "%");
			ret.put("Keyed DMS Match Rate", Math.round((DMS_Match/(Total - LSR_No_Key))*10000)/100d + "%");
			ret.put("DMS Match Rate", Math.round((DMS_Match/Total)*10000)/100d + "%");

			ret.put("Mapped Rate", Math.round((IMSI_Mapped/Total)*10000)/100d + "%");
			ret.put("Keyed Mapped Rate", Math.round((IMSI_Mapped/(Total-LSR_No_Key))*10000)/100d + "%");
			ret.put("Keyed Mapped Rate (with Blank)", Math.round(((IMSI_Mapped+IMSI_Blank)/(Total-LSR_No_Key))*10000)/100d + "%");


			ret.put("KEY IMSI Mapping Rate", Math.round((KeyMappingCount/Total)*10000)/100d + "%");
			ret.put("IMEI IMSI Mapping Rate", Math.round((ImeiMappingCount/Total)*10000)/100d + "%");
			ret.put("DMS IMSI Mapping Rate", Math.round((DmsMappinCount/Total)*10000)/100d + "%");
			ret.put("KEY NO IMEI Mapping Rate", Math.round((KeyNoImeiMappinfCount/Total)*10000)/100d + "%");
			ret.put("IMEI NO KEY Mapping Rate", Math.round((ImeiNoKeyMappinfCount/Total)*10000)/100d + "%");
			ret.put("File Count", s(fileCount));
//			ret.put("Session Error", s(sessionErrorCount));
			ret.put("File Error", s(fileErrorCount));
			ret.put("File Date", currentDate);

			return ret;
		}

		private static double getMapValue2(Map<String, String> map, String key, double defValue) {
			String ret = map.get(key);
			return ret == null? defValue : Double.valueOf(ret);
		}

		private static String s(double d) {
			return String.format("%.0f", d);
		}
	}
}
