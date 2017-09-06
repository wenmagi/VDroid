package com.magi.base.ui.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.magi.base.R;
import com.magi.base.ui.activities.BaseFragmentActivity;
import com.magi.base.utils.VirtualBoardUtils;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-24-2017
 */

public abstract class BaseToolbarFragment extends BaseFragment implements Toolbar.OnMenuItemClickListener {
    private boolean mHasToolbar = false;

    private boolean mOverlay = false;

    private Toolbar mToolbar;

    private int mIconColor = Color.WHITE;

    @SuppressWarnings("unused")
    protected void setHasToolbar(final boolean hasToolbar) {
        mHasToolbar = hasToolbar;
    }

    @SuppressWarnings("unused")
    protected void setOverlay(final boolean overlay) {
        mOverlay = overlay;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater layoutInflater, final ViewGroup container, final Bundle
            savedInstanceState) {

        if (!mHasToolbar) {
            return onCreateContentView(layoutInflater, container, savedInstanceState);
        }

        final RelativeLayout layout = new RelativeLayout(getContext());

        mToolbar = onCreateCustomToolbar();

        mToolbar.setId(R.id.toolbar);

        final RelativeLayout.LayoutParams ToolbarLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout
                .LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams
                .WRAP_CONTENT);

        onToolbarCreated(mToolbar, savedInstanceState);

        final RelativeLayout.LayoutParams contentViewLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout
                .LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams
                .MATCH_PARENT);

        if (!mOverlay) {
            contentViewLayoutParams.addRule(RelativeLayout.BELOW, mToolbar.getId());
        }

        final View contentView = onCreateContentView(layoutInflater, layout, savedInstanceState);

        layout.addView(contentView, contentViewLayoutParams);
        layout.addView(mToolbar, ToolbarLayoutParams);

        return layout;
    }

    protected View onCreateContentView(LayoutInflater layoutInflater, ViewGroup container, Bundle
            savedInstanceState) {
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Activity activity = getActivity();
        if (activity != null && activity.getCurrentFocus() != null) {
            VirtualBoardUtils.hideKeyBoard(activity, activity.getCurrentFocus().getWindowToken());
        }
    }

    protected void checkToolbar() {
        if (!mHasToolbar) {
            throw new IllegalStateException("Please call setHasToolbar(true)!");
        }
    }

    // ========================================================
    // set/get Toolbar

    @SuppressWarnings("unused")
    public void setToolbarTitle(@StringRes final int resId) {
        checkToolbar();

        if (mToolbar != null) {
            mToolbar.setTitle(resId);
        }
    }


    @SuppressWarnings("unused")
    public void setToolbarTitle(final CharSequence title) {
        checkToolbar();
        if (mToolbar != null) {
            mToolbar.setTitle(title);
        }
    }

    @SuppressWarnings("unused")
    public void setToolbarDisplayHomeAsUp() {
        setToolbarNavigation(R.drawable.ic_arrow_back, mBackBtnClickListener);
    }

    @SuppressWarnings("unused")
    public void setToolbarDisplayHomeAsClose() {
        setToolbarNavigation(R.drawable.ic_clear, mBackBtnClickListener);
    }

    public void setToolbarNavigation(@DrawableRes final int resId, final View.OnClickListener clickListener) {
        checkToolbar();

        Drawable drawable = ContextCompat.getDrawable(getActivity(), resId);
        setToolbarNavigation(drawable, clickListener);
    }

    @SuppressWarnings("unused")
    protected void setBackBtnClickListener(View.OnClickListener onClickListener) {
        mBackBtnClickListener = onClickListener;
    }

    @SuppressWarnings("unused")
    public void setToolbarNavigation(final Drawable drawable, final View.OnClickListener clickListener) {
        checkToolbar();

        if (mToolbar != null) {

            drawable.mutate().setColorFilter(mIconColor, PorterDuff.Mode.SRC_IN);

            mToolbar.setNavigationIcon(drawable);

            mToolbar.setNavigationOnClickListener(clickListener);
        }
    }

    @SuppressWarnings("unused")
    public void setToolbarIconColor(@ColorInt final int color) {
        checkToolbar();

        mIconColor = color;

        if (mToolbar.getNavigationIcon() != null) {
            mToolbar.getNavigationIcon().mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

        if (mToolbar.getLogo() != null) {
            mToolbar.getLogo().mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

        if (mToolbar.getMenu() != null) {
            Menu menu = mToolbar.getMenu();

            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);

                if (item.getIcon() != null) {
                    item.getIcon().mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                }
            }
        }

        if (mToolbar.getOverflowIcon() != null) {
            mToolbar.getOverflowIcon().mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    @SuppressWarnings("unused")
    public void setSystemBarTitleColor(@ColorInt final int color) {
        checkToolbar();
        mToolbar.setTitleTextColor(color);
    }

    private Toolbar onCreateCustomToolbar() {
        return new Toolbar(getContext());
    }

    protected void onToolbarCreated(Toolbar mToolbar, Bundle savedInstanceState) {
        checkToolbar();

        mToolbar.setNavigationIcon(new DrawerArrowDrawable(mToolbar.getContext()));
        mToolbar.setTitle(R.string.app_name);

        //noinspection RestrictedApi
        onCreateOptionsMenu(mToolbar.getMenu(), new SupportMenuInflater(mToolbar.getContext()));

        onPrepareOptionsMenu(mToolbar.getMenu());

        mToolbar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onToolbarClick();
            }
        });

        mToolbar.setOnMenuItemClickListener(this);
    }

    protected void onToolbarClick() {
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return onOptionsItemSelected(item);
    }

    @SuppressWarnings("unused")
    protected void invalidateOptionsMenu() {
        if (getActivity() == null) {
            return;
        }

        onPrepareOptionsMenu(mToolbar.getMenu());
    }

    private View.OnClickListener mBackBtnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(final View view) {
            VirtualBoardUtils.hideKeyBoard(view.getContext(), view.getWindowToken());
            if (getActivity() instanceof BaseFragmentActivity) {
                ((BaseFragmentActivity) getActivity()).popBack(true);
            }
        }
    };
}
