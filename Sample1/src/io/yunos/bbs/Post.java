package io.yunos.bbs;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Post extends FragmentActivity {
	private static final String TAG = "Post";
	private Context mContext;
	private PostHandler handler;
	public static final int DO_FAVORITE_SUCCESS = 0x0001;
	public static final int DO_FAVORITE_FAILED = 0x0002;

	private List<View> views; // ViewPager内的View对象集合
	private FragmentManager manager; // Activity管理器
	private ViewPager pager; // ViewPager

	public static final int TAB_NUM = 2;
	private static final int GET_REPLY_CONTENT = 0;
	public static final int Request_Code_Login_Favorite = 3026;
	private static final String BASE_URL = "http://m.bbs.yunos.com";
	private static final String ADD_FAVORITE = "/thread/addFavorite.do";

	private RelativeLayout contentHead;
	private RelativeLayout replyHead;
	private TextView contentTitle;
	private TextView replyTitle;

	private Button reply;
	private ImageButton share;
	private ImageButton collect;

	private ImageButton back;

	private String tid;
	private String detailInfo;
	private PostContent postContent;
	private PostReply postReply;

	private SharedPreferences mPreferences;
	private String mToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.post_layout);

		mContext = (Context) Post.this;
		mPreferences = getSharedPreferences(PersonCenter.TOKEN_TAG, MODE_PRIVATE);
		handler = new PostHandler();

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		tid = bundle.getString("tid");
		detailInfo = bundle.getString("detailInfo");
		Log.d(TAG, "get from Intent: tid=" + tid);

		pager = (ViewPager) findViewById(R.id.post_viewpager);
		contentHead = (RelativeLayout) findViewById(R.id.post_content_head);
		replyHead = (RelativeLayout) findViewById(R.id.post_reply_head);
		contentTitle = (TextView) findViewById(R.id.post_content_title);
		replyTitle = (TextView) findViewById(R.id.post_reply_title);

		back = (ImageButton) findViewById(R.id.post_content_back);

		reply = (Button) findViewById(R.id.post_reply_button);
		share = (ImageButton) findViewById(R.id.post_share);
		collect = (ImageButton) findViewById(R.id.post_collect);

		reply.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (checkLoginInfo()) {
					// Login in
					jumpToReply();
				} else {
					// 本地未登陆，跳转到LoginManager进行登陆
					Intent intent = new Intent(mContext, LoginManager.class);
					startActivityForResult(intent,
							PersonCenter.Request_Code_Login);
				}
			}
		});
		share.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
				intent.putExtra(Intent.EXTRA_TEXT,
						"我是Robin，我正在使用Android分享功能为大家分享这条信息");
				startActivity(Intent.createChooser(intent, "分享到"));
			}
		});
		
		collect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (checkLoginInfo()) {
					// Logged in
					addToFavorite();
				} else {
					// 本地未登陆，跳转到LoginManager进行登陆
					Intent intent = new Intent(mContext, LoginManager.class);
					startActivityForResult(intent,
							Request_Code_Login_Favorite);
				}
			}
			
		});

		manager = getSupportFragmentManager();
		views = new ArrayList<View>();

		views.add(manager.findFragmentById(R.id.post_content_fragment)
				.getView());
		views.add(manager.findFragmentById(R.id.post_reply_fragment).getView());

		replyTitle.setText(getString(R.string.reply));
		postContent = (PostContent) manager
				.findFragmentById(R.id.post_content_fragment);
		postReply = (PostReply) manager
				.findFragmentById(R.id.post_reply_fragment);
		if (postContent != null) {
			postContent.updateWebContent(tid);
			postReply.updatePostReply(tid);
			if (detailInfo != null) {
				postReply.updateDetailInfo(detailInfo);
			}
		}

		pager.setAdapter(new PageAdapter());
		pager.setOnPageChangeListener(new PageChangeListener());

		contentHead.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pager.setCurrentItem(0);
			}
		});

		replyHead.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pager.setCurrentItem(1);
			}
		});

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	/**
	 * Check whether user login or not
	 * 
	 * @return
	 */
	private boolean checkLoginInfo() {
		boolean flag = mPreferences.contains(PersonCenter.TOKEN_TAG);

		if (flag)
			mToken = mPreferences.getString(PersonCenter.TOKEN_TAG, "");
		return flag;
	}

	/**
	 * 跳转到回复界面
	 */
	private void jumpToReply() {
		Intent intent = new Intent(mContext, NewPostOrReply.class);
		Bundle bundle = new Bundle();
		bundle.putBoolean("IS_NEW_POST", false);
		bundle.putString("tid", tid);
		bundle.putString("replyTo", "");
		intent.putExtras(bundle);
		startActivityForResult(intent, GET_REPLY_CONTENT);
	}
	
	private class addToFavoriteRunnable implements Runnable {

		private void doAddToFavorite() {
			int status = -1;
			StringBuffer sb = new StringBuffer();
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("tid", tid));
			params.add(new BasicNameValuePair("token", mToken));
			if (HttpUtils.doHttpPost(BASE_URL + ADD_FAVORITE, params, sb)
					&& (status = HttpUtils.getResponseCode(sb.toString())) == HttpStatus.SC_OK) {
				Log.d(TAG, "doAddToFavorite success");
				handler.sendMessage(handler.obtainMessage(DO_FAVORITE_SUCCESS));
			} else if (status != -1) {
				Log.d(TAG, "doAddToFavorite failed");
				handler.sendMessage(handler.obtainMessage(DO_FAVORITE_FAILED));
			}

		}

		@Override
		public void run() {
			doAddToFavorite();
		}

	};
	
	private void addToFavorite() {
		new Thread(new addToFavoriteRunnable()).start();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GET_REPLY_CONTENT && resultCode == RESULT_OK) {
			String response_content = data.getExtras().getString(
					"response_content");
			Log.d(TAG, "Post onActivityResult get response_content: "
					+ response_content);

			postReply.updatePostReply(tid);
			postReply.setReplyContent(response_content);
			postContent.handler.post(postContent.replyUpdate);
			postReply.handler.post(postReply.replyUpdate);
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
				jumpToReply();
			}
		} else if (requestCode == Request_Code_Login_Favorite
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
				mToken = token;
				addToFavorite();
			}
		}
	}
	
	private class PostHandler extends Handler {
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch(msg.what) {
			case DO_FAVORITE_SUCCESS:
				Toast.makeText(mContext, getString(R.string.favorite_success), Toast.LENGTH_SHORT).show();
				collect.setImageResource(R.drawable.collected);
				break;
			case DO_FAVORITE_FAILED:
				Toast.makeText(mContext, getString(R.string.favorite_fail), Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
		
	}

	private class PageAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object arg2) {
			container.removeView(views.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(views.get(position));
			return views.get(position);
		}
	}

	/**
	 * ViewPager切换监听器
	 * 
	 * @author Administrator
	 * 
	 */
	private class PageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int arg0) {
			switch (arg0) {
			case 0:
				contentHead.setBackgroundResource(R.drawable.post_content_tab);
				contentTitle.setTextColor(getResources()
						.getColor(R.color.green));
				replyHead.setBackgroundColor(getResources().getColor(
						R.color.white));
				replyTitle.setTextColor(getResources().getColor(
						R.color.header_text));
				break;
			case 1:
				replyHead.setBackgroundResource(R.drawable.post_content_tab);
				replyTitle.setTextColor(getResources().getColor(R.color.green));
				contentHead.setBackgroundColor(getResources().getColor(
						R.color.white));
				contentTitle.setTextColor(getResources().getColor(
						R.color.header_text));
				break;
			default:
				break;
			}
		}
	}

}
