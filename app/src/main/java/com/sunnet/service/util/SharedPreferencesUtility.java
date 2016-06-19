package com.sunnet.service.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import com.sunnet.service.application.MyApplication;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */

public class SharedPreferencesUtility {
    Context activity;
    private SharedPreferences pref;
    private static SharedPreferencesUtility utility;
    private Editor editor;
    private boolean autoCommit = true;

    public static final String VERSION_APP_INSTALLED = "VERSION_APP_DOCTELLA";
    public static final String CONTACT_CHANGED = "CONTACT_CHANGED";
    public static final String LOCATION_LAST = "LOCATION_LAST";
    public static final String BATTERY_STATE = "BATTERY_STATE";

    public static SharedPreferencesUtility getInstance(Context context) {
        if (utility == null) {
            utility = new SharedPreferencesUtility(context.getApplicationContext());
        }
        return utility;
    }

    public static SharedPreferencesUtility getInstance() {
        return getInstance(MyApplication.getContext());
    }

    private SharedPreferencesUtility(Context activity, int mode) {
        this.autoCommit = true;
        this.activity = activity;
        pref = activity.getSharedPreferences(activity.getPackageName() + "_preferences", mode);
        editor = pref.edit();

    }

    private SharedPreferencesUtility(Context activity) {
        this.autoCommit = true;
        this.activity = activity;
        pref = activity.getSharedPreferences(activity.getPackageName() + "_preferences", Context.MODE_WORLD_WRITEABLE);
        editor = pref.edit();
    }

    private SharedPreferencesUtility(Activity activity, boolean autoCommit) {
        this.autoCommit = autoCommit;
        this.activity = activity;
        pref = PreferenceManager.getDefaultSharedPreferences(activity);
        editor = pref.edit();
    }

    public void removeValue(String key) {
        editor.remove(key);
        if (autoCommit) commit();
    }

    public void putString(String key, String value) {
        editor.putString(key, value);
        if (autoCommit) {
            commit();
        }
    }

    public String getString(String key, String defaultValue) {
        return pref.getString(key, defaultValue);
    }

    public void putInt(String key, int value) {
        editor.putInt(key, value);
        if (autoCommit) {
            commit();
        }
    }

    public int getInt(String key, int defaultValue) {
        return pref.getInt(key, defaultValue);
    }

    public void putLong(String key, long value) {
        editor.putLong(key, value);
        if (autoCommit) {
            commit();
        }
    }

    public long getLong(String key, long defaultValue) {
        return pref.getLong(key, defaultValue);
    }

    public void commit() {
        editor.commit();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return pref.getBoolean(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        if (autoCommit) {
            commit();
        }
    }

    public void putDouble(String key, double value) {
        editor.putString(key, String.valueOf(value));
        if (autoCommit) {
            commit();
        }
    }

    public double getDouble(String key, double defaultValue) {
        double value = 0;
        String valueStr = pref.getString(key, "0");
        if (valueStr != null) {
            value = Double.valueOf(valueStr);
        }
        return value;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public boolean containsKey(String key) {
        return pref.contains(key);
    }

}
