package com.aliyun.oss.module;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import com.aliyun.oss.api.Utils;

public class ListAllMyBucketsResponse extends Response {
	private String ownerId;
	private String displayName;
	private Map<String, String> buckets;

	public ListAllMyBucketsResponse(HttpMethod method) throws IOException {
		super(method);
		if (getStatusCode() / 100 == 2) {
			InputStream is = method.getResponseBodyAsStream();
			String xml = Utils.inputStreamToString(is);
			parseGetServiceXml(xml);
		}
	}

	void parseGetServiceXml(String xmlString) {
		Document document = Utils.parseString(xmlString);
		Element root = document.getRootElement();
		Namespace ns = root.getNamespace();
		Element owner = root.getChild("Owner", ns);
		ownerId = owner.getChildText("ID", ns);
		displayName = owner.getChildText("DisplayName", ns);
		Map<String, String> buckets = new HashMap<String, String>();
		List<Element> tempList = root.getChild("Buckets", ns).getChildren(
				"Bucket", ns);
		for (Element e : tempList) {
			buckets.put(e.getChildText("Name", ns),
					e.getChildText("CreationDate", ns));
		}
		this.buckets = buckets;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Map<String, String> getBuckets() {
		return buckets;
	}
}
