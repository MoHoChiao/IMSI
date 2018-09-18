package test.csv;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import tw.moze.util.dev.XXX;
import tw.moze.util.fileformat.FieldCSVReader;

public class CsvReadDMS2 {
	private String path = "D:\\imsi\\dms155\\s1ap_1_prb_MSP6000-12251_ipx_reports_data_1504881060_60_0_7.csv";
	public CsvReadDMS2() {
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
//            	XXX.xxx("IMSI = " + reader.get("IMSI(string)"));
//            	XXX.xxx("M-TMSI = " + reader.get("M-TMSI (hex.)(string)"));
//            	XXX.xxx("GUMMEI = " + reader.get("GUMMEI(string)"));
//            	XXX.xxx("Start Time = " + reader.get("Start Time(uint64-nsec)"));
//            	XXX.xxx("End Time = " + reader.get("End Time(uint64-nsec)"));



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
		CsvReadDMS2 dmsReader = new CsvReadDMS2();
		dmsReader.doRead();
	}
}
