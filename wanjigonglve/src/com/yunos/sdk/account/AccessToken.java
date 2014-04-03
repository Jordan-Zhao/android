package com.yunos.sdk.account;

import java.io.Serializable;


public class AccessToken implements Serializable {
    private static final long serialVersionUID = -5222490904556080200L;
    /**
     * 应用获取到的认证TOKEN
     */
    private String mAccessToken;
    /**
     * RefreshToken，用来刷新认证TOKEN
     */
    //private String mRefreshToken;
    
    /**
     * TOKEN的失效时间，需要再失效后进行Refresh操作
     */
    //private long mExpireTime;
    
    /**
     * RefreshToken的失效时间
     */
    //private long mRefreshExpireTime;
    
    /**
     * YUNOS业务唯一标识
     */
    private String mYunosKP;
    /**
     * 当前登录名
     */
    private String mUserName;
    
    
    @Override
    public String toString() {
        return "Token: " + mAccessToken
            + "; KP:" + mYunosKP
            + "; UserName:" + mUserName;
    }
    
    
    public void setAccessToken(String token) {
        mAccessToken = token;
    }
    
    /**
     * 获取应用的Access Token
     * @return mAccessToken
     */
    public String getAccessToken() {
        return mAccessToken;
    }
    
    
    public void setUserInfo(String name, String kp) {
        mUserName = name;
        mYunosKP = kp;
    }
    /**
     * 获取当前授权帐号的云OS用户ID
     * @return mYunosKP
     */
    public String getYunOSKP() {
        return mYunosKP;
    }
    
    public String getUserName() {
        return mUserName;
    }
}
