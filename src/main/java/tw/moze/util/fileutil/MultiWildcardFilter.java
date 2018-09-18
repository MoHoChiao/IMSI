package tw.moze.util.fileutil;


import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Multiple file name filter with WildcardFilter
 *
 * @author Edward Hsieh
 */
public class MultiWildcardFilter implements FilenameFilter {
	private List<Pattern> plist;
	public MultiWildcardFilter(String filterRegex){
		plist = new ArrayList<Pattern>();
		splitText(filterRegex);
	}

	@Override
	public boolean accept(File file, String name) {

		Pattern[] plists =new Pattern[plist.size()];
		plist.toArray(plists);
		for(Pattern pattern:plists) {
			boolean match = pattern.matcher(name).matches();
			if (match)
				return true;
		}
		return false;
	}

	private void addPattern(String match){
		plist.add(Pattern.compile(WildcardFilter.wildcardToRegex(match)));
	}

	private void splitText(String text){
		if (text.indexOf('|') != -1) {
			int splitPoint;
	    	while ((splitPoint = text.indexOf('|')) != -1) {
	    		addPattern(text.substring(0,splitPoint));
	    		text=text.substring(splitPoint+1,text.length());
	    	}
		}
		addPattern(text);
	}

}
