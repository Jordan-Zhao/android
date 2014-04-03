package com.aliyun.oss.module;

public class GroupPart {
	private int partNumber;
	private String partName;
	private String eTag;
	private long partSize;
	
	public int getPartNumber() {
		return partNumber;
	}
	public void setPartNumber(int partNumber) {
		this.partNumber = partNumber;
	}
	public String getPartName() {
		return partName;
	}
	public void setPartName(String partName) {
		this.partName = partName;
	}
	public String getETag() {
		return eTag;
	}
	public void setETag(String eTag) {
		this.eTag = eTag;
	}
	public long getPartSize() {
		return partSize;
	}
	public void setPartSize(long partSize) {
		this.partSize = partSize;
	}
}
