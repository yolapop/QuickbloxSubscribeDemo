package com.example.quickbloxsubscribedemo;

import android.app.Application;

import com.quickblox.core.QBSettings;

/**
 * Created by yolapop on 8/5/16.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Set your QuickBlox application credentials here
        String APP_ID = "43597";
        String AUTH_KEY = "M63sNGNzsOwQJW5";
        String AUTH_SECRET = "a2EFD8P-MPKrRhX";
        String ACCOUNT_KEY = "HpyLXxUC7H486e4eizBz";

        QBSettings.getInstance().init(getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }
}
