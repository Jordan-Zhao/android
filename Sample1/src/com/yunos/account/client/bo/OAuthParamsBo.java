/*
 * Copyright 2007, 2008 Netflix, Inc.
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

package com.yunos.account.client.bo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author hanqi
 */
public class OAuthParamsBo {
    public final String           requestMethod;
    public final String           url;
    private final List<OAuthPairBo> parameters = new ArrayList<OAuthPairBo>();

    public OAuthParamsBo(String requestMethod, String url, Collection<OAuthPairBo> parameters) {
        this.requestMethod = requestMethod;
        this.url = url;
        if (parameters != null) {
        	this.parameters.addAll(parameters);
        }
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getUrl() {
        return url;
    }

    public List<OAuthPairBo> getParameters() {
        return parameters;
    }

    public void addParameter(OAuthPairBo parameter) {
    	this.parameters.add(parameter);
    }
}
