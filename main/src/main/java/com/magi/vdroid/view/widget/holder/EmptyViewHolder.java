package com.magi.vdroid.view.widget.holder;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.magi.base.widget.recycler.VViewHolder;
import com.magi.vdroid.R;
import com.magi.vdroid.databinding.RecyclerItemEmptyBinding;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-25-2017
 */

public class EmptyViewHolder extends VViewHolder<EmptyViewHolder.EmptyInfo, RecyclerItemEmptyBinding> {

    public EmptyViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public static class EmptyInfo {
        int emptyID;
        int emptyStr;
        int height;
        String mErrorMessage;
        int emptyBtnId;
        View.OnClickListener actionBtnListener;


        public EmptyInfo(final int emptyIcon, final int emptyStr, final int height) {
            this.emptyID = emptyIcon;
            this.emptyStr = emptyStr;
            this.height = height;
        }

        public EmptyInfo(String errorMessage, int iconRid, int height, int emptyBtnStringRid, View.OnClickListener
                listener) {
            this.mErrorMessage = errorMessage;
            this.emptyID = iconRid;
            this.height = height;
            this.emptyBtnId = emptyBtnStringRid;
            this.actionBtnListener = listener;
        }

    }

    @Override
    protected void onBindData(EmptyInfo data) {
        super.onBindData(data);
        if (data == null)
            return;

        if (data.actionBtnListener != null) {
            mBinding.button.setOnClickListener(data.actionBtnListener);
            mBinding.button.setVisibility(View.VISIBLE);
            mBinding.button.setText(data.emptyID);
        } else {
            mBinding.button.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(data.mErrorMessage)) {
            this.mBinding.message.setText(data.mErrorMessage);
        } else {
            mBinding.message.setText(data.emptyStr > 0 ? data.emptyStr : R.string.empty_info_default);
        }

        if (data.emptyID > 0) {
            this.mBinding.icon.setVisibility(View.VISIBLE);
            this.mBinding.icon.setImageResource(data.emptyID);
        } else {
            this.mBinding.icon.setVisibility(View.GONE);
        }


        mBinding.icon.setImageResource(data.emptyID > 0 ? data.emptyID : R.drawable.ic_empty);
        this.itemView.getLayoutParams().height = data.height;

        this.itemView.requestLayout();
        mBinding.executePendingBindings();
    }
}
