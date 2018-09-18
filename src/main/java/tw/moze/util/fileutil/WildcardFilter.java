package tw.moze.util.fileutil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filename filter. Used in FileTypeControl.<br>
 * For example, abc*.exe: accept all execution files start with abc.<br>
 * For example, abc?e.exe: accept files, abc1d.exe or abcde.exe<br>
 *
 * @author Jackson Wong
 * @author Edward Hsieh
 */
public class WildcardFilter implements FilenameFilter {
	private Pattern pattern;

	public WildcardFilter(String filterRegex){
		pattern = Pattern.compile(wildcardToRegex(filterRegex));
	}

	@Override
	public boolean accept(File file, String name) {
		Matcher fit = pattern.matcher(name);
		return fit.matches();
	}

	public static String wildcardToRegex(String text){
		StringBuffer regular = new StringBuffer();
		regular.append('^');
		char cha = 'c';
		for (int i = 0; i < text.length(); i++){
			cha = text.charAt(i);

			switch (cha) {
			case '*':
				regular.append(".*");
				break;
			case '?':
				regular.append(".");
				break;
			case '(': case ')': case '[': case ']':
			case '^': case '.': case '{': case '}':
			case '$': case '|': case '\\':
				regular.append("\\");
				regular.append(cha);
				break;
			default:
				regular.append(cha);
				break;
			}
		}
		regular.append("$");
		return regular.toString();
	}
}
