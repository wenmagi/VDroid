package com.magi.base.widget;

/*
 * Copyright (c) 2015 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.magi.base.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-23-2017
 */
public class FixRefreshLayout extends SwipeRefreshLayout implements AppBarLayout.OnOffsetChangedListener {
    private AppBarLayout mAppBarLayout;

    public FixRefreshLayout(Context context) {
        this(context,null);
    }

    public FixRefreshLayout(Context pContext, AttributeSet pAttributeSet) {
        super(pContext, pAttributeSet);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mAppBarLayout = (AppBarLayout) this.findViewById(R.id.appbar);

        if (mAppBarLayout != null) {
            mAppBarLayout.addOnOffsetChangedListener(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mAppBarLayout != null) {
            mAppBarLayout.removeOnOffsetChangedListener(this);
            mAppBarLayout = null;
        }

        super.onDetachedFromWindow();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        this.setEnabled(i == 0);
    }

    // 解决官方 SwipeRefreshLayout 重复下拉刷新的 bug ，
    // https://code.google.com/p/android/issues/detail?id=78191
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return !isRefreshing() && super.onStartNestedScroll(child, target, nestedScrollAxes);
    }

    // bug resolved: 解决SwipeRefreshLayout在拖回原始位置后无法finishSpinner的问题
    // 在SwipeRefreshLayout里 onNestedPreScroll 里会将 mTotalUnconsumed 设成0，而在 onStopNestedScroll 中 mTotalUnconsumed>0
    // 才会隐藏spinner，发生问题。
    @TargetApi(21)
    @Override
    public void onStopNestedScroll(final View target) {
        try {
            Field field1 = SwipeRefreshLayout.class.getDeclaredField("mNestedScrollingParentHelper");
            field1.setAccessible(true);
            NestedScrollingParentHelper mNestedScrollingParentHelper = (NestedScrollingParentHelper) field1.get(this);

            Field field2 = SwipeRefreshLayout.class.getDeclaredField("mNestedScrollInProgress");
            field2.setAccessible(true);

            Field field3 = SwipeRefreshLayout.class.getDeclaredField("mTotalUnconsumed");
            field3.setAccessible(true);
            float mTotalUnconsumed = (float) field3.get(this);

            Method method = SwipeRefreshLayout.class.getDeclaredMethod("finishSpinner", float.class);
            method.setAccessible(true);

            mNestedScrollingParentHelper.onStopNestedScroll(target);
            field2.set(this, false);

            boolean isRefreshing = isRefreshing();
            float scale = getCircleImageView() == null ? 0.0f : ViewCompat.getScaleX(getCircleImageView());
            boolean hasScale = scale > 0;

            if (mTotalUnconsumed > 0 || (!isRefreshing && mTotalUnconsumed == 0 && hasScale)) {
                //修改此处，如果为0，也执行 finishSpinner。追加：如果为0，并且当前没有在refresh，同时圆圈控件的scale大于0（即没有消失）。
                method.invoke(this, mTotalUnconsumed);
                field3.set(this, 0);
            }

            // Dispatch up our nested parent
            stopNestedScroll();
        } catch (Exception pException) {
            super.onStopNestedScroll(target);
        }
    }

    public ImageView getCircleImageView() {
        try {
            Field field = SwipeRefreshLayout.class.getDeclaredField("mCircleView");
            field.setAccessible(true);
            return (ImageView) field.get(this);
        } catch (Exception pException) {
            pException.printStackTrace();
            return null;
        }
    }

    public void setCircleImageViewY(float y) {
        ImageView circle = getCircleImageView();
        if (circle != null) {
            ViewCompat.setTranslationY(circle, y);
        }
    }

}
