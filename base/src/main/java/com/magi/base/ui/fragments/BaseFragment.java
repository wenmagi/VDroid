package com.magi.base.ui.fragments;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;

import com.magi.base.ui.function.FunctionLazyLoad;
import com.magi.base.utils.SystemUtils;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-23-2017
 */

public class BaseFragment extends Fragment {

    // 用于与 Fragment 显示相关的非 Fragment/View 组件回调
    public interface OnScreenDisplayingCallback {

        void onScreenDisplaying();
    }

    private OnScreenDisplayingCallback mOnScreenDisplayingCallback;

    public void setOnScreenDisplayingCallback(OnScreenDisplayingCallback callback) {
        mOnScreenDisplayingCallback = callback;
    }


    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (this instanceof FunctionLazyLoad.OnLazyLoadListener) {

            FunctionLazyLoad lazyLoadFunc = ((FunctionLazyLoad.OnLazyLoadListener) this).getLazyLoadFunction();
            if (lazyLoadFunc != null) {
                lazyLoadFunc.setViewCreated(true);
                lazyLoadFunc.lazyLoad();
            }
        }

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (this instanceof FunctionLazyLoad.OnLazyLoadListener) {
            FunctionLazyLoad lazyLoadFunc = ((FunctionLazyLoad.OnLazyLoadListener) this).getLazyLoadFunction();
            if (lazyLoadFunc == null)
                return;

            if (isVisibleToUser) {
                lazyLoadFunc.setUIVisible(true);
                lazyLoadFunc.lazyLoad();
            } else {
                lazyLoadFunc.setUIVisible(false);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (this instanceof FunctionLazyLoad.OnLazyLoadListener) {

            FunctionLazyLoad lazyLoadFunc = ((FunctionLazyLoad.OnLazyLoadListener) this).getLazyLoadFunction();
            //页面销毁,恢复标记
            if (lazyLoadFunc != null) {
                lazyLoadFunc.setViewCreated(false);
                lazyLoadFunc.setUIVisible(false);
            }
        }
    }

    public boolean isSystemUiFullscreen() {
        return false;
    }

    @CallSuper
    public void onScreenDisplaying() {
        if (isSystemUiFullscreen()) {
            switchToTranslucentStatus();
        } else {
            switchToNormalStatus();
        }


        if (mOnScreenDisplayingCallback != null) {
            mOnScreenDisplayingCallback.onScreenDisplaying();
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void switchToTranslucentStatus() {
        if (SystemUtils.SDK_VERSION_LOLLIPOP_OR_LATER && getActivity() != null) {
            getActivity().getWindow().setStatusBarColor(
                    ContextCompat.getColor(getContext(), android.R.color.transparent));
            forceToFitsSystemWindows(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void switchToNormalStatus() {
        if (SystemUtils.SDK_VERSION_LOLLIPOP_OR_LATER && getActivity() != null) {
            getActivity().getWindow().setStatusBarColor(provideStatusBarColor());
            forceToFitsSystemWindows(false);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void forceToFitsSystemWindows(boolean fits) {
        if (!SystemUtils.SDK_VERSION_LOLLIPOP_OR_LATER) {
            return;
        }

        View view = getActivity().findViewById(android.R.id.content);

        if (fits) {
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else {
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }

        ViewCompat.setFitsSystemWindows(view, fits);
        ViewCompat.requestApplyInsets(view);
    }

    @ColorInt
    protected int provideStatusBarColor() {
        return ContextCompat.getColor(getContext(), android.R.color.transparent);
    }

}
