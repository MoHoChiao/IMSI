package tw.moze.util.fileformat;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.ICSVParser;

public class FieldCSVReader extends CSVReader {
	private Map<String, Integer> headerMap;
	private String[] headerNames;
	private String[] cells;

	public FieldCSVReader(Reader reader) {
		super(reader);
		headerMap = new LinkedHashMap<>();
	}

	public FieldCSVReader(Reader reader, char sep, char quote) {
		super(reader, sep, quote);
		headerMap = new LinkedHashMap<>();
	}

	public FieldCSVReader(Reader reader, ICSVParser parser) {
		super(reader, 0, parser);
		headerMap = new LinkedHashMap<>();
	}


	public FieldCSVReader setHeader(String[] names) {
		for (int i = 0; i < names.length; i++) {
			headerMap.put(names[i], i);
		}
		return this;
	}

	public FieldCSVReader readHeader() throws IOException {
		headerNames = super.readNext();
		if (headerNames == null)
			throw new IOException("No line to read as header!");
		return setHeader(headerNames);
	}

	public Map<String, Integer> getHeaderMap() {
		return headerMap;
	}

	public String[] getHeaderNames() {
		return headerNames;
	}

	public boolean next() throws IOException {
		cells = super.readNext();
		return cells != null;
	}

	public String get(int column) {
		return cells[column];
	}

	public String[] getValues() {
		return cells;
	}

	public int getValueCount() {
		return cells == null ? 0 : cells.length;

	}

	public String get(String field) {
		Integer column = headerMap.get(field);
		return (column == null) ? null : cells[column];
	}

	public String get(String field, String[] cells) {
		Integer column = headerMap.get(field);
		return (column == null) ? null : cells[column];
	}

	public void set(String field, String[] cells, String value) {
		Integer column = headerMap.get(field);
		if (column != null)
			cells[column] = value;
	}
}
