package com.tm.ws.interfaces;

import com.trello.rxlifecycle.LifecycleTransformer;

/**
 * Created by ws on 2018/4/23.
 */

public interface BaseView {
    void onFailure(Throwable e);

    void showErrorMsg(String msg);

    /**
     * 显示动画
     */
    void showLoading();

    /**
     * 隐藏动画
     */
    void hideLoading();


    /**
     * 显示网络错误
     *
     * @param onRetryListener 点击监听
     */
    void showNetError(OnRetryListener onRetryListener);


    /**
     * 完成刷新，新增控制刷新
     */

    void finishRefresh();

    /**
     * 设置是否加载中
     */
    void setIsLoading(boolean isLoading);


    /**
     * 绑定生命周期
     * @param <T>
     * @return
     */
    <T> LifecycleTransformer<T> bindToLife();
}
