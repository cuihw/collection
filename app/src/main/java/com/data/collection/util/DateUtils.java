package com.data.collection.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    // public static final SimpleDateFormat fmtYYYYMMDD = new SimpleDateFormat("yyyy-MM-dd");
    public static final String fmtYYYYMMDDhhmmss = "yyyy-MM-dd HH:mm:ss";
    public static final String fmtYYYYMMDD = "yyyy-MM-dd";
    public static final String fmtYYYYMM = "yyyy-MM";


    public static String getNow(String format) {
        Date date = new Date();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            String result = sdf.format(date);
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }
    /**
     * 日期转换为字符串
     *
     * @param date   日期
     * @param format 日期格式
     * @return 指定格式的日期字符串
     */
    public static String formatDateByFormat(Date date, String format) {
        String result = "";
        if (date != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                result = sdf.format(date);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 日期转换为字符串
     *
     * @param calendar 日期
     * @param format   日期格式
     * @return 指定格式的日期字符串
     */
    public static String formatDateByFormat(Calendar calendar, String format) {
        String result = "";
        if (calendar != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                result = sdf.format(calendar.getTime());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
}
