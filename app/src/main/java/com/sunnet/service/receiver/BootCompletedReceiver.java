package com.sunnet.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.sunnet.service.log.Log;
import com.sunnet.service.service.LocationService;
import com.sunnet.service.service.NewScreenShotService;
import com.sunnet.service.service.SchedulingService;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            //-- Start scheduling upload information to Server
//            new AlarmReceiver().setAlarm(context);
            //-- Start scheduling screenshot
//            NewScreenShotService.star
//            new AlarmScreenShotReceiver().setAlarm(context);
            //-- Start service update location

            context.startService(new Intent(context, SchedulingService.class));
            context.startService(new Intent(context, NewScreenShotService.class));
            context.startService(new Intent(context, LocationService.class));

            Log.d(BootCompletedReceiver.class.getName() + " device on boot completed");
            if (Log.IS_DEBUG) {
                Toast.makeText(context, "device on boot completed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
