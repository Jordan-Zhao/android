package com.aliyun.oss.module;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;

import com.aliyun.oss.api.Utils;

/**
 * The parent class of all other Responses. This class keeps track of the
 * HttpMethod response.
 */
public class Response {

	private HttpMethod method = null;
	private int statusCode = 0;
	private String errorMessage = null;
	private Map<String, String> headers = null;

	public Response(HttpMethod method) throws IOException {

		this.method = method;
		statusCode = method.getStatusCode();
		if (statusCode / 100 != 2) {
			InputStream is = method.getResponseBodyAsStream();
			errorMessage = Utils.inputStreamToString(is);
		} else {
			headers = headersToMap(method.getResponseHeaders());
		}
	}

	/**
	 * Header[] to map<string,string>
	 */
	static Map<String, String> headersToMap(Header[] headers) {
		Map<String, String> map = new HashMap<String, String>();
		for (Header header : headers) {
			map.put(header.getName(), header.getValue());
		}
		return map;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public HttpMethod getMethod() {
		return method;
	}

}
