package com.sunnet.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sunnet.service.log.Log;
import com.sunnet.service.util.SharedPreferencesUtility;

public class BatteryLevelReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Log.e(BatteryLevelReceiver.class.getSimpleName() + " Battery state: " + intent.getAction());
            SharedPreferencesUtility.getInstance().putString(
                    SharedPreferencesUtility.BATTERY_STATE, intent.getAction());
        }
    }
}
