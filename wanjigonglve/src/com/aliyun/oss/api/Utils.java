package com.aliyun.oss.api;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.Calendar;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

public class Utils {
	public static final String CHARSET = "utf-8";
	public static final String DEFAULT_XML_CHARSET = "utf-8";
	public static final int DEFAULT_PORT = 8080;
	public static final String DEFAULT_HOST = "oss.aliyuncs.com";
	public static final String ACL = "x-oss-acl";
	public static final String ACL_PRIVATE = "private";
	public static final String ACL_PUBLIC_READ = "public-read";
	public static final String ACL_PUBLIC_READ_WRITE = "public-read-write";
	public static final String CONTENT_MD5 = "Content-Md5";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String CONTENT_LENGTH = "Content-Length";
	public static final String COPY_SOURCE = "x-oss-copy-source";
	public static final String DATE = "Date";
	public static final String HOST = "Host";
	public static final String AUTHORIZATION = "Authorization";
	public static final int BUFFER_SIZE = 8192;

	/**
	 * Validate bucket name
	 */
	static boolean validateBucketName(String bucket) {
		if (bucket == null) {
			return false;
		}
		final String BUCKET_REGEX = "^[a-z0-9][a-z0-9_\\-]{2,254}$";
		return bucket.matches(BUCKET_REGEX);
	}

	/**
	 * Validate object name
	 */
	static boolean validateObjectName(String object) {
		if (object == null) {
			return false;
		}
		final String OBJECT_REGEX = "^.{1,255}$";
		return object.matches(OBJECT_REGEX);
	}

