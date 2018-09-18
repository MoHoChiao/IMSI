//package test.csv;
//
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.Reader;
//import java.util.Map;
//
//import org.apache.commons.csv.CSVFormat;
//import org.apache.commons.csv.CSVParser;
//import org.apache.commons.csv.CSVRecord;
//
//import tw.moze.util.dev.XXX;
//
//public class DMSReader {
//	private String path = "D:\\imsi_mapping\\dms\\10.108.61.133\\s1ap_1_prb_MSP6000-11154_ipx_reports_data_1490063700_60_0_5.csv";
//	public DMSReader() {
//	}
//
//	public void doRead() {
//		Reader reader = null;
//
//        try {
//            reader = new FileReader(path);
//
//            int i = 0;
//            CSVParser records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(reader);
//            Map<String, Integer> headerMap = records.getHeaderMap();
//            XXX.xxx("column number = " +  headerMap.size());
//            for (CSVRecord record : records) {
//                if (record.size() != headerMap.size())
//                	XXX.xxx("error line >>>" + record.toString());
//                i++;
//            }
//
//            XXX.xxx("total line = " + i);
//
//        } catch (Exception e) {
//            System.out.println("Error in CsvFileReader !!!" + e.getMessage());
//        } finally {
//            try {
//            	if (reader != null)
//            		reader.close();
//            } catch (IOException e) {
//                ;
//            }
//        }
//
//
//	}
//
//	public static void main(String[] args) {
//		DMSReader dmsReader = new DMSReader();
//		dmsReader.doRead();
//	}
//}
