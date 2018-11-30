package com.microsoft.windowsazure.notifications;

import android.content.Context;

import android.os.Bundle;
import java.util.Map;

import com.google.firebase.messaging.RemoteMessage;

public class NotificationsBroadcastReceiver extends NotificationsHandler {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Context context = getApplicationContext();
        NotificationsHandler handler = NotificationsManager.getHandler(context);

        if (handler != null) {

            Bundle bundle = new Bundle();
            for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                bundle.putString(entry.getKey(), entry.getValue());
            }

            handler.onReceive(context, bundle );
        }
    }

}
