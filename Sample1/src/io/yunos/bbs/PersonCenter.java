package io.yunos.bbs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.yunos.sdk.account.AccessToken;
import com.yunos.sdk.account.AccessTokenManager;

public class PersonCenter extends Fragment {

	public static final String TAG = "PersonCenter";
	private Context mContext;
	private Button login;
	// token类，可以拿到本地储存的token
	private AccessToken mToken;
	public static final int Request_Code_Carema = 3023;
	public static final int Request_Code_Album = 3021;
	public static final int Request_Code_Login = 3025;

	private String token;

	private ExpandableListView expandableList;

	// upload head picture
	private OSSUtils ossUtils = new OSSUtils();
	private HeadPicData picData = new HeadPicData();
	private Bitmap overrideHeadPicBitmap;
	private static final int TIME_OUT = 30000;

	// Screen Widget
	private ImageButton headPic;
	private TextView personUsername;
	private TextView goldNumber;
	private TextView coinNumber;
	private TextView weiwangNumber;
	private ExAdapter adapter;

	// Http request url
	private static final String BASE_URL = "http://m.bbs.yunos.com";
	private static final String GET_MEMBERINFO = "/member/getMemberInfo.do";
	private static final String GET_THREADS = "/member/getMemberThreads.do";
	private static final String GET_FAVORITES = "/member/getMemberFavorites.do";
	private static final String IS_LOGIN = "/member/register.do";
	private static final String UPDATE_USER_ICON = "/member/updateUserIcon.do";

	// Main UI data object
	private PersonCenterData personCenterData = null;
	private static final String THREAD_NAME = "帖子";
	private static final String FAVORITES_NAME = "收藏";

	// Handler to handle message
	private PersonCenterHandler handler;
	public static final int LOGIN_UPDATE_UI = 0x0001;
	public static final int GET_PERSON_DATA = 0x0004;
	public static final int LOGOUT_UPDATE_UI = 0x0002;
	public static final int UPLOAD_HEAD_PIC = 0x0010;
	public static final int UPDATE_HEAD_PIC = 0x0020;

	// ImageLoader to display picture
	ImageLoader imageLoader;

	// Keep login state
	public static final String TOKEN_TAG = "token";
	private SharedPreferences mPreferences;

	public PersonCenterData getPersonCenterData() {
		return personCenterData;
	}

