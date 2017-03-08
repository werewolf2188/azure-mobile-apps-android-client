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

package com.microsoft.windowsazure.mobileservices.zumoe2elogintestapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;


import java.net.MalformedURLException;

public class MainActivity extends Activity {

    public static final int LOGIN_REQUEST_CODE_GOOGLE = 1;
    public static final int LOGIN_REQUEST_CODE_FACEBOOK = 2;
    public static final int LOGIN_REQUEST_CODE_MSA = 3;
    public static final int LOGIN_REQUEST_CODE_AAD = 4;
    public static final int LOGIN_REQUEST_CODE_TWITTER = 5;

    public static final String URL_SCHEME = "zumoe2etestapp";

    private MobileServiceClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_main);
        // Create the client instance, using the provided mobile app URL.
        try {
            mClient = new MobileServiceClient("https://dihei-e2e-app.azurewebsites.net", this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Button googleLoginButton = (Button) findViewById(R.id.buttonGoogleLogin);
        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClient.login("Google", URL_SCHEME, LOGIN_REQUEST_CODE_GOOGLE);
            }
        });

        Button facebookLoginButton = (Button) findViewById(R.id.buttonFacebookLogin);
        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClient.login("Facebook", URL_SCHEME, LOGIN_REQUEST_CODE_FACEBOOK);
            }
        });

        Button msaLoginButton = (Button) findViewById(R.id.buttonMSALogin);
        msaLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClient.login("microsoftaccount", URL_SCHEME, LOGIN_REQUEST_CODE_MSA);
            }
        });

        Button aadLoginButton = (Button) findViewById(R.id.buttonAADLogin);
        aadLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClient.login("AAD", URL_SCHEME, LOGIN_REQUEST_CODE_AAD);
            }
        });

        Button twitterLoginButton = (Button) findViewById(R.id.buttonTwitterLogin);
        twitterLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClient.login("Twitter", URL_SCHEME, LOGIN_REQUEST_CODE_TWITTER);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        String provider = findProviderFromLoginRequestCode(requestCode);
        if (resultCode == RESULT_OK) {
            MobileServiceUser user = MobileServiceClient.getMobileServiceUserFromLoginResult(data);
            if (user != null) {
                mClient.setCurrentUser(user);
                displayResultOnLoginSuccess(provider);
            } else {
                String detailedErrorMessage = data.getStringExtra("error");
                displayMessageLoginFailure(provider, detailedErrorMessage);
            }
        }
    }

    private void displayResultOnLoginSuccess(String provider) {
        String text = String.format("%s Login succeeded.\nUserId: %s, authenticationToken: %s",
                provider,
                mClient.getCurrentUser().getUserId(),
                mClient.getCurrentUser().getAuthenticationToken());

        int textViewId = findTextViewIdFromProvider(provider);
        TextView textView = (TextView) findViewById(textViewId);
        textView.setText(text);
    }

    private void displayMessageLoginFailure(String provider, String errorMessage) {
        String text = String.format("%s Login failed.\nError message: %s", provider, errorMessage);

        int textViewId = findTextViewIdFromProvider(provider);
        TextView textView = (TextView) findViewById(textViewId);
        textView.setText(text);
    }

    private String findProviderFromLoginRequestCode(int requestCode) {
        String provider;
        switch (requestCode) {
            case LOGIN_REQUEST_CODE_GOOGLE:
                provider = "Google";
                break;
            case LOGIN_REQUEST_CODE_FACEBOOK:
                provider = "Facebook";
                break;
            case LOGIN_REQUEST_CODE_MSA:
                provider = "MSA";
                break;
            case LOGIN_REQUEST_CODE_AAD:
                provider = "AAD";
                break;
            case LOGIN_REQUEST_CODE_TWITTER:
                provider = "Twitter";
                break;
            default:
                throw new IllegalArgumentException("request code does not match any provider");
        }
        return provider;
    }

    private int findTextViewIdFromProvider(String provider) {
        int textViewId;
        switch (provider) {
            case "Google":
                textViewId = R.id.textViewGoogleLogin;
                break;
            case "Facebook":
                textViewId = R.id.textViewFacebookLogin;
                break;
            case "MSA":
                textViewId = R.id.textViewMSALogin;
                break;
            case "AAD":
                textViewId = R.id.textViewAADLogin;
                break;
            case "Twitter":
                textViewId = R.id.textViewTwitterLogin;
                break;
            default:
                throw new IllegalArgumentException("provider is not supported");
        }
        return textViewId;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mClient.getCustomTabsLoginManager().dispose();
    }
}
