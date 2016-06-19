package com.sunnet.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sunnet.service.log.Log;
import com.sunnet.service.service.LocationService;
import com.sunnet.service.util.Utils;

public class LocationStatusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("LocationStatusReceiver called");
        if (Utils.checkLocationService(context))
            context.startService(new Intent(context, LocationService.class));
    }
}
