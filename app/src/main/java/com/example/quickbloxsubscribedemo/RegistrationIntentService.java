package com.example.quickbloxsubscribedemo;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBNotificationChannel;
import com.quickblox.messages.model.QBSubscription;

import java.io.IOException;

/**
 * Created by yolapop on 5/30/16.
 */
public class RegistrationIntentService extends IntentService {
    public static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            /**
             * Initially this call goes out to the network to retrieve the token, subsequent calls
             * are local. R.string.gcm_defaultSenderId (the Sender ID) is typically derived from
             * google-services.json. See https://developers.google.com/cloud-messaging/android/start
             * for details on this file.
             */
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            subscribeToQuickBloxPushNotifications(token);

            SharedPreferences preferences = getSharedPreferences("default", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("registered", true);
            editor.commit();

        } catch (IOException e) {
            e.printStackTrace();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent("registrationComplete");
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    public void subscribeToQuickBloxPushNotifications(String registrationID) {
        try {
            QBSubscription subscription = new QBSubscription(QBNotificationChannel.GCM);
            subscription.setEnvironment(QBEnvironment.PRODUCTION);
            //
            String deviceId;
            final TelephonyManager mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephony.getDeviceId() != null) {
                deviceId = mTelephony.getDeviceId(); //*** use for mobiles
            } else {
                deviceId = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID); //*** use for tablets
            }
            subscription.setDeviceUdid(deviceId);
            subscription.setRegistrationID(registrationID);

            QBPushNotifications.createSubscription(subscription);
        } catch (QBResponseException e) {
            e.printStackTrace();
        }
    }
}
