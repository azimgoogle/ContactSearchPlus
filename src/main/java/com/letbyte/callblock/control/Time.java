package com.letbyte.callblock.control;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by nuc on 10/3/2015.
 */
public class Time {
    private static final int milliSeconds = 1000;
    private static final int minute = 60 * milliSeconds;
    private static final int hour = 60 * minute;
    private static final int day = 24 * hour;

    private static final String timeZone = "UTC";

    public static long getStartOfDayInMillis(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.setTimeZone(TimeZone.getTimeZone(timeZone));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getEndOfDayInMillis(long millis) {
        return getStartOfDayInMillis(millis) + day;
    }
}
