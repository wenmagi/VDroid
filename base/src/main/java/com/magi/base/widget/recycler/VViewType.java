package com.magi.base.widget.recycler;

import android.support.annotation.LayoutRes;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-25-2017
 */

public class VViewType {

    private final int mTypeId;

    @LayoutRes private final int mLayoutResourceId;

    private final Class<? extends VViewHolder> mViewHolderClass;

    public VViewType(final int typeId, @LayoutRes final int layoutResourceId, final Class<? extends VViewHolder>
            viewHolderClass) {
        this.mTypeId = typeId;

        this.mLayoutResourceId = layoutResourceId;

        this.mViewHolderClass = viewHolderClass;
    }

    public int getTypeId() {
        return this.mTypeId;
    }

    public int getLayoutResourceId() {
        return this.mLayoutResourceId;
    }

    public Class<? extends VViewHolder> getViewHolderClass() {
        return this.mViewHolderClass;
    }
}
