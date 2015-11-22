package com.letbyte.callblock.control;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by nuc on 2/25/2015.
 */
public class Util {

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


    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }


    public static String getCurrentDate() {
        return dateFormat.format(getCurrentTime());
    }

    public static String getDateFromTime(long time) {
        return dateFormat.format(time);
    }

    public static Date getDateFromTimeInDateFormat(long time) {
        return new Date(time);
    }

    public static String getTodayTimeFromTime(long time) {
        return dateFormatToday.format(time);
    }

    public static String getDayFromTime(long date) {
        return dateFormatWeek.format(date);
    }

    public static String getShortDateFromTime(long date) {
        return dateFormatDate.format(date);
    }

    public static Map<TimeUnit, Long> getDateAsMapWithTimeUnit(long newestDate, long oldestDate) {

        List<TimeUnit> units = new ArrayList<>(EnumSet.allOf(TimeUnit.class));
        Collections.reverse(units);

        Map<TimeUnit, Long> result = new LinkedHashMap<>();
        long milliesRest = newestDate - oldestDate;

        for (TimeUnit unit : units) {

            long diff = unit.convert(milliesRest, TimeUnit.MILLISECONDS);
            long diffInMilliesForUnit = unit.toMillis(diff);
            milliesRest = milliesRest - diffInMilliesForUnit;
            result.put(unit, diff);
        }

        return result;
    }

    public static String buildUid(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static boolean getIsToday(Map<TimeUnit, Long> dateMap) {
        boolean isToday = false;

        if (dateMap != null) {
            isToday = dateMap.get(TimeUnit.DAYS) == 0;
        }

        return isToday;
    }

    public static boolean getIsWeek(Map<TimeUnit, Long> dateMap) {
        boolean isWeek = false;

        if (dateMap != null) {
            isWeek = dateMap.get(TimeUnit.DAYS) > 0 && dateMap.get(TimeUnit.DAYS) <= 7;
        }

        return isWeek;
    }


    /**
     * ClipboardManager
     */
    public static boolean getIsClipped(Context context) {
        boolean isClipped;

        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (!(clipboard.hasPrimaryClip())) {
            isClipped = false;
        } else
            isClipped = clipboard.getPrimaryClipDescription().hasMimeType(android.content.ClipDescription.MIMETYPE_TEXT_PLAIN);

        return isClipped;
    }

    public static CharSequence getClippedData(Context context) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
        return item.getText();
    }

    public static <t> boolean getIsEqual(t t1, t t2) {
        if (t1 instanceof Object) {
            return t1 != null && t1.equals(t2);
        }
        return t1 == t2;
    }

    public static boolean isValidUrl(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }

    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && name.length() >= sNameLength;
    }

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= sPasswordLength;
    }

    public static void log(String text) {
        Log.i(TAG, text);
    }


    public static boolean getIsNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        return isConnected;
    }

    public static boolean isOnline() {

        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String toDelayTime(long time) {

        StringBuilder builder = new StringBuilder();

        Map<TimeUnit, Long> timeMap = getDateAsMapWithTimeUnit(getCurrentTime(), time * Constant.MILLI);

        if (timeMap.containsKey(TimeUnit.HOURS)) {

            long hour = timeMap.get(TimeUnit.HOURS);

            if (hour > 0) {
                builder.append(hour);
                builder.append(" hour");
                builder.append(hour > 1 ? "s" : "");
            }
        }

        if (timeMap.containsKey(TimeUnit.MINUTES)) {
            long minute = timeMap.get(TimeUnit.MINUTES);

            if (minute > 0) {
                builder.append(" " + minute);
                builder.append(" minute");
                builder.append(minute > 1 ? "s" : "");
            }
        }

        builder.append(" ago");

        return builder.toString();
    }

    public static boolean isEmpty(String content) {
        return TextUtils.isEmpty(content);
    }

    public static boolean isM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static int getColor(Resources resources, int colorId) {
        if (isM()) return resources.getColor(colorId, null);
        return resources.getColor(colorId);
    }

    public static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * Constant.MILLI);
        } catch (InterruptedException e) {

        }
    }
}

