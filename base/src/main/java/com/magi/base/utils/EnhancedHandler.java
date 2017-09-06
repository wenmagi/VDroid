package com.magi.base.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * {@link EnhancedHandler {@link #runInHandlerThread(Runnable)}}
 * 可以保证 runnable 运行在 UI Thread，使用比较方便
 * 非 RX 编码工具类
 *
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-24-2017
 */
final class EnhancedHandlerImpl extends Handler {

    private static final String TAG = "EnhancedHandler";
    private final Thread _thread;


    EnhancedHandlerImpl() {
        _thread = Thread.currentThread();
    }

    void runInHandlerThread(Runnable runnable) {
        if (Thread.currentThread() != _thread)
            post(runnable);
        else
            try {
                runnable.run();
            } catch (Throwable e) {
                Log.e(TAG, "Error occurred in handler run thread : ");
                e.printStackTrace();
            }

    }

    public void runInHandlerThreadDelay(Runnable runnable) {
        post(runnable);
    }

    public <T> T callInHandlerThread(Callable<T> callable, T defaultValue) {
        T result = null;
        try {
            if (Thread.currentThread() != _thread)
                result = postCallable(callable).get();
            else
                result = callable.call();
        } catch (Throwable e) {
            Log.e(TAG, "Error occurred in handler call thread : ");
            e.printStackTrace();
        }

        return result == null ? defaultValue : result;
    }

    public <T> Future<T> postCallable(Callable<T> callable) {
        FutureTask<T> task = new FutureTask<T>(callable);
        post(task);
        return task;
    }

    @Override
    public void dispatchMessage(Message msg) {
        Runnable callback = msg.getCallback();
        if (callback != null) {
            try {
                callback.run();
            } catch (Throwable e) {
                Log.e(TAG, "Error occurred in handler thread, dispatchMessage : ");
                e.printStackTrace();
            }
        } else {
            handleMessage(msg);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
    }
}

public class EnhancedHandler {
    private static boolean isInitialed = false;
    private static EnhancedHandlerImpl mHandler;
    private static WeakReference<Thread> _thread_ref;

    public EnhancedHandler() {
    }

    public static void initialize() {
        if (isInitialed)
            return;
        mHandler = new EnhancedHandlerImpl();
        _thread_ref = new WeakReference<>(Thread.currentThread());
        isInitialed = true;
    }

    /**
     * let runnable run in mainThread
     *
     * @param runnable runnable
     */
    public static void runInHandlerThread(Runnable runnable) {
        if (mHandler != null)
            mHandler.runInHandlerThread(runnable);
    }

    public static void removeRunnable(Runnable runnable) {
        if (mHandler != null)
            mHandler.removeCallbacks(runnable);
    }

    public static void post(Runnable runnable) {
        if (mHandler != null)
            mHandler.post(runnable);
    }

    public static void postDelayed(Runnable runnable, long delayed) {
        if (mHandler != null)
            mHandler.postDelayed(runnable, delayed);
    }

    /**
     * check if is in MainThread
     *
     * @return true:in false:not in
     */
    public static boolean isInMainThread() {
        if (!isInitialed)
            return true;
        else
            return _thread_ref != null && _thread_ref.get() == Thread.currentThread();
    }
}

