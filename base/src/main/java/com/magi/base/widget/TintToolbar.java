package com.magi.base.widget;

import android.content.Context;
import android.support.annotation.MenuRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;


/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 09-26-2017
 */

public class TintToolbar extends Toolbar {
    public TintToolbar(Context context) {
        this(context, null);
    }

    public TintToolbar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // inflate menu 时，动态修改 tintColor
    @Override
    public void inflateMenu(@MenuRes int resId) {
        super.inflateMenu(resId);
    }

}
