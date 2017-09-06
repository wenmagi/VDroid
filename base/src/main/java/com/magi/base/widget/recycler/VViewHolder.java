package com.magi.base.widget.recycler;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-24-2017
 */

public class VViewHolder<T, V extends ViewDataBinding> extends RecyclerView.ViewHolder implements View.OnClickListener {

    public interface OnRecyclerItemClickListener<T, V extends ViewDataBinding> {

        void onClick(View view, VViewHolder<T, V> viewHolder);
    }

    public interface OnRecyclerItemLongClickListener<T, V extends ViewDataBinding> {

        void onLongClick(View view, VViewHolder<T, V> viewHolder);
    }

    protected VRecyclerAdapter mAdapter;

    protected OnRecyclerItemClickListener<T, V> mOnRecyclerItemClickListener;

    protected OnRecyclerItemLongClickListener<T, V> mOnRecyclerItemLongClickListener;

    protected T data;

    public V mBinding;

    public VViewHolder(@NonNull final View itemView) {
        super(itemView);
        mBinding = DataBindingUtil.bind(itemView);
    }

    protected void setAdapter(final VRecyclerAdapter adapter) {
        this.mAdapter = adapter;
    }

    protected void onBindData(final T data) {
        this.data = data;
    }

    protected void onUnbind() {
        this.data = null;
    }

    public void setOnClickListener(OnRecyclerItemClickListener<T, V> onClickListener) {
        this.mOnRecyclerItemClickListener = onClickListener;
    }

    public void setOnLongClickListener(OnRecyclerItemLongClickListener<T, V> onLongClickListener) {
        this.mOnRecyclerItemLongClickListener = onLongClickListener;
    }

    public void onAttachedToWindow() {

    }

    public void onDetachedFromWindow() {

    }

    protected Resources getResources() {
        return this.itemView.getResources();
    }

    @Override
    public void onClick(final View view) {
        if (mOnRecyclerItemClickListener != null) {
            mOnRecyclerItemClickListener.onClick(view, this);
        }
    }

    public T getData() {
        return this.data;
    }

    public Context getContext() {
        return itemView.getContext();
    }
}
