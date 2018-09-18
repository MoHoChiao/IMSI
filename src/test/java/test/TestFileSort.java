package test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import tw.moze.imsi.reader.DMSReader;
import tw.moze.util.fileutil.DirLister;

public class TestFileSort {
	public static void main(String[] args) {
		List<String> files = new ArrayList<>();
		List<String> dirs = new ArrayList<>();
		
		DirLister.deepListFiles("D:/temp", "*", files, dirs);
		Collections.sort(files, DMSReader.comp);
		for (String file: files) {
			long xdate = new File(file).lastModified();
			System.out.println(new Date(xdate) + " " + file);
		}
	}
}
