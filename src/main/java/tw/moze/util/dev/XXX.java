package tw.moze.util.dev;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class XXX {
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	public static String nowString() {
		return sdf.format(new Date());
	}
	public static void out(String out) {
		System.out.println(nowString() + " " + out);
	}

	public static void err(String out) {
		System.err.println(nowString() + " " + out);
	}

	public static <T> void xxx(T[] ary) {
		System.out.println(nowString() + Arrays.toString(ary));
	}

	public static void main(String[] argv) {
		Integer[] ary = {1, 2, 4};
		XXX.xxx(ary);
	}
}
