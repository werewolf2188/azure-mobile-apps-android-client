//------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
//------------------------------------------------------------

package com.microsoft.windowsazure.mobileservices.zumoe2etestapp;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.microsoft.windowsazure.mobileservices.zumoe2etestapp.framework.PushMessageManager;

public class ListenerService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String message = remoteMessage.getData().get("message");
        JsonElement msgObj = new JsonParser().parse(message);
        PushMessageManager.instance.AddMessage(msgObj);
    }
}
