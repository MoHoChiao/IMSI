package test.csv;

import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tw.moze.util.dev.XXX;
import tw.moze.util.fileformat.FieldCSVReader;

public class CheckDMSReportFileTime {
	
	private String path = null;
	public CheckDMSReportFileTime(String path) {
		this.path = path;
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
            int errorCount = 0;

            while (reader.next()) {
            	if (reader.getValueCount() != headerMap.size()) {
            		XXX.out( "error line >>> " + Arrays.toString(reader.getValues()));
            		continue;
            	}
            	String filePath = reader.get("File Path");
            	String fileTime = reader.get("File Time");
               	if (i < 3) {
            		System.out.println("filePath = " + filePath + ", fileTime = " + fileTime);
            	}
            	String fileTimeCheck = getFileTime(filePath);
            	if (!fileTime.equals(fileTimeCheck)) {
            		errorCount++;
            		XXX.out("File path time error: " + filePath + ", fileTime=" + fileTime + ", fileTimeCheck=" + fileTimeCheck);
            	}
            	
           	 	i++;
            }

            XXX.out("error/total = " + errorCount + "/" + i);

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
				"D:/temp/dmsreport.2017-08-08.csv.out.csv",
				"D:/temp/dmsreport.2017-08-06.csv.out.csv",
				"D:/temp/dmsreport.2017-08-07.csv.out.csv"				
		};
		for (String path: pathes) {
			CheckDMSReportFileTime dmsReader = new CheckDMSReportFileTime(path);
			dmsReader.doRead();
		}
	}
}
