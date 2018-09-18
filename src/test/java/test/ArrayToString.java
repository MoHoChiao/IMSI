package test;

import java.util.Arrays;

public class ArrayToString {
	public static void main(String[] args) {
		System.out.println(join("A", "B", "C"));
//		System.out.println("Done!");
	}

	public static String join(String... strs) {
		return Arrays.toString(strs);
	}
}
