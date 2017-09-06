package com.magi.base.utils;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-23-2017
 */

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

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * @author zlin @ Zhihu Inc.
 * @since 12-15-2015
 */
public class VIntent implements Parcelable {

    public static final String EXTRA_TAG = "extra_tag";

    private String mClassName;

    private Bundle mArguments;

    private String mTag;

    private boolean mClearTask = false;

    private boolean mOverlay = false;

    private boolean mPopSelf = false;

    private boolean mSingleTask = false;

    private boolean mHideKeyboard;

    private int mPriorityTab = MainTabs.CURRENT_TAB;

    private SharedElementAnimation mElementAnimation;

    public VIntent(final Class<? extends Fragment> pClazz, final Bundle pArguments, String tag) {
        mClassName = pClazz.getName();
        mArguments = pArguments;
        mTag = tag;
    }

    public Bundle getArguments() {
        return mArguments;
    }

    @NonNull
    public String getClassName() {
        return mClassName == null ? "" : mClassName;
    }

    public String getTag() {
        return mTag;
    }

    public boolean isClearTask() {
        return mClearTask;
    }

    public boolean isOverlay() {
        return mOverlay;
    }

    public boolean isPopSelf() {
        return mPopSelf;
    }

    public boolean isHideKeyboard() {
        return mHideKeyboard;
    }

    public int getPriorityTab() {
        return mPriorityTab;
    }

    public VIntent setArguments(final Bundle pArguments) {
        mArguments = pArguments;
        return this;
    }

    public VIntent setClazz(final Class<? extends Fragment> pClazz) {
        mClassName = pClazz.getName();
        return this;
    }

    public VIntent setClearTask(final boolean pClearTask) {
        mClearTask = pClearTask;
        return this;
    }

    public VIntent setOverlay(final boolean pOverlay) {
        mOverlay = pOverlay;
        return this;
    }

    public VIntent setPopSelf(final boolean pPopSelf) {
        mPopSelf = pPopSelf;
        return this;
    }

    public VIntent setHideKeyboard(boolean hideKeyboard) {
        mHideKeyboard = hideKeyboard;
        return this;
    }

    public VIntent setPriorityTab(int priorityTab) {
        mPriorityTab = priorityTab;
        return this;
    }

    public SharedElementAnimation getElementAnimation() {
        return mElementAnimation;
    }

    public VIntent setElementAnimation(SharedElementAnimation mElementAnimation) {
        this.mElementAnimation = mElementAnimation;
        return this;
    }

    public boolean isSingleTask() {
        return mSingleTask;
    }

    /**
     * 这里的 singleTask 与 activity 的 singleTask 的表现相似，
     * 不同点在于 Fragment 不是同一个 Fragment，而是一个新的 Fragment，并执行它的生命周期，
     * 不会像 Activity 一样回调 onNewIntent(),
     *
     * @param singleTask
     * @return
     */
    public VIntent setSingleTask(boolean singleTask) {
        mSingleTask = singleTask;
        return this;
    }

    public static class SharedElementAnimation {
        private int sharedElementId;

        private String sharedTransitionName;

        private Object sharedElementAnimation;

        private View sharedView;

        public int getSharedElementId() {
            return sharedElementId;
        }

        public void setSharedElementId(int sharedElementId) {
            this.sharedElementId = sharedElementId;
        }

        public String getSharedTransitionName() {
            return sharedTransitionName;
        }

        public void setSharedTransitionName(String sharedTransitionName) {
            this.sharedTransitionName = sharedTransitionName;
        }

        public Object getSharedElementAnimation() {
            return sharedElementAnimation;
        }

        public void setSharedElementAnimation(Object sharedElementAnimation) {
            this.sharedElementAnimation = sharedElementAnimation;
        }

        public View getSharedView() {
            return sharedView;
        }

        public void setSharedView(View sharedView) {
            this.sharedView = sharedView;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mClassName);
        dest.writeBundle(this.mArguments);
        dest.writeByte(this.mClearTask ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mOverlay ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mPopSelf ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mHideKeyboard ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mPriorityTab);
    }

    protected VIntent(Parcel in) {
        this.mClassName = in.readString();
        this.mArguments = in.readBundle(getClass().getClassLoader());
        this.mClearTask = in.readByte() != 0;
        this.mOverlay = in.readByte() != 0;
        this.mPopSelf = in.readByte() != 0;
        this.mHideKeyboard = in.readByte() != 0;
        this.mPriorityTab = in.readInt();
    }

    public static final Creator<VIntent> CREATOR = new Creator<VIntent>() {
        @Override
        public VIntent createFromParcel(Parcel source) {
            return new VIntent(source);
        }

        @Override
        public VIntent[] newArray(int size) {
            return new VIntent[size];
        }
    };
}