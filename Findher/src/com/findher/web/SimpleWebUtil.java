package com.findher.web;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class SimpleWebUtil {
	private static final String LOG_TAG = "SimpleWebUtil";

	/**
	 * 网络请求。
	 * 
	 * @param requestUrl
	 * @param method
	 * @return
	 */
	// public static String request(String requestUrl, String method) {
	// InputStream is = null;
	// try {
	// URL url = new URL(requestUrl);
	// HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	// conn.setReadTimeout(10000); // milliseconds
	// conn.setConnectTimeout(15000); // milliseconds
	// conn.setRequestMethod(method);
	// conn.setDoInput(true);
	// conn.connect();
	// int response = conn.getResponseCode();
	// Log.d(LOG_TAG, "response code:" + response);
	// is = conn.getInputStream();
	// // 将InputStream转化为string
	// char[] buffer = new char[conn.getContentLength()];
	// BufferedReader reader = new BufferedReader(new InputStreamReader(is,
	// "UTF-8"));
	// reader.read(buffer);
	// return new String(buffer);
	// } catch (Exception e) {
	// Log.e(LOG_TAG, "web request error!", e);
	// } finally {
	// if (is != null) {
	// try {
	// is.close();
	// } catch (Exception e2) {
	// Log.e(LOG_TAG, "close inputstream error.!", e2);
	// }
	// }
	// }
	// return null;
	// }

	public static String request(String requestUrl, String method) {
		InputStream is = null;
		try {
			// HttpGet连接对象
			HttpGet httpRequest = new HttpGet(requestUrl);
			// 取得HttpClient对象
			HttpClient httpclient = new DefaultHttpClient();
			// 请求HttpClient，取得HttpResponse
			HttpResponse httpResponse = httpclient.execute(httpRequest);
			// 请求成功
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
			{
				// 取得返回的字符串
				String strResult = EntityUtils.toString(httpResponse.getEntity());
				return strResult;
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "web request error!", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e2) {
					Log.e(LOG_TAG, "close inputstream error.!", e2);
				}
			}
		}
		return null;
	}
}
