package io.yunos.bbs;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class Picked extends Fragment {

	private static final String TAG = "Picked";
	private Context mContext;
	private WebView webView;
	private String detailInfo;
	private Timer timer;
	private Handler mHandler = new Handler();
	// 超时时间
	private long timeout = 20000;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		mContext = getActivity().getApplicationContext();
		webView = (WebView) getActivity().findViewById(R.id.picked_webview);
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

		webView.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				Log.i(TAG, "url=" + url);
				view.loadUrl(url);
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
		if (isNetworkAvailable(mContext)) {
			webView.loadUrl("http://m.bbs.yunos.com/app-inner/recomment.html"
					+ "?" + HttpUtils.getKeyring());
		} else {
			webView.loadUrl("file:///android_asset/error.html");
		}
	}

	@Override
	public android.view.View onCreateView(android.view.LayoutInflater inflater,
			android.view.ViewGroup container,
			android.os.Bundle savedInstanceState) {

		return inflater.inflate(R.layout.picked_layout, container, false);
	};

	/**
	 * 自定义的Android代码和JavaScript代码之间的桥梁类
	 * 
	 * @author 1
	 * 
	 */
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
		public void refresh() {
			if (isNetworkAvailable(mContext)) {
				webView.loadUrl("http://m.bbs.yunos.com/app-inner/recomment.html"
						+ "?" + HttpUtils.getKeyring());
			} else {
				webView.loadUrl("file:///android_asset/error.html");
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
