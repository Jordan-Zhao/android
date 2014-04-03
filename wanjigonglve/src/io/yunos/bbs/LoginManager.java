package io.yunos.bbs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.yunos.account.client.YunosAccountClient;
import com.yunos.account.client.bo.OAuthPairBo;
import com.yunos.account.client.exception.OAuthException;
import com.yunos.sdk.account.AccessToken;
import com.yunos.sdk.account.AccessTokenManager;
import com.yunos.sdk.account.AuthError;
import com.yunos.sdk.account.AuthorizeListener;
import com.yunos.sdk.account.SsoClient;

//该Activity是一个无界面的Activity，负责处理登陆逻辑。
public class LoginManager extends Activity {

	private Context mContext;
	private AccessToken mToken;
	private SsoClient mSsoClient = null;
	public static final String TAG = "Login";
	private static final int UNYUNOS_LOGIN = 0;
	private static final int YUNOS_LOGIN = 1;
	private Handler handler = new Handler();
	private static final String apiUrl = "http://account.yunos.com/openapi";
	private static final String APPKEY = "f766f93b69b257255eacf53c5f124c4e";
	private static final String APPSECRET = "975f57409d412c7e1bbfb39e81e47f44";
	private String resp;
	private SharedPreferences sp;
	private boolean mSsoSupport = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mContext = (Context) LoginManager.this;

		sp = mContext.getSharedPreferences("IsFirstLogin",
				MODE_PRIVATE);

		Log.d(TAG, "Login start");
		// 判断是否第一次登陆
		if (sp.getBoolean("IsFirstLogin", true)) {
			// 判断是否支持免登
			mSsoSupport = SsoClient.isSsoSupport(this);
			Log.d(TAG, "mSSoSupport:" + mSsoSupport);
			if (mSsoSupport) {
				// 云OS3.0以上支持免登
				mSsoClient = new SsoClient(this, APPKEY, null);
				boolean start = mSsoClient.authorizeSso(mAuthorizeListener,
						YUNOS_LOGIN);
				if (!start) {
					Intent intent = new Intent(mContext, UnYunosLogin.class);
					startActivityForResult(intent, UNYUNOS_LOGIN);
				}
			} else {
				// 若不支持免登，调到UnYunosLogin进行网页端登陆

				Intent intent = new Intent(mContext, UnYunosLogin.class);
				startActivityForResult(intent, UNYUNOS_LOGIN);
			}
		} else {
			Intent intent = new Intent(mContext, UnYunosLogin.class);
			startActivityForResult(intent, UNYUNOS_LOGIN);
		}
	}

	private AuthorizeListener mAuthorizeListener = new AuthorizeListener() {
		@Override
		public void onComplete(AccessToken token) {
			Log.d(TAG, "get Token Success!");
			AccessTokenManager.saveAccessToken(mContext, token);
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("token", token.getAccessToken());
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			Toast.makeText(mContext, getString(R.string.login_success),
					Toast.LENGTH_SHORT).show();
			loginSuccessSetSP();
			finish();
		}

		@Override
		public void onError(AuthError authError) {
			Toast.makeText(mContext, getString(R.string.login_fail),
					Toast.LENGTH_SHORT).show();
			Log.d(TAG, "get Token Error");
			finish();
		}

		@Override
		public void onCancel() {
			Toast.makeText(mContext, getString(R.string.login_fail),
					Toast.LENGTH_SHORT).show();
			Log.d(TAG, "get Token Cancled");
			finish();
		}
	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case UNYUNOS_LOGIN:
				// 网页端返回登陆ticket，在这里换成Token。
				Bundle bundle = data.getExtras();
				final String ticket = bundle.getString("ticket");
				final Runnable r = new Runnable() {

					@Override
					public void run() {
						try {
							JSONObject jsonObject = new JSONObject(resp);
							if (jsonObject.getString("status").equals("200")) {
								JSONObject jsonData = jsonObject
										.getJSONObject("data");
								JSONObject jsonTokenInfo = jsonData
										.getJSONObject("accessTokenInfo");
								mToken = new AccessToken();
								mToken.setAccessToken(jsonTokenInfo
										.getString("accessToken"));
								mToken.setUserInfo(
										jsonTokenInfo.getString("username"),
										jsonData.getString("kp"));
								AccessTokenManager.saveAccessToken(mContext,
										mToken);
								Intent intent = new Intent();
								Bundle bundle = new Bundle();
								bundle.putString("token",
										mToken.getAccessToken());
								intent.putExtras(bundle);
								Log.w(TAG, "code :" + RESULT_OK);
								setResult(RESULT_OK, intent);
								loginSuccessSetSP();
								finish();
							} else {
								Toast.makeText(mContext,
										getString(R.string.login_fail),
										Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Toast.makeText(mContext,
									getString(R.string.login_fail),
									Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
					}

				};

				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							YunosAccountClient client = new YunosAccountClient(
									apiUrl, APPKEY, APPSECRET);
							List<OAuthPairBo> params = new ArrayList<OAuthPairBo>();
							params.add(new OAuthPairBo("sessionid", ticket));
							resp = client.callApi(
									"account.get_access_token_by_sessionid",
									params);
						} catch (OAuthException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						handler.post(r);
					}

				}).start();
				break;
			case YUNOS_LOGIN:
				if (mSsoClient != null) {
					mSsoClient.authorizeCallBack(requestCode, resultCode, data);
				}
				break;
			default:
				break;
			}
		} else {
			finish();
		}
	};
	
	public void loginSuccessSetSP(){
		Editor editor = sp.edit();
		editor.putBoolean("IsFirstLogin", false);
		editor.commit();
	}

}
