package com.yunos.account.client.service;

import com.yunos.account.client.bo.OAuthParamsBo;

/**
 * 
 * @author hanqi
 *
 */
public interface OAuthSignatureService {
	/**
	 * md5加密
	 * @param paramsBo  请求参数
	 * @param appSecret  秘钥
	 * @return
	 */
	String sign(OAuthParamsBo paramsBo, String appSecret);
}
