package io.yunos.bbs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class HttpUtils {
	private static final String TAG = "HttpUtils";
	private static final boolean debug = true;
	private static final String key = "B_b@s#1.1.0";

	/**
	 * Send http post request, save the response to StringBuffer sb.
	 *
	 * @param baseUrl
	 *            The url to request.
	 * @param params
	 *            the http params
	 * @param sb
	 *            save the http response. The caller should initialize it before use.
	 * @return true if http post request succeeds
	 */
	public static boolean doHttpPost(String baseUrl, ArrayList<NameValuePair> params, StringBuffer sb) {
		HttpPost httpPost = new HttpPost(baseUrl);
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse httpResponse = null;
        HttpEntity entity = null;
        BufferedReader reader = null;
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        params.add(new BasicNameValuePair("_r", md5(md5(timestamp + key))));
        params.add(new BasicNameValuePair("_t", timestamp));

        if (debug)
        	Log.d(TAG, "request URL:" + baseUrl + ", params:" + params.toString());
        try {
        	entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
        	httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost);
        } catch (UnsupportedEncodingException e) {
        	Log.d(TAG, "http request error" + e);
        	return false;
        } catch (ClientProtocolException e) {
        	Log.d(TAG, "http request error" + e);
           	return false;
        } catch (IOException e) {
        	Log.d(TAG, "http request error" + e);
           	return false;
        }

        if (httpResponse != null) {
            StatusLine statusLine = httpResponse.getStatusLine();
            if (statusLine != null && statusLine.getStatusCode() == HttpStatus.SC_OK) {
		        try {
					reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
					String line = null;
					if (sb != null)
						while ((line = reader.readLine()) != null)
							sb.append(line);
					if (debug)
						Log.d(TAG, "HttpUtils doHttpPost(" + baseUrl + ") response: " + sb.toString());
					return true;
				} catch (ParseException e) {
					Log.d(TAG, "http request error" + e);
			       	return false;
				} catch (IOException e) {
					Log.d(TAG, "http request error" + e);
			       	return false;
				}
            }
        }
        Log.d(TAG, "doHttpPost error");
        return false;
	}

	/**
	 * Parse JSONArray response to ArrayList
	 *
	 * @param response
	 *            The JSONArray response to parse.
	 * @param from
	 *            the keys to be parsed
	 * @param to
	 *            the keys to be stored
	 * @return ArrayList which contains all the key-values
	 */
	public static ArrayList<Map<String, Object>> parseArray(
			String response, String[] from, String[] to) {
		JSONArray jsonArray;
		JSONObject json;
		ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			jsonArray = new JSONObject(response).getJSONArray("data");
			for ( int i = 0; i < jsonArray.length(); i++) {
				json = (JSONObject)jsonArray.opt(i);				
				Map<String, Object> map = new HashMap<String, Object>();
				for (int j = 0; j < from.length; j++) {
					map.put(to[j], json.getString(from[j]));
				}
				list.add(map);
			}
		} catch (JSONException e) {
			Log.e(TAG, "failed to parse Json array: " + e);
			e.printStackTrace();
		}
		return list;
	}

	public static int getResponseCode(String response) {
		int code = 0;
		try {
			code = new JSONObject(response).getInt("code");
		} catch (JSONException e) {
			Log.e(TAG, "error to parse http response code" + e);
		}
		return code;
	}

	public static String getKeyring() {
		StringBuffer sb = new StringBuffer();
	String timestamp = String.valueOf(System.currentTimeMillis());
	
	sb.append("_t=").append(timestamp);
	sb.append("&_r=").append(md5(md5(timestamp + key)));
	if (debug)
		Log.d(TAG, "keyring: " + sb.toString());
	return sb.toString();
	}

	private static String md5(String s) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(s.getBytes());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte byteArray[] = messageDigest.digest();
		StringBuilder sStringBuilder = new StringBuilder();
		sStringBuilder.setLength(0);
		for (int i = 0; i < byteArray.length; i++) {
			final int b = byteArray[i] & 255;
			if (b < 16) {
				sStringBuilder.append('0');
			}
			sStringBuilder.append(Integer.toHexString(b));
		}

		return sStringBuilder.toString();
	}
}