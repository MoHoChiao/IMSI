package test.csv;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import com.opencsv.CSVParserBuilder;
import com.opencsv.ICSVParser;

import tw.moze.util.dev.XXX;
import tw.moze.util.fileformat.FieldCSVReader;

public class OpenCSVTest {
	public static void main(String[] args) throws IOException {
//		String srcFile = "D:\\imsi\\2017092810.1506565958.csv\\2017092810.1506565958.csv";
		String srcFile = "D:\\imsi\\2017092810.1506565958.csv\\cp.csv";
		XXX.out( "Start Reading" );

//		FieldCSVReader reader = new FieldCSVReader(new InputStreamReader(new FileInputStream(srcFile)), ',', '\'');

//		CSVReaderBuilder sb = new CSVReaderBuilder(new InputStreamReader(new FileInputStream(srcFile)));

		ICSVParser cp = new CSVParserBuilder().withQuoteChar('\01').withIgnoreQuotations(true).build();
//		ICSVParser cp = new CSVParserBuilder().withStrictQuotes(true).build();
		FieldCSVReader reader = new FieldCSVReader(new InputStreamReader(new FileInputStream(srcFile)), cp);

//		FieldCSVReader reader = new FieldCSVReader(new InputStreamReader(new FileInputStream(srcFile)));
		reader.readHeader();
		int i = 0;
		while (reader.next()) {
			i++;
//			if (i % 10000 == 0) {
//			if (reader.get("Model").contains("SMART 4G OCTA"))
			{
				System.out.println(i);
				System.out.println(Arrays.toString(reader.getValues()));
			}
		}
		XXX.out( "Done!" );
		reader.close();
	}
}
