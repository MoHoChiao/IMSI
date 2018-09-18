package test.csv;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import tw.moze.util.dev.XXX;
import tw.moze.util.fileformat.FieldCSVReader;

public class CsvReadLSR {

	private String path = "D:\\imsi\\lsr141\\2017090910.1504925663.csv";
	public CsvReadLSR() {
	}

	public void doRead() {
		FieldCSVReader reader = null;

        try {
            reader = new FieldCSVReader(new FileReader(path));
            reader.readHeader();
            Map<String, Integer> headerMap = reader.getHeaderMap();
        	XXX.out( "header.length = " + headerMap.size());
        	XXX.out(headerMap.keySet().toString());

            int i = 0;

            while (reader.next()) {
            	if (reader.getValueCount() != headerMap.size()) {
            		XXX.out( "error line >>> " + Arrays.toString(reader.getValues()));
            		continue;
            	}
            	if (i < 10) {
	            	XXX.out("IMSI = " + reader.get("IMSI"));
	            	XXX.out("Start m-TMSI = " + reader.get("Start m-TMSI"));
	            	XXX.out("GUMMEI = " + reader.get("Start GUMMEI"));
	            	XXX.out("Start Time = " + reader.get("Start Time"));
	            	XXX.out("End Time = " + reader.get("End Time"));
            	}


           	 	i++;
            }

            XXX.out("total line = " + i);

        } catch (Exception e) {
            System.out.println("Error in CsvFileReader !!!" + e.getMessage());
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                ;
            }
        }


	}

	public static void main(String[] args) {
		CsvReadLSR dmsReader = new CsvReadLSR();
		dmsReader.doRead();
	}
}
