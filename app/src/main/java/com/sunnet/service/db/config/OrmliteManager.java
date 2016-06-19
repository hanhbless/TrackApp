package com.sunnet.service.db.config;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.sunnet.service.application.MyApplication;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */

public class OrmliteManager {

    private static OrmliteHelper helper;

    public static IOrmliteManager manager() {
        if (helper == null) {
            initialize(MyApplication.getContext());
        }
        return helper;
    }

    public static void initialize(Context context) {
        if (helper != null) {
            OpenHelperManager.releaseHelper();
        }
        helper = OpenHelperManager.getHelper(context, OrmliteHelper.class);
        //helper.getWritableDatabase();
    }

    // only call it when we have no long use it
    public static void release() {
        OpenHelperManager.releaseHelper();
        helper = null;
    }
}
