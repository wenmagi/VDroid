package com.magi.base;

import android.app.Application;
import android.content.Context;

import com.magi.base.utils.EnhancedHandler;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-24-2017
 */

public class VApplication extends Application {

    private static VApplication mApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        EnhancedHandler.initialize();
    }

    public static Context getAppContext() {
        return mApplication;
    }
}
