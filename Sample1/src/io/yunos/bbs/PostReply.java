package io.yunos.bbs;

import java.util.Timer;
import java.util.TimerTask;

import io.yunos.bbs.Picked.WebAppInterface;
import android.annotation.SuppressLint;
import android.app.Activity;
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

public class PostReply extends Fragment {

	private Context mContext;
	private static final String TAG = "PostReply";
	private static final String baseUrl = "http://m.bbs.yunos.com/app-inner/comment.html?test=1";
	public Handler handler = new Handler();
	public Runnable r;
	public Runnable replyUpdate;
	private WebView webView;
	private String tid;
	private String detailInfo;
	private String replyUrl;
	private String replyContent;
	private Timer timer;
	private Handler mHandler = new Handler();
	// 超时时间
	private long timeout = 20000;

	private static final int GET_REPLY_CONTENT = 0;

	@SuppressLint("SetJavaScriptEnabled")
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
				R.id.post_reply_webview);

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
		replyUrl = baseUrl + "&tid=" + tid;

		Log.d(TAG, "replyUrl: " + replyUrl);
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		if (isNetworkAvailable(mContext)) {
			webView.loadUrl(replyUrl + "&" + HttpUtils.getKeyring());
		} else {
			webView.loadUrl("file:///android_asset/error.html");
		}

		r = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				webView.loadUrl(replyUrl + "&" + HttpUtils.getKeyring());
			}

		};

		replyUpdate = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Log.d(TAG, "replyUpdate running");
				webView.loadUrl("javascript:BBS.replySuccess('" + replyContent
						+ "')");
			}

		};

	}

	public class WebAppInterface {
		Context mContext;

		/** Instantiate the interface and set the context */
		WebAppInterface(Context c) {
			mContext = c;
		}

		/** Show a toast from the web page */
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
			startActivityForResult(intent, GET_REPLY_CONTENT);
		}

		@JavascriptInterface
		public void refresh() {
			if (isNetworkAvailable(mContext)) {
				webView.loadUrl(replyUrl + "&" + HttpUtils.getKeyring());
			} else {
				webView.loadUrl("file:///android_asset/error.html");
			}
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GET_REPLY_CONTENT
				&& resultCode == Activity.RESULT_OK) {
			String reply_content = data.getExtras().getString("reply_content");
			Log.d(TAG, "Post onActivityResult get reply_content: "
					+ reply_content);
			updatePostReply(tid);
		}
	}

	@Override
	public android.view.View onCreateView(android.view.LayoutInflater inflater,
			android.view.ViewGroup container,
			android.os.Bundle savedInstanceState) {

		return inflater.inflate(R.layout.post_reply_layout, container, false);
	};

	public void updatePostReply(String args) {
		Log.d(TAG, "updatePostReply tid=" + args);
		tid = args;
	}

	public void updateDetailInfo(String info) {
		Log.i(TAG, "updateDetailInfo info=" + info);
		detailInfo = info;
	}

	public void setReplyContent(String response) {
		replyContent = response;
		Log.d(TAG, replyContent);
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
