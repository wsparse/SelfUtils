package com.tm.ws.util;

import android.content.Context;
import android.widget.Toast;
/**
 * Created by WS on 2017/7/21.
 */

public class ToastUtils {
    //http://blog.csdn.net/study_zhxu/article/details/50630000
    //Android 特殊的单例Toast（防止重复显示）
    /**
     * 之前显示的内容
     */
    private static String oldMsg;
    /**
     * Toast对象
     */
    private static Toast toast = null;
    /**
     * 第一次时间
     */
    private static long oneTime = 0;
    /**
     * 第二次时间
     */
    private static long twoTime = 0;


    private static Context context;

    static {
        context = Utils.getContext();
    }

    public static void showToast(String message) {
        if (toast == null) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            toast.show();
            oneTime = System.currentTimeMillis();

        } else {
            twoTime = System.currentTimeMillis();
            LogUtils.Log("twoTime - oneTime > Toast.LENGTH_SHORT " + ((twoTime - oneTime) > Toast.LENGTH_SHORT));
            LogUtils.Log("message = " + message + " , oldMsg = " + oldMsg);
            if (message.equals(oldMsg)) {
                if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                    toast.show();
                }
            } else {
                oldMsg = message;
                toast.setText(message);
                toast.show();
            }
        }
        oneTime = twoTime;
    }



    //小米手机会在消息前加上应用名称
    public static void toastMsg(String msg) {
        Toast toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        toast.setText(msg);
        toast.show();
    }
}
