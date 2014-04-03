package com.yunos.sdk.account;

public class ErrorCode {
    /** 异常失败 */
    public static final int FAILED  = 0;  
    
    /** 成功状态 */
    public static final int SUCCESS  = 200;    
    /** 拒绝授权 */
    public static final int REJECT                              = 201;
    /** 参数错误 */
    public static final int SYS_INVALID_PARAMS                = 51001;
    /** openapi key错误 */  
    public static final int OPENAPI_KEY_ERROR                 = 61001; 
    /** openapi sign错误 */  
    public static final int OPENAPI_SIGN_ERROR                = 61002; 
    /** openapi key未启用 */
    public static final int OPENAPI_KEY_UNACTIVE             = 61003; 
    /** accessToken错误 */
    public static final int ACCESS_TOKEN_ERROR               = 71001;
    /** 代理生成accessToken错误 */
    public static final int ACCESS_TOKEN_APPLY_ERROR        = 71002;
    /** 登陆账号存在冲突账号 */
    public static final int LOGIN_ACCOUNT_CONFLICT          = 72001; 
    /** 登陆账号未激活 云OS业务*/
    public static final int LOGIN_ACCOUNT_NOACTIVE          = 72002;
}