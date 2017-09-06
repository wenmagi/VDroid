package com.magi.base.widget;


/*
 * Copyright (c) 2015 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.v7.graphics.drawable.DrawableWrapper;

import java.util.Arrays;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-24-2017
 */
@SuppressWarnings("RestrictedApi")
public class TintDrawable extends DrawableWrapper {

    private ColorStateList mTintColorStateList;

    public TintDrawable(final Drawable pDrawable) {
        super(pDrawable.mutate());

        this.setBounds(pDrawable.getBounds());
    }

    public void setTintColorRes(final Resources pResources, @ColorRes final int pTintColorRes) {
        this.setTintColor(pResources.getColorStateList(pTintColorRes));
    }

    public void setTintColor(final ColorStateList pColorStateList) {
        if (pColorStateList == null) {
            return;
        }
        this.mTintColorStateList = pColorStateList;

        this.setColorFilter(this.mTintColorStateList.getColorForState(this.getState(), this.mTintColorStateList
                .getDefaultColor()), PorterDuff.Mode.SRC_IN);
    }

    @Override
    protected boolean onStateChange(final int[] pState) {
        if (this.mTintColorStateList != null) {
            super.setColorFilter(this.mTintColorStateList.getColorForState(pState, this.mTintColorStateList
                    .getDefaultColor()), PorterDuff.Mode.SRC_IN);
        }

        return super.onStateChange(pState);
    }

    @Override
    public boolean setState(final int[] stateSet) {
        if (!Arrays.equals(getWrappedDrawable().getState(), stateSet)) {
            return onStateChange(stateSet) && super.setState(stateSet);
        }
        return super.setState(stateSet);
    }

    @Override
    public boolean isStateful() {
        if (this.mTintColorStateList != null) {
            return this.mTintColorStateList.isStateful();
        } else {
            return super.isStateful();
        }
    }

    /**
     * 避免丢失callback，导致动画失效
     */
    @Override
    public void setWrappedDrawable(Drawable pDrawable) {
        if (pDrawable != null && pDrawable.getCallback() != null) {
            setCallback(pDrawable.getCallback());
        }
        super.setWrappedDrawable(pDrawable);
    }
}