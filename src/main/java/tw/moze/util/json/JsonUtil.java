package tw.moze.util.json;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class JsonUtil {
	static ObjectMapper mapper = new ObjectMapper()
			.setSerializationInclusion(JsonInclude.Include.NON_NULL)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static <T> T map2Pojo(Map<String, Object> map, Class<T> clazz) {
        T ret = mapper.convertValue(map, clazz);
        return ret;
    }

    public static Map<String, Object> pojo2Map(Object obj) {
        @SuppressWarnings("unchecked")
        Map<String, Object> ret = mapper.convertValue(obj, LinkedHashMap.class);
        return ret;
    }

	public static String toJsonString (Object obj) throws JsonProcessingException {
//		String jsonInString = mapper.writeValueAsString(obj);
		if (obj == null)
			return null;
		String jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		return jsonInString;
	}

	public static  <T> T fromJsonString (String jsonInString, Class<T> clazz) throws IOException {
		if (jsonInString == null)
			return null;
		T ret = mapper.readValue(jsonInString, clazz);
		return ret;
	}

	public static  <T> T fromJsonString (String jsonInString, TypeReference<T> ref) throws IOException {
		if (jsonInString == null)
			return null;
		T ret = mapper.readValue(jsonInString, ref);
		return ret;
	}

	public static JsonNode readFromFile(String file) throws IOException {
		try {
			String json = new String(Files.readAllBytes(Paths.get(file)), "UTF8");
			JsonNode node = mapper.readTree(json);
			return node;
		}
		catch (UnsupportedEncodingException ex) {
			// impossible
		}
		return mapper.createObjectNode();
	}
}
