/**
 * Project: yunos.oauth
 * 
 * File Created at 2013-02-28
 * 
 * Copyright 2013 yunos.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
package com.yunos.account.client.bo;

import com.yunos.account.client.exception.OAuthException;


/**
 * @author hanqi
 */
public class OAuthPairBo {
    private String key;

    private String value;

    public OAuthPairBo(String key, String value) throws OAuthException {
        if(key == null || value == null) throw new OAuthException("OAuthPair should not have NULL key or value: " + key + " = " + value);
        
        this.key = key;
        this.value = value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final OAuthPairBo that = (OAuthPairBo) obj;
        if (key == null) {
            if (that.key != null)
                return false;
        } else if (!key.equals(that.key))
            return false;
        if (value == null) {
            if (that.value != null)
                return false;
        } else if (!value.equals(that.value))
            return false;
        return true;
    }
}
