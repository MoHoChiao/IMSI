package bulkimport.message;

public class BatchCompleteMsg {

    private int batchNo;
    private int noOfRecordsProcessed;

    public BatchCompleteMsg(int batchNo,int noOfRecordsProcessed){
        this.batchNo = batchNo;
        this.noOfRecordsProcessed = noOfRecordsProcessed;
    }
    public int getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(int batchNo) {
        this.batchNo = batchNo;
    }

    public int getNoOfRecordsProcessed() {
        return noOfRecordsProcessed;
    }

    public void setNoOfRecordsProcessed(int noOfRecordsProcessed) {
        this.noOfRecordsProcessed = noOfRecordsProcessed;
    }
    
}