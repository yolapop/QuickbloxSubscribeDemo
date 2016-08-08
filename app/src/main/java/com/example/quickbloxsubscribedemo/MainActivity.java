package com.example.quickbloxsubscribedemo;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBSubscription;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

/**
 * Created by yolapop on 8/5/16.
 */
public class MainActivity extends AppCompatActivity {
    TextView tvTokens;
    Button btnSubscribe, btnUnsubscribe, btnRefreshToken;

    boolean destroyed = false;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        tvTokens = (TextView) findViewById(R.id.tv_tokens);
        btnSubscribe = (Button) findViewById(R.id.btn_subscribe);
        btnUnsubscribe = (Button) findViewById(R.id.btn_unsubscribe);
        btnRefreshToken = (Button) findViewById(R.id.btn_refresh_token);

        btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribePushNotif();
            }
        });

        btnRefreshToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllSubscription();
            }
        });

        btnUnsubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unsubscribePushNotif();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("registrationComplete"));

        QBUser user = new QBUser();
        user.setLogin("sansastark");
        user.setPassword("sansastark");
        user.setId(16099319);

        // Create quickblox session with user
        progressDialog = ProgressDialog.show(this, "Creating session", null, true, false);
        QBAuth.createSession(user, new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle params) {
                if (!destroyed) progressDialog.dismiss();
            }

            @Override
            public void onError(QBResponseException errors) {
                if (!destroyed) progressDialog.dismiss();
                Toast.makeText(MainActivity.this, errors.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        destroyed = true;
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
    }

    private void subscribePushNotif() {
        SharedPreferences preferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        boolean registered = preferences.getBoolean("registered", false);

        if (!registered) {
            progressDialog = ProgressDialog.show(this, "Subscribing to push notification...", null, true, false);
            Intent intent = new Intent(MainActivity.this, RegistrationIntentService.class);
            startService(intent);
        } else
            Toast.makeText(this, "This device is already registered", Toast.LENGTH_SHORT).show();
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    };

    private void getAllSubscription() {
        // unregister from QuickBlox
        progressDialog = ProgressDialog.show(this, "Getting subscriptions...", null, true, false);
        QBPushNotifications.getSubscriptions(new QBEntityCallback<ArrayList<QBSubscription>>() {
            @Override
            public void onSuccess(ArrayList<QBSubscription> subscriptions, Bundle args) {
                if (destroyed) return;
                StringBuilder builder = new StringBuilder();
                for(QBSubscription subscription : subscriptions){
                    builder.append(subscription.getRegistrationID());
                    builder.append("\n");
                }
                tvTokens.setText(builder.toString());
                progressDialog.dismiss();
            }

            @Override
            public void onError(QBResponseException errors) {
                errors.printStackTrace();
                if (!destroyed) progressDialog.dismiss();
                Toast.makeText(MainActivity.this, errors.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unsubscribePushNotif() {
        progressDialog = ProgressDialog.show(this, "Unsubscribing...", null, true, false);
        // unregister from QuickBlox
        QBPushNotifications.getSubscriptions(new QBEntityCallback<ArrayList<QBSubscription>>() {
            @Override
            public void onSuccess(ArrayList<QBSubscription> subscriptions, Bundle args) {

                String deviceId = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

                for(QBSubscription subscription : subscriptions){
                    if(subscription.getDevice().getId().equals(deviceId)){
                        QBPushNotifications.deleteSubscription(subscription.getId(), new QBEntityCallback<Void>() {

                            @Override
                            public void onSuccess(Void aVoid, Bundle bundle) {
                                if (!destroyed) progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                e.printStackTrace();
                                if (!destroyed) progressDialog.dismiss();
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    }
                }
            }

            @Override
            public void onError(QBResponseException errors) {
                errors.printStackTrace();
                if (!destroyed) progressDialog.dismiss();
                Toast.makeText(MainActivity.this, errors.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
