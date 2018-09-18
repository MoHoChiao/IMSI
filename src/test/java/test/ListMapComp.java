package test;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ListMapComp {
	public static void main(String[] args) {
		List<Map<String, String>> data = new LinkedList<>();
		Map<String, String> map1 = new HashMap<>();
		map1.put("File Time", "123");
		map1.put("File Path", "abc");

		Map<String, String> map2 = new HashMap<>();
		map2.put("File Time", "890");
		map2.put("File Path", "zyx");

		Map<String, String> map3 = new HashMap<>();
		map3.put("File Time", "456");
		map3.put("File Path", "def");

		data.add(map1);
		data.add(map2);
		data.add(map3);

		System.out.println(data);
		Collections.sort(data, comp);

		System.out.println(data);
	}



	public static Comparator<Map<String, String>> comp = new Comparator<Map<String, String>>() {
		@Override
		public int compare(Map<String, String> r1, Map<String, String> r2) {
			String v1 = r1.get("File Time");
			String v2 = r2.get("File Time");

			int value1 = v2.compareTo(v1);
	        if (value1 == 0) {
				v1 = r1.get("File Path");
				v2 = r2.get("File Path");
	        	return v1.compareTo(v2);
	        }
	        return value1;
		}
	};
}