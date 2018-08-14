package com.tm.ws.util;

import android.util.Log;

public class LogUtils {
    private static String TAG = "ws";

    public static void setTAG(String TAG) {
        LogUtils.TAG = TAG;
    }

    public static void Log(String msg) {
        Log.e(TAG, msg);
    }
}
