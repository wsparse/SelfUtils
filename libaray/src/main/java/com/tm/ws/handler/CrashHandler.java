package com.tm.ws.handler;

/**
 * Created by WS on 2017/6/1.
 */
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.tm.ws.util.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author WS
 * @time 2017/6/1 17:08
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {


    public static final String TAG = "CrashHandler";
    //系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    //CrashHandler实例
    private static CrashHandler instance;
    //上下文
    private Context mContext;
    //用来存储设备信息和异常信息
    private Map<String, String> info = new HashMap<String, String>();
    //用于格式化日期，作为日志文件名的一部分
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String savePath;//保存日志文件

    //单例模式
    private CrashHandler() {
    }


    public static synchronized CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }


    public void init(Context context) {
        mContext = context;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    //当UncaughtException发生时会转入该函数来处理

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
        //上传至服务器
        boolean b = handleException(ex);
        //upCrash();
        LogUtils.Log("执行了上传异常信息!");
        if (!b && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            LogUtils.Log("布尔值 = " + !b + ", mDefaultHandler = " + mDefaultHandler);
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                Log.e(TAG, "error : ", e);
                LogUtils.Log("处理异常抛异常了" + e.getMessage());
            }

            LogUtils.Log("布尔值 = " + !b);
        }
        exitApp();
    }

    /**
     * author ws
     * created 2017/10/17 9:10
     */
    //退出app
    private void exitApp() {
        //退出程序
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    /**
     * @author WS
     * @time 2017/6/1 17:19
     */
    //自定义错误处理，收集错误信息，发送错误报告等操作均在此完成。
    private boolean handleException(final Throwable ex) {
        LogUtils.Log("进入了处理异常！");
        if (ex == null) {
            LogUtils.Log("handleException .............返回false！");
            return false;
        }

        LogUtils.Log("ex = " + ex.getMessage());
        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                super.run();
                Looper.prepare();
                Toast.makeText(mContext, "很抱歉，遇到问题了，即将退出，之后请重新登录！"/* + ex.getMessage()*/, Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();

        //收集设备参数信息
        collectDeviceInfo(mContext);
        //保存日志文件
        savePath = saveCrashInfo2File(ex);
        LogUtils.Log("handleException .............返回true！");
        return true;
    }

    /**
     * @return 返回文件名称，便于将文件传送大服务器
     * @author WS
     * @time 2017/6/1 17:36
     */
    //保存错误信息到文件中
    private String saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : info.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(timestamp);
            //String fileName = "crash-" + time + "-" + timestamp + ".log";
            String fileName = time + ".txt";
            String savePath = null;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "crash";
                File dir = new File(path);
                if (!dir.exists()) {
                    boolean mkdirs = dir.mkdirs();
                    Log.e(TAG, "创建目录" + mkdirs);
                }
                savePath = path + File.separator + fileName;
                FileOutputStream fos = new FileOutputStream(savePath);
                fos.write(sb.toString().getBytes("utf-8"));
                Log.e(TAG, "写入异常信息到 " + savePath);
                fos.close();
                this.savePath = savePath;
                //upCrash();
            }
            return savePath;
        } catch (Exception e) {
            Log.e(TAG, "an error occur while writing file...", e);
        }
        return null;
    }

    /**
     * @author WS
     * @time 2017/6/1 17:26
     */
    //收集设备信息
    private void collectDeviceInfo(Context mContext) {
        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;//版本名称
                String versionCode = pi.versionCode + "";
                info.put("versionName", versionName);
                info.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occur when collect package info ", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                info.put(field.getName(), field.get(null).toString());
                Log.e(TAG, field.getName() + ":" + field.get(null));
            } catch (IllegalAccessException e) {
                Log.e(TAG, "an error occur when collect crash info", e);
            }
        }
    }

}
