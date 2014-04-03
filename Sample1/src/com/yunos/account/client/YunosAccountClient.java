package com.yunos.account.client;


import java.util.ArrayList;
import java.util.List;

import com.yunos.account.client.bo.ConstBo;
import com.yunos.account.client.bo.OAuthPairBo;
import com.yunos.account.client.bo.OAuthParamsBo;
import com.yunos.account.client.exception.OAuthException;
import com.yunos.account.client.service.OAuthService;
import com.yunos.account.client.service.impl.OAuthServiceImpl;
import com.yunos.account.client.utility.SSLHandlerUitl;

/**
 * @author hanqi
 */
public class YunosAccountClient {

    private String appKey;
    private String appSecret;
    private String apiUrl; //such like : http://account.yunos.com/openapi
    private String version = ConstBo.VERSION_1;
    private int timeoutInMilliSeconds = 2000; // 2 seconds timeout
    private OAuthService service;

    /**
     * @param appKey
     * @param appSecret
     * @throws OAuthException
     */
    public YunosAccountClient(String apiUrl, String appKey, String appSecret) throws OAuthException {
        if (apiUrl == null || apiUrl.trim().length() == 0) {
            throw new OAuthException("apiMethod should not be empty or null");
        }
        if (appKey == null || appKey.trim().length() == 0) {
            throw new OAuthException("appKey should not be empty or null");
        }
        if (appSecret == null || appSecret.trim().length() == 0) {
            throw new OAuthException("appSecret should not be empty or null");
        }
        setApiUrl(apiUrl);
        setAppKey(appKey);
        setAppSecret(appSecret);
        this.service = new OAuthServiceImpl();
        SSLHandlerUitl.handleHttpsCertification(); //解决部分证书无效的问题，使所有证书有效，这会降低系统安全
    }
    
    /**
     * call open api with full path and query, such like
     * http://account.yunos.com/openapi?api=account.simple_case&v=1&token=aaaaa
     * 
     * @param apiName
     *            , Request full path, like:
     *            account.simple_case
     * @return
     * @throws OAuthException
     */
    public String callApi(String apiName) throws OAuthException {
        return callApi(apiName, new ArrayList<OAuthPairBo>());
    }


    /**
     * call open api, such like
     * http://account.yunos.com/openapi
     * 
     * @param apiName
     *            , Request full path, like:
     *            account.simple_case
     * @param params
     *            , parameters need for certain api, no need to add oauth
     *            specified parameter like oauth_nonce
     */
    public String callApi(String apiName, List<OAuthPairBo> params) throws OAuthException {
        return callApi(apiName, params, getVersion());
    }

    /**
     * call open api, such like
     * http://account.yunos.com/openapi
     * 
     * @param apiName
     *            , api名称, like:
     *            account.simple_case
     * @param params
     *            , parameters need for certain api, no need to add oauth
     *            specified parameter like oauth_nonce
     */
    public String callApi(String apiName, List<OAuthPairBo> params, String version) throws OAuthException {
        OAuthParamsBo paramsBo = service.createOAuthParamsBo(getApiUrl(), apiName, params, getAppKey(), getAppSecret(), version);
        String resp = service.sendRequest(paramsBo, timeoutInMilliSeconds);
        return resp;
    }

    /**
     * 获取完整的请求地址
     * 
     * @param apiName api名称
     * @param params
     * @return
     * @throws OAuthException
     */
    public String getUri(String apiName, List<OAuthPairBo> params) throws OAuthException {
        return getUri(apiName, params, getVersion());
    }
    
    /**
     * 获取完整的请求地址
     * 
     * @param apiName api名称
     * @param params
     * @param version
     * @return
     * @throws OAuthException
     */
    public String getUri(String apiName, List<OAuthPairBo> params, String version) throws OAuthException {
        OAuthParamsBo paramsBo = service.createOAuthParamsBo(getApiUrl(), apiName, params, getAppKey(), getAppSecret(), version);
        return service.getUri(paramsBo);
    }

