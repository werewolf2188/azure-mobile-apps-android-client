package com.microsoft.windowsazure.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import com.google.android.gms.tasks.OnSuccessListener;

public class NotificationsManager {

    /**
     * Key for handler class name in local storage
     */
    private static final String NOTIFICATIONS_HANDLER_CLASS = "WAMS_NotificationsHandlerClass";

    /**
     * Key for registration id in local storage
     */
    private static final String FIREBASE_CLOUD_MESSAGING_REGISTRATION_ID = "WAMS_GoogleCloudMessagingRegistrationId";

    /**
     * NotificationsHandler instance
     */
    private static NotificationsHandler mHandler;

    /**
     * Handles notifications with the provided NotificationsHandler class
     *
     * @param context                   Application Context
     * @param notificationsHandlerClass NotificationHandler class used for handling notifications
     */
    public static <T extends NotificationsHandler> void handleNotifications(final Context context, final Class<T> notificationsHandlerClass) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    setHandler(context, notificationsHandlerClass);

                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                        @Override
                        public void onSuccess(InstanceIdResult instanceIdResult) {

                            NotificationsHandler handler = getHandler(context);

                            if (handler != null) {
                                handler.onRegistered(context);
                            }

                        }
                    });


                } catch (Exception e) {
                    Log.e("NotificationsManager", e.toString());
                }

                return null;
            }
        }.execute();
    }

    /**
     * Stops handlind notifications
     *
     * @param context Application Context
     */
    public static void stopHandlingNotifications(final Context context) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();

                    NotificationsHandler handler = getHandler(context);

                    if (handler != null ) {
                        handler.onUnregistered(context);
                    }
                } catch (Exception e) {
                    Log.e("NotificationsManager", e.toString());
                }

                return null;
            }
        }.execute();
    }

    /**
     * Retrieves the NotificationsHandler from local storage
     *
     * @param context Application Context
     */
    static NotificationsHandler getHandler(Context context) {
        if (mHandler == null) {
            SharedPreferences prefereneces = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

            String className = prefereneces.getString(NOTIFICATIONS_HANDLER_CLASS, null);
            if (className != null) {
                try {
                    Class<?> notificationsHandlerClass = Class.forName(className);
                    mHandler = (NotificationsHandler) notificationsHandlerClass.newInstance();
                } catch (Exception e) {
                    return null;
                }
            }
        }

        return mHandler;
    }

    /**
     * Stores the NotificationsHandler class in local storage
     *
     * @param notificationsHandlerClass NotificationsHandler class
     * @param context                   Application Context
     */
    private static <T extends NotificationsHandler> void setHandler(Context context, Class<T> notificationsHandlerClass) {
        SharedPreferences prefereneces = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

        Editor editor = prefereneces.edit();
        editor.putString(NOTIFICATIONS_HANDLER_CLASS, notificationsHandlerClass.getName());
        editor.commit();
    }

}
