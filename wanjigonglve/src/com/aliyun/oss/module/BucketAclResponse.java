package com.aliyun.oss.module;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import com.aliyun.oss.api.Utils;

public class BucketAclResponse extends Response {
	private String acl = null;
	private String ownerId = null;
	private String ownerDisplayName = null;

	public BucketAclResponse(HttpMethod method) throws IOException {
		super(method);
		if (getStatusCode() / 100 == 2) {
			InputStream is = method.getResponseBodyAsStream();
			String xml = Utils.inputStreamToString(is);
			paresBucketAcl(xml);
		}
	}

	void paresBucketAcl(String xmlString) {
		Document document = Utils.parseString(xmlString);
		Element root = document.getRootElement();
		Namespace ns = root.getNamespace();
		try {
			String acl = root.getChild("AccessControlList").getChildText(
					"Grant", ns);
			if (acl != null && !"".equals(acl)) {
				this.acl = acl;
			}
			this.ownerId = root.getChild("Owner").getChildText("ID", ns);
			this.ownerDisplayName = root.getChild("Owner").getChildText(
					"DisplayName", ns);
		} catch (Exception e) {
			throw new RuntimeException("Unexpected error parsing xml", e);
		}
	}

	public String getAcl() {
		return acl;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public String getOwnerDisplayName() {
		return ownerDisplayName;
	}
}
