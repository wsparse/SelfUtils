package com.tm.ws.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;

/**
 * Created by ws on 2018/1/9.
 */

public class ModeUtil {

    public static boolean APP_DBG = false;//是否debug模式

    public static void init(Context context) {
        APP_DBG = isApkDebug(context);
    }

    private static boolean isApkDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
