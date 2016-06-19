package com.sunnet.service.log;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */

public class Log {

    public static boolean IS_DEBUG = false;

    public static String tag = "TRACK-APP--------> ";

    public static void obj(Object object) {
        try {
            if (IS_DEBUG)
                android.util.Log.i(tag, String.valueOf(object));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void obj(String desc, Object object) {
        try {
            if (IS_DEBUG)
                android.util.Log.i(tag + " " + desc + ": ", String.valueOf(object));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void i(String msg) {
        if (IS_DEBUG)
            android.util.Log.i(tag, msg);
    }

    public static void d(String msg) {
        if (IS_DEBUG)
            android.util.Log.d(tag, msg);
    }

    public static void e(String msg) {
        if (IS_DEBUG)
            android.util.Log.e(tag, msg);
    }

    public static void v(String msg) {
        if (IS_DEBUG)
            android.util.Log.v(tag, msg);
    }

    public static void w(String msg) {
        if (IS_DEBUG)
            android.util.Log.w(tag, msg);
    }

    public static void i(String msg, Throwable tr) {
        if (IS_DEBUG)
            android.util.Log.i(tag, msg, tr);
    }

    public static void d(String msg, Throwable tr) {
        if (IS_DEBUG)
            android.util.Log.d(tag, msg, tr);
    }

    public static void e(String msg, Throwable tr) {
        if (IS_DEBUG)
            android.util.Log.e(tag, msg, tr);
    }

    public static void v(String msg, Throwable tr) {
        if (IS_DEBUG)
            android.util.Log.v(tag, msg, tr);
    }

    public static void w(String msg, Throwable tr) {
        if (IS_DEBUG)
            android.util.Log.w(tag, msg, tr);
    }


}
