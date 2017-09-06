package com.magi.vdroid.view.fragment;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-23-2017
 */

/*
 * Copyright (c) 2016 Zhihu Inc.
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

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.magi.base.ifaces.IBackPressedConcerned;
import com.magi.base.ui.fragments.BaseFragment;
import com.magi.base.ui.fragments.BaseTabsFragment;
import com.magi.base.utils.VIntent;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ParentFragment extends Fragment {

    private static final String KEY_HOST_INITIALIZED = "zhihu:parent_fragment:host_initialized";

    private final AtomicInteger mCounter = new AtomicInteger(0);

    private FragmentManager mFragmentManager;

    private String mHostFragmentClassName;

    private boolean mInitializedHost = false;

    private Fragment mHostFragment;

    private static final int mStackLimit = 10;

    private TreeMap<String, Fragment> mChildren = new TreeMap<>();

    public ParentFragment() {
    }

    @Override
    public void onCreate(@Nullable final Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);

        this.mFragmentManager = this.getChildFragmentManager();

        final Bundle arguments = this.getArguments();

        if (arguments != null) {
            this.mHostFragmentClassName = arguments.getString("host");
        } else {
            throw new IllegalStateException("must set host fragment class name");
        }

    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater pLayoutInflater, @Nullable final ViewGroup pContainer, @Nullable
    final Bundle pSavedInstanceState) {

        final FrameLayout layout = new FrameLayout(this.getContext());

        layout.setId(android.R.id.content);

        return layout;
    }

    @Override
    public void onViewCreated(final View pView, @Nullable final Bundle pSavedInstanceState) {
        super.onViewCreated(pView, pSavedInstanceState);

        if (pSavedInstanceState != null) {
            this.mInitializedHost = pSavedInstanceState.getBoolean(ParentFragment.KEY_HOST_INITIALIZED);
        }
    }

    private void initializeHostIfNeed() {
        if (this.mInitializedHost) {
            return;
        }

        final FragmentTransaction transaction = this.mFragmentManager.beginTransaction();
        transaction.add(android.R.id.content, this.mHostFragment = Fragment.instantiate(this.getContext(), this
                .mHostFragmentClassName), "host");
        transaction.commitNowAllowingStateLoss();

        this.mInitializedHost = true;
    }

    private void onBackStackChanged() {
        final Fragment fragment = this.getCurrentChild();

        if (fragment instanceof BaseFragment) {
            ((BaseFragment) fragment).onScreenDisplaying();
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle pOutState) {
        super.onSaveInstanceState(pOutState);

        pOutState.putBoolean(ParentFragment.KEY_HOST_INITIALIZED, this.mInitializedHost);
    }

    public boolean onBackPressed(boolean isForce) {
        if (this.getCurrentChild() instanceof IBackPressedConcerned && !isForce) {
            final boolean handle = ((IBackPressedConcerned) this.getCurrentChild()).onBackPressed();
            if (handle) {
                return true;
            }
        }

        if (this.mChildren.size() > 0) {

            final Fragment currentRemoveFragment = this.mChildren.pollLastEntry().getValue();

            final FragmentTransaction transaction = this.mFragmentManager.beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

            if (this.mChildren.size() > 0) {
                transaction.show(this.mChildren.lastEntry().getValue());
            }

            transaction.remove(currentRemoveFragment);
            transaction.commitNowAllowingStateLoss();

            this.onBackStackChanged();

            return true;
        } else {
            return false;
        }
    }

    public void addChild(final Fragment fragment, final VIntent intent) {
        addChild(fragment, intent, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void addChild(final Fragment fragment, final VIntent intent, VIntent.SharedElementAnimation animation) {

        this.initializeHostIfNeed();

        final FragmentTransaction transaction = this.getChildFragmentManager().beginTransaction();

        final Fragment currentFragment = this.getCurrentChild();

        if (currentFragment != null && this.mHostFragment != currentFragment) {
            transaction.hide(currentFragment);
        }

        if (animation != null) {

            fragment.setAllowEnterTransitionOverlap(false);
            fragment.setAllowReturnTransitionOverlap(false);

            View sharedView = animation.getSharedView();
            fragment.setSharedElementEnterTransition(animation.getSharedElementAnimation());
            transaction.addSharedElement(sharedView, animation.getSharedTransitionName());
        }

        if (intent.isSingleTask()) {
            final Map.Entry<String, Fragment> lastSameChild = this.findLastChild(intent.getTag());

            if (lastSameChild != null) {
                transaction.remove(lastSameChild.getValue());
            }
        }

        final String key = this.getKey(intent.getTag());

        transaction.add(android.R.id.content, fragment, key);

        transaction.commitNowAllowingStateLoss();

        this.mChildren.put(key, fragment);

        if (this.mChildren.size() > this.mStackLimit) {
            transaction.remove(this.mChildren.pollFirstEntry().getValue());
        }

        this.onBackStackChanged();
    }

    private String getKey(final String tag) {
        return String.format(Locale.getDefault(), "%05d-%s", this.mCounter.incrementAndGet(), tag);
    }

    private Map.Entry<String, Fragment> findLastChild(final String tag) {
        for (final Map.Entry<String, Fragment> entry : this.mChildren.entrySet()) {
            if (entry.getKey().endsWith(tag)) {
                return entry;
            }
        }

        return null;
    }

    public void clearAllChildren() {
        final FragmentTransaction transaction = this.mFragmentManager.beginTransaction();

        for (final Map.Entry<String, Fragment> entry : this.mChildren.entrySet()) {
            transaction.remove(entry.getValue());
        }

        transaction.commitNowAllowingStateLoss();

        this.mChildren.clear();

        this.onBackStackChanged();
    }

    public Fragment getCurrentChild() {

        return this.mChildren.size() > 0 ? this.mChildren.lastEntry().getValue() : this.mHostFragment;
    }

    @MainThread
    public void onSelected() {
        final Fragment fragment = this.getCurrentChild();

        if (fragment instanceof BaseFragment) {
            // MainTab 切换时调用 GA 统计
            ((BaseFragment) fragment).onScreenDisplaying();
        }

        this.initializeHostIfNeed();
    }

    @MainThread
    public void onReselected() {
        if (this.mChildren.size() > 0) {
            this.clearAllChildren();
        } else {
            if (this.mHostFragment instanceof BaseRecyclerFragment) {
                ((BaseRecyclerFragment) this.mHostFragment).scrollToTopOrRefresh(true);

            } else if (this.mHostFragment instanceof BaseTabsFragment) {
                final Fragment currentChildTabItem = ((BaseTabsFragment) this.mHostFragment).getCurrentTabItem();

                if (currentChildTabItem != null && currentChildTabItem instanceof BaseRecyclerFragment) {
                    ((BaseRecyclerFragment) currentChildTabItem).scrollToTopOrRefresh(true);
                }
            }
        }
    }

    public interface Child {

        boolean isShowBottomNavigation();
    }
}