package test;

import java.util.Arrays;

public class TestArrayCopy {

	public static void main(String[] args) {
		String[] a = {"A", "B", "C"};
		String[] b = new String[a.length + 2];
		
		for (int i = 0; i < a.length; i++)
			b[i] = a[i];
		
		b[a.length + 0] = "X";
		b[a.length + 1] = "Y";
		
		a[0] = "Z";
		
		System.out.println(Arrays.asList(b));
	}

}
