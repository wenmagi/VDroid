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

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joosun @ Zhihu Inc.
 * @author mthli @ Zhihu Inc.
 * @since 04-23-2015
 */
public class VPagerAdapter extends FragmentPagerAdapter implements IPagerAdapter {
    private final List<PagerItem> mPagerItems;

    private Context mContext;

    private FragmentManager mManager;

    private Fragment mCurrentPrimaryItem;

    private IOnPrimaryItemChangedListener mPrimaryItemChangeListener;
    private IOnItemInitialedListener mOnItemInitialedListener;

    // 保存已经存在的 Fragment 实例
    private SparseArray<Fragment> mFragments = new SparseArray<>();

    public VPagerAdapter(final FragmentActivity activity) {
        super(activity.getSupportFragmentManager());

        this.mContext = activity;

        this.mManager = activity.getSupportFragmentManager();

        this.mPagerItems = new ArrayList<>();
    }

    public VPagerAdapter(final Fragment fragment) {
        super(fragment.getChildFragmentManager());

        this.mContext = fragment.getActivity();

        this.mManager = fragment.getChildFragmentManager();

        this.mPagerItems = new ArrayList<>();
    }

    @SuppressWarnings("unused")
    public void addPagerItem(final PagerItem pagerItem) {
        this.mPagerItems.add(pagerItem);

        this.notifyDataSetChanged();
    }

    @SuppressWarnings("unused")
    public void addPagerItems(List<PagerItem> pagerItems) {
        this.mPagerItems.addAll(pagerItems);
        this.notifyDataSetChanged();
    }

    /**
     * 之前命名为#addPagerItems，是有问题的，改为setPagerItems
     *
     * @param forceClearOldItems 设为true将干掉所在fragmentManager里面所有的Fragment，这样是有问题和限制的，
     *                            一是有可能此Fragment是还有ViewPager之外的其他Fragment，
     *                            二是有可能导致应用状态不正确，单独的Tab在初始化的时候如果是由父Fragment主导初始化就可能会有问题。
     */
    public void setPagerItems(List<PagerItem> pagerItems, boolean forceClearOldItems) {
        if (forceClearOldItems) {
            this.clearItems();
        }

        this.mPagerItems.addAll(pagerItems);

        this.notifyDataSetChanged();
    }

    public void clearItems() {
        this.mPagerItems.clear();
        this.notifyDataSetChanged();

        // getItemPosition() 返回 POSITION_NONE 并不能达到预期的效果，这里强行移除掉旧的 Fragment 实例，
        // 是比较 dirty 和暴力的做法。但是要做的比较优雅，需要对核心代码做一些改动，有风险，有时间的时候再做。
        // http://www.cnblogs.com/dancefire/archive/2013/01/02/why-notifydatasetchanged-does-not-work.html
        // http://linroid.com/2015/01/29/view-not-updated-after-call-FragmentPagerAdapter-notifyDataSetChanged/
        @SuppressWarnings("RestrictedApi") List<Fragment> fragments = mManager.getFragments();
        if (fragments != null) {
            FragmentTransaction ft = mManager.beginTransaction();
            fragments.stream().filter(fragment -> fragment != null).forEach(ft::remove);
            ft.commitNowAllowingStateLoss();
        }
    }

    public PagerItem getPagerItem(final int position) {
        return this.mPagerItems.get(position);
    }

    @Override
    public Fragment getItem(final int position) {
        final PagerItem pagerItem = this.getPagerItem(position);

        Fragment fragment = Fragment.instantiate(this.mContext, pagerItem.getFragmentClass().getName(), pagerItem
                .getArguments());
        mFragments.put(position, fragment);

        if (mOnItemInitialedListener != null) {
            mOnItemInitialedListener.onItemInitialed(position, fragment);
        }

        return fragment;
    }

    @Override
    public int getItemPosition(final Object object) {
        // 强制触发 notifyDataSetChanged 时刷新 fragment
        return android.support.v4.view.PagerAdapter.POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        return this.getPagerItem(position).getTitle();
    }

    @Override
    public int getCount() {
        return this.mPagerItems.size();
    }

    @Override
    public void setPrimaryItem(final ViewGroup container, final int position, final Object object) {
        super.setPrimaryItem(container, position, object);

        final Fragment newFragment = (Fragment) object;

        if (this.mPrimaryItemChangeListener != null && newFragment != this.mCurrentPrimaryItem) {
            this.mPrimaryItemChangeListener.onPrimaryItemChanged(container, position, this.mCurrentPrimaryItem,
                    newFragment);
        }

        this.mCurrentPrimaryItem = newFragment;
    }

    public Fragment getCurrentPrimaryItem() {
        return this.mCurrentPrimaryItem;
    }

    /**
     * 获取某个位置的已经存在的 Fragment 实例
     */
    public Fragment retrieveFragment(int position) {
        return mFragments.get(position);
    }

    public void setOnItemInitialedListener(IOnItemInitialedListener onItemInitialedListener) {
        mOnItemInitialedListener = onItemInitialedListener;
    }

    public void setOnPrimaryItemChangedListener(final IOnPrimaryItemChangedListener primaryItemChangeListener) {
        this.mPrimaryItemChangeListener = primaryItemChangeListener;
    }

}