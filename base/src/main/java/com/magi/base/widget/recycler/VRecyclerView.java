package com.magi.base.widget.recycler;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;

/**
 * do what you want to do
 *
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-24-2017
 */

public class VRecyclerView extends ObservableRecyclerView {

    public VRecyclerView(Context context) {
        super(context);
    }

    public VRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setBackgroundResource(@DrawableRes int resid) {
        if (resid < 0) {
            resid = 0;
        }
        super.setBackgroundResource(resid);
    }


    @Deprecated
    public void setBackgroundId(int redId, boolean b) {
        setBackgroundResource(redId);
    }


}
