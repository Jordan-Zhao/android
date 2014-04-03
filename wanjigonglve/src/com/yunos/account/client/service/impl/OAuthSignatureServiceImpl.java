package com.yunos.account.client.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.yunos.account.client.bo.ConstBo;
import com.yunos.account.client.bo.OAuthPairBo;
import com.yunos.account.client.bo.OAuthParamsBo;
import com.yunos.account.client.service.OAuthSignatureService;
import com.yunos.account.client.utility.Md5Util;

/**
 * 
 * @author hanqi
 *
 */
public class OAuthSignatureServiceImpl implements OAuthSignatureService {
	public String sign(OAuthParamsBo paramsBo, String appSecret) {
		/* 参数类型转换  */
		Map<String, String> paramsMap = new HashMap<String, String>();
		for (OAuthPairBo parameter : paramsBo.getParameters()) {
			paramsMap.put(parameter.getKey(), parameter.getValue());
        }
		paramsMap.remove(ConstBo.SIGN_EKY); //删除不希望加入加密的参数sign
		
		/* 组装加密串 */
		String paramsStr = "";
		Object[] keyArray = paramsMap.keySet().toArray();
		Arrays.sort(keyArray); 
		for (int i = 0; i < keyArray.length; i++) {
			Object values = paramsMap.get(keyArray[i]);
			String[] value = new String[1]; 
	        if(values instanceof String[]){ 
	            value=(String[])values; 
	        }else{ 
	            value[0]=values.toString(); 
	        }
	        paramsStr += keyArray[i] + value[0];
		}
		
		/* 加密 */
		String sign = Md5Util.MD5(paramsStr + appSecret);
//		sign = sign.substring(8, 24);//去16位
		return sign;
	}
}
