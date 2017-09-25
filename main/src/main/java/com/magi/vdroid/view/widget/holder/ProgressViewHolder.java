package com.magi.vdroid.view.widget.holder;

import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.magi.base.widget.recycler.VViewHolder;
import com.magi.vdroid.R;
import com.magi.vdroid.databinding.RecyclerItemProgressBinding;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 07-02-2017
 */

public class ProgressViewHolder extends VViewHolder<Object, RecyclerItemProgressBinding> {

    public ProgressViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    protected void onBindData(Object data) {
        super.onBindData(data);
        mBinding.progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getContext(), R.color
                .colorPrimary), PorterDuff.Mode.MULTIPLY);
    }
}
