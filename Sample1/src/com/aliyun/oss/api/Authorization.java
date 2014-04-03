package com.aliyun.oss.api;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.aliyun.oss.module.BASE64Encoder;

public class Authorization {
	private final static String DEFINE_PREFIX = "x-oss-";
	private final static String ALGORITHM = "HmacSHA1";

	public static String safeGetElement(String key, Map<String, String> map) {
		return map == null ? "" : (map.containsKey(key) ? map.get(key) : "");
	}

	public static TreeMap<String, String> formatHeader(
			Map<String, String> headers) {
		TreeMap<String, String> tmpHeaders = new TreeMap<String, String>();
		Set<String> keySet = headers.keySet();
		for (String key : keySet) {
			if (key.toLowerCase().startsWith(DEFINE_PREFIX)) {
				tmpHeaders.put(key.toLowerCase(), headers.get(key));
			} else {
				tmpHeaders.put(key, headers.get(key));
			}
		}
		return tmpHeaders;
	}

	public static String getAssign(String secretAccessKey, String method,
			Map<String, String> headers, String resource) {
		StringBuffer canonicalizedOssHeaders = new StringBuffer();
		StringBuffer stringToSign = new StringBuffer();
		byte[] byteHMAC = null;
		String contentMd5 = safeGetElement(Utils.CONTENT_MD5, headers);
		String contentType = safeGetElement(Utils.CONTENT_TYPE, headers);
		String date = safeGetElement(Utils.DATE, headers);
		String canonicalizedResource = resource;
		TreeMap<String, String> tmpHeaders = formatHeader(headers);
		if (tmpHeaders.size() > 0) {
			Set<String> keySet = tmpHeaders.keySet();
			for (String key : keySet) {
				if (key.toLowerCase().startsWith(DEFINE_PREFIX)) {
					canonicalizedOssHeaders.append(key).append(":")
							.append(tmpHeaders.get(key)).append("\n");
				}
			}
		}
		stringToSign.append(method).append("\n").append(contentMd5)
				.append("\n").append(contentType).append("\n").append(date)
				.append("\n").append(canonicalizedOssHeaders)
				.append(canonicalizedResource);
		try {
			Mac mac = Mac.getInstance(ALGORITHM);
			SecretKeySpec spec = new SecretKeySpec(secretAccessKey.getBytes(Utils.CHARSET),
					ALGORITHM);
			mac.init(spec);
			byteHMAC = mac.doFinal(stringToSign.toString().getBytes(Utils.CHARSET));
		} catch (Exception e) {
		} 
		return new BASE64Encoder().encode(byteHMAC).toString();
	}
}
