package tw.moze.imsi.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVWriter;

import tw.moze.util.dev.XXX;

public class ReportUtil {
	public static void writeReport(List<Map<String, String>> list, String reportPath) {
		XXX.out("Generating report file: " + reportPath);
		try (CSVWriter writer = new CSVWriter(new BufferedWriter(new FileWriter(reportPath)))) {
			String [] fields = list.get(0).keySet().toArray(new String[list.get(0).size()]);
			writer.writeNext(fields);
			for (Map<String, String> row : list) {
				String[] values = new String[fields.length];
				for (int i = 0; i < fields.length; i++) {
					String field = fields[i];
					String value = row.get(field);
					values[i] = value == null ? "": value;
				}
				writer.writeNext(values);
			}
			XXX.out("Done!");
		} catch (IOException e) {
			XXX.out("Unable to create report file: " + reportPath);
			e.printStackTrace();
		}
	}
}
