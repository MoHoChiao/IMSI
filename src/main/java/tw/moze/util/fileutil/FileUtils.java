package tw.moze.util.fileutil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class FileUtils {
	private FileUtils() {}

    public static void writeFile(String path, String content) throws IOException {
    	try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
    		bw.write(content);
    	}
    }

    public static String readFile(String path) throws IOException {
    	return new String(Files.readAllBytes(Paths.get(path)));
    }

    public static String readFile(String path, String charSet) throws IOException {
    	return new String(Files.readAllBytes(Paths.get(path)), charSet);
    }
}
