package tw.moze.util.collection;

import java.util.TreeMap;

public class CountableMap extends TreeMap<String, Long> {
	private static final long serialVersionUID = 1L;
	public void add(String key) {
		Long val = this.get(key);
		if (val == null)
			this.put(key, 1L);
		else
			this.put(key, val + 1);
	}
}
