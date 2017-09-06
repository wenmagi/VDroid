package com.magi.base.ui.activities;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;

import com.magi.base.utils.VIntent;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-23-2017
 */

public abstract class BaseFragmentActivity extends FragmentActivity {
    // 进入后台前，假设当前屏幕上显示的 Fragment A 是全屏的，且存在一个不全屏的 Fragment B ；
    // 进入后台后，再恢复，由于 Activity 恢复 Fragment 的顺序难以确定，
    // 可能出现 A 先恢复，B 后恢复的现象；
    // 虽然显示的是 A ，但是由于 B 不是全屏的，所以导致 A 也不是全屏的；
    // 同理, 恢复状态栏颜色时也会因为顺序不同而出错;
    // 因此添加一个变量, 用于记录进入后台前的屏幕上显示的 Fragment,
    // 只允许这个 Fragment 对状态栏进行操作
    protected String mLatestFragmentName;

    public void startFragment(final VIntent intent) {
        startFragment(intent, null);
    }

    public void startFragment(final VIntent intent, final View view) {
        startFragmentForResult(intent, null, 0, view);
    }

    public void startFragmentForResult(final VIntent intent, final Fragment target, final int requestCode) {
        startFragmentForResult(intent, target, requestCode, null);
    }

    abstract public void startFragmentForResult(@Nullable final VIntent intent, final Fragment target, final int
            requestCode, final View view);

    abstract public void intentPopSelf(VIntent intent);

    abstract public String getLatestFragmentName();

    abstract public boolean isCurrentDisplayFragment(Fragment fragment);

    abstract public Fragment getCurrentDisplayFragment();

    abstract public ViewGroup getRootView();

    abstract public void popBack(boolean popBack);

    public void addFragmentToOverlay(@Nullable VIntent pIntent) {
        addFragmentToOverlay(pIntent, null, 0);
    }

    abstract public void addFragmentToOverlay(@Nullable VIntent pIntent, Fragment pTargetFragment, int pRequestCode);

}
