package test.csv;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opencsv.CSVWriter;

import tw.moze.util.dev.XXX;
import tw.moze.util.fileformat.FieldCSVReader;

public class RebuildDMSReportFileTime {

	private String path = null;
	public RebuildDMSReportFileTime(String path) {
		this.path = path;
	}

	public void doRead() {
		FieldCSVReader reader = null;
		List<Map<String, String>> list = Collections.synchronizedList(new ArrayList<Map<String, String>>());

        try {
            reader = new FieldCSVReader(new FileReader(path));
            reader.readHeader();
            String[] fields = reader.getHeaderNames();
        	XXX.out( "header.length = " + fields.length);

            while (reader.next()) {
            	if (reader.getValueCount() != fields.length) {
            		XXX.out( "error line >>> " + Arrays.toString(reader.getValues()));
            		continue;
            	}
            	Map<String, String> ret = new LinkedHashMap<>();
            	for(String field: fields) {
            		String value = reader.get(field);
            		ret.put(field, value);
            	}

            	String filePath = reader.get("File Path");
            	String fileTime = getFileTime(filePath);

            	ret.put("File Time", fileTime);
            	list.add(ret);
            }

            XXX.out("finished read!");


        } catch (Exception e) {
            System.out.println("Error in CsvFileReader !!!" + e.getMessage());
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                ;
            }
        }


		XXX.out("Sorting report items: " + list.size());
		Collections.sort(list, comp);
		String reportPath = path + ".out.csv";
		XXX.out("Generating report file: " + reportPath);

		try (CSVWriter writer = new CSVWriter(new BufferedWriter(new FileWriter(reportPath)))) {
			String [] fields = list.get(0).keySet().toArray(new String[list.get(0).size()]);
			writer.writeNext(fields);
			for (Map<String, String> row : list) {
				String[] values = row.values().toArray(new String[row.size()]);
				writer.writeNext(values);
			}
		} catch (IOException e) {
			XXX.out("Unable to create report file: " + reportPath);
			e.printStackTrace();
		}
		genDailyReport(list);
		XXX.out("Done!");
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
		String reportPath  = path + ".daily.csv";
		try (CSVWriter writer = new CSVWriter(new BufferedWriter(new FileWriter(reportPath)))) {
			String [] fields = ret.get(0).keySet().toArray(new String[ret.get(0).size()]);
			writer.writeNext(fields);
			for (Map<String, String> row : ret) {
				String[] values = new String[fields.length];
				for (int i = 0; i < fields.length; i++) {
					String field = fields[i];
					String value = row.get(field);
					values[i] = value == null ? "": value;
				}
				writer.writeNext(values);
			}
		} catch (IOException e) {
			XXX.out("Unable to create daily report file: " + reportPath);
			e.printStackTrace();
		}
	}


	private Map<String, String> genDRowMap(Map<String, Double> drow, String currentDate) {
		Map<String, String> ret = new LinkedHashMap<>();
		for (Map.Entry<String, Double> entry: drow.entrySet()) {
			ret.put(entry.getKey(), s(entry.getValue()));
		}
		double matchRate = 0d;
		double imsiRate = 0d;
		try {
			matchRate = drow.get("Matched")/drow.get("Total");
			imsiRate = drow.get("IMSI(string)")/drow.get("Total");
		}
		catch (Throwable t) {
			; // do nothing
		}

		ret.put("IMSI Rate", Math.round(imsiRate*10000)/100d + "%");
		ret.put("Match Rate", Math.round(matchRate*10000)/100d + "%");
		ret.put("File Date", currentDate);
		return ret;
	}

	private String s(double d) {
		return String.format("%.0f", d);
	}

	private void sumDRow(Map<String, Double> drow, Map<String, String> row) {
		String[] fieldToSum = {
				"IMSI(string)", "M-TMSI (hex.)(string)", "GUMMEI(string)", "MME UE S1AP ID(int64)",
				"eNB UE S1AP ID(int64)", "Start Time(uint64-nsec)", "End Time(uint64-nsec)", "Total",
				"Matched"
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
	}


	private double getMapValue2(Map<String, String> map, String key, double defValue) {
		String ret = map.get(key);
		return ret == null? defValue : Double.valueOf(ret);
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

	public static void main(String[] args) {
		String[] pathes = {
				"D:/temp/dmsreport.2017-08-05.csv",
//				"D:/temp/dmsreport.2017-08-05.csv",
//				"D:/temp/dmsreport.2017-08-07.csv",
//				"D:/temp/dmsreport.2017-08-08.csv"
		};

		for (String path : pathes) {
			RebuildDMSReportFileTime dmsReader = new RebuildDMSReportFileTime(path);
			dmsReader.doRead();
		}

	}
}
