package com.yunos.sdk.account;

import com.yunos.sdk.account.ISsoService;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


public class SsoClient {
    private Activity mAuthActivity;
    private String mRedirectUri = "https://localhost.yunos.com";
    private String mAppKey;
    private AuthorizeListener mAuthListener;
    private String mSsoPackageName;
    private String mSsoActivityName;
    private int mRequestCode;
    private static final String SIGNATURE =         
"3082022730820190a00302010202045257ca75300d06092a864886f70d01010505003057310b300906035504061302636e310b3009060355040813027a6a310b300906035504071302687a310e300c060355040a130579756e6f73310e300c060355040b130579756e6f73310e300c0603550403130579756e6f733020170d3133313031313039353235335a180f32303638303731343039353235335a3057310b300906035504061302636e310b3009060355040813027a6a310b300906035504071302687a310e300c060355040a130579756e6f73310e300c060355040b130579756e6f73310e300c0603550403130579756e6f7330819f300d06092a864886f70d010101050003818d003081890281810093c02568065cfd72d809ed5fe3eb4ee4d42816dd8d5545527debcf1db7991559c67b8636e5f33dd4c6a66e7bf82c6f1a70a16a22d46ee57284380a5b923cd47f3249aec7cb56749698bbbba21ece6017f6bf8919f3d76d01b3306708187498d945818c8a8518ad21fd6d2b73c9c7e91d9359d19b91975b34fd7a0a5e564f744b0203010001300d06092a864886f70d0101050500038181000070f16368cd63fa3f81556daa419cdfed7dd3f8b50f52485d66f83796f0222019246a3ff0eb4b4c6ad271c746c98606f9b8af65e37788cd9492e85ccc9d2fea5b043e7cb5c9445c05e80ac7b51971188349a685c1082ec00e4d8135f965c7ab663239278926dbf19f50c4094513fc152b3e6a52ad7b67376ab6025edf38829f";
    
    
    private ServiceConnection  mSsoConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ISsoService remoteSSOService = ISsoService.Stub
                    .asInterface(service);
            try {
                mSsoPackageName = remoteSSOService.getPackageName();
                mSsoActivityName = remoteSSOService.getActivityName();
                Log.d("account", "mSsoPackageName:" + mSsoPackageName +
                        ";mSsoActivityName:" + mSsoActivityName);
                startSSOActivityForResult();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
        
    };
    /**
     * 应用在SSO授权时，需要先创建SsoClient对象，然后再调用{@link #authorizeSso(AuthorizeListener, int)}进行授权
     * @param activity 当前授权的Activity对象
     * @param appkey   授权应用的AppKey
     * @param redirectUri  授权成功后的重定向url，传空使用默认重定向url
     */
    public SsoClient(Activity activity, String appkey, String redirectUri) {
        if(activity == null || TextUtils.isEmpty(appkey)) {
            throw new IllegalArgumentException(
                    "activity and appkey must not null.");
        }
        mAuthActivity = activity;
        mAppKey = appkey;
        if(!TextUtils.isEmpty(redirectUri)) {
            mRedirectUri = redirectUri;         
        }
    }
    
    /**
     * 应用调用该函数进行登录授权
     * @param listener 应用在listener中处理授权结果
     * @param requestCode  授权过程中，启动activity的requestCode >= 0
     * @return true，当前系统支持SSO授权，false不支持SSO授权
     */
    public boolean authorizeSso(final AuthorizeListener listener, final int requestCode) {
        if(listener == null || requestCode < 0 ) {
            throw new IllegalArgumentException(
            "listener must not null and requestCode should >= 0");
        }
        mRequestCode = requestCode;
        mAuthListener = listener;
        Context context = mAuthActivity.getApplicationContext();
        Intent intent = new Intent("com.yunos.sdk.account.ssoservice");
        boolean ssoStarted = context.bindService(intent, mSsoConn, Context.BIND_AUTO_CREATE);
        if (!ssoStarted) {
            //startAuthDialog(mAuthActivity, mAuthListener);
        }        
        return ssoStarted;
    }
    
   /* private void startAuthDialog(Activity activity, final AuthorizeListener listener) {
        String signStr = null;
        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(
            activity.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];
            signStr =  Md5Util.MD5(sign.toByteArray());
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        new WebAuthDialog(activity, mAppKey, signStr, mRedirectUri, listener).show();
    }*/
    
    
    /**
     * 发起SSO授权的Activity必须重写onActivityResult， 这个方法必须在onActivityResult方法内调用，
     * 例如：<br/>
     * 
     * @Override
     * protected void onActivityResult(int requestCode, int resultCode, Intent data) {<br/>
     *    super.onActivityResult(requestCode, resultCode, data);<br/>
     *    if(mSsoClient!=null){<br/>
     *       mSsoClient.authorizeCallBack(requestCode, resultCode, data);<br/>
     *   }<br/>
     * }
     */
    public void authorizeCallBack(int requestCode, int resultCode, Intent data) {
        if(requestCode == mRequestCode) {
            if(resultCode == Activity.RESULT_OK) {
                Bundle values = data.getExtras();
                String errorStr = values.getString("status");
                int error = 0;
                if(!TextUtils.isEmpty(errorStr)){
                    error = Integer.valueOf(errorStr);
                }
                String error_description = values.getString("message");
                if(error == ErrorCode.SUCCESS) {
                    AccessToken token = AccessTokenManager.convertToAccessToken(values);
                    AccessTokenManager.saveAccessToken(mAuthActivity.getApplicationContext(), token);
                    mAuthListener.onComplete(token);
                } else {
                    AuthError authError = new AuthError();
                    authError.setError(error);
                    authError.setErrorDescription(error_description);
                    mAuthListener.onError(authError);
                }
            } else {
                mAuthListener.onCancel();
            }
        }
    }
    

    private boolean startSSOActivityForResult() {
        boolean succeed = true;
        Intent intent = new Intent();
        intent.setClassName(mSsoPackageName, mSsoActivityName);
        Bundle options = new Bundle();
        options.putString("key", mAppKey);
        options.putString("redirectURL", mRedirectUri);
        intent.putExtras(options);
        if(validateSsoAppSignature(mAuthActivity, intent)) {
            try {
                mAuthActivity.startActivityForResult(intent, mRequestCode);
            } catch (ActivityNotFoundException e) {
                succeed = false;
            }
        } else {
            succeed = false;
        }
        mAuthActivity.getApplication().unbindService(mSsoConn);
        if(!succeed) {
            Toast.makeText(mAuthActivity, "The system not support sso authorize!", Toast.LENGTH_LONG);
        }
        return succeed;
    }
    
    private boolean validateSsoAppSignature(Context context,
            Intent intent) {
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(
                intent, 0);
        if (resolveInfo == null) {
            return false;
        }
        String packageName = resolveInfo.activityInfo.packageName;
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfo.signatures) {
                if (SIGNATURE.equals(signature.toCharsString())){
                    return true;
                }
            }
        } catch (NameNotFoundException e) {
            return false;
        }
        return false;
    }

    public static boolean isSsoSupport(Context context) {
        Intent intent = new Intent("com.yunos.sdk.account.ssoservice");
        ResolveInfo resolveInfo = context.getPackageManager().resolveService(
                intent, 0);
        if (resolveInfo == null) {
            return false;
        }
        String packageName = resolveInfo.serviceInfo.packageName;
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfo.signatures) {
                if (SIGNATURE.equals(signature.toCharsString())){
                    return true;
                }
            }
        } catch (NameNotFoundException e) {
            return false;
        }
        return false;
    }
}