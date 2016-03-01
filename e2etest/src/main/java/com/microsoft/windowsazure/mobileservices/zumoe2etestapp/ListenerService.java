//------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
//------------------------------------------------------------

package com.microsoft.windowsazure.mobileservices.zumoe2etestapp;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.microsoft.windowsazure.mobileservices.zumoe2etestapp.framework.PushMessageManager;

public class ListenerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.get("message").toString();
        JsonElement msgObj = new JsonParser().parse(message);
        PushMessageManager.instance.AddMessage(msgObj);
    }
}
