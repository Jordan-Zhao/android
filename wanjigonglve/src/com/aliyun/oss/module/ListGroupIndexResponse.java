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

public class ListGroupIndexResponse extends Response{
	private String bucket;
	private String Key;
	private String eTag;
	private long fileLength;
	private List<GroupPart> filePart;
	
	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getKey() {
		return Key;
	}

	public void setKey(String key) {
		Key = key;
	}

	public String geteTag() {
		return eTag;
	}

	public void seteTag(String eTag) {
		this.eTag = eTag;
	}

	public long getFileLength() {
		return fileLength;
	}

	public void setFileLength(long flieLength) {
		this.fileLength = flieLength;
	}

	public List<GroupPart> getFilePart() {
		return filePart;
	}

	public void setFilePart(List<GroupPart> filePart) {
		this.filePart = filePart;
	}
	
	public ListGroupIndexResponse(HttpMethod method) throws IOException {
		super(method);
		if (getStatusCode() / 100 == 2) {
			InputStream is = method.getResponseBodyAsStream();
			String xml = Utils.inputStreamToString(is);
			parseListGroupIndexXml(xml);
		}
	}

	void parseListGroupIndexXml(String xmlString)
			throws UnsupportedEncodingException {
		filePart = new LinkedList<GroupPart>();
		Document document = Utils.parseString(xmlString);
		Element root = document.getRootElement();
		Namespace ns = root.getNamespace();

		bucket = root.getChildText("Bucket", ns);
		Key = root.getChildText("Key", ns);
		eTag = root.getChildText("ETag", ns);
		fileLength = Long.valueOf(root.getChildText("FileLength", ns));
		List<Element> tempList = root.getChild("FilePart", ns).getChildren("Part", ns);
		for (Element e : tempList) {
			GroupPart groupPart = new GroupPart();
			groupPart.setPartNumber(Integer.valueOf(e.getChildText("PartNumber", ns)));
			groupPart.setPartName(e.getChildText("PartName", ns));
			groupPart.setETag(e.getChildText("ETag", ns));
			groupPart.setPartSize(Long.valueOf(e.getChildText("PartSize", ns)));
			filePart.add(groupPart);
		}
	}
}
