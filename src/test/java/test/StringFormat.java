package test;

import tw.moze.util.dev.XXX;

public class StringFormat {
	public static void main(String[] args) {
		int a = 466;
		int b = 787;
		double d =  (double)a/b;
		d = Math.round(d * 10000)/100d;
		System.out.println(d);

		try {
			System.out.println("try");
			throw new RuntimeException("Haha");
		}
		finally {
			XXX.out("finally!");
		}
	}
}
