package com.yunos.account.client.service.impl;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import com.yunos.account.client.bo.ConstBo;
import com.yunos.account.client.bo.OAuthPairBo;
import com.yunos.account.client.bo.OAuthParamsBo;
import com.yunos.account.client.exception.OAuthException;
import com.yunos.account.client.service.OAuthService;
import com.yunos.account.client.service.OAuthSignatureService;
import com.yunos.account.client.utility.OAuthUtil;

/**
 * @author hanqi
 */
public class OAuthServiceImpl implements OAuthService {

    /**
     * 获取完整的请求地址
     * 
     * @param from
     * @return
     * @throws OAuthException
     */
    public String getUri(OAuthParamsBo from) throws OAuthException {
        StringBuilder buidler = createBuilder(from);
        return from.getUrl() + "?" + buidler.toString();
    }

    /**
     * 组建请求参数
     */
    public OAuthParamsBo createOAuthParamsBo(String api, String apiMethod, List<OAuthPairBo> params, String appKey, String appSecret, String version)
            throws OAuthException {
        Long now = System.currentTimeMillis() / 1000;
        String timestamp = Long.toString(now);
        OAuthParamsBo paramsBo = new OAuthParamsBo(ConstBo.HTTP_POST_METHOD, api, params);

        paramsBo.addParameter(new OAuthPairBo(ConstBo.API_METHOD_KEY, apiMethod));
        paramsBo.addParameter(new OAuthPairBo(ConstBo.APPKEY_KEY, appKey));
        paramsBo.addParameter(new OAuthPairBo(ConstBo.VERSION_KEY, version));
        paramsBo.addParameter(new OAuthPairBo(ConstBo.TIMESTAMP_KEY, timestamp));

        OAuthSignatureService signService = new OAuthSignatureServiceImpl();
        String sign = signService.sign(paramsBo, appSecret);
        paramsBo.addParameter(new OAuthPairBo(ConstBo.SIGN_EKY, sign));
        return paramsBo;
    }

    /**
     * 发送请求并返回结果
     */
    public String sendRequest(OAuthParamsBo from, int timeoutInMilliSeconds) throws OAuthException {
        URL url;
        try {
            url = new URL(from.getUrl());
        } catch (MalformedURLException e) {
            throw new OAuthException(e);
        }

        HttpURLConnection conn = null;
        OutputStream outStream = null;
        InputStream inputStream = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(from.getRequestMethod());
            conn.setConnectTimeout(timeoutInMilliSeconds);
            conn.setReadTimeout(timeoutInMilliSeconds);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);

            StringBuilder buidler = createBuilder(from);
            //            System.out.println(url.toURI() + "?" + buidler.toString());

            outStream = conn.getOutputStream();
            outStream.write(buidler.toString().getBytes(ConstBo.ENCODING));
            outStream.flush();

            inputStream = conn.getInputStream();

            ByteArrayOutputStream byteArrayBuff = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int l = -1;
            while ((l = inputStream.read(buff, 0, 1024)) != -1) {
                byteArrayBuff.write(buff, 0, l);
            }
            return new String(byteArrayBuff.toByteArray(), ConstBo.ENCODING);
        } catch (Exception e) {
            throw new OAuthException(e);
        } finally {
            if (outStream != null)
                try {
                    outStream.close();
                } catch (IOException e) {
                }
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            if (conn != null)
                conn.disconnect();
        }
    }

    /**
     * 组装请求参数URL
     * 
     * @param from
     * @return
     * @throws OAuthException
     */
    private StringBuilder createBuilder(OAuthParamsBo from) throws OAuthException {
        StringBuilder buidler = new StringBuilder();
        for (OAuthPairBo parameter : from.getParameters()) {
            if (buidler.length() > 0) {
                buidler.append("&");
            }
            buidler.append(OAuthUtil.percentEncode(parameter.getKey())).append("=");
            //            buidler.append(OAuthUtil.percentEncode(parameter.getValue()));      
            try {
                //                buidler.append(URLEncoder.encode(OAuthUtil.percentEncode(parameter.getKey()), ConstBo.ENCODING)).append("=");
                //                buidler.append(URLEncoder.encode(OAuthUtil.percentEncode(parameter.getValue()), ConstBo.ENCODING));
                buidler.append(URLEncoder.encode(parameter.getValue(), ConstBo.ENCODING));
            } catch (UnsupportedEncodingException e) {
                throw new OAuthException(e);
            } catch (Exception e) {
                throw new OAuthException(e);
            }
        }
        return buidler;
    }

}