	public void setPersonCenterData(PersonCenterData personCenterData) {
		this.personCenterData = personCenterData;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mContext = getActivity().getApplicationContext();

		mPreferences = mContext.getSharedPreferences(TOKEN_TAG,
				Context.MODE_PRIVATE);

		// Handler to update UI
		handler = new PersonCenterHandler(this);

		// Initialize Image Loader
		imageLoader = new ImageLoader(mContext);

		personCenterData = new PersonCenterData();
		
		// Login in and login out button
		login = (Button) getActivity().findViewById(R.id.person_center_login);
		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Button button = (Button) v;
				String text = button.getText().toString();

				if (text.equals(getResources().getString(R.string.login))) {
					tryLogin();
				} else {
					tryLogout();
				}
			}
		});

		expandableList = (ExpandableListView) getActivity().findViewById(
				R.id.expandableListView);

		// Head Widget
		View headView = LayoutInflater.from(mContext).inflate(
				R.layout.person_center_headview, null);
		expandableList.addHeaderView(headView);

		// Set Default ExpandableListView
		expandableList.setGroupIndicator(null);
		List<GroupPersonPosts> group = createDefaultExpandlistData();
		adapter = new ExAdapter(mContext, group);
		expandableList.setAdapter(adapter);

		personUsername = (TextView) headView
				.findViewById(R.id.person_center_username);
		goldNumber = (TextView) headView.findViewById(R.id.yuanbao_num);
		coinNumber = (TextView) headView.findViewById(R.id.tongbi_num);
		weiwangNumber = (TextView) headView.findViewById(R.id.weiwang_num);
		headPic = (ImageButton) headView.findViewById(R.id.person_center_head);

		headPic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (checkLoginInfo())
					howToGetPic();
			}
		});

		expandableList.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Intent intent = new Intent(mContext, Post.class);
				Bundle bundle = new Bundle();
				bundle.putString("tid",
						personCenterData.getGroupPosts().get(groupPosition)
								.getChild(childPosition).getTid());
				intent.putExtras(bundle);
				startActivity(intent);
				return true;
			}
		});

		expandableList.setOnGroupExpandListener(new OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {
				// TODO Auto-generated method stub
				for (int i = 0; i < adapter.getGroupCount(); i++) {
					if (groupPosition != i) {
						expandableList.collapseGroup(i);
					}
				}
			}
		});
	}

	@Override
	public android.view.View onCreateView(android.view.LayoutInflater inflater,
			android.view.ViewGroup container,
			android.os.Bundle savedInstanceState) {

		return inflater
				.inflate(R.layout.person_center_layout, container, false);
	};

	@Override
	public void onStart() {
		super.onStart();

		if (checkLoginInfo()) {
			// Already login in
			new Thread(new IsLoginRunnable()).start();
		} else {
			// Set UI to login out
			Log.e(PersonCenter.TAG, "meidenglu");
			setLogoutUI();
		}
		
	}

	/**
	 * 构造ExpandableListView默认数据
	 */
	private List<GroupPersonPosts> createDefaultExpandlistData() {
		List<GroupPersonPosts> group = new ArrayList<GroupPersonPosts>();
		// 构造帖子
		GroupPersonPosts thread = new GroupPersonPosts();
		thread.setGroupName(THREAD_NAME);
		thread.setNumber("0");
		thread.setGroupChild(new ArrayList<ForumPost>());

		group.add(thread);

		// 构造收藏
		GroupPersonPosts favorites = new GroupPersonPosts();
		favorites.setGroupName(FAVORITES_NAME);
		favorites.setNumber("0");
		favorites.setGroupChild(new ArrayList<ForumPost>());

		group.add(favorites);

		return group;
	}

	/**
	 * Check whether the token has stored already or not
	 * 
	 * @return boolean flag
	 */
	private final boolean checkLoginInfo() {
		boolean flag = mPreferences.contains(TOKEN_TAG);

		if (flag) {
			token = mPreferences.getString(TOKEN_TAG, "");
		}

		return flag;
	}

	/**
	 * Click login button to login in
	 */
	public void tryLogin() {
		// 检测本地有没有token
		mToken = AccessTokenManager.readAccessToken(mContext);
		if (mToken != null && !TextUtils.isEmpty(mToken.getAccessToken())) {
			token = mToken.getAccessToken();
			// Store token in SharedPreferences
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putString(TOKEN_TAG, token);
			editor.commit();

			// Judge user login state
			new Thread(new IsLoginRunnable()).start();
		} else {
			// 本地没有token，跳到LoginManager进行登陆
			Intent intent = new Intent(mContext, LoginManager.class);
			startActivityForResult(intent, Request_Code_Login);
		}
	}

	/**
	 * 清理数据后登陆，供回调使用
	 */
	public void afterCleanCacheLogin() {
		// Store token in SharedPreferences
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString(TOKEN_TAG, token);
		editor.commit();

		// Judge user login state
		new Thread(new IsLoginRunnable()).start();
	}

	/**
	 * Click logout button to login out
	 */
	public void tryLogout() {
		if (checkLoginInfo()) {
			// Delete token in SharedPreferences
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.remove(TOKEN_TAG);
			editor.commit();

			// Delete token from token manager
			AccessTokenManager.removeAccessToken(mContext);

			// In order to change UI to send message to handler
			handler.sendMessage(handler.obtainMessage(LOGOUT_UPDATE_UI));
		}
	}

	/**
	 * Change UI to login in
	 */
	public void setLoginUI() {
		if (personCenterData != null) {
			// Change Head picture
			String url = personCenterData.getImageurl();
			if (url != null && !url.equals("")) {
				// TODO:There is image button, not image view
			}

			Bitmap bitmap = personCenterData.getHeadPicBitmap();
			if (overrideHeadPicBitmap != null) {
				Log.d(TAG, "use overrideHeadPicBitmap to headPic");
				headPic.setImageBitmap(toRoundBitmap(overrideHeadPicBitmap));
				overrideHeadPicBitmap = null;
			} else if (bitmap != null) {
				headPic.setImageBitmap(toRoundBitmap(bitmap));
			}

			// Change user name
			String username = personCenterData.getUsername();
			if (username != null && !username.equals("")) {
				personUsername.setText(username);
			}

			// Change gold number
			String gold = personCenterData.getGold();
			if (gold != null && !gold.equals("")) {
				goldNumber.setText(gold);
			}

			// Change coin number
			String coin = personCenterData.getCoin();
			if (coin != null && !coin.equals("")) {
				coinNumber.setText(coin);
			}

			// Change prestige number
			String weiwang = personCenterData.getWeiwang();
			if (weiwang != null && !weiwang.equals("")) {
				weiwangNumber.setText(weiwang);
			}

			// Update expandable list
			List<GroupPersonPosts> networkData = personCenterData
					.getGroupPosts();
			adapter.refresh(networkData);

			// Set login in to login out
			login.setText(getResources().getString(R.string.logout));
		} else {
			Log.e(PersonCenter.TAG, "Person data object is null!");
		}
	}

	/**
	 * Change UI to login out
	 */
	public void setLogoutUI() {
		// Set head default picture
		headPic.setImageResource(R.drawable.head_empty);

		// Set user default name
		personUsername.setText(getResources().getString(R.string.unsigned));

		// Set default gold && coin && prestige number
		goldNumber.setText("0");
		coinNumber.setText("0");
		weiwangNumber.setText("0");

		// Set default expandable list
		List<GroupPersonPosts> group = createDefaultExpandlistData();
		adapter.refresh(group);

		// Set the button text to logout
		login.setText(getResources().getString(R.string.login));
	}

	/**
	 * Handler to handle asynchronous request
	 * 
	 * @author zhengyi.wzy
	 */
	private static class PersonCenterHandler extends Handler {
		private final WeakReference<PersonCenter> personCenter;

		public PersonCenterHandler(PersonCenter personCenter) {
			super();
			this.personCenter = new WeakReference<PersonCenter>(personCenter);
		}

		@Override
		public void handleMessage(Message msg) {
			PersonCenter outclass = personCenter.get();
			switch (msg.what) {
			case LOGIN_UPDATE_UI:
				outclass.setLoginUI();
				break;
			case LOGOUT_UPDATE_UI:
				outclass.setLogoutUI();
				break;
			case GET_PERSON_DATA:
				new Thread(outclass.new GetInfoRunnable()).start();
				break;
			case UPLOAD_HEAD_PIC:
				new Thread(outclass.new uploadHeadPic()).start();
				break;
			case UPDATE_HEAD_PIC:
				new Thread(outclass.new updateUserIcon()).start();
				break;
			default:
				break;
			}
		}

	}

	/**
	 * Sign up
	 * 
	 * @author zhengyi.wzy
	 */
	class IsLoginRunnable implements Runnable {

		private String isUserLogin() {
			String requestLoginUrl = BASE_URL + IS_LOGIN;
			StringBuffer sb = new StringBuffer();
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("token", token));
			HttpUtils.doHttpPost(requestLoginUrl, params, sb);
			String code = parseJsonToCode(sb.toString());

			return code;
		}

		private String parseJsonToCode(String sb) {
			String code = "";
			try {
				JSONObject obj = new JSONObject(sb);
				code = obj.getString("code");
				return code;
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return code;
		}

		@Override
		public void run() {
			String code = isUserLogin();
			if (code.equals("302")) {
				// User already login in, get personal center data
				Log.e(TAG, "需要获取用户信息了");
				handler.sendMessage(handler.obtainMessage(GET_PERSON_DATA));
			} else if (code.equals("200")) {
				// Sign up success, get personal center data
				Log.e(TAG, "注册成功，获取用户信息");
				handler.sendMessage(handler.obtainMessage(GET_PERSON_DATA));
			} else {
				// TODO:Another illegal info
			}
		}
	}

	/**
	 * Get Person Center Data
	 * 
	 * @author zhengyi.wzy
	 */
	class GetInfoRunnable implements Runnable {
		/**
		 * 根据token获取登陆信息
		 * 
		 * @return
		 */
		private void getLoginInfo() {
			String requestInfoUrl = BASE_URL + GET_MEMBERINFO;
			StringBuffer sb = new StringBuffer();
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("token", token));
			HttpUtils.doHttpPost(requestInfoUrl, params, sb);

			parseJson(sb.toString(), personCenterData);
			parseHeadPicBitmap(personCenterData);

			// TODO:test
			Log.e(TAG,
					personCenterData.getCoin() + ":" + personCenterData.getFavorites()
							+ ":" + personCenterData.getGold() + ":"
							+ personCenterData.getGroup() + ":"
							+ personCenterData.getImageurl() + ":"
							+ personCenterData.getThreads() + ":"
							+ personCenterData.getUsername());

			personCenterData.clearGroupPersonPosts();
			// Get threads info
			if (Integer.parseInt(personCenterData.getThreads()) > 0) {
				List<ThreadsData> threadData = getThreadInfo();
				personCenterData.setThreadsList(threadData);

				// 构造BaseExpandableListAdapter展示数据对象
				GroupPersonPosts gpp = new GroupPersonPosts();
				gpp.setGroupName(THREAD_NAME);
				gpp.setNumber(String.valueOf(threadData.size()));
				List<ForumPost> posts = new ArrayList<ForumPost>();
				for (int i = 0; i < threadData.size(); i++) {
					ForumPost tmp = new ForumPost();
					tmp.setTid(threadData.get(i).getTid());
					tmp.setTitle(threadData.get(i).getTitle());
					tmp.setReplies(threadData.get(i).getReplies());
					posts.add(tmp);
				}
				gpp.setGroupChild(posts);
				personCenterData.setGroupPersonPosts(gpp);
			} else {
				personCenterData.setThreadsList(new ArrayList<ThreadsData>());

				// 构造BaseExpandableListAdapter默认展示对象
				GroupPersonPosts gpp = new GroupPersonPosts();
				gpp.setGroupName(THREAD_NAME);
				gpp.setNumber("0");
				gpp.setGroupChild(new ArrayList<ForumPost>());
				personCenterData.setGroupPersonPosts(gpp);
			}

			// Get favorites info
			if (Integer.parseInt(personCenterData.getFavorites()) > 0) {
				List<FavoritesData> favoritesData = getFavoritesInfo();
				personCenterData.setFavoritesList(favoritesData);

				// 构造BaseExpandableListAdapter展示数据对象
				GroupPersonPosts gpp = new GroupPersonPosts();
				gpp.setGroupName(FAVORITES_NAME);
				gpp.setNumber(String.valueOf(favoritesData.size()));
				List<ForumPost> posts = new ArrayList<ForumPost>();
				for (int i = 0; i < favoritesData.size(); i++) {
					ForumPost tmp = new ForumPost();
					tmp.setTid(favoritesData.get(i).getTid());
					tmp.setTitle(favoritesData.get(i).getTitle());
					tmp.setReplies(favoritesData.get(i).getReplies());
					posts.add(tmp);
				}
				gpp.setGroupChild(posts);
				personCenterData.setGroupPersonPosts(gpp);
			} else {
				personCenterData.setFavoritesList(new ArrayList<FavoritesData>());

				// 构造BaseExpandableListAdapter默认展示对象
				GroupPersonPosts gpp = new GroupPersonPosts();
				gpp.setGroupName(FAVORITES_NAME);
				gpp.setNumber("0");
				gpp.setGroupChild(new ArrayList<ForumPost>());
				personCenterData.setGroupPersonPosts(gpp);
			}

		}

		private void parseJson(String json, PersonCenterData pData) {
			try {
				String code = new JSONObject(json).getString("code");
				if (code.equals("200")) { // Request Success
					JSONObject dataObj = new JSONObject(json)
							.getJSONObject("data");
					String username = dataObj.getString("username");
					String group = dataObj.getString("group");
					String img = dataObj.getString("img");
					String gold = dataObj.getString("gold");
					String coin = dataObj.getString("coin");
					String weiwang = dataObj.getString("weiwang");
					String threads = dataObj.getString("threads");
					String favorites = dataObj.getString("favorites");

					pData.setUsername(username);
					pData.setGroup(group);
					pData.setImageurl(img);
					pData.setGold(gold);
					pData.setCoin(coin);
					pData.setWeiwang(weiwang);
					pData.setThreads(threads);
					pData.setFavorites(favorites);
				} else {
					// TODO: Deal with illegal request
					if (code.equals("403")) { //

					}
				}
			} catch (JSONException e) {
			}
		}

		void parseHeadPicBitmap(PersonCenterData data) {
			if (data.getImageurl() == null) {
				Log.d(TAG, "user head picture image url is null");
				return;
			}
			Log.d(TAG, "parseHeadPicBitmap img url: " + data.getImageurl());
			String s = data.getImageurl();
			if (s == null)
				return;

			Log.d(TAG, "parseHeadPicBitmap after substring url: " + s);
			Bitmap bitmap = null;
			URL imageUrl;
			HttpURLConnection conn = null;
			try {
				imageUrl = new URL(s);
				conn = (HttpURLConnection) imageUrl.openConnection();
				conn.setConnectTimeout(TIME_OUT);
				conn.setReadTimeout(TIME_OUT);
				conn.setInstanceFollowRedirects(true);
				InputStream is = conn.getInputStream();
				bitmap = BitmapFactory.decodeStream(is);
				conn.disconnect();
			} catch (MalformedURLException e) {
				Log.d(TAG, "get user head bitmap failed " + e);
			} catch (IOException e) {
				Log.d(TAG, "get user head bitmap failed " + e);
			}

			if (bitmap != null)
				data.setHeadPicBitmap(bitmap);
		}

		/**
		 * Get threads list
		 */
		private List<ThreadsData> getThreadInfo() {
			String requestThreadsUrl = BASE_URL + GET_THREADS;
			StringBuffer sb = new StringBuffer();
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("token", token));
			params.add(new BasicNameValuePair("page", "1"));
			HttpUtils.doHttpPost(requestThreadsUrl, params, sb);
			List<ThreadsData> threadsList = parseJsonForThreads(sb.toString());
			return threadsList;
		}

		private List<ThreadsData> parseJsonForThreads(String json) {
			ArrayList<ThreadsData> threadsList = new ArrayList<ThreadsData>();

			try {
				String code = new JSONObject(json).getString("code");
				if (code.equals("200")) {
					JSONArray data = new JSONObject(json).getJSONArray("data");
					for (int i = 0; i < data.length(); i++) {
						ThreadsData tmp = new ThreadsData();
						JSONObject object = data.getJSONObject(i);
						tmp.setTid(object.getString("tid"));
						tmp.setTitle(object.getString("title"));
						String replies = object.getString("replies");
						if (replies == null || replies.equals("")) {
							replies = "0";
						}
						tmp.setReplies(replies);
						threadsList.add(tmp);
					}
				}
			} catch (JSONException e) {

			}

			return threadsList;
		}

		/**
		 * Get favorites list
		 */
		private List<FavoritesData> getFavoritesInfo() {
			String requestFavoritesUrl = BASE_URL + GET_FAVORITES;
			StringBuffer sb = new StringBuffer();
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("token", token));
			params.add(new BasicNameValuePair("page", "1"));
			HttpUtils.doHttpPost(requestFavoritesUrl, params, sb);

			List<FavoritesData> favoritesList = parseJsonForFavorites(sb
					.toString());
			return favoritesList;
		}

		private List<FavoritesData> parseJsonForFavorites(String json) {
			ArrayList<FavoritesData> favoritesList = new ArrayList<FavoritesData>();
			try {
				String code = new JSONObject(json).getString("code");
				if (code.equals("200")) {
					JSONArray data = new JSONObject(json).getJSONArray("data");
					for (int i = 0; i < data.length(); i++) {
						FavoritesData tmp = new FavoritesData();
						JSONObject object = data.getJSONObject(i);
						tmp.setTid(object.getString("tid"));
						tmp.setTitle(object.getString("title"));
						String replies = object.getString("replies");
						if (replies == null || replies.equals("")) {
							replies = "0";
						}
						tmp.setReplies(replies);
						favoritesList.add(tmp);
					}
				}
			} catch (JSONException e) {

			}

			return favoritesList;
		}

		@Override
		public void run() {
			// TODO:Just a test
			Log.e(TAG, "I AM RUNNING");

			// Get person center data object
			getLoginInfo();

			// Send message to handler
			handler.sendMessage(handler.obtainMessage(LOGIN_UPDATE_UI));
		}

	}

	// 更改头像
	public void howToGetPic() {
		new AlertDialog.Builder(MainActivity.getContext()).setItems(
				new CharSequence[] { getString(R.string.from_camera),
						getString(R.string.from_album),
						getString(R.string.cancel) },
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						switch (item) {
						case 0:
							takePhotoFromCamera();
							break;
						case 1:
							takePhotoFromAlbum();
							break;
						default:
							break;
						}
					}
				}).show();
	}

	private static final File PHOTO_DIR = new File(Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera");
	private File mCurrentPhotoFile;

	public void takePhotoFromCamera() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡
			try {
				// Launch camera to take photo for selected contact
				PHOTO_DIR.mkdirs();// 创建照片的存储目录
				mCurrentPhotoFile = new File(PHOTO_DIR, getPhotoFileName());// 给新照的照片文件命名
				Log.i(TAG, PHOTO_DIR.toString());
				final Intent intent = new Intent(
						MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
				intent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(mCurrentPhotoFile));
				startActivityForResult(intent, Request_Code_Carema);
			} catch (ActivityNotFoundException e) {
			}
		} else {
			Toast.makeText(mContext, "没有检测到SD卡", Toast.LENGTH_SHORT).show();
		}

	}

	/**
	 * 用当前时间给取得的图片命名
	 */
	private String getPhotoFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"'IMG'_yyyyMMdd_HHmmss");
		return dateFormat.format(date) + ".jpg";
	}

	public void takePhotoFromAlbum() {
		try {
			// Launch picker to choose photo for selected contact
			final Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
			intent.setType("image/*");
			intent.putExtra("crop", "true");
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("outputX", 300);
			intent.putExtra("outputY", 300);
			intent.putExtra("return-data", true);
			startActivityForResult(intent, Request_Code_Album);
		} catch (ActivityNotFoundException e) {
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case Request_Code_Carema:
				doCropPhoto(mCurrentPhotoFile);
				break;
			case Request_Code_Album:
				Bitmap bitmap = null;
				Uri imgUri = null;

				bitmap = data.getParcelableExtra("data");
				if (bitmap == null) {
					// used on OS3.0
					imgUri = data.getData();
					if (imgUri == null) {
						Log.e(TAG, "onActivityResult Request_Code_Carema bitmap is null");
						break;
					} else {
						try {
							bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), imgUri);
						} catch (FileNotFoundException e) {
							Log.e(TAG, "failed to get Bitmap from Gallery: " + e);
						} catch (IOException e) {
							Log.e(TAG, "failed to get Bitmap from Gallery: " + e);
						}
					}
				}

				Log.d(TAG, "Request_Code_Album send message UPLOAD_HEAD_PIC");
				//picData.setBitmap(resizeBitmap(bitmap, 120, 120));
				Bitmap roundBitmap = toRoundBitmap(bitmap);
				overrideHeadPicBitmap = roundBitmap;
				picData.setBitmap(roundBitmap);
				handler.sendMessage(handler.obtainMessage(UPLOAD_HEAD_PIC));
				break;
			case Request_Code_Login:
				Bundle bundle = data.getExtras();
				token = bundle.getString("token");
				// Success to login in
				afterCleanCacheLogin();
			default:
				break;
			}
		} else {
			if (requestCode == Request_Code_Login) {
				Log.e(LoginManager.TAG, "回调反而慢了");
			}
		}
	}

	public Bitmap resizeBitmap(Bitmap bitmap, int height, int width) {
		float scaleWidth = ((float) width) / bitmap.getWidth();
		float scaleHeight = ((float) height) / bitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);

		Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,
				true);
		return newbm;
	}

	private class HeadPicData {
		String token;
		Bitmap bitmap;
		String ossUrl;

		String getToken() {
			return token;
		}

		void setToken(String token) {
			this.token = token;
		}

		Bitmap getBitmap() {
			return bitmap;
		}

		void setBitmap(Bitmap bitmap) {
			this.bitmap = bitmap;
		}

		String getOssUrl() {
			return ossUrl;
		}

		void setOssUrl(String ossUrl) {
			this.ossUrl = ossUrl;
		}

	}

	private class uploadHeadPic implements Runnable {

		void doUploadHeadPic() {
			if (picData.getBitmap() == null) {
				Log.d(TAG, "uploadHeadPic bitmap in picData is null");
				return;
			}

			File tmpFile = new File(mContext.getCacheDir(), "tmp.jpg");
			OutputStream out = null;
			try {
				out = new BufferedOutputStream(new FileOutputStream(tmpFile));
				picData.getBitmap().compress(CompressFormat.JPEG, 100, out);
			} catch (FileNotFoundException e) {
				Log.e(TAG, "failed to write file " + e);
			}

			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					Log.d(TAG, "failed to close OutputStream " + e);
				}
			else {
				return;
			}

			// upload image file to aliyun OSS
			StringBuffer ossUrl = new StringBuffer();
			String fileKey = "yunosbbs/attachment/"
					+ UUID.randomUUID().toString() + ".jpg";
			if (ossUtils.pubObjectFromInputStream(fileKey, tmpFile.getPath(),
					ossUrl)) {
				Log.d(TAG,
						"upload user headPic success, url = "
								+ ossUrl.toString());
				picData.setOssUrl(ossUrl.toString());
				Log.d(TAG, "icon filename: " + ossUrl.toString());
				handler.sendMessage(handler.obtainMessage(UPDATE_HEAD_PIC));
			} else {
				Log.d(TAG, "oss upload file error");
			}
		}

		@Override
		public void run() {
			Log.d(TAG, "uploadHeadPic running");
			doUploadHeadPic();
		}

	};

	private class updateUserIcon implements Runnable {

		private void doUpdateUserIcon() {
			int status = -1;
			StringBuffer sb = new StringBuffer();
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("token", token));
			params.add(new BasicNameValuePair("icon", picData.getOssUrl()));
			if (HttpUtils.doHttpPost(BASE_URL + UPDATE_USER_ICON, params, sb)
					&& (status = HttpUtils.getResponseCode(sb.toString())) == HttpStatus.SC_OK) {
				Log.d(TAG, "doUpdateUserIcon success");
			} else if (status != -1) {
				Log.d(TAG, "doUpdateUserIcon failed");
			}

		}

		@Override
		public void run() {
			doUpdateUserIcon();
		}

	};

	protected void doCropPhoto(File f) {
		try {
			// 启动gallery去剪辑这个照片
			final Intent intent = getCropImageIntent(Uri.fromFile(f));
			startActivityForResult(intent, Request_Code_Album);
		} catch (Exception e) {
		}
	}

	/**
	 * Constructs an intent for image cropping. 调用图片剪辑程序
	 */
	public static Intent getCropImageIntent(Uri photoUri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(photoUri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 300);
		intent.putExtra("outputY", 300);
		intent.putExtra("return-data", true);
		return intent;
	}

	// 裁剪头像成圆形
	public Bitmap toRoundBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;
			top = 0;
			bottom = width;
			left = 0;
			right = width;
			height = width;
			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}

		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right,
				(int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top,
				(int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);

		paint.setAntiAlias(true);

		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		return output;
	}
}
