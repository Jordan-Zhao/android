package com.yunos.account.client.utility;

import java.security.MessageDigest;

import android.util.Log;


/**
 * MD5工具类
 * @author zhuli.zhul@aliyun-inc.com 2012-12-19 下午6:22:42
 */
public class Md5Util { 
    
    /**
     * 字符串MD5加密后返回字符串格式
     * @param md5Str
     * @return
     */
    public static String MD5(String md5Str) { 
    	char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};       
        try {
            byte[] btInput = (md5Str).getBytes("UTF-8");
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            String result = new String(str);
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    } 
}

