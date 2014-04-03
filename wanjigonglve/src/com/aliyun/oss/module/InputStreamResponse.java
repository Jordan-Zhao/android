package com.aliyun.oss.module;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpMethod;

public class InputStreamResponse extends Response {
	private InputStream inputStream = null;

	public InputStreamResponse(HttpMethod method) throws IOException {
		super(method);
		if (getStatusCode() / 100 == 2) {
			inputStream = method.getResponseBodyAsStream();
		}
	}

	public InputStream getInputStream() {
		return inputStream;
	}

}
