package com.yunos.account.client.service;

import java.util.List;

import com.yunos.account.client.bo.OAuthPairBo;
import com.yunos.account.client.bo.OAuthParamsBo;
import com.yunos.account.client.exception.OAuthException;

/**
 * 
 * @author hanqi
 *
 */
public interface OAuthService {
	/**
	 * 获取完整的请求地址
	 * @param from
	 * @return
	 * @throws OAuthException
	 */
	String getUri(OAuthParamsBo from) throws OAuthException;
	/**
	 * 组建请求参数
	 */
	OAuthParamsBo createOAuthParamsBo(String api, String apiMethod, List<OAuthPairBo> params, String appKey, String appSecret, String version) throws OAuthException;
	/**
	 * 发送请求并返回结果
	 */
	String sendRequest (OAuthParamsBo from, int timeoutInMilliSeconds)  throws OAuthException;
}
