package com.yunos.sdk.account;

public interface AuthorizeListener {
    /**
     * 认证成功，SDK对token缓存，返回处理后的AccessToken
     * @param token 认证成功返回的AccessToken对象
     */
    public void onComplete(AccessToken token);
    
    /**
     * 认证失败
     * @param authError
     */
    public void onError(AuthError authError);
    
    /**
     * 授权中断
     */
    public void onCancel();
}