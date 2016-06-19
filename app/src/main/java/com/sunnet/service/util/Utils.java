package com.sunnet.service.util;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.display.DisplayManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.Display;

import com.sunnet.service.application.MyApplication;
import com.sunnet.service.db.entity.CallVoiceEntity;
import com.sunnet.service.db.entity.LocationEntity;
import com.sunnet.service.db.entity.SMSEntity;
import com.sunnet.service.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.channels.FileChannel;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class Utils {
    public static final int MEDIA_MOUNTED = 0;
    public static final int MEDIA_MOUNTED_READ_ONLY = 1;
    public static final int NO_MEDIA = 2;

    private static final String CRYPT_KEY = "PgdwQqFDJk5AkSVLCFgr9dhZGLCbTVXQ";

    public static String hashMac(String paramString, String secretKey)
            throws SignatureException {
        try {
            SecretKeySpec localSecretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(), "hmacSHA512");
            Mac localMac = Mac.getInstance(localSecretKeySpec.getAlgorithm());
            localMac.init(localSecretKeySpec);

            return toHexString(localMac.doFinal(paramString.getBytes()));
        } catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
            throw new SignatureException(
                    "error building signature, no such algorithm in device hmacSHA512");
        } catch (InvalidKeyException localInvalidKeyException) {
        }
        throw new SignatureException(
                "error building signature, invalid key hmacSHA512");
    }

    public static String hashMac(String paramString) {
        try {
            return hashMac(paramString, CRYPT_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String toHexString(byte[] paramArrayOfByte) {
        StringBuilder localStringBuilder = new StringBuilder(
                2 * paramArrayOfByte.length);
        Formatter localFormatter = new Formatter(localStringBuilder);
        int i = paramArrayOfByte.length;
        for (int j = 0; ; j++) {
            if (j >= i) {
                localFormatter.close();
                return localStringBuilder.toString();
            }
            byte b = paramArrayOfByte[j];
            Object[] arrayOfObject = new Object[1];
            arrayOfObject[0] = Byte.valueOf(b);
            localFormatter.format("%02x", arrayOfObject);
        }
    }

    /**
     * Check location service enable/disable
     */
    /**
     * check location is enable
     */
    public static boolean checkLocationService(Context context) {
        boolean result;
        LocationManager lm = null;
        boolean gps_enabled = false, network_enabled = false;
        if (lm == null)
            lm = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = lm
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    public static boolean isNetworkProviderEnable(Context context) {
        LocationManager lm = null;
        boolean gps_enabled = false, network_enabled = false;
        if (lm == null)
            lm = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);
        try {
            network_enabled = lm
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            return network_enabled;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name

            if (ipAddr.equals("")) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            return false;
        }

    }

    /**
     * checks if an external memory card is available
     *
     * @return
     */
    public static int updateExternalStorageState() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return MEDIA_MOUNTED;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return MEDIA_MOUNTED_READ_ONLY;
        } else {
            return NO_MEDIA;
        }
    }

    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    public static ZipInputStream getFileFromZip(InputStream zipFileStream) throws IOException {
        ZipInputStream zis = new ZipInputStream(zipFileStream);
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            Log.w("extracting file: '" + ze.getName() + "'...");
            return zis;
        }
        return null;
    }

    public static void writeExtractedFileToDisk(InputStream in, OutputStream outs) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) > 0) {
            outs.write(buffer, 0, length);
        }
        outs.flush();
        outs.close();
        in.close();
    }

    public static boolean newFolderIfNotExist(String dir) {
        File file = new File(dir);
        boolean bool = false;
        if (!file.exists()) {
            bool = file.mkdir();
        }

        return bool;
    }

    public static boolean existFile(String dir) {
        File file = new File(dir);
        return file.exists();
    }

    public static void deleteFile(String path) {
        if (Utils.isEmptyString(path))
            return;
        File f = new File(path);
        f.delete();
    }

    public static boolean isEmptyString(String string) {
        if (string == null || string.trim().equals("") || string.trim().equals("null") || string.trim().length() <= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static String getStringLocationId(List<LocationEntity> list) {
        StringBuilder buffer = new StringBuilder();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            buffer.append("'" + list.get(i).getId() + "'");
            if (i < size - 1)
                buffer.append(",");
        }
        if (buffer.length() > 0)
            return buffer.toString();

        return "";
    }

    public static String getStringSmsId(List<SMSEntity> list) {
        StringBuilder buffer = new StringBuilder();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            buffer.append("'" + list.get(i).getId() + "'");
            if (i < size - 1)
                buffer.append(",");
        }
        if (buffer.length() > 0)
            return buffer.toString();

        return "";
    }

    public static String getStringCallVoiceId(List<CallVoiceEntity> list) {
        StringBuilder buffer = new StringBuilder();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            buffer.append("'" + list.get(i).getId() + "'");
            if (i < size - 1)
                buffer.append(",");
        }
        if (buffer.length() > 0)
            return buffer.toString();

        return "";
    }

    public static void deleteDir(String path) {
        deleteDir(new File(path));
    }

    public static void deleteDir(File dir) {
        if (dir.isFile()) {
            dir.delete();
            Log.i("hanh delete file: " + dir.getPath());
            return;
        }
        File[] files = dir.listFiles();
        if (files == null)
            return;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            if (file.isDirectory()) {
                deleteDir(file);
            } else {
                boolean deleted = file.delete();
                if (!deleted)
                    continue;
            }
        }
        dir.delete();
        Log.i("hanh delete dir: " + dir.getPath());
    }

    public static boolean renameFile(String pathFileOld, String pathFileNew) {
        File fOld = new File(pathFileOld);
        File fNew = new File(pathFileNew);
        return fOld.renameTo(fNew);
    }

    public static String getPhoneNumber() {
        TelephonyManager tMgr = (TelephonyManager) MyApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String phone = tMgr.getLine1Number();
        if (Utils.isEmptyString(phone)) {
            phone = ConfigApi.DEFAULT_PHONE_NUMBER;
        }
        return phone;
    }

    public static boolean isNetworkAvailable(Context appContext) {
        if (appContext == null)
            return false;
        Context context = appContext.getApplicationContext();
        try {
            if (context != null) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager
                        .getActiveNetworkInfo();
                return activeNetworkInfo != null
                        && activeNetworkInfo.isConnected();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static boolean isConnectedViaWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) MyApplication.getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    public static boolean isNetworkAvailable() {
        return isNetworkAvailable(MyApplication.getContext());
    }

    public static boolean isFullTextSearch(String originText, String inputText) {

        if (originText == null && inputText == null)
            return true;

        if ((originText == null && inputText != null) || (inputText == null && originText != null))
            return false;

        originText = originText.trim().toLowerCase(Locale.ENGLISH);
        inputText = inputText.trim().toLowerCase(Locale.ENGLISH);

        return originText.compareTo(inputText) == 0;
    }

    public static boolean isFullTextSearch2(String originText, String inputText) {

        if (originText == null)
            originText = "";
        if (inputText == null)
            inputText = "";

        originText = originText.trim().toLowerCase(Locale.ENGLISH);
        inputText = inputText.trim().toLowerCase(Locale.ENGLISH);

        return originText.compareTo(inputText) == 0;
    }

    /**
     * File
     */
    public static String getRootFile(String folder) {
        File root;
        if (MyApplication.getContext().getExternalCacheDir() == null)
            root = Environment.getExternalStorageDirectory();
        else
            root = MyApplication.getContext().getExternalCacheDir();

        String filepath = root.getAbsolutePath();
        File file = new File(filepath, folder);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file.getAbsolutePath();
    }

    public static String getPathFileStorage(String folder, String fileName) {
        return Environment.getExternalStorageDirectory() + "/" + folder + "/" + fileName;
    }

    public static String getPathFileStore(String folder, String fileName) {
        return getRootFile(folder) + "/" + fileName;
    }

    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }

    public static void copyFile(String fileSource, String fileDist) {
        try {
            FileInputStream fInputStream = new FileInputStream(fileSource);
            FileOutputStream fOutStream = new FileOutputStream(fileDist);
            copyFile(fInputStream, fOutStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*************************************************************************************************/
    public static void showAlertDialog(Context context, String title,
                                       String message, boolean isSuccess, final Handler handler) {
        if (context != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setIcon(
                    !isSuccess ? android.R.drawable.ic_dialog_alert
                            : android.R.drawable.ic_dialog_info
            ).setTitle(title)
                    .setMessage(message).setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (handler != null) {
                                handler.sendEmptyMessage(4);
                            }
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    /***********************************************************************************************/
    public static float getBatteryLevel() {
        Intent batteryIntent = MyApplication.getContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) {
            return 50.0f;
        }
        return ((float) level / (float) scale) * 100.0f;
    }

    public static boolean canBatteryLow() {
        return getBatteryLevel() <= 14f;
    }


    /**
     * Is the screen of the device on.
     *
     * @param context the context
     * @return true when (at least one) screen is on
     */
    public static boolean isScreenOn(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
        }
    }
}
