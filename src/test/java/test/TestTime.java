package test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestTime {

	public static void main(String[] args) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss"); //
		System.out.println(sdf.parse("2017-07-05 17:26:13").getTime());

		System.out.println(Long.MAX_VALUE);

		String d_start_time  = "1499263099425730000";
		long d_start_timel = Long.valueOf(d_start_time) / 1000000;
		long start_timel = 1483262773000L;

		System.out.println(d_start_timel);
		System.out.println(start_timel);

	//	2017-07-24+14:06:58.7483991090 186031285 ./10.108.61.155/s1ap_1_prb_MSP6000-12251_ipx_reports_data_1500876300_60_0_7.csv

		long t = 1500876243282283936L/ 1000000;
		System.out.println(sdf.format(new Date(t)));
		System.out.println(sdf.format(new Date(1500876300 * 1000L)));

		System.out.println(sdf.format(new Date(1502807340 * 1000L)));
		System.out.println(sdf.format(new Date(1502807202 * 1000L)));
		System.out.println(sdf.format(new Date(1504346400 * 1000L)));
		System.out.println(sdf.format(new Date(1504337100 * 1000L)));
		System.out.println(sdf.format(new Date(1526967698 * 1000L)));


//		s1ap_1_prb_MSP6000-12251_ipx_reports_data_1504346400_60_0_7.csv
	}

}
