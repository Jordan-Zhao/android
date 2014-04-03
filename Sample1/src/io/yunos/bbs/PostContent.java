package io.yunos.bbs;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import io.yunos.bbs.PostReply.WebAppInterface;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.Toast;

public class PostContent extends Fragment {

	private Context mContext;
	private static final String TAG = "PostContent";
	private static final String baseUrl = "http://m.bbs.yunos.com/app-inner/detail.html?test=1";
	private String tid;
	private String detailInfo;
	private WebView webView;
	private Timer timer;
	private Handler mHandler = new Handler();
	public Handler handler = new Handler();
	public Runnable replyUpdate;
	// 超时时间
	private long timeout = 20000;
	private String detailUrl;
	
	//是否回复可见贴
	private boolean needReply;
	//是否需要回复验证码
	private boolean needValidate;
	//是否被收藏
	private boolean isFavorite;
	//是否锁定
	private boolean isLocked;
	//是否关闭
	private boolean isClosed;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		mContext = (Context) getActivity().getApplicationContext();
		if (savedInstanceState != null) {
			tid = getArguments().getString("tid");
			Log.d(TAG, "onActivityCreated get tid: " + tid);
		}

		webView = (WebView) this.getActivity().findViewById(
				R.id.post_content_webview);
		webView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// webview里的链接调用系统浏览器打开
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(intent);

				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				timer.cancel();
				timer.purge();
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
				timer = new Timer();
				TimerTask tt = new TimerTask() {
					@Override
					public void run() {
						/*
						 * 超时后,首先判断页面加载进度,超时并且进度小于100,就执行超时后的动作
						 */
						if (webView.getProgress() < 100) {
							Log.d("testTimeout", "timeout...........");
							webView.loadUrl("file:///android_asset/error.html");
							Message msg = new Message();
							msg.what = 1;
							mHandler.sendMessage(msg);
							timer.cancel();
							timer.purge();
						}
					}
				};
				timer.schedule(tt, timeout, 1);
			}
		});
		webView.addJavascriptInterface(new WebAppInterface(mContext), "Android");
		detailUrl = baseUrl + "&tid=" + tid;

		Log.d(TAG, "detailUrl: " + detailUrl);
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		if (isNetworkAvailable(mContext)) {
			webView.loadUrl(detailUrl + "&" + HttpUtils.getKeyring());
		} else {
			webView.loadUrl("file:///android_asset/error.html");
		}

		replyUpdate = new Runnable() {

			@Override
			public void run() {
				Log.d(TAG, "replyUpdate running");
				webView.loadUrl("javascript:BBS.replySuccess()");
			}

		};
	}

	@Override
	public android.view.View onCreateView(android.view.LayoutInflater inflater,
			android.view.ViewGroup container,
			android.os.Bundle savedInstanceState) {

		if (container == null) {
			Log.d(TAG, "ViewGroup is null");
		}

		if (savedInstanceState != null) {
			tid = getArguments().getString("tid");
			Log.d(TAG, "onCreateView get tid: " + tid);
		}

		return inflater.inflate(R.layout.post_content_layout, container, false);
	};

	public void updateWebContent(String args) {

		tid = args;
		Log.d(TAG, "updateWebContent tid=" + args);
	}

	public class WebAppInterface {
		Context mContext;

		/** Instantiate the interface and set the context */
		WebAppInterface(Context c) {
			mContext = c;
		}

		// 如果target 大于等于API 17，则需要加上如下注解
		@JavascriptInterface
		public void setCurrentDetailInfo(String Info) {
			detailInfo = Info;
		}

		@JavascriptInterface
		public String getCurrentDetailInfo() {
			if (detailInfo != null) {
				return detailInfo;
			} else {
				return "";
			}
		}

		@JavascriptInterface
		public void openList(String fid) {
			Intent intent = new Intent(mContext, PlateList.class);
			Bundle bundle = new Bundle();
			if (fid != null) {
				bundle.putString("fid", fid);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		}

		@JavascriptInterface
		public void openDetail(String tid) {
			Log.i(TAG, "tid=" + tid);
			Intent intent = new Intent(mContext, Post.class);
			Bundle bundle = new Bundle();
			if (detailInfo != null) {
				bundle.putString("detailInfo", detailInfo);
			}
			if (tid != null) {
				bundle.putString("tid", tid);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		}

		@JavascriptInterface
		public void openWebView(String url) {
			Intent intent = new Intent(mContext, WebViewActivity.class);
			Bundle bundle = new Bundle();
			if (url != null) {
				bundle.putString("url", url);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		}

		@JavascriptInterface
		public void showToast(String info) {
			Toast.makeText(mContext, info, 1000).show();
		}

		@JavascriptInterface
		public void openComment(String replyto) {
			Intent intent = new Intent(mContext, NewPostOrReply.class);
			Bundle bundle = new Bundle();
			bundle.putBoolean("IS_NEW_POST", false);
			bundle.putString("tid", tid);
			bundle.putString("replyTo", replyto);
			intent.putExtras(bundle);
			startActivity(intent);
		}

		@JavascriptInterface
		public void refresh() {
			if (isNetworkAvailable(mContext)) {
				webView.loadUrl(detailUrl + "&" + HttpUtils.getKeyring());
			} else {
				webView.loadUrl("file:///android_asset/error.html");
			}
		}
		
		//获得帖子相关状态
		@JavascriptInterface
		public void setTStatus(String status){
			try {
				JSONObject jsonObject = new JSONObject(status);
				needReply = (jsonObject.getInt("needReply")!=0)?true:false;
				needValidate = (jsonObject.getInt("needValidate")!=0)?true:false;
				isFavorite = (jsonObject.getInt("isFavorite")!=0)?true:false;
				isLocked = (jsonObject.getInt("isLocked")!=0)?true:false;
				isClosed = (jsonObject.getInt("isClosed")!=0)?true:false;
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			Log.i("NetWorkState", "Unavailabel");
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						Log.i("NetWorkState", "Availabel");
						return true;
					}
				}
			}
		}
		return false;
	}
}
