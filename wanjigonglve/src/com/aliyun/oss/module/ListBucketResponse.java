package com.aliyun.oss.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.httpclient.HttpMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import com.aliyun.oss.api.Utils;

public class ListBucketResponse extends Response {
	/**
	 * The name of the bucket being listed. Null if request fails.
	 */
	public String name = null;

	/**
	 * The prefix echoed back from the request. Null if request fails.
	 */
	public String prefix = null;

	/**
	 * The marker echoed back from the request. Null if request fails.
	 */
	public String marker = null;

	/**
	 * The delimiter echoed back from the request. Null if not specified in the
	 * request, or if it fails.
	 */
	public String delimiter = null;

	/**
	 * The maxKeys echoed back from the request if specified. 0 if request
	 * fails.
	 */
	public int maxKeys = 0;

	/**
	 * Indicates if there are more results to the list. True if the current list
	 * results have been truncated. false if request fails.
	 */
	public boolean isTruncated = false;

	/**
	 * Indicates what to use as a marker for subsequent list requests in the
	 * event that the results are truncated. Present only when a delimiter is
	 * specified. Null if request fails.
	 */
	public String nextMarker = null;

	/**
	 * A List of ObjectInfo representing the objects in the given bucket. Null
	 * if the request fails.
	 */
	private List<ObjectMeta> contents;

	/**
	 * A List of CommonPrefix objects representing the common prefixes of the
	 * keys that matched up to the delimiter. Null if the request fails.
	 */
	private List<String> commonPrefixes;

	public ListBucketResponse(HttpMethod method) throws IOException {
		super(method);
		if (getStatusCode() / 100 == 2) {
			InputStream is = method.getResponseBodyAsStream();
			String xml = Utils.inputStreamToString(is);
			parseGetBucketXml(xml);
		}
	}

	void parseGetBucketXml(String xmlString)
			throws UnsupportedEncodingException {
		List<ObjectMeta> contents = new LinkedList<ObjectMeta>();
		List<String> commonPrefixes = new LinkedList<String>();
		Document document = Utils.parseString(xmlString);
		Element root = document.getRootElement();
		Namespace ns = root.getNamespace();

		name = root.getChildText("Name", ns);
		prefix = root.getChildText("Prefix", ns);
		marker = root.getChildText("Marker", ns);
		maxKeys = Integer.valueOf(root.getChildText("MaxKeys", ns));
		delimiter = root.getChildText("Delimiter", ns);
		isTruncated = Boolean.valueOf(root.getChildText("IsTruncated", ns));
		nextMarker = root.getChildText("NextMarker", ns);
		List<Element> tempList = root.getChildren("Contents", ns);
		for (Element e : tempList) {
			ObjectMeta object = new ObjectMeta();
			object.setKey(e.getChildText("Key", ns));
			object.setETag(e.getChildText("ETag", ns));
			object.setType(e.getChildText("Type", ns));
			object.setLastModify(e.getChildText("LastModified", ns));
			object.setSize(Long.valueOf(e.getChildText("Size", ns)));
			object.setStorageClass(e.getChildText("StorageClass", ns));
			object.setOwnerId(e.getChild("Owner", ns).getChildText("ID", ns));
			object.setOwnerDisplayName(e.getChild("Owner", ns).getChildText(
					"DisplayName", ns));
			contents.add(object);
		}
		this.contents = contents;

		tempList = root.getChildren("CommonPrefixes", ns);
		for (Element e : tempList) {
			commonPrefixes.add(e.getChildText("Prefix", ns));
		}
		this.commonPrefixes = commonPrefixes;
	}

	public String getName() {
		return name;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getMarker() {
		return marker;
	}

	public int getMaxKeys() {
		return maxKeys;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public boolean isTruncated() {
		return isTruncated;
	}

	public List<ObjectMeta> getContents() {
		return contents;
	}

	public List<String> getCommonPrefixes() {
		return commonPrefixes;
	}

	public String getNextMarker() {
		return nextMarker;
	}
}
