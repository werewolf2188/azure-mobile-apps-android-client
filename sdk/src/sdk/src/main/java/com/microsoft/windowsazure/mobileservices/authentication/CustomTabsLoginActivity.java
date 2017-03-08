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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.Gson;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.UserAuthenticationCallback;
import com.microsoft.windowsazure.mobileservices.http.MobileServiceConnection;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequestImpl;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;

import java.net.URL;

import static com.microsoft.windowsazure.mobileservices.authentication.CustomTabsLoginManager.*;

/**
 * Activity working together with {@link CustomTabsIntermediateActivity} to handles
 * the login flow using Chrome Custom Tabs or a regular browser. When Chrome Custom Tabs are not
 * available on the device, fallback to browser. In the Custom Tabs login flow, this activity instance
 * is started by {@link CustomTabsIntermediateActivity}. The same instance is started by
 * {@link RedirectUrlActivity} again in the code exchange process.
 */
public class CustomTabsLoginActivity extends Activity {

    private boolean mLoginInProgress = false;

    private CustomTabsLoginState mLoginState;

    private Intent mLoginIntent;

    private MobileServiceClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            extractState(getIntent().getExtras());
        } else {
            extractState(savedInstanceState);
        }
    }

    private void extractState(Bundle state) {
        if (state != null) {
            mLoginInProgress = state.getBoolean(KEY_LOGIN_IN_PROGRESS);
            mLoginIntent = state.getParcelable(KEY_LOGIN_INTENT);
            mLoginState = new Gson().fromJson(state.getString(KEY_LOGIN_STATE), CustomTabsLoginState.class);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri redirectUri = intent.getData();
        if (redirectUri != null) {
            setIntent(intent);
        } else {
            finishLoginFlow(null, TokenRequestAsyncTask.AUTHENTICATION_ERROR_MESSAGE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_LOGIN_IN_PROGRESS, mLoginInProgress);
        outState.putParcelable(KEY_LOGIN_INTENT, mLoginIntent);
        outState.putString(KEY_LOGIN_STATE, new Gson().toJson(mLoginState));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        extractState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean loginFlowStatus = false;

        // Note that CustomTabsLoginActivity launchmode is singleTask. When this activity starts up
        // the first time during the login flow, beginLoginFlow() will start the mLoginIntent to
        // present user with login UI from the authentication provider. The login UI will be opened
        // in Chrome Custom Tabs if com.chrome.customtabs is available. When EasyAuth server calls
        // back RedirectUrlActivity with authorization code, this instance of activity will be launched
        // again. At this time, onNewIntent() will be called following with onResume(). Perform the
        // code exchange call with the EasyAuth server now to continue the login flow.

        if (!mLoginInProgress) {
            loginFlowStatus = beginLoginFlow();
        } else {
            loginFlowStatus = exchangeAuthCodeForToken();
        }

        if (!loginFlowStatus) {
            finishLoginFlow(null, TokenRequestAsyncTask.AUTHENTICATION_ERROR_MESSAGE);
        }
    }

    /**
     * Begin the login flow. Invoke the EasyAuth server login endpoint
     * If Chrome Custom Tabs are available on this device, a Custom Tab will be launched
     * to open the login UI. If Chrome Custom Tabs is not available, will fall back to
     * open in browser.
     * @return login flow status where false means login failed at this point.
     */
    private boolean beginLoginFlow() {

        if (mLoginIntent != null) {
            startActivity(mLoginIntent);
            mLoginInProgress = true;
            return true;
        }
        return false;
    }

    /**
     * Finish the login flow. Return an authenticated MobileServiceUser back to
     * the caller if login succeeded. In the event of login failure, errorMessage
     * provides high level information about the failure.
     * @param user Authenticated MobileService user to return
     * @param errorMessage Error message
     */
    private void finishLoginFlow(MobileServiceUser user, String errorMessage) {
        Intent intent = CustomTabsIntermediateActivity.createLoginCompletionIntent(this, user, errorMessage);
        startActivity(intent);
        finish();
    }

    /**
     * Resume the login flow - perform code exchange with the EasyAuth server
     * @return login flow status where false means login failed at this point
     */
    private boolean exchangeAuthCodeForToken() {

        boolean loginFlowStatus = false;

        try {
            mClient = new MobileServiceClient(mLoginState.getAppUrl(), this);
            mClient.setLoginUriPrefix(mLoginState.getLoginUriPrefix());
            URL alternateLoginHost = mLoginState.getAlternateLoginHost() != null ? new URL(mLoginState.getAlternateLoginHost()) : null;
            mClient.setAlternateLoginHost(alternateLoginHost);
        } catch (Exception ex) {
            return false;
        }

        Uri redirectUrl = getIntent().getData();

        if (mLoginState != null && redirectUrl != null) {

            if (CustomTabsLoginManager.isRedirectURLValid(redirectUrl, mLoginState.getUriScheme())) {

                String authorizationCode = CustomTabsLoginManager.authorizationCodeFromRedirectUrl(redirectUrl);

                if (authorizationCode != null) {

                    String codeExchangeUrl = CustomTabsLoginManager.buildCodeExchangeUrl(mLoginState, authorizationCode);

                    if (codeExchangeUrl != null) {
                        loginFlowStatus = true;

                        this.performCodeExchange(codeExchangeUrl, new UserAuthenticationCallback() {

                            @Override
                            public void onCompleted(MobileServiceUser user, Exception exception, ServiceFilterResponse response) {
                                String errorMessage = null;
                                if (exception instanceof MobileServiceException) {
                                    if (exception.getCause() != null) {
                                        errorMessage = exception.getCause().getMessage();
                                    }
                                }

                                finishLoginFlow(user, errorMessage);
                            }
                        });
                    }
                }
            }
        }

        return loginFlowStatus;
    }

    protected void performCodeExchange(String codeExchangeUrl, final UserAuthenticationCallback callback) {

        ServiceFilterRequest request = ServiceFilterRequestImpl.get(mClient.getOkHttpClientFactory(), codeExchangeUrl);

        MobileServiceConnection connection = mClient.createConnection();

        TokenRequestAsyncTask task = new TokenRequestAsyncTask(request, connection);

        task.executeTask();

        SettableFuture<MobileServiceUser> future = task.getMobileServiceUserFuture();

        Futures.addCallback(future, new FutureCallback<MobileServiceUser>() {
            @Override
            public void onFailure(Throwable exception) {
                if (exception instanceof Exception) {
                    callback.onCompleted(null, (Exception) exception, MobileServiceException.getServiceResponse(exception));
                } else {
                    callback.onCompleted(null, new Exception(exception), MobileServiceException.getServiceResponse(exception));
                }
            }

            @Override
            public void onSuccess(MobileServiceUser user) {
                callback.onCompleted(user, null, null);
            }
        });
    }

    protected MobileServiceClient getMobileServiceClient() {
        return mClient;
    }

    protected void setMobileServiceClient(MobileServiceClient client) {
        this.mClient = client;
    }

    protected CustomTabsLoginState getLoginState() {
        return mLoginState;
    }

    protected void setLoginState(CustomTabsLoginState loginState) {
        this.mLoginState = loginState;
    }
}
