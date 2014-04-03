/*
 * Copyright 2007 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yunos.account.client.utility;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Random;

import com.yunos.account.client.bo.ConstBo;
import com.yunos.account.client.exception.OAuthException;


/**
 * @author guanbin
 */
public class OAuthUtil {

    /**
     * normal url encode: space => + => %2B, decode: %2B => + => space => space
     * percent url encode: space => %20, + => %2B decode: %20 => space => space,
     * %2B => + => +
     */
    public static String percentEncode(String s) throws OAuthException {
        if (s == null)
            return null;

        try {
            return URLEncoder.encode(s, ConstBo.ENCODING).replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (Exception e) {
            throw new OAuthException(e);
        }
    }

    // This implements http://oauth.pbwiki.com/FlexibleDecoding
    public static String percentDecode(String s) throws OAuthException {
        if (s == null)
            return null;

        try {
            return URLDecoder.decode(s.replace("+", "%2B"), ConstBo.ENCODING); // + will not decode to space
        } catch (Exception e) {
            throw new OAuthException(e);
        }
    }
    
    private static final String RAND_BASE        = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int    RAND_BASE_LENGTH = RAND_BASE.length();

    public static final String randomString(int length) {
        Random random = new Random(System.nanoTime());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(RAND_BASE_LENGTH);
            sb.append(RAND_BASE.charAt(number));
        }
        return sb.toString();
    }
}
