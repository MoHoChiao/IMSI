package bulkimport.message;

import java.util.ArrayList;
import java.util.List;

public class CSVRecordBatchMsg<T> {
    
    public CSVRecordBatchMsg(){
        records = new ArrayList<T>();
    }
    private int batchNo;
    private List<T> records;
    public int getBatchNo() {
        return batchNo;
    }
    public void setBatchNo(int batchNo) {
        this.batchNo = batchNo;
    }
    public List<T> getRecords() {
        return records;
    }
    public void setRecords(List<T> records) {
        this.records = records;
    }
    
    public void add(T person){
        records.add(person);
    }
    
    public int size() {
    	return records.size();
    }
}