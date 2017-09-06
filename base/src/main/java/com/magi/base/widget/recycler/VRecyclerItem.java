package com.magi.base.widget.recycler;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-25-2017
 */

public class VRecyclerItem<T> {
    private final int mViewType;

    private T mData;

    public VRecyclerItem(final int viewType, final T data) {
        this.mViewType = viewType;

        this.mData = data;
    }

    public int getViewType() {
        return this.mViewType;
    }

    public T getData() {
        return this.mData;
    }

    public void setData(final T data) {
        this.mData = data;
    }
}
