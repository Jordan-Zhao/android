package io.yunos.bbs;

import io.yunos.bbs.UnYunosLogin.WebAppInterface;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class WebViewActivity extends Activity {

	private WebView webView;
	private Context mContext;
	private TextView title;
	private ImageButton back;
	private Timer timer;
	private Handler mHandler = new Handler();
	// 超时时间
	private long timeout = 20000;
	private final String TAG = "WebViewActivity";
	private String URL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.web_view_layout);

		mContext = (Context) WebViewActivity.this;
		webView = (WebView) findViewById(R.id.web_view_webview);
		title = (TextView) findViewById(R.id.web_view_title);
		back = (ImageButton) findViewById(R.id.web_view_back);

		webView.getSettings().setJavaScriptEnabled(true);
		WebViewClient mWebviewclient = new WebViewClient() {
			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				// TODO Auto-generated method stub
				handler.proceed();
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				Log.i(TAG, "url=" + url);
				URL = url;
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				title.setText(webView.getTitle());
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
		};
		webView.setWebViewClient(mWebviewclient);
		webView.addJavascriptInterface(new WebAppInterface(mContext),
				"wanjigonglue");
		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		URL = bundle.getString("url");

		if (isNetworkAvailable(mContext)) {
			webView.loadUrl(URL + "&" + HttpUtils.getKeyring());
		} else {
			webView.loadUrl("file:///android_asset/error.html");
		}

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (webView.canGoBack()) {
					webView.goBack();
				} else {
					finish();
				}
			}
		});
	}

	public class WebAppInterface {
		Context mContext;

		/** Instantiate the interface and set the context */
		WebAppInterface(Context c) {
			mContext = c;
		}

		// 如果target 大于等于API 17，则需要加上如下注解
		@JavascriptInterface
		public void setLoginTicket(String ticket) {
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("ticket", ticket);
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			finish();
		}

		@JavascriptInterface
		public void refresh() {
			if (isNetworkAvailable(mContext)) {
				webView.loadUrl(URL + "&" + HttpUtils.getKeyring());
			} else {
				webView.loadUrl("file:///android_asset/error.html");
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
			webView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
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
