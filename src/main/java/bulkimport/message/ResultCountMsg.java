package bulkimport.message;

public class ResultCountMsg {
	private int resultCount;
	
	public ResultCountMsg(int resultCount) {
		this.resultCount = resultCount;
	}

	public int getResultCount() {
		return resultCount;
	}

	public void setResultCount(int resultCount) {
		this.resultCount = resultCount;
	}
	
}
