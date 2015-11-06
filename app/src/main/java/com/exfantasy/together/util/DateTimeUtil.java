package com.exfantasy.together.util;

import java.util.Calendar;

/**
 * Created by Tommy on 2015/11/4.
 */
public class DateTimeUtil {


    public static int dateValue(Calendar cal) {
        return cal.get(Calendar.YEAR) * 10000 +
                (cal.get(Calendar.MONTH) + 1) * 100 + cal.get(Calendar.DAY_OF_MONTH);
    }

    public static int timeValue(Calendar cal) {
        return cal.get(Calendar.HOUR_OF_DAY) * 10000000 +
                cal.get(Calendar.MINUTE) * 100000 + cal.get(Calendar.SECOND) * 1000 +
                cal.get(Calendar.MILLISECOND);
    }

    public static long dateTimeValue(Calendar cal) {
        return cal.get(Calendar.YEAR) * 10000000000000L +
                (cal.get(Calendar.MONTH) + 1) * 100000000000L +
                cal.get(Calendar.DAY_OF_MONTH) * 1000000000L +
                cal.get(Calendar.HOUR_OF_DAY) * 10000000L +
                cal.get(Calendar.MINUTE) * 100000L +
                cal.get(Calendar.SECOND) * 1000L + cal.get(Calendar.MILLISECOND);
    }

    public static Calendar parseDateValue(int date) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, date / 10000);
        cal.set(Calendar.MONTH, (date % 10000) / 100 - 1);
        cal.set(Calendar.DAY_OF_MONTH, date % 100);
        return cal;
    }

    public static Calendar parseTimeValue(int time) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, time / 10000000);
        cal.set(Calendar.MINUTE, (time % 10000000) / 100000);
        cal.set(Calendar.SECOND, (time % 100000) / 1000);
        cal.set(Calendar.MILLISECOND, time % 1000);
        return cal;
    }

    public static Calendar parseDateTimeValue(long dateTime) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, (int) (dateTime / 10000000000000L));
        cal.set(Calendar.MONTH, (int) ((dateTime % 10000000000000L) / 100000000000L) - 1);
        cal.set(Calendar.DAY_OF_MONTH, (int) ((dateTime % 100000000000L) / 1000000000));
        cal.set(Calendar.HOUR_OF_DAY, (int) ((dateTime % 1000000000) / 10000000));
        cal.set(Calendar.MINUTE, (int) ((dateTime % 10000000) / 100000));
        cal.set(Calendar.SECOND, (int) ((dateTime % 100000) / 1000));
        cal.set(Calendar.MILLISECOND, (int) (dateTime % 1000));
        return cal;
    }

    public static Calendar parseDateTimeValue(int date, int time) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, date / 10000);
        cal.set(Calendar.MONTH, (date % 10000) / 100 - 1);
        cal.set(Calendar.DAY_OF_MONTH, date % 100);
        cal.set(Calendar.HOUR_OF_DAY, time / 10000000);
        cal.set(Calendar.MINUTE, (time % 10000000) / 100000);
        cal.set(Calendar.SECOND, (time % 100000) / 1000);
        cal.set(Calendar.MILLISECOND, time % 1000);
        return cal;
    }
}