	/**
	 * Generate an rfc822 date for use in the Date HTTP header.
	 */
	static String getGMTTime() {
		String dateTime = null;
		Date now = new Date();
		SimpleDateFormat format = new SimpleDateFormat(
				"E, dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
		Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
		format.setCalendar(cal);
		dateTime = format.format(now) + " GMT";
		return dateTime;
	}

	/**
	 * Set the given headers to the HttpMethod.
	 * 
	 * @param method
	 *            The HttpMethod to which the headers will be added.
	 * @param headers
	 *            A Map of String to List of Strings representing the HTTP
	 *            headers to pass(can be null).
	 */
	static void setHeaders(HttpMethod method, Map<String, String> headers) {
		if (headers != null) {
			Set<String> keySet = headers.keySet();
			for (String key : keySet) {
				Header header = new Header(key, headers.get(key));
				method.setRequestHeader(header);
			}
		}
	}

	static String paramsToString(Map<String, String> params) {
		if (params == null) {
			return "";
		}
		String url = "?";
		Set<String> keySet = params.keySet();
		boolean hasParams = false;
		for (String key : keySet) {
			String value = params.get(key);
			key = key.replace("_", "-");
			if ("maxkeys".equals(key)) {
				key = "max-keys";
			}
			if (value != null && !"".equals(value)) {
				url += (hasParams ? "&" : "");
				hasParams = true;
				try {
					value = URLEncoder.encode(value, CHARSET);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("Could not url encode to "
							+ CHARSET, e);
				}
				url += key + "=" + value;
			} else if ("acl".equals(key)) {
				url += (hasParams ? "&" : "");
				hasParams = true;
				url += key;
			}
		}
		url = url.replaceAll("\\+", "%20");
		if (hasParams)
			return url;
		else
			return "";
	}

	/**
	 * Generate object URI
	 * 
	 * @param object
	 *            Object name
	 * @return object URI
	 */
	public static String objectUri(String object) {
		String[] objects = object.split("/");
		StringBuffer uri = new StringBuffer();
		try {
			uri.append(URLEncoder.encode(objects[0], CHARSET));
			for (int i = 1; i < objects.length; i++) {
				uri.append("/").append(URLEncoder.encode(objects[i], CHARSET));
			}
			if (object.endsWith("/")) {
				// String.split ignore trailing empty strings
				for (int i = object.length() - 1; i >= 0; i--) {
					if (object.charAt(i) == '/') {
						uri.append("/");
					} else {
						break;
					}
				}
			}
			return uri.toString().replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Could not url encode to " + CHARSET, e);
		}
	}

	/**
	 * Save InputStream to disk
	 * 
	 * @throws FileNotFoundException
	 * @throws HttpException
	 * @throws IOException
	 */
	public static void saveInputStreamToFile(InputStream in, String filename)
			throws FileNotFoundException, HttpException, IOException {
		BufferedOutputStream out = null;
		byte[] bt = new byte[BUFFER_SIZE];
		try {
			File file = new File(filename);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			out = new BufferedOutputStream(new FileOutputStream(file));
			int readLength = in.read(bt);
			while (readLength != -1) {
				out.write(bt, 0, readLength);
				readLength = in.read(bt);
			}
		} finally {
			if (in != null) {
				try {in.close();} catch (Exception e) {}
			}
			if (out != null) {
				try {out.close();} catch (Exception e) {}
			}
		}
	}

	/**
	 * Convert InputStream to String
	 * 
	 * @throws IOException
	 */
	public static String inputStreamToString(InputStream is) throws IOException {
		if (is != null) {
			BufferedReader in = new BufferedReader(new InputStreamReader(is,
					CHARSET));
			StringBuffer buffer = new StringBuffer();
			String line = "";
			boolean flag = false;
			while ((line = in.readLine()) != null) {
				if (flag) {
					buffer.append('\n');
				} else {
					flag = true;
				}
				buffer.append(line);
			}
			try {is.close();} catch (Exception e) {}
			try {in.close();} catch (Exception e) {}
			return buffer.toString();
		}
		return null;
	}

	public static String generateGroupRequestXml(String bucket, List<String> objects, List<String> eTags) 
			throws IllegalArgumentException {
		StringBuffer xml = new StringBuffer();
		int size = objects.size();
		if(size > 1000){
			throw new IllegalArgumentException("The number of parts must be less than 1000 in one file group,Size:"
					+ size);
		}
		xml.append("<CreateFileGroup>");
		for (int i = 0; i < size; i++){
			String eTag = "&quot;" + eTags.get(i).replace("\"", "") + "&quot;";
			xml.append("<Part>");
			xml.append("<PartNumber>" + (i + 1) + "</PartNumber>");
			xml.append("<PartName>" + objects.get(i) + "</PartName>");
			xml.append("<ETag>" + eTag + "</ETag>");
			xml.append("</Part>");
		}
		xml.append("</CreateFileGroup>");
		return xml.toString();
	}
	
	public static Document parseString(String xmlString) {
		Document document = null;
		ByteArrayInputStream bais = null;
		try {
			bais = new ByteArrayInputStream(
					xmlString.getBytes(DEFAULT_XML_CHARSET));
			SAXBuilder saxBuilder = new SAXBuilder();
			document = saxBuilder.build(bais);
		} catch (Exception e) {
			throw new RuntimeException("Unexpected error parsing xml", e);
		} finally {
			if (bais != null) {
				try {bais.close();} catch (Exception e) {}
			}
		}
		return document;
	}

	/**
	 * Create a URL to interact with OSS
	 * 
	 * @param accessId
	 *            Your user key into OSS
	 * @param secretAccessKey
	 *            The secret string used to generate signatures for
	 *            authentication.
	 * @param method
	 *            One of PUT, GET, DELETE, HEAD.
	 * @param resource
	 *            The URI of request
	 * @param timeout
	 *            The number of seconds to wait before the request times out(must be between 0 and 900).
	 */
	public static String generateRequestUrl(String accessKeyId,
			String secretAccessKey, String method, String resource, int timeout) {
		Map<String, String> params = new HashMap<String, String>();
		Map<String, String> headers = new HashMap<String, String>();
		Date now = new Date();
		String date = String.valueOf(now.getTime() / 1000 + timeout);
		headers.put(DATE, date);
		if (resource == null || "".equals(resource)) {
			resource = "/";
		}
		params.put("Expires", date);
		params.put("OSSAccessKeyId", accessKeyId);
		params.put("Signature", Authorization.getAssign(secretAccessKey, method, headers, resource));
		return objectUri(resource) + paramsToString(params);
	}
}
