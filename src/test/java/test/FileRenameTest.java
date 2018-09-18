package test;

import java.io.File;
import java.io.IOException;

import tw.moze.util.fileutil.FileUtils;

public class FileRenameTest {
	public static void main(String[] args) throws IOException {
		File txtFile = new File("/data/test1.txt");
		FileUtils.writeFile(txtFile.getPath(), "file1");
		txtFile.renameTo(new File("/data/test2.txt"));
		FileUtils.writeFile(txtFile.getPath(), "fileNew");
	}
}
