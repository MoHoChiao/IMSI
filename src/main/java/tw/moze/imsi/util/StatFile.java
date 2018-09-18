package tw.moze.imsi.util;

import java.util.Map;

import tw.moze.util.fileutil.FileUtils;
import tw.moze.util.json.JsonUtil;

public class StatFile {
	public static <T> void save(String path, Map<String, T> stat) {
		try {
			FileUtils.writeFile(path, JsonUtil.toJsonString(stat));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
