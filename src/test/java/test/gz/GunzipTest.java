package test.gz;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class GunzipTest {
	private static final String INPUT_GZIP_FILE = "D:\\imsi_mapping\\lsr\\10.108.200.141\\170320\\22\\2017032022.1490019526.csv.gz";
	private static final String OUTPUT_FILE = "D:/data/file1.csv";

	public static void main(String[] args) {
		GunzipTest gZip = new GunzipTest();
		gZip.gunzipIt();
	}

	/**
	 * GunZip it
	 */
	public void gunzipIt() {

		byte[] buffer = new byte[1024];

		try {

			GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(INPUT_GZIP_FILE));

			FileOutputStream out = new FileOutputStream(OUTPUT_FILE);

			int len;
			while ((len = gzis.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}

			gzis.close();
			out.close();

			System.out.println("Done");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
