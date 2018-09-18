package tw.moze.core.conf;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

import tw.moze.util.json.JsonUtil;

public class Config {
	private final static String configFile = "cfg/appconfig.json";
	private static JsonNode instance;
	static {
		try {
			instance = JsonUtil.readFromFile(configFile);
		} catch (IOException e) {
			System.err.println("Unable to read system config file " + new File(configFile).getAbsolutePath());
		}
	}
	public static JsonNode get() {
		return instance;
	}
	public static JsonNode get(String path) {
		String[] pathes = path.split("\\/");
		JsonNode obj = instance;
		for(String part: pathes) {
			obj = obj.get(part);
			if (obj == null)
				return null;
		}
		return obj;
	}
}
