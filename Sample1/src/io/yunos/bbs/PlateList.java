package io.yunos.bbs;

import io.yunos.bbs.layout.DragListView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class PlateList extends Activity implements
		DragListView.OnRefreshLoadingMoreListener {

	private static final String TAG = "PlateList";
	private static final String url = "http://m.bbs.yunos.com";
	private static final String getThreadsByFid = "/forum/getThreadsByFid.do";
	private static final String getInfoByFid = "/forum/getInfoByFid.do";
	private static final int GET_NEW_CONTENT = 0;
	private Context mContext;
	private DragListView listView;
	private View headView;
	private ImageView headImage;
	private TextView headText;
	private ImageButton btn_back;
	private RelativeLayout headview_rlayout;
	private TextView plate_list_title;
	private ImageView headview_image;
	private TextView headview_title;
	private TextView headview_today_num;
	private TextView headview_title_num;
	private TextView headview_post_num;
	private Button post_new_button;

	// Handle message for asynchronous task
	private static final int UPDATE_HEADVIEW_UI = 0x0001;
	private Handler handler = new Handler();
	private ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

	private String fid;
	private String imageUrl;
	private String title;
	private PlateInfo plateInfo;
	private ImageLoader imageLoader;
	private PlateListHandler pHandler;
	// 将要加载的页数
	private int pageNow = 1;

	// 登陆认证
	private SharedPreferences mPreferences;

	public PlateInfo getPlateInfo() {
		return plateInfo;
	}

	public void setPlateInfo(PlateInfo plateInfo) {
		this.plateInfo = plateInfo;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.plate_list_layout);

		mContext = (Context) PlateList.this;
		mPreferences = getSharedPreferences(PersonCenter.TOKEN_TAG,
				Context.MODE_PRIVATE);

		listView = (DragListView) findViewById(R.id.plate_list_listview);
		btn_back = (ImageButton) findViewById(R.id.plate_list_back);

		plate_list_title = (TextView) findViewById(R.id.plate_list_title);
		
		headView = LayoutInflater.from(mContext).inflate(
				R.layout.plate_list_headview, null);
		listView.addHeaderView(headView);

		headview_rlayout = (RelativeLayout) headView
				.findViewById(R.id.plate_list_headview_relativelayout);
		headview_image = (ImageView) headView
				.findViewById(R.id.plate_list_headview_image);
		headview_title = (TextView) headView
				.findViewById(R.id.plate_list_headview_text);
		headview_today_num = (TextView) headView
				.findViewById(R.id.plate_list_headview_today_num);
		headview_title_num = (TextView) headView
				.findViewById(R.id.plate_list_headview_title_num);
		headview_post_num = (TextView) headView
				.findViewById(R.id.plate_list_headview_post_num);

		post_new_button = (Button) findViewById(R.id.post_new_button);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		fid = bundle.getString("fid");
		imageUrl = bundle.getString("url");
		title = bundle.getString("title");
		
		// 设置版头名称
		plate_list_title.setText(title);
		
		// TODO: update head view UI
		pHandler = new PlateListHandler(this);
		startThreadToUpdateHeadView();

		loadList();

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String tidClicked;
				Map<String, Object> map = new HashMap<String, Object>();
				map = list.get(position - 1);
				tidClicked = (String) map.get("tid");
				Log.d(TAG, "onItemClick, arg2=" + position + " ,arg3=" + id
						+ ", tidClicked=" + tidClicked);

				Intent intent = new Intent(mContext, Post.class);
				Bundle bundle = new Bundle();
				bundle.putString("tid", tidClicked);
				intent.putExtras(bundle);
				startActivity(intent);

			}
		});

		listView.setOnRefreshListener(this);

		btn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		// 登陆后可以发帖，没登陆跳转到个人中心界面
		post_new_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (checkLoginInfo()) {
					// Login in
					Log.e(PersonCenter.TAG, "已经登陆");
					jumpToPost();
				} else {
					Log.e(PersonCenter.TAG, "未登陆");
					// 本地未登陆，跳转到LoginManager进行登陆
					Intent intent = new Intent(mContext, LoginManager.class);
					startActivityForResult(intent,
							PersonCenter.Request_Code_Login);
				}
			}
		});
	}

	public void loadList() {

		final Runnable r = new Runnable() {

			@Override
			public void run() {
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
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				ArrayList<Map<String, Object>> newList;
				params.add(new BasicNameValuePair("fid", fid));
				params.add(new BasicNameValuePair("page", pageNow + ""));
				Log.e(PersonCenter.TAG, "ok 1");
				if (HttpUtils.doHttpPost(url + getThreadsByFid, params, sb)
						&& HttpUtils.getResponseCode(sb.toString()) == HttpStatus.SC_OK) {
					Log.d(PersonCenter.TAG,
							"get from doHttpPost: " + sb.toString());
					String[] from = { "tid", "title", "username", "replies",
							"isDigest" };
					String[] to = { "tid", "title", "author", "num", "isDigest" };
					newList = HttpUtils.parseArray(sb.toString(), from, to);
					if (newList.size() > 0) {
						for (int i = 0; i < newList.size(); i++) {
							list.add(newList.get(i));
						}
						handler.post(r);
					} else {
						handler.post(e);
					}
				} else {
					Log.e(PersonCenter.TAG, sb.toString());
				}
			}

		});

		if (!NetworkUtils.isNetworkAvailable(mContext)) {
			Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		thread.start();
	}

	/**
	 * Check whether user login or not
	 * 
	 * @return
	 */
	private boolean checkLoginInfo() {
		boolean flag = mPreferences.contains(PersonCenter.TOKEN_TAG);

		return flag;
	}

	/**
	 * 跳转到发帖或者回复界面
	 */
	private void jumpToPost() {
		Intent intent = new Intent(mContext, NewPostOrReply.class);
		Bundle bundle = new Bundle();
		bundle.putBoolean("IS_NEW_POST", true);
		bundle.putString("fid", fid);
		intent.putExtras(bundle);
		startActivityForResult(intent, GET_NEW_CONTENT);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GET_NEW_CONTENT && resultCode == RESULT_OK) {
			String response_content = data.getExtras().getString(
					"response_content");
			String tid = null;
			try {
				String responseCode = new JSONObject(response_content).getString("code");
				if (responseCode.equals("200")) {
					JSONObject responseData = new JSONObject(response_content)
							.getJSONObject("data");
					tid = responseData.getString("tid");
				}
			} catch (JSONException e) {
				Log.d(TAG, "failed to parse NEW_POST response json: " + e);
			}

			if (tid != null) {
				Log.d(TAG, "new post OK, now load it, tid=" + tid);
				Intent intent = new Intent(mContext, Post.class);
				Bundle bundle = new Bundle();
				bundle.putString("tid", tid);
				bundle.putString("detailInfo", "");
				intent.putExtras(bundle);
				startActivity(intent);
			}
		} else if (requestCode == PersonCenter.Request_Code_Login
				&& resultCode == Activity.RESULT_OK) {
			// 集团登陆回调
			Bundle bundle = data.getExtras();
			String token = bundle.getString("token");
			Log.e(PersonCenter.TAG, "token:" + token);
			// 存储token
			if (token != null && !token.equals("")) {
				SharedPreferences.Editor editor = mPreferences.edit();
				editor.putString(PersonCenter.TOKEN_TAG, token);
				editor.commit();

				// 跳转到发帖界面
				jumpToPost();
			}
		}
	}

	public void startThreadToUpdateHeadView() {
		// Update text view
		new Thread(new UpdateHeadViewRunnable()).start();

		// Update head picture
		if (imageUrl != null && !imageUrl.equals("")) {
			imageLoader = new ImageLoader(mContext);
			imageLoader.disPlayImage(imageUrl, headview_image);
		}
	}

	public void changeHeadView() {
		Log.e(PersonCenter.TAG, "Change Textview UI");
		if (plateInfo != null) {
			// Change text view
			headview_title.setText(plateInfo.getTitle());
			headview_today_num.setText(plateInfo.getToday());
			headview_title_num.setText(plateInfo.getTopics());
			headview_post_num.setText(plateInfo.getThreads());

			String msg = plateInfo.getTitle() + ":" + plateInfo.getToday()
					+ ":" + plateInfo.getTopics() + ":"
					+ plateInfo.getThreads();
			Log.e(PersonCenter.TAG, msg);
		}
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		loadList();
	}

	class UpdateHeadViewRunnable implements Runnable {
		private PlateInfo getPlateInfo() {
			String headviewurl = url + getInfoByFid;
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("fid", fid));
			StringBuffer sb = new StringBuffer();

			HttpUtils.doHttpPost(headviewurl, params, sb);

			PlateInfo info = new PlateInfo();

			try {
				String code = new JSONObject(sb.toString()).getString("code");
				if (code.equals("200")) {
					JSONObject data = new JSONObject(sb.toString())
							.getJSONObject("data");
					String today = data.getString("today");
					String topics = data.getString("topics");
					String threads = data.getString("threads");
					String title = data.getString("title");

					// 更新对象属性
					info.setToday(today);
					info.setTopics(topics);
					info.setThreads(threads);
					info.setTitle(title);
				}
			} catch (JSONException e) {
				// Do nothing
			}

			return info;
		}

		@Override
		public void run() {
			if (fid != null && !fid.equals("")) {
				PlateInfo pInfo = getPlateInfo();
				pHandler.sendMessage(handler.obtainMessage(UPDATE_HEADVIEW_UI,
						pInfo));
			}
		}
	}

	/**
	 * Define Class for HeadView info
	 * 
	 * @author zhengyi.wzy
	 */
	private class PlateInfo {
		private String title;
		private String today;
		private String topics;
		private String threads;

		public PlateInfo() {
			super();
			this.title = "";
			this.today = this.topics = this.threads = "0";
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getToday() {
			return today;
		}

		public void setToday(String today) {
			this.today = today;
		}

		public String getTopics() {
			return topics;
		}

		public void setTopics(String topics) {
			this.topics = topics;
		}

		public String getThreads() {
			return threads;
		}

		public void setThreads(String threads) {
			this.threads = threads;
		}
	}

	private static class PlateListHandler extends Handler {
		private final WeakReference<PlateList> plateReference;

		public PlateListHandler(PlateList plateList) {
			this.plateReference = new WeakReference<PlateList>(plateList);
		}

		@Override
		public void handleMessage(Message msg) {
			PlateList outclass = plateReference.get();

			switch (msg.what) {
			case UPDATE_HEADVIEW_UI:
				PlateInfo pinfo = (PlateInfo) msg.obj;
				outclass.setPlateInfo(pinfo);
				outclass.changeHeadView();
				break;
			}
		}
	}
}
