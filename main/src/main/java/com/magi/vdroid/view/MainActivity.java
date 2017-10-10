package com.magi.vdroid.view;

import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.ViewGroup;

import com.magi.base.ifaces.IBackPressedConcerned;
import com.magi.base.ui.activities.BaseFragmentActivity;
import com.magi.base.utils.MainTabs;
import com.magi.base.utils.VIntent;
import com.magi.base.utils.VirtualBoardUtils;
import com.magi.base.widget.TintDrawable;
import com.magi.base.widget.pager.IOnItemInitialedListener;
import com.magi.base.widget.pager.IPagerAdapter;
import com.magi.base.widget.pager.PagerItem;
import com.magi.base.widget.pager.VPagerAdapter;
import com.magi.vdroid.R;
import com.magi.vdroid.databinding.ActivityMainBinding;
import com.magi.vdroid.view.fragment.BaseRecyclerFragment;
import com.magi.vdroid.view.fragment.ParentFragment;
import com.magi.vdroid.view.fragment.TestFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseFragmentActivity implements TabLayout.OnTabSelectedListener, IOnItemInitialedListener {

    private List<TabLayout.OnTabSelectedListener> mTabObservers = new ArrayList<>();

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        VPagerAdapter adapter = new VPagerAdapter(this);
        adapter.setOnItemInitialedListener(this);
        this.mBinding.mainPager.setAdapter(adapter);
        this.mBinding.mainPager.setOffscreenPageLimit(3);
        this.mBinding.mainTab.setupWithViewPager(mBinding.mainPager);
        setupTab();
    }

    private void setupTab() {
        VPagerAdapter tabAdapter = (VPagerAdapter) mBinding.mainPager.getAdapter();
        final int tabColorRes = R.color.main_tab_color;
        final List<PagerItem> items = new ArrayList<>();

        final TintDrawable iconHome = new TintDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable
                .ic_bottomtabbar_feed, this.getTheme()));
        iconHome.setTintColor(ResourcesCompat.getColorStateList(this.getResources(), tabColorRes, this.getTheme()));
        final Bundle feedItemArgs = new Bundle();
        feedItemArgs.putString(ParentFragment.KEY_HOST, TestFragment.class.getName());
        items.add(new PagerItem(ParentFragment.class, getString(R.string.tab_name_feed), iconHome, feedItemArgs));


        Bundle exploreItemArgs = new Bundle();
        exploreItemArgs.putString(ParentFragment.KEY_HOST, TestFragment.class.getName());
        TintDrawable iconExplore = new TintDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_bottomtabbar_discover, getTheme()));
        iconExplore.setTintColor(ResourcesCompat.getColorStateList(getResources(), tabColorRes, getTheme()));
        items.add(new PagerItem(ParentFragment.class, getString(R.string.tab_name_explore), iconExplore,
                exploreItemArgs));
        tabAdapter.setPagerItems(items, true);

        for (int i = 0; i < tabAdapter.getCount(); i++) {
            this.mBinding.mainTab.getTabAt(i).setIcon(tabAdapter.getPagerItem(i).getIcon());
        }


    }

    public int getCurrentTab() {
        return mBinding.mainPager.getCurrentItem();
    }

    /**
     * 需要监听 Tab 事件的地方，实现 {@link TabLayout.OnTabSelectedListener}，在这里进行注册。
     */
    @SuppressWarnings("unused")
    public void registerTabObserver(final TabLayout.OnTabSelectedListener tabObserver) {
        if (!mTabObservers.contains(tabObserver)) {
            mTabObservers.add(tabObserver);
        }
    }

    /**
     * 需要监听 Tab 事件的地方，实现 {@link TabLayout.OnTabSelectedListener}，在这里取消注册。
     */
    @SuppressWarnings("unused")
    public void unregisterTabObserver(final TabLayout.OnTabSelectedListener tabObserver) {
        if (mTabObservers.contains(tabObserver)) {
            mTabObservers.remove(tabObserver);
        }
    }


    @Override
    public void startFragmentForResult(@Nullable VIntent intent, Fragment target, int requestCode, View view) {
        if (intent == null) {
            return;
        }

        mLatestFragmentName = null;

        intentPopSelf(intent);

        if (intent.isHideKeyboard()) {
            VirtualBoardUtils.hideKeyBoard(this, mBinding.getRoot().getWindowToken());
        }

        boolean startTopLevelFragment = tryToStartTopLevelFragment(intent);

        if (startTopLevelFragment) {
            return;
        }

        int tab = intent.getPriorityTab();
        if (tab != MainTabs.CURRENT_TAB && tab != getCurrentTab()) {
            mBinding.mainPager.setCurrentItem(tab, false);
        }

        final Fragment currentFragment = this.getCurrentDisplayFragment();

        if (currentFragment != null && intent.getTag().equals(getCurrentDisplayFragment().getTag())) {
            return;
        }

        if (intent.isOverlay()) {
            addFragmentToOverlay(intent, target, requestCode);
        } else if (this.getCurrentTabItemContainer() != null) {
            final Fragment fragment = Fragment.instantiate(this, intent.getClassName(), intent.getArguments());
            if (target != null) {
                fragment.setTargetFragment(target, requestCode);
            }

            if (intent.getElementAnimation() != null)
                this.addFragmentToBackStack(fragment, intent, intent.getElementAnimation());
            else
                this.addFragmentToBackStack(fragment, intent);
        }

    }

    private boolean tryToStartTopLevelFragment(VIntent intent) {
        int count = mBinding.mainPager.getChildCount();
        for (int i = 0; i < count; i++) {
            PagerItem pagerItem = ((IPagerAdapter) mBinding.mainPager.getAdapter()).getPagerItem(i);
            if (intent.getClassName().equals(pagerItem.getArguments().getString("host"))) {
                mBinding.mainPager.setCurrentItem(i, false);
                final ParentFragment currentTabItemContainer = this.getCurrentTabItemContainer();
                if (currentTabItemContainer != null) {
                    currentTabItemContainer.onReselected();
                }
                return true;
            }
        }
        return false;
    }

    protected void addFragmentToBackStack(final Fragment fragment, final VIntent intent) {
        addFragmentToBackStack(fragment, intent, null);
    }

    protected void addFragmentToBackStack(final Fragment fragment, final VIntent intent, VIntent
            .SharedElementAnimation animation) {
        if (getCurrentTabItemContainer() == null)
            return;

        this.getCurrentTabItemContainer().addChild(fragment, intent, animation);
    }

    @Override
    public void intentPopSelf(VIntent intent) {
        if (intent == null) {
            return;
        }

        if (intent.isPopSelf()) {
            popupCurrentDisplayFragment();
        }
    }

    protected void popupCurrentDisplayFragment() {
        final int overlayItemCount = this.getSupportFragmentManager().getBackStackEntryCount();
        if (overlayItemCount > 0) {
            try {
                this.getSupportFragmentManager().popBackStackImmediate();
            } catch (IllegalStateException ex) {
                // java.lang.IllegalStateException - Can not perform this action after onSaveInstanceState
                ex.printStackTrace();
            }

        } else if (getCurrentTabItemContainer() != null) {
            try {
                this.getCurrentTabItemContainer().onBackPressed(true);
            } catch (IllegalStateException ex) {
                // java.lang.IllegalStateException - Can not perform this action after onSaveInstanceState
                ex.printStackTrace();
            }
        }
    }

    @Override
    public String getLatestFragmentName() {
        return mLatestFragmentName;
    }

    @Override
    public boolean isCurrentDisplayFragment(Fragment fragment) {
        return false;
    }

    @Override
    public Fragment getCurrentDisplayFragment() {
        final int overlayItemCount = this.getSupportFragmentManager().getBackStackEntryCount();
        if (overlayItemCount > 0) {
            final String currentName = this.getSupportFragmentManager().getBackStackEntryAt(overlayItemCount - 1)
                    .getName();
            return this.getSupportFragmentManager().findFragmentByTag(currentName);
        }

        if (getCurrentTabItemContainer() == null || getCurrentTabItemContainer().isDetached() ||
                !getCurrentTabItemContainer().isAdded()) {
            return null;
        }

        return this.getCurrentTabItemContainer().getCurrentChild();
    }

    @Override
    public ViewGroup getRootView() {
        return (ViewGroup) mBinding.getRoot();
    }

    @Override
    public void popBack(boolean popBack) {
        onBackPressed(true);
    }

    private void onBackPressed(boolean isForce) {
        final int overlayItemCount = this.getSupportFragmentManager().getBackStackEntryCount();

        ParentFragment currentTabItemContainer = this.getCurrentTabItemContainer();

        if (overlayItemCount > (currentTabItemContainer == null ? 1 : 0)) {
            boolean handle = false;
            if (getCurrentDisplayFragment() instanceof IBackPressedConcerned && !isForce) {
                handle = ((IBackPressedConcerned) getCurrentDisplayFragment()).onBackPressed();
            }
            if (!handle) {
                try {
                    getSupportFragmentManager().popBackStackImmediate();
                } catch (IllegalStateException ex) {
                    // java.lang.IllegalStateException - Can not perform this action after onSaveInstanceState
                }
            }
        } else {
            if (currentTabItemContainer != null) {
                if (!currentTabItemContainer.onBackPressed(isForce)) {
                    if (this.mBinding.mainPager.getCurrentItem() != 0) {
                        this.mBinding.mainPager.setCurrentItem(0, false);
                    } else {
                        super.onBackPressed();
                    }
                }
            } else {
                this.supportFinishAfterTransition();
            }
        }

    }

    @Nullable
    public ParentFragment getCurrentTabItemContainer() {
        return this.mBinding.mainPager != null ?
                (ParentFragment) ((IPagerAdapter) this.mBinding.mainPager.getAdapter()).getCurrentPrimaryItem()
                : null;
    }

    @Override
    public void addFragmentToOverlay(@Nullable VIntent pIntent, Fragment pTargetFragment, int pRequestCode) {
        if (pIntent == null) {
            return;
        }

        final FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();

        final Fragment fragment = Fragment.instantiate(this, pIntent.getClassName(), pIntent.getArguments());

        if (pTargetFragment != null) {
            fragment.setTargetFragment(pTargetFragment, pRequestCode);
        }

        fragmentTransaction.add(R.id.overlay_container, fragment, pIntent.getTag());

        fragmentTransaction.addToBackStack(pIntent.getTag());

        if (pIntent.getElementAnimation() != null)
            fragmentTransaction.addSharedElement(pIntent.getElementAnimation().getSharedView(), pIntent
                    .getElementAnimation().getSharedTransitionName());

        fragmentTransaction.commitAllowingStateLoss();
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void onTabSelected(TabLayout.Tab tab) {

        this.mBinding.mainPager.setCurrentItem(tab.getPosition(), false);

        for (TabLayout.OnTabSelectedListener tabObserver : mTabObservers) {
            tabObserver.onTabSelected(tab);
        }

        this.mBinding.mainTab.post(() -> {
            if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 && MainActivity.this.isFinishing())
                    || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && MainActivity.this
                    .isDestroyed())) {
                return;
            }

            final ParentFragment currentTabItemContainer = MainActivity.this.getCurrentTabItemContainer();
            if (currentTabItemContainer != null) {
                currentTabItemContainer.onSelected();
            }

        });

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        for (TabLayout.OnTabSelectedListener tabObserver : mTabObservers) {
            tabObserver.onTabUnselected(tab);
        }
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        for (TabLayout.OnTabSelectedListener tabObserver : mTabObservers) {
            tabObserver.onTabReselected(tab);
        }
        final ParentFragment currentTabItemContainer = this.getCurrentTabItemContainer();
        if (currentTabItemContainer != null) {
            currentTabItemContainer.onReselected();
        }
    }

    @Override
    public void onItemInitialed(int position, Fragment fragment) {

    }
}
