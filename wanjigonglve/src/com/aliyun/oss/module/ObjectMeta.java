package com.aliyun.oss.module;

public class ObjectMeta {
	/**
	 * The name of the object
	 */
	private String key;

	/**
	 * The date at which the object was last modified.
	 */
	private String lastModify;

	/**
	 * The size of the object in bytes.
	 */
	private long size;

	/**
	 * The object's ETag, which can be used for conditional GETs.
	 */
	private String eTag;

	/**
	 * The type of the object
	 */
	private String type;

	/**
	 * The object's storage class
	 */
	private String storageClass;

	/**
	 * The object's ownerId
	 */
	private String ownerId;

	/**
	 * the owner's display name
	 */
	private String ownerDisplayName;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLastModify() {
		return lastModify;
	}

	public void setLastModify(String lastModify) {
		this.lastModify = lastModify;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getETag() {
		return eTag;
	}

	public void setETag(String eTag) {
		this.eTag = eTag;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStorageClass() {
		return storageClass;
	}

	public void setStorageClass(String storageClass) {
		this.storageClass = storageClass;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerDisplayName() {
		return ownerDisplayName;
	}

	public void setOwnerDisplayName(String ownerDisplayName) {
		this.ownerDisplayName = ownerDisplayName;
	}

}
