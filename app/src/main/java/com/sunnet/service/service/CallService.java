/*
 *  Copyright 2012 Kobi Krasnoff
 * 
 * This file is part of Call recorder For Android.

    Call recorder For Android is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Call recorder For Android is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Call recorder For Android.  If not, see <http://www.gnu.org/licenses/>
 */
package com.sunnet.service.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Environment;
import android.os.IBinder;
import android.text.format.DateFormat;

import com.sunnet.service.db.DatabaseHelper;
import com.sunnet.service.db.entity.CallVoiceEntity;
import com.sunnet.service.log.Log;
import com.sunnet.service.task.request.RequestHelper;
import com.sunnet.service.util.Constants;
import com.sunnet.service.util.ThreadPool;
import com.sunnet.service.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class CallService extends Service {
    public static final String FILE_DIRECTORY = "recordedCalls";
    private MediaRecorder recorder = null;
    private String phoneNumber = null;

    private String fileName;
    private boolean onCall = false;
    private boolean recording = false;
    //private boolean silentMode = false;
    private boolean onForeground = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("CallService onStartCommand");
        if (intent != null) {
            int commandType = intent.getIntExtra("commandType", 0);
            if (commandType != 0) {
                if (commandType == Constants.RECORDING_ENABLED) {
                    Log.d("CallService RECORDING_ENABLED");
                    //silentMode = intent.getBooleanExtra("silentMode", true);
                    if (/*!silentMode && */phoneNumber != null && onCall
                            && !recording)
                        commandType = Constants.STATE_START_RECORDING;

                } else if (commandType == Constants.RECORDING_DISABLED) {
                    Log.d("CallService RECORDING_DISABLED");
                    //silentMode = intent.getBooleanExtra("silentMode", true);
                    if (onCall && phoneNumber != null && recording)
                        commandType = Constants.STATE_STOP_RECORDING;
                }

                if (commandType == Constants.STATE_INCOMING_NUMBER) {
                    Log.d("CallService STATE_INCOMING_NUMBER");
                    if (phoneNumber == null)
                        phoneNumber = intent.getStringExtra("phoneNumber");

                    //silentMode = intent.getBooleanExtra("silentMode", true);
                } else if (commandType == Constants.STATE_CALL_START) {
                    Log.d("CallService STATE_CALL_START");
                    onCall = true;

                    if (/*!silentMode && */phoneNumber != null && onCall
                            && !recording) {
                        startRecording(intent);
                    }
                } else if (commandType == Constants.STATE_CALL_END) {
                    Log.d("CallService STATE_CALL_END");
                    onCall = false;
                    stopAndReleaseRecorder(phoneNumber);
                    phoneNumber = null;
                    recording = false;
                    stopService();
                } else if (commandType == Constants.STATE_START_RECORDING) {
                    Log.d("CallService STATE_START_RECORDING");
                    if (/*!silentMode &&*/ phoneNumber != null && onCall) {
                        startRecording(intent);
                    }
                } else if (commandType == Constants.STATE_STOP_RECORDING) {
                    Log.d("CallService STATE_STOP_RECORDING");
                    stopAndReleaseRecorder(phoneNumber);
                    recording = false;
                }
            }
        }

        return START_STICKY;
//        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * in case it is impossible to record
     */
    private void terminateAndEraseFile() {
        Log.d("CallService terminateAndEraseFile");
        stopAndReleaseRecorder(phoneNumber);
        recording = false;
        deleteFile();
    }

    private void stopService() {
        Log.d("CallService stopService");
        stopForeground(true);
        onForeground = false;
        this.stopSelf();
    }

    private void deleteFile() {
        Log.d("CallService deleteFile");
        Utils.deleteFile(fileName);
        fileName = null;
    }

    private void stopAndReleaseRecorder(String phoneNumber) {
        if (recorder == null)
            return;
        Log.d("CallService stopAndReleaseRecorder");
        boolean recorderStopped = false;
        boolean exception = false;

        try {
            recorder.stop();
            recorderStopped = true;
        } catch (IllegalStateException e) {
            Log.e("IllegalStateException");
            e.printStackTrace();
            exception = true;
        } catch (RuntimeException e) {
            Log.e("RuntimeException");
            exception = true;
        } catch (Exception e) {
            Log.e("Exception");
            e.printStackTrace();
            exception = true;
        }
        try {
            recorder.reset();
        } catch (Exception e) {
            Log.e("Exception");
            e.printStackTrace();
            exception = true;
        }
        try {
            recorder.release();
        } catch (Exception e) {
            Log.e("Exception");
            e.printStackTrace();
            exception = true;
        }

        recorder = null;
        if (exception) {
            deleteFile();
        } else {
            Log.v("SAVE AUDIO INTO DB: " + phoneNumber);
            String phoneName = Utils.getContactName(this, phoneNumber);
            if (phoneName == null) {
                phoneName = phoneNumber;
            }
            final CallVoiceEntity entity = DatabaseHelper.createVoiceCall(phoneNumber, phoneName, fileName);
            // Save record info into database
            Log.d("FILE_SAVED: " + fileName);
            //-- Update voice call to server
            ThreadPool.doUpload(new Runnable() {
                @Override
                public void run() {
                    RequestHelper.uploadCallVoiceToServer(entity);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        Log.d("CallService onDestroy");
        stopAndReleaseRecorder(phoneNumber);
        stopService();
        super.onDestroy();
    }

    private void startRecording(Intent intent) {
        Log.d("CallService startRecording");
        boolean exception = false;
        recorder = new MediaRecorder();

        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            fileName = generateVoiceFilePath(phoneNumber);
            recorder.setOutputFile(fileName);

            OnErrorListener errorListener = new OnErrorListener() {
                public void onError(MediaRecorder arg0, int arg1, int arg2) {
                    Log.e("OnErrorListener " + arg1 + "," + arg2);
                    terminateAndEraseFile();
                }
            };
            recorder.setOnErrorListener(errorListener);

            OnInfoListener infoListener = new OnInfoListener() {
                public void onInfo(MediaRecorder arg0, int arg1, int arg2) {
                    Log.e("OnInfoListener " + arg1 + "," + arg2);
                    terminateAndEraseFile();
                }
            };
            recorder.setOnInfoListener(infoListener);

            recorder.prepare();
            // Sometimes prepare takes some time to complete
            Thread.sleep(2000);
            recorder.start();
            recording = true;
            Log.d("CallService recorderStarted");
        } catch (IllegalStateException e) {
            Log.e("IllegalStateException");
            e.printStackTrace();
            exception = true;
        } catch (IOException e) {
            Log.e("IOException");
            e.printStackTrace();
            exception = true;
        } catch (Exception e) {
            Log.e("Exception");
            e.printStackTrace();
            exception = true;
        }

        if (exception) {
            terminateAndEraseFile();
        }
    }


    /**
     * returns absolute file directory
     *
     * @return
     */
    private File getFileDir() {
        File root = android.os.Environment.getExternalStorageDirectory();
        String filepath = root.getAbsolutePath();
        return new File(filepath, FILE_DIRECTORY);
    }

    private String generateVoiceFilePath(String phoneNumber) {
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

        String myDate = new String();
        myDate = (String) DateFormat.format("yyyyMMddkkmmss", new Date());

        // Clean characters in file name
        phoneNumber = phoneNumber.replaceAll("[\\*\\+-]", "");
        if (phoneNumber.length() > 10) {
            phoneNumber.substring(phoneNumber.length() - 10,
                    phoneNumber.length());
        }

        return (file.getAbsolutePath() + "/d" + myDate + "p" + (phoneNumber == null ? "" : phoneNumber) + ".tvh");
    }
}
