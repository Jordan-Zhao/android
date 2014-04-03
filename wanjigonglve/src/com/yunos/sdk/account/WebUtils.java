package com.yunos.sdk.account;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.text.TextUtils;

public abstract class WebUtils {
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static URL buildGetUrl(String url, Map<String, String> params,
            String charset) throws IOException {
        String queryString = buildQuery(params, charset);
        return buildGetUrl(url, queryString);
    }

    public static byte[] buildPostData( Map<String, String> params,
            String charset) throws IOException {
        String data = buildQuery(params, charset);
        return data.getBytes();
    }
    
    private static URL buildGetUrl(String strUrl, String query)
            throws IOException {
        URL url = new URL(strUrl);
        if (TextUtils.isEmpty(query)) {
            return url;
        }
        
        if (TextUtils.isEmpty(url.getQuery())) {
            if (strUrl.endsWith("?")) {
                strUrl = strUrl + query;
            } else {
                strUrl = strUrl + "?" + query;
            }
        } else {
            if (strUrl.endsWith("&")) {
                strUrl = strUrl + query;
            } else {
                strUrl = strUrl + "&" + query;
            }
        }
        return new URL(strUrl);
    }

    public static String buildQuery(Map<String, String> params, String charset)
            throws UnsupportedEncodingException {
        if (params == null || params.isEmpty()) {
            return null;
        }
        if (TextUtils.isEmpty(charset)) {
            charset = DEFAULT_CHARSET;
        }
        StringBuilder query = new StringBuilder();
        Set<Entry<String, String>> entries = params.entrySet();
        boolean hasParam = false;
        for (Entry<String, String> entry : entries) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
                if (hasParam) {
                    query.append("&");
                } else {
                    hasParam = true;
                }
                query.append(name).append("=")
                    .append(URLEncoder.encode(value, charset));
            }
        }
        return query.toString();
    }
}