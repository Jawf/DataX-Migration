package com.yxt.data.migration.bean;

public class DataTableStatus extends DataTable {

	private boolean finished = false;
	private boolean hasException = false;
	private String costTime="";
	private String readWriteRateSpeed="";
	private String readWriteRecordSpeed="";
	private long pendingRecords = 0;
	private long finishedRecords = 0;
	private long readWriteRecords = 0;
	private long readWriteFailRecords = 0;
	
	public boolean isFinished() {
		return finished;
	}
	public void setFinished(boolean finished) {
		this.finished = finished;
	}
	public boolean isHasException() {
		return hasException;
	}
	public void setHasException(boolean hasException) {
		this.hasException = hasException;
	}
	public String getCostTime() {
		return costTime;
	}
	public void setCostTime(String costTime) {
		this.costTime = costTime;
	}
	public String getReadWriteRateSpeed() {
		return readWriteRateSpeed;
	}
	public void setReadWriteRateSpeed(String readWriteRateSpeed) {
		this.readWriteRateSpeed = readWriteRateSpeed;
	}
	public String getReadWriteRecordSpeed() {
		return readWriteRecordSpeed;
	}
	public void setReadWriteRecordSpeed(String readWriteRecordSpeed) {
		this.readWriteRecordSpeed = readWriteRecordSpeed;
	}
	public long getReadWriteRecords() {
		return readWriteRecords;
	}
	public void setReadWriteRecords(long readWriteRecords) {
		this.readWriteRecords = readWriteRecords;
	}
	public long getReadWriteFailRecords() {
		return readWriteFailRecords;
	}
	public void setReadWriteFailRecords(long readWriteFailRecords) {
		this.readWriteFailRecords = readWriteFailRecords;
	}
	public long getPendingRecords() {
		return pendingRecords;
	}
	public void setPendingRecords(long pendingRecords) {
		this.pendingRecords = pendingRecords;
	}
	public long getFinishedRecords() {
		return finishedRecords;
	}
	public void setFinishedRecords(long finishedRecords) {
		this.finishedRecords = finishedRecords;
	}
	
}
