package tw.moze.core.conf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	
	public static List<Object> getAsList(String path) {
		List<Object> list = new ArrayList<Object>();
		JsonNode objs = get(path);
		if(null != objs && objs.isArray()) {
			for(JsonNode obj : objs) {
				if(obj.isInt())
					list.add(obj.asInt());
				else
					list.add(obj.asText());
			}
		}
		return list;
	}
}
