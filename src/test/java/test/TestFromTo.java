package test;

import java.text.SimpleDateFormat;
import java.util.Date;

import tw.moze.util.dev.XXX;

public class TestFromTo {
	public static void main(String[] args) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long timeTo = System.currentTimeMillis() / 1000L;
		long timeFrom = timeTo - (1 * 60 * 60) - (10 * 60);

		timeTo /= 1000;
		timeTo += 1;
		timeFrom /= 1000;

		XXX.out("Rsync " + sdf.format(new Date(timeFrom * 1000 * 1000)) + " ~ " + sdf.format(new Date(timeTo * 1000 * 1000)));
		for (long i = timeFrom; i <= timeTo; i++) {
			System.out.println(i);
		}
	}
}
