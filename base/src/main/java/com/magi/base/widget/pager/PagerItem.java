/*
 * Copyright (c) 2017 Zhihu Inc.
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

package com.magi.base.widget.pager;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 05-27-2017
 */

public class PagerItem {

    private final Class<? extends Fragment> mFragmentClass;

    private CharSequence mTitle;

    private final int mIconResId;

    private final Drawable mIcon;

    private final Bundle mArguments;

    public PagerItem(final Class<? extends Fragment> fragmentClass, final CharSequence title) {
        this(fragmentClass, title, null);
    }

    public PagerItem(final Class<? extends Fragment> fragmentClass, final CharSequence title, final Bundle
            arguments) {
        this(fragmentClass, title, 0, arguments);
    }

    public PagerItem(final Class<? extends Fragment> fragmentClass, final int iconResId) {
        this(fragmentClass, iconResId, null);
    }

    public PagerItem(final Class<? extends Fragment> fragmentClass, final Drawable icon) {
        this(fragmentClass, icon, null);
    }

    public PagerItem(final Class<? extends Fragment> fragmentClass, final int iconResId, final Bundle
            arguments) {
        this(fragmentClass, null, iconResId, arguments);
    }

    public PagerItem(final Class<? extends Fragment> fragmentClass, final Drawable icon, final Bundle
            arguments) {
        this(fragmentClass, null, icon, arguments);
    }

    private PagerItem(final Class<? extends Fragment> fragmentClass, final CharSequence title, final int
            iconResId, final Bundle arguments) {
        this.mFragmentClass = fragmentClass;

        this.mTitle = title;

        this.mIconResId = iconResId;

        this.mIcon = null;

        this.mArguments = arguments;
    }

    public PagerItem(final Class<? extends Fragment> fragmentClass, final CharSequence title, final Drawable
            icon, final Bundle arguments) {
        this.mFragmentClass = fragmentClass;

        this.mTitle = title;

        this.mIcon = icon;

        this.mIconResId = 0;

        this.mArguments = arguments;
    }

    public Class<? extends Fragment> getFragmentClass() {
        return this.mFragmentClass;
    }

    public Bundle getArguments() {
        return this.mArguments;
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public void setTitle(final CharSequence title) {
        this.mTitle = title;
    }

    public int getIconResId() {
        return this.mIconResId;
    }

    public Drawable getIcon() {
        return mIcon;
    }
}