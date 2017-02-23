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
import android.os.Bundle;

import com.google.gson.Gson;

import static com.microsoft.windowsazure.mobileservices.authentication.CustomTabsLoginManager.*;

public class CustomTabsIntermediateActivity extends Activity {

    private boolean mLoginInProgress = false;

    private String mUserId;

    private String mAuthenticationToken;

    private String mErrorMessage;

    private Intent mLoginIntent;

    private CustomTabsLoginState mLoginState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            this.extractState(getIntent().getExtras());
        } else {
            this.extractState(savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_LOGIN_IN_PROGRESS, mLoginInProgress);
        outState.putString(KEY_LOGIN_USER_ID, mUserId);
        outState.putString(KEY_LOGIN_ERROR, mErrorMessage);
        outState.putString(KEY_LOGIN_AUTHENTICATION_TOKEN, mAuthenticationToken);
        outState.putParcelable(KEY_LOGIN_INTENT, mLoginIntent);

        outState.putString(KEY_LOGIN_STATE, new Gson().toJson(mLoginState));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if (intent.getExtras() != null) {
            mUserId = intent.getExtras().getString(KEY_LOGIN_USER_ID);
            mAuthenticationToken = intent.getExtras().getString(KEY_LOGIN_AUTHENTICATION_TOKEN);
            mErrorMessage = intent.getExtras().getString(KEY_LOGIN_ERROR);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mLoginInProgress) {
            if (mLoginIntent != null && mLoginState != null) {
                Intent i = new Intent(this, CustomTabsLoginActivity.class);

                i.putExtra(KEY_LOGIN_INTENT, mLoginIntent);
                String loginStateJson = new Gson().toJson(mLoginState);
                i.putExtra(KEY_LOGIN_STATE, loginStateJson);
                startActivity(i);

                mLoginInProgress = true;
            } else {
                Intent data = new Intent();
                data.putExtra(KEY_LOGIN_ERROR, LoginRequestAsyncTask.AUTHENTICATION_ERROR_MESSAGE);
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        } else {
            Intent data = new Intent();
            if (mUserId != null && mAuthenticationToken != null) {
                data.putExtra(KEY_LOGIN_USER_ID, mUserId);
                data.putExtra(KEY_LOGIN_AUTHENTICATION_TOKEN, mAuthenticationToken);
            } else {
                data.putExtra(KEY_LOGIN_ERROR, mErrorMessage != null ? mErrorMessage : LoginRequestAsyncTask.AUTHENTICATION_ERROR_MESSAGE);
            }
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

    public static Intent createLoginCompletionIntent(Context context, MobileServiceUser user, String errorMessage) {
        Intent intent = new Intent(context, CustomTabsIntermediateActivity.class);

        if (user != null) {
            intent.putExtra(KEY_LOGIN_USER_ID, user.getUserId());
            intent.putExtra(KEY_LOGIN_AUTHENTICATION_TOKEN, user.getAuthenticationToken());
        }

        if (errorMessage != null) {
            intent.putExtra(KEY_LOGIN_ERROR, errorMessage);
        }

        // TODO: add doc explain why use these flags
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    private void extractState(Bundle state) {
        if (state != null) {
            mLoginInProgress = state.getBoolean(KEY_LOGIN_IN_PROGRESS);
            mUserId = state.getString(KEY_LOGIN_USER_ID);
            mAuthenticationToken = state.getString(KEY_LOGIN_AUTHENTICATION_TOKEN);
            mErrorMessage = state.getString(KEY_LOGIN_ERROR);
            mLoginIntent = state.getParcelable(KEY_LOGIN_INTENT);

            String loginStateJson = state.getString(KEY_LOGIN_STATE);
            mLoginState = new Gson().fromJson(loginStateJson, CustomTabsLoginState.class);
        }
    }
}
