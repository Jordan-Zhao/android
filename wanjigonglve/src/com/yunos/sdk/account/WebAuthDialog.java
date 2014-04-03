package com.yunos.sdk.account;

import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class WebAuthDialog extends Dialog {
    static  FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private static final String AUTHORIZE_LOGIN_HOST = "https://account.yunos.com/login/authorizelogin.htm";
    private String mAppKey;
    private String mSign;
    private String mRedirectUri;
    private AuthorizeListener mAuthListener;
    private ProgressDialog mProgressDialog;
    private WebView mWebView;
    private RelativeLayout mWebViewContainer;
    private Activity mActivity;
    public WebAuthDialog(Activity context, String appkey, String sign, String redirectUri, AuthorizeListener listener) {
        super(context,android.R.style.Theme_Translucent_NoTitleBar);
        mAppKey = appkey;
        mSign = sign;
        mRedirectUri = redirectUri;
        mActivity = context;
        mAuthListener = listener;
    }
     
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFeatureDrawableAlpha(Window.FEATURE_OPTIONS_PANEL, 0);  
        this.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                mActivity.finish();
            }
        });
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                onBack();
                return false;
            }
        });
        setUpWebView();
    }

    protected void onBack() {
        try {
            mProgressDialog.dismiss();
        } catch (Exception e) {
        }
        dismiss();
        mActivity.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mWebView) {
            mWebView.stopLoading();
            mWebView.destroy();
        }
    }

    private byte[] getAuthorizePostData() {
        byte[] postData = null;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", mAppKey);
        params.put("sign", mSign);
        params.put("redirectURL", mRedirectUri);
        try {
            postData = WebUtils.buildPostData(params, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return postData;
    }
    
    private void setUpWebView() {
        mWebViewContainer = new RelativeLayout(getContext());
        mWebView = new WebView(getContext());
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new AuthWebViewClient());
        byte[] data = getAuthorizePostData();
        if(data != null) {
            //use post let's the token security
            mWebView.postUrl(AUTHORIZE_LOGIN_HOST, data);
        } else {
            //error
            mWebView.loadUrl(AUTHORIZE_LOGIN_HOST);
        }
        mWebView.setLayoutParams(FILL);
        mWebView.setVisibility(View.INVISIBLE);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT);
        mWebViewContainer.addView(mWebView,lp);
        mWebViewContainer.setGravity(Gravity.CENTER);
        addContentView(mWebViewContainer, new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT));
    }

    private class AuthWebViewClient extends WebViewClient {
        private static final String KEY_ERROR = "status";
        private static final String KEY_DESCRIPITON = "message";
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) { 
            return super.shouldOverrideUrlLoading(view, url);
        }
        
        @Override
        public void onReceivedError(WebView view, int errorCode, String description,
            String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            handleError(errorCode, description, failingUrl);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (url.startsWith(mRedirectUri)) {
                handleRedirectUrl(url);
                return;
            }
            super.onPageStarted(view, url, favicon);
            mProgressDialog.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            mWebView.setVisibility(View.VISIBLE);
        }
        
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
        
        private void handleError(int errorCode, String description, String failingUrl) {
            AuthError authError = new AuthError();
            authError.setError(errorCode);
            authError.setErrorDescription(description);
            mAuthListener.onError(authError);
            WebAuthDialog.this.dismiss();
        }
        
        private void handleRedirectUrl(String url) {
            Uri uri = Uri.parse(url);
            Bundle values = new Bundle();
            String ret = uri.getQuery();
            String[] kv = ret.split("&");
            for (String each : kv) {
                String[] ss = each.split("=");
                if (ss != null && ss.length == 2) {
                    values.putString(ss[0], ss[1]);
                }
            }
            String status  = values.getString(KEY_ERROR);
            int error = ErrorCode.FAILED;
            if(!TextUtils.isEmpty(status)) {
                try {
                    error = Integer.valueOf(status);
                } catch(NumberFormatException e) {
                    Log.e("account", e.toString());
                }
            }
            if(error == ErrorCode.SUCCESS) {
                AccessToken token = AccessTokenManager.convertToAccessToken(values);
                AccessTokenManager.saveAccessToken(mActivity.getApplicationContext(), token);
                mAuthListener.onComplete(token);
            } else {
                AuthError authError = new AuthError();
                String error_description = values.getString(KEY_DESCRIPITON);
                authError.setError(error);
                authError.setErrorDescription(error_description);
                mAuthListener.onError(authError);
            }
            WebAuthDialog.this.dismiss();
        }
    }
}