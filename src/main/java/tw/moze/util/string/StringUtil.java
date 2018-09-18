package tw.moze.util.string;

import java.util.Random;

public abstract class StringUtil {
	public static String getRandomString(Random r, int minLen) {
		int len = minLen + r.nextInt(minLen);
		String src = "ABCDE FGHIJKLM NOPQRS TUVWXYZ abcde fghij klmno pqrst uvwxyz";
		StringBuilder sb = new StringBuilder();
		char prevCh = ' ';
		while (sb.length() < len) {
			int index = r.nextInt(src.length());
			char ch = src.charAt(index);
			if (ch == ' ' && prevCh == ' ')
				continue;
			sb.append(ch);
			prevCh = ch;
		}

		return sb.toString();
	}

	public static String join(String delimiter, String... strs) {
		StringBuilder sb = new StringBuilder();
		for (String str: strs) {
			sb.append(str);
			sb.append(delimiter);
		}
		if (sb.length() > delimiter.length()) {
			sb.setLength(sb.length() - delimiter.length());
		}
		return sb.toString();
	}
}
