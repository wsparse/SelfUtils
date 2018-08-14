package com.tm.ws.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import com.tm.ws.util.LogUtils;

/**
 * Created by WS on 2017/9/22.
 */

public class SelfListView extends ListView {
    public static final String TAG = "ws";

    private static final int mTouchSlop = 2;//有效滑动距离

    private float downY;//上次按下的点的Y坐标

    public SelfListView(Context context) {
        super(context);
    }

    public SelfListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelfListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean scrollToBottom() {
        int first = getFirstVisiblePosition();
        int visibleCount = getChildCount(); //可见元素数量
        int lastVisiblePosition = getLastVisiblePosition();

        getLastVisiblePosition();
        int count = getCount();

        View lastView;
        if (visibleCount != 0) {
            lastVisiblePosition = visibleCount - 1;
        }
        lastView = getChildAt(lastVisiblePosition);
        if ((first + visibleCount) == count && lastView != null && lastView.getBottom() == this.getHeight()) {
            return true;
        }
        return false;
    }

    /**
     * author ws
     * created 2017/10/16 14:23
     */
    //判断内容是否低于父容器的高度
    private boolean isLowerParentHeight() {
        int childCount = getChildCount();
        if (childCount == 0) {
            //LogUtils.Log("SelfListViews.size = 0");
            return true;
        } else {
            View childView = getChildAt(childCount - 1);
            int count = getCount();
            int height = childView.getHeight();
            int totalHeight = count * height;
            if (totalHeight < this.getHeight()) {
                return true;
            }
        }
        return false;
    }

    public boolean scrollToTop() {
        int childCount = getChildCount();
        if (childCount != 0) { //可见元素数量不为0
            int firstPosition = getFirstVisiblePosition();
            View firstChild = getChildAt(0);
            if (firstChild != null) {
                int top = firstChild.getTop();
                if (firstPosition == 0 && top == 0) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.e(TAG, "SelfListViews dispatchTouchEvent ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "SelfListViews dispatchTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "SelfListViews dispatchTouchEvent ACTION_UP");
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
        //return false; //返回false则后续的onInterceptTouchEvent以及onTouchEvent都不执行
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        float y;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                LogUtils.Log("SelfListViews onTouchEvent ACTION_DOWN");
                y = downY = ev.getRawY();
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                LogUtils.Log("SelfListViews onTouchEvent ACTION_MOVE");
                y = ev.getRawY();
                if (isLowerParentHeight()) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                    LogUtils.Log("父容器消费!");
                    // break; //避免判断条件既到达顶部又到达底部，三者只能取其一。
                    return false; //dispatchTouchEvent返回false,避免卡顿现象。
                }

                if (scrollToTop()) {

                    if (y - downY > mTouchSlop) {
                        /**
                         * Point 1 : 如果滑动到顶部，并且手指还想向下滑动，则事件交还给父控件，要求父控件可以拦截事件
                         */
                        getParent().requestDisallowInterceptTouchEvent(false);
                        return false;
                    } else if (y - downY < -mTouchSlop) {
                        /**
                         * Point 2 : 如果滑动到顶部，并且手指正常向上滑动，则事件由自己处理，要求父控件不许拦截事件
                         */
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    break; //避免判断条件既到达顶部又到达底部，三者只能取其一。
                }
                if (scrollToBottom()) {

                    if (y - downY < -mTouchSlop) {
                        /**
                         * Point 3 : 如果滑动到底部，并且手指还想向上滑动，则事件交还给父控件，要求父控件可以拦截事件
                         */
                        getParent().requestDisallowInterceptTouchEvent(false);
                        return false;
                    } else if (y - downY > mTouchSlop) {
                        /**
                         * Point 4 : 如果滑动到底部，并且手指正常向下滑动，则事件由自己处理，要求父控件不许拦截事件
                         */
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                LogUtils.Log("SelfListViews onTouchEvent ACTION_UP");
                break;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }
}
