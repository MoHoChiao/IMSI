package tw.moze.util.fileutil;

import java.io.File;
import java.util.Objects;

public class FileExtUtils {
	public static File newExtension(File f, String newExtension) {
		Objects.requireNonNull(newExtension);

		if ( !newExtension.startsWith(".") ) {
			newExtension = "." + newExtension;
		}

		String name = f.getName();
		int dotIndex = name.lastIndexOf(".");
		if (dotIndex == -1)
			return new File(f.getParentFile(), name + newExtension);
		return new File(f.getParentFile(), name.substring(0, dotIndex) + newExtension);
	}

	public static File replaceExtension(File f, String oldExtension, String newExtension) {
		Objects.requireNonNull(oldExtension);
		Objects.requireNonNull(newExtension);

		if ( !newExtension.startsWith(".") ) {
			newExtension = "." + newExtension;
		}

		if ( !oldExtension.startsWith(".") ) {
			oldExtension = "." + oldExtension;
		}

		String name = f.getName();
		int dotIndex = name.length() - oldExtension.length();
		if (dotIndex < 0)
			return new File(f.getParentFile(), name + newExtension);
		return new File(f.getParentFile(), name.substring(0, dotIndex) + newExtension);
	}
	public static void main(String[] argv) {
		String path = "D:/imsi_mapping/lsr/10.108.200.141/170320/22/2017032022.1490023741.csv";
		File f = new File(path);
		File f1 = newExtension(f, ".mapped.csv");
		System.out.println(f1);
	}
}
