package io.yunos.bbs;

import io.yunos.bbs.layout.DragListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class Hot extends Fragment implements
		DragListView.OnRefreshLoadingMoreListener {

	private static final String TAG = "Hot";
	private Context mContext;
	private DragListView listView;
	private static final String url = "http://m.bbs.yunos.com";
	private static final String getHotTList = "/thread/getHotTList.do";
	private Handler handler = new Handler();
	private ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	// 将要加载的页数
	private int pageNow = 1;
	// 网络是否可用
	private boolean networkState = true;
	// 是否在加载数据
	private boolean isLoading = false;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		mContext = (Context) getActivity().getApplicationContext();
		listView = (DragListView) getActivity().findViewById(R.id.hot_listview);

		// 获取网络状态
		networkState = isNetworkAvailable(mContext);
		Log.d(TAG, "networkState = " + networkState);

		loadList();

		// TODO should move them to handler?
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				String tidClicked;
				String replyNum;
				// TODO Auto-generated method stub
				Map<String, Object> map = new HashMap<String, Object>();
				map = list.get(position);
				tidClicked = (String) map.get("tid");
				replyNum = (String) map.get("num");
				Log.d(TAG, "onItemClick, arg2=" + position + " ,arg3=" + id
						+ ", tidClicked=" + tidClicked + " , replyNum="
						+ replyNum);

				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("user", (String) map.get("author"));
					jsonObject.put("reply", (String) map.get("num"));
					jsonObject.put("title", (String) map.get("title"));
					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy.MM.dd HH:mm");
					String sd = sdf.format(new Date(Long.parseLong((String) map
							.get("date")) * 1000L));
					jsonObject.put("date", sd);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.i(TAG, "jsonObject = " + jsonObject.toString());
				Intent intent = new Intent(mContext, Post.class);
				Bundle bundle = new Bundle();
				bundle.putString("tid", tidClicked);
				bundle.putString("detailInfo", jsonObject.toString());
				bundle.putString("replyNum", replyNum);
				intent.putExtras(bundle);
				startActivity(intent);
			}

		});
		listView.setOnRefreshListener(this);

		// 注册广播接收器
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mContext.registerReceiver(mReceiver, mFilter);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// 注销广播接收器
		mContext.unregisterReceiver(mReceiver);
	}

	public void loadList() {
		final Runnable r = new Runnable() {

			@Override
			public void run() {
				Log.d(TAG, "runnable.run()");
				// TODO Auto-generated method stub
				SimpleAdapter adapter = new SimpleAdapter(mContext, list,
						R.layout.post_list_item, new String[] { "icon",
								"title", "num", "author" }, new int[] {
								R.id.post_list_item_icon,
								R.id.post_list_item_title,
								R.id.post_list_item_num,
								R.id.post_list_item_author });

				if (pageNow == 1) {
					listView.setAdapter(adapter);
				} else {
					adapter.notifyDataSetChanged();
				}
				pageNow++;
				listView.onLoadMoreComplete(false);
			}

		};

		final Runnable e = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				SimpleAdapter adapter = new SimpleAdapter(mContext, list,
						R.layout.post_list_item, new String[] { "icon",
								"title", "num", "author" }, new int[] {
								R.id.post_list_item_icon,
								R.id.post_list_item_title,
								R.id.post_list_item_num,
								R.id.post_list_item_author });

				if (pageNow == 1) {
					listView.setAdapter(adapter);
				}
				listView.onLoadMoreComplete(true);
			}
		};

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				StringBuffer sb = new StringBuffer();
				Log.d(TAG, "thread(runnable).run()");
				// TODO Auto-generated method stub
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				ArrayList<Map<String, Object>> newList;
				params.add(new BasicNameValuePair("page", pageNow + ""));
				if (HttpUtils.doHttpPost(url + getHotTList, params, sb)
						&& HttpUtils.getResponseCode(sb.toString()) == HttpStatus.SC_OK) {
					Log.d(TAG, "get from doHttpPost: " + sb.toString());
					String[] from = { "tid", "title", "username", "time", "replies" };
					String[] to = { "tid", "title", "author", "date", "num" };
					newList = HttpUtils.parseArray(sb.toString(), from, to);

					Log.d(TAG, "isLoading = false");
					isLoading = false;

					if (newList.size() > 0) {
						for (int i = 0; i < newList.size(); i++) {
							list.add(newList.get(i));
						}
						handler.post(r);
					} else {
						handler.post(e);
					}
				}
			}

		});

		isLoading = true;
		Log.d(TAG, "isLoading = true");
		if (!NetworkUtils.isNetworkAvailable(mContext)) {
			Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_SHORT)
					.show();
		} else {
			thread.start();
		}
	}

	public ArrayList<Map<String, Object>> parseArray(String response) {
		JSONArray jsonArray;
		JSONObject json;
		ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			jsonArray = new JSONObject(response).getJSONArray("data");
			for (int i = 0; i < jsonArray.length(); i++) {
				json = (JSONObject) jsonArray.opt(i);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("tid", json.getString("tid"));
				map.put("title", json.getString("title"));
				map.put("author", json.getString("user"));
				map.put("date", json.getString("time"));
				map.put("num", json.getString("reply"));
				map.put("row", i);
				Log.d(TAG, "title=" + json.getString("title"));
				Log.d(TAG, "tid=" + json.getString("tid"));
				Log.d(TAG, "user=" + json.getString("user"));
				list.add(map);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list;
	}

	@Override
	public android.view.View onCreateView(android.view.LayoutInflater inflater,
			android.view.ViewGroup container,
			android.os.Bundle savedInstanceState) {

		return inflater.inflate(R.layout.hot_layout, container, false);
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		loadList();
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			Log.d(TAG, "Unavailabel");
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						Log.d(TAG, "Availabel");
						return true;
					}
				}
			}
		}
		return false;
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				Log.d(TAG, "网络状态已经改变");
				// 若网络重新联通，且数据加载未完成则重新加载
				if (networkState == false && (isNetworkAvailable(context))
						&& isLoading) {
					Log.d(TAG, "loadList()");
					loadList();
					networkState = true;
				} else if (isNetworkAvailable(mContext) == false) {
					networkState = false;
				}
			}
		}
	};
}
