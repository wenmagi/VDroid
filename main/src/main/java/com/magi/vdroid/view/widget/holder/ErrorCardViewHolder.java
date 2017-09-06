package com.magi.vdroid.view.widget.holder;

import android.support.annotation.NonNull;
import android.view.View;

import com.magi.base.widget.recycler.VViewHolder;
import com.magi.vdroid.databinding.RecyclerItemErrorCardBinding;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 07-02-2017
 */

public class ErrorCardViewHolder extends VViewHolder<ErrorCardViewHolder.ErrorInfo, RecyclerItemErrorCardBinding> {
    public ErrorCardViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    protected void onBindData(ErrorInfo data) {
        super.onBindData(data);
        
        this.mBinding.errorMessage.setText(data.mErrorMsg);

        this.mBinding.actionPositive.setOnClickListener(data.mRetryClickListener);

        this.mBinding.actionNegative.setOnClickListener(data.mIgnoreClickListener);

        this.mBinding.executePendingBindings();
    }

    public static class ErrorInfo {

        String mErrorMsg;

        View.OnClickListener mRetryClickListener;

        View.OnClickListener mIgnoreClickListener;

        public ErrorInfo(String pErrorMsg, View.OnClickListener pOnRetryClickListener, View.OnClickListener
                pOnIgnoreClickListener) {
            this.mErrorMsg = pErrorMsg;
            this.mRetryClickListener = pOnRetryClickListener;
            this.mIgnoreClickListener = pOnIgnoreClickListener;
        }
    }

}
