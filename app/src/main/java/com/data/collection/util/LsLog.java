package com.data.collection.util;

import android.util.Log;

import com.data.collection.BuildConfig;


public class LsLog {
    private static boolean isDebug = BuildConfig.DEBUG;
    private static String logfilter = "LsLog : ";

    public static void v(String tag, String msg) {
        if (isDebug) {
            Log.v(tag, logfilter + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (isDebug) {
            Log.d(tag, logfilter + msg);
        }
    }

    public static void i(String tag, String msg) {
        if (isDebug) {
            Log.w(tag, logfilter + msg);
        }
    }

    public static void w(String tag, String msg) {
        if (isDebug) {
            Log.w(tag, logfilter + msg);
        }
    }

    public static void e(String tag, String msg) {
        if (isDebug) {
            Log.e(tag, logfilter + msg);
        }
    }
}
