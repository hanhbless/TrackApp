package com.sunnet.service.application;

import android.app.Application;
import android.content.Context;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class MyApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mContext = getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }
}
