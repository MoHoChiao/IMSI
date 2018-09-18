package test.timer;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestString {
	static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
	public static void main(String[] args) {
		String str = "2017-06-27 22:18:07";
		Date d = new Date();
		String today = sdf1.format(d);
		System.out.println("today = " + today);
		String rowDate = str.substring(0,  10);
		System.out.println("row day = " + rowDate);
		String currentDate = today;
		int comp  = rowDate.compareTo(currentDate);
		System.out.println("comp = " + comp);
		
		Double d1 = 1d;
		System.out.println(d1.intValue());
		System.out.format("%.0f\n", 19999999999999999999999999.0);
	}
}
