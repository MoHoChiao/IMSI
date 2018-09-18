package test.csv;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import com.opencsv.CSVReader;

import tw.moze.util.dev.XXX;

public class CsvReadDMS {
	private String path = "D:\\imsi\\dms155\\s1ap_1_prb_MSP6000-12251_ipx_reports_data_1504881060_60_0_7.csv";
	public CsvReadDMS() {
	}

	public void doRead() {
		CSVReader reader = null;

        try {
            reader = new CSVReader(new FileReader(path));

            int i = 0;

            String [] nextLine;
            // skip header
            String[] header = reader.readNext();
            	XXX.out( "header.length = " + header.length);
            	XXX.out(Arrays.toString(header));

            while ((nextLine = reader.readNext()) != null) {
            	if (nextLine.length != header.length) {
            		XXX.out( "error line >>> " + Arrays.toString(nextLine));
//            		reader.
            	}

           	 i++;
            }

            // Last batch maybe pending if there are less than batch size left over records. Sending last batch of such records explicitly

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
		CsvReadDMS dmsReader = new CsvReadDMS();
		dmsReader.doRead();
	}
}
