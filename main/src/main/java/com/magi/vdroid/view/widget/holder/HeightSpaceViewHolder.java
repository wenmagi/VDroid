package com.magi.vdroid.view.widget.holder;

import android.support.annotation.NonNull;
import android.view.View;

import com.magi.base.widget.recycler.VViewHolder;
import com.magi.vdroid.databinding.RecyclerItemSpaceByHeightBinding;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-25-2017
 */

public class HeightSpaceViewHolder extends VViewHolder<Integer, RecyclerItemSpaceByHeightBinding> {

    public HeightSpaceViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    protected void onBindData(Integer data) {
        super.onBindData(data);
    }
}
