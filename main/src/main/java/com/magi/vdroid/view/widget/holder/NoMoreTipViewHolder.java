package com.magi.vdroid.view.widget.holder;

import android.support.annotation.NonNull;
import android.view.View;

import com.magi.base.utils.DisplayUtils;
import com.magi.base.widget.recycler.VViewHolder;
import com.magi.vdroid.R;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 07-02-2017
 */

public class NoMoreTipViewHolder extends VViewHolder {

    public static class Tip {
        private int mPaddingBottom;
        private CharSequence mTip;

        public Tip(int paddingBottom, CharSequence charSequence) {
            mPaddingBottom = paddingBottom;
            mTip = charSequence;
        }

        public int getPaddingBottom() {
            return mPaddingBottom;
        }

        public CharSequence getTip() {
            return mTip;
        }
    }

    public NoMoreTipViewHolder(@NonNull View itemView) {
        super(itemView);
    }
}
