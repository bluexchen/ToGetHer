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
        return cal.get(Calendar.HOUR_OF_DAY) * 10000 +
                cal.get(Calendar.MINUTE) * 100 + cal.get(Calendar.SECOND);
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
        cal.set(Calendar.HOUR_OF_DAY, time / 10000);
        cal.set(Calendar.MINUTE, (time % 10000) / 100);
        cal.set(Calendar.SECOND, (time % 100));
        return cal;
    }
}
