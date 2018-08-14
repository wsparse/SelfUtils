package com.tm.ws.util;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import com.tm.ws.handler.DynamicHandler;
import com.tm.ws.inject.ContentView;
import com.tm.ws.inject.EventBase;
import com.tm.ws.inject.ViewInject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by WS on 2017/3/1.
 */

public class ViewInjectUtils {
    private final static String METHOD_SET_CONTENT_VIEW = "setContentView";
    private final static String METHOD_FIND_VIEW_BY_ID = "findViewById";

    //Activity注解
    private static void injectContentView(Activity activity) {
        Class<? extends Activity> clazz = activity.getClass();
        //查询类上是否存在ContentView注解
        ContentView contentView = clazz.getAnnotation(ContentView.class);
        if (contentView != null) {
            int contentViewLayoutId = contentView.value();
            try {
                Method method = clazz.getMethod(METHOD_SET_CONTENT_VIEW, int.class);
                method.setAccessible(true);
                method.invoke(activity, contentViewLayoutId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @author WS
     * @time 2017/3/1 21:30
     */
    //注入所有的控件
    private static void injectViews(Activity activity) {
        Class<? extends Activity> clazz = activity.getClass();
        Field[] fields = clazz.getDeclaredFields();

        //遍历所有成员变量
        for (Field field : fields) {
            ViewInject viewInjectAnnotation = field.getAnnotation(ViewInject.class);
            if (viewInjectAnnotation != null) {
                int viewId = viewInjectAnnotation.value();
                if (viewId != -1) {
                    Log.e("ws", "初始化控件  viewId = " + viewId);
                    try {
                        Method method = clazz.getMethod(METHOD_FIND_VIEW_BY_ID, int.class);
                        Object resView = method.invoke(activity, viewId);
                        field.setAccessible(true);
                        field.set(activity, resView);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * @author WS
     * @time 2017/3/1 22:26
     */
    //事件的注解
    private static void injectEvents(Activity activity) {
        Class<? extends Activity> clazz = activity.getClass();
        Method[] methods = clazz.getMethods();
        //遍历所有的方法
        for (Method method : methods) {
            Annotation[] annotations = method.getAnnotations();
            //拿到方法上的所有的注解
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                //拿到注解上的注解
                EventBase eventBaseAnnotation = annotationType.getAnnotation(EventBase.class);
                //如果设置为EventBase
                if (eventBaseAnnotation != null) {
                    //取出设置监听器的名称，监听器的类型，调用的方法名
                    String listenerSetter = eventBaseAnnotation.listenerSetter();
                    Class<?> listenerType = eventBaseAnnotation.listenerType();
                    String methodName = eventBaseAnnotation.methodName();

                    //拿到OnClick注解方法中的value方法
                    try {
                        Method aMethod = annotationType.getDeclaredMethod("value");
                        //取出所有的viewId
                        int[] viewIds = (int[]) aMethod.invoke(annotation, new Object[]{});
                        //通过InvocationHandler设置代理
                        DynamicHandler handler = new DynamicHandler(activity);
                        handler.addMethod(methodName, method);
                        Object listener = Proxy.newProxyInstance(listenerType.getClassLoader(), new Class<?>[]{listenerType}, handler);

                        //遍历所有的view,设置事件
                        for (int viewId : viewIds) {
                            View view = activity.findViewById(viewId);
                            Method setEventListenerMethod = view.getClass().getMethod(listenerSetter, listenerType);
                            setEventListenerMethod.invoke(view, listener);
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void inject(Activity activity) {
        injectContentView(activity);
        injectViews(activity);
        injectEvents(activity);
    }

}