    //	@Autowired
    //	private Gson gson;
    //	public OAuthResponseBo decode(String str) {
    //		return gson.fromJson(str, OAuthResponseBo.class);
    //	}
    //	/**
    //	 * spring 自动加@Autowired
    //	 */
    //	public static void main(String[] args) throws OAuthException {
    //		ClassPathXmlApplicationContext context =	new ClassPathXmlApplicationContext(new String[]{"classpath:/spring/app-context.xml"});
    //		// ----------- initial client with appKey & appSecret
    //		String my_appKey = "clientTest";
    //		String my_appSecret = "cd60cf3c72c5d039fd240bf549ae602b";
    //		String token = "aaaaaaaaa";
    //		
    //		YunosAccountClient client  = (YunosAccountClient)context.getBean("client");
    //		
    //		client.setAppKey(my_appKey);
    //		client.setAppSecret(my_appSecret);
    //		
    //		// ----------- set openapi parameters defined in openapi document
    //        List<OAuthPairBo> params = new ArrayList<OAuthPairBo>();
    //        params.add(new OAuthPairBo(ConstBo.OAUTH_TOKEN, token));
    //        
    //        // ----------- call openapi
    //        String resp = client.callApi("http://account.yunos.com:8080/openapi/v1/account/simple_case", params);
    //        System.out.println(resp);
    //        System.out.println(client.decode(resp));
    //	}

    /**
     * 调试
     * 
     * @param args
     */
    public static void main(String[] args) throws OAuthException {
        // ----------- initial client with appKey & appSecret

        String[] apis = { "demo.hello" };
        String apiUrl = "https://account.yunos.com/openapi";
        String my_appKey = "clientTest";
        String my_appSecret = "cd60cf3c72c5d039fd240bf549ae602b";
        String token = "aaaaaaaaa";

//        YunosAccountClient client1 = new YunosAccountClient(apiUrl, my_appKey, my_appSecret);
//        OAuthResponseBo resp1 = client1.callApi("account.simple_case");
//        System.out.println(resp1);

        for (int i = 0; i < apis.length; i++) {

            YunosAccountClient client = new YunosAccountClient(apiUrl, my_appKey, my_appSecret);

            // ----------- set openapi parameters defined in openapi document
            List<OAuthPairBo> params = new ArrayList<OAuthPairBo>();
            params.add(new OAuthPairBo(ConstBo.TOKEN_KEY, token));

            // ----------- call openapi
            System.out.println(client.getUri(apis[i], params));
            String resp = client.callApi(apis[i], params);
            System.out.println(resp);
        }
    }

    /**
     * 获得API地址
     * 
     * @return
     */
    public String getApiUrl() {
        return apiUrl;
    }

    /**
     * 设置API地址
     * 
     * @param apiUrl 接口地址 : http://account.yunos.com/openapi
     */
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    /**
     * 用户的KEY，即公钥
     * 
     * @return
     */
    public String getAppKey() {
        return appKey;
    }

    /**
     * 用户的KEY，即公钥
     * 
     * @param appKey
     */
    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    /**
     * 用户的Secret，即秘钥
     * 
     * @return
     */
    public String getAppSecret() {
        return appSecret;
    }

    /**
     * 用户的Secret，即秘钥
     * 
     * @param appSecret
     */
    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    /**
     * 版本号
     * 
     * @return
     */
    public String getVersion() {
        return version;
    }

    /**
     * 版本号
     * 
     * @param version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 连接和调用API接口的超时时间，单位：毫秒
     * 
     * @return
     */
    public int getTimeoutInMilliSeconds() {
        return timeoutInMilliSeconds;
    }

    /**
     * 连接和调用API接口的超时时间，单位：毫秒
     * 
     * @param timeoutInMilliSeconds
     */
    public void setTimeoutInMilliSeconds(int timeoutInMilliSeconds) {
        this.timeoutInMilliSeconds = timeoutInMilliSeconds;
    }

}
