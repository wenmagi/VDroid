package com.magi.base.ui.fragments;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.magi.base.R;
import com.magi.base.utils.DisplayUtils;
import com.magi.base.widget.pager.IZHPagerAdapter;
import com.magi.base.widget.pager.PagerItem;
import com.magi.base.widget.pager.ZHPagerAdapter;

import java.util.List;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-24-2017
 */

public abstract class BaseTabsFragment extends BaseToolbarFragment implements ViewPager.OnPageChangeListener {

    protected IZHPagerAdapter mZHPagerAdapter;

    private int mMaxTabsTranslationY;

    protected TabLayout mTabLayout;

    protected ViewPager mViewPager;


    @Override
    protected View onCreateContentView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout root = new RelativeLayout(this.getContext());
        mTabLayout = new TabLayout(getContext());
        mTabLayout.setId(R.id.tab_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTabLayout.setElevation(DisplayUtils.dpToPixel(this.getContext(), 4));
        }
        root.addView(mTabLayout);


        mViewPager = new ViewPager(getContext());
        mViewPager.setId(R.id.tab_viewpager);
        RelativeLayout.LayoutParams contentLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams
                .MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentLayoutParams.addRule(RelativeLayout.BELOW, R.id.tab_layout);
        root.addView(mViewPager, contentLayoutParams);

        return super.onCreateContentView(layoutInflater, container, savedInstanceState);
    }


    @Override
    public void onViewCreated(final View pView, @Nullable final Bundle pSavedInstanceState) {
        super.onViewCreated(pView, pSavedInstanceState);

        this.mZHPagerAdapter = createPagerAdapter();

        final List<PagerItem> pagerItems = this.onCreatePagerItems();

        this.mZHPagerAdapter.setPagerItems(pagerItems, false);

        mViewPager.setAdapter((PagerAdapter) this.mZHPagerAdapter);

        mTabLayout.setupWithViewPager(mViewPager);

        for (int i = 0; i < pagerItems.size(); i++) {
            int iconResId = pagerItems.get(i).getIconResId();
            Drawable icon = pagerItems.get(i).getIcon();
            if (iconResId != 0) {
                mTabLayout.getTabAt(i).setIcon(iconResId);
            } else if (icon != null) {
                mTabLayout.getTabAt(i).setIcon(icon);
            }
        }

        mTabLayout.setTranslationY(0);
    }

    protected IZHPagerAdapter createPagerAdapter() {
        return new ZHPagerAdapter(this);
    }

    public abstract List<PagerItem> onCreatePagerItems();

    public void onListScrolled(final long pTotalScrolledDistance) {

    }

    public int getMaxTabsTranslationY() {
        return this.mMaxTabsTranslationY;
    }

    @Nullable
    public Fragment getCurrentTabItem() {
        return this.mZHPagerAdapter.getCurrentPrimaryItem();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageSelected(int position) {
    }

    public void selectPage(int position) {
        mViewPager.setCurrentItem(position);
    }

}