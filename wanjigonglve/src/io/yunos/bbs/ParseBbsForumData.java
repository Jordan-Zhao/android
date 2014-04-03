package io.yunos.bbs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ParseBbsForumData {
	private static final String BBS_URL = "http://act.yun.taobao.com/go/rgn/yunos/bbs-plate.php";
	private static final String FORUM_URL = "http://m.bbs.yunos.com/forum/getInfoByFids.do";
	private static final String REGEX = "getBoardData\\(\\s*(.*?)\\s*\\)";
	private static final String NO_DISPLAY_FID = "hot";
	private static final String BOARD_LIST_DATA = "boardListData";
	private static final String LIST_ARRAY_KEY = "list";

	public static ArrayList<BbsPlateData> getBbsForumData() {
		String responseData = "";
		StringBuilder fids = new StringBuilder();
		HashMap<String, BbsPlateData> map = new HashMap<String, BbsPlateData>();

		// get title and fid
		DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
		HttpGet request = new HttpGet(BBS_URL);
		HttpResponse httpResponse;
		try {
			httpResponse = defaultHttpClient.execute(request);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				InputStream is = httpResponse.getEntity().getContent();
				responseData = convertInputStreamToString(is);
				parseJsonString(responseData, map, fids);
			}
		} catch (ClientProtocolException e) {
			// TODO: handle exception
		} catch (IOException e) {
			// TODO: handle exception
		}

		// get topics and threads
		if (fids.length() > 0 && fids.charAt(fids.length() - 1) == '_') {
			fids.deleteCharAt(fids.length() - 1);
		}
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("fids", fids.toString()));

		StringBuffer forumResponseData = new StringBuffer();
		boolean flag = HttpUtils.doHttpPost(FORUM_URL, params,
				forumResponseData);
		if (flag) {
			getInfoByFids(forumResponseData.toString(), map);
		}

		// covert hashmap to arraylist
		ArrayList<BbsPlateData> list = new ArrayList<BbsPlateData>(map.values());

		// rank by authority
		Collections.sort(list);

		return list;
	}

	/**
	 * convert InputStream to String
	 * 
	 * @param inStream
	 * @return
	 * @author zhengyi.wzy
	 */
	public static String convertInputStreamToString(InputStream inStream) {
		BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
		StringBuilder sBuilder = new StringBuilder();
		String line;

		try {
			while ((line = br.readLine()) != null) {
				sBuilder.append(line);
			}
		} catch (IOException e) {
			return sBuilder.toString();
		}

		return sBuilder.toString();
	}

	/**
	 * get json data by regex expression
	 * 
	 * @param data
	 */
	public static String getJsonData(String data) {
		Pattern pattern = Pattern.compile(REGEX);
		Matcher matcher = pattern.matcher(data);
		String res = "";
		if (matcher.find()) {
			res = matcher.group(1);
		}
		return res;
	}

	/**
	 * parse BbsPlateData object from json
	 * 
	 * @param data
	 * @param map
	 * @param fids
	 */
	public static void parseJsonString(String data,
			HashMap<String, BbsPlateData> map, StringBuilder fids) {

		String res = getJsonData(data);

		try {
			JSONArray listArray = new JSONObject(res).getJSONObject(
					BOARD_LIST_DATA).getJSONArray(LIST_ARRAY_KEY);
			for (int i = 0; i < listArray.length(); i++) {
				JSONObject listobj = listArray.getJSONObject(i);
				String title = listobj.optString("title");
				String fid = listobj.optString("fid");
				String icon = listobj.optString("icon");
				if (fid.equals(NO_DISPLAY_FID)) {
					// In case of bad title
					continue;
				}
				String image = listobj.getJSONObject("head").getString("img");

				BbsPlateData bbsData = new BbsPlateData();
				bbsData.setTitle(title);
				bbsData.setIcon(icon);
				bbsData.setFid(fid);
				bbsData.setAuthority(i);
				bbsData.setImageurl(image);
				map.put(fid, bbsData);
				fids.append(fid).append("_");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * fill topics and threads
	 * 
	 * @param response
	 * @param map
	 */
	private static void getInfoByFids(String response,
			HashMap<String, BbsPlateData> map) {
		String[] from = new String[] { "fid", "topics", "threads" };
		String[] to = new String[] { "fid", "topics", "threads" };

		ArrayList<Map<String, Object>> list = HttpUtils.parseArray(response,
				from, to);
		for (int i = 0; i < list.size(); i++) {
			String fid = (String) list.get(i).get("fid");
			String topics = (String) list.get(i).get("topics");
			String threads = (String) list.get(i).get("threads");
			BbsPlateData bbsObj = map.get(fid);
			bbsObj.setTopics(topics);
			bbsObj.setThreads(threads);
		}
	}

}
