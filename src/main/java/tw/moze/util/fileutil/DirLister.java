package tw.moze.util.fileutil;

import java.io.File;
import java.util.List;

import tw.moze.util.dev.XXX;

public class DirLister {
	public static void deepListFiles(String dir, String wildcard, List<String> files, List<String> dirs) {
		WildcardFilter filter = new WildcardFilter(wildcard);
		File[] items = new File(dir).listFiles();
		if (items == null) {
			XXX.out("Deep list files: directory [" + dir + "] not found.");
			return;
		}
		for (File f: items) {
			String fullPath = f.getAbsolutePath();
			if (f.isFile()) {
				if (filter.accept(f.getParentFile(), f.getName())) {
					files.add(fullPath);
				}
			}
			else if (f.isDirectory()) {
				dirs.add(f.getAbsolutePath());
				deepListFiles(fullPath, wildcard, files, dirs);
			}
		}
	}

	public static void deepListFiles(String dir, String wildcard, long mofifyAfter, List<String> files, List<String> dirs) {
		WildcardFilter filter = new WildcardFilter(wildcard);
		File[] items = new File(dir).listFiles();
		if (items == null) {
			XXX.out("Deep list files: directory [" + dir + "] not found.");
			return;
		}
		for (File f: items) {
			String fullPath = f.getAbsolutePath();
			if (f.isFile()) {
//				if (f.lastModified() < mofifyAfter) {
//					continue;
//				}

				if (filter.accept(f.getParentFile(), f.getName())) {
					files.add(fullPath);
				}
			}
			else if (f.isDirectory()) {
				int fileCount = files.size();
				deepListFiles(fullPath, wildcard, files, dirs);

				if (files.size() > fileCount)
					dirs.add(f.getAbsolutePath());
			}
		}
	}
}
