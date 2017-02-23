/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
Apache 2.0 License

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

See the Apache Version 2.0 License for specific language governing permissions and limitations under the License.
 */

package com.microsoft.windowsazure.mobileservices.authentication;

/**
 * CustomTabsLoginState.java
 */
public class CustomTabsLoginState {

    private String mUriScheme;

    private String mCodeVerifier;

    private String mAuthenticationProvider;

    private String mAppUrl;

    private String mLoginUriPrefix;

    private String mAlternateLoginHost;

    public CustomTabsLoginState(String urlScheme, String codeVerifier, String authenticationProvider, String appUrl, String loginUriPrefix, String alternateLoginHost) {
        this.mUriScheme = urlScheme;
        this.mCodeVerifier = codeVerifier;
        this.mAuthenticationProvider = authenticationProvider;
        this.mAppUrl = appUrl;
        this.mLoginUriPrefix = loginUriPrefix;
        this.mAlternateLoginHost = alternateLoginHost;
    }

    public String getUriScheme() {
        return mUriScheme;
    }

    public void setUriScheme(String urlScheme) {
        this.mUriScheme = urlScheme;
    }

    public String getCodeVerifier() {
        return mCodeVerifier;
    }

    public void setCodeVerifier(String codeVerifier) {
        this.mCodeVerifier = codeVerifier;
    }

    public String getAuthenticationProvider() {
        return mAuthenticationProvider;
    }

    public void setAuthenticationProvider(String authenticationProvider) {
        this.mAuthenticationProvider = authenticationProvider;
    }

    public String getAppUrl() {
        return mAppUrl;
    }

    public void setAppUrl(String appUrl) {
        this.mAppUrl = appUrl;
    }

    public String getLoginUriPrefix() {
        return mLoginUriPrefix;
    }

    public void setLoginUriPrefix(String loginUriPrefix) {
        this.mLoginUriPrefix = loginUriPrefix;
    }

    public String getAlternateLoginHost() {
        return mAlternateLoginHost;
    }

    public void setAlternateLoginHost(String alternateLoginHost) {
        this.mAlternateLoginHost = alternateLoginHost;
    }
}
