package com.letbyte.contact.control;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by hp on 2/25/2015.
 */

public class Control {

    private static final String TAG = "ControlTag";

    private static final String NULL = null;
    private static final String pattern = "MMMM dd, yyyy, hh:mm aaa";
    private static final String patternToday = "hh:mm aaa";
    private static final String patternDay = "EEEE";
    private static final String patternDate = "MM/dd/yy";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(pattern.toString());
    private static final SimpleDateFormat dateFormatToday = new SimpleDateFormat(patternToday.toString());
    private static final SimpleDateFormat dateFormatWeek = new SimpleDateFormat(patternDay.toString());
    private static final SimpleDateFormat dateFormatDate = new SimpleDateFormat(patternDate.toString());

    private static final int sNameLength = 3;
    private static final int sPasswordLength = 6;


    public static void log(String text) {
        Log.i(TAG, text);
    }

    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public static boolean getIsNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        return isConnected;
    }


    public static String buildUid(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}