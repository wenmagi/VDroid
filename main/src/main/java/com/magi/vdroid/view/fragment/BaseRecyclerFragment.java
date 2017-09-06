package com.magi.vdroid.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.magi.base.R;
import com.magi.base.ui.fragments.BaseToolbarFragment;
import com.magi.base.ui.function.FunctionLazyLoad;
import com.magi.base.utils.DisplayUtils;
import com.magi.base.widget.recycler.VRecyclerAdapter;
import com.magi.base.widget.recycler.VRecyclerItem;
import com.magi.base.widget.recycler.VRecyclerView;
import com.magi.modle.Paging;
import com.magi.modle.ResponseList;
import com.magi.vdroid.view.widget.factory.RecyclerItemFactory;
import com.magi.vdroid.view.widget.factory.ViewTypeFactory;
import com.magi.vdroid.view.widget.holder.EmptyViewHolder;
import com.magi.vdroid.view.widget.holder.ErrorCardViewHolder;
import com.magi.vdroid.view.widget.holder.NoMoreTipViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-24-2017
 */

abstract public class BaseRecyclerFragment<T extends ResponseList> extends BaseToolbarFragment implements
        SwipeRefreshLayout
                .OnRefreshListener, ObservableScrollViewCallbacks {

    protected SwipeRefreshLayout mSwipeRefreshLayout;

    public VRecyclerView mRecyclerView;

    public VRecyclerAdapter mAdapter;

    protected boolean mLoading;

    protected boolean mLoadedAll;

    protected boolean mIsEmpty = true;

    protected String mErrorMsg;

    private Paging mPaging;

    public RecyclerView getRecyclerView() {
        return this.mRecyclerView;
    }

    protected RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

            int totalItemCount = layoutManager.getItemCount();
            int lastVisibleItemPosition;

            if (LinearLayoutManager.class.isInstance(layoutManager)) {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else {
                lastVisibleItemPosition = 0;
            }

            if (totalItemCount > 0 && (totalItemCount - lastVisibleItemPosition - 1 <= 10) && !mLoading &&
                    !mLoadedAll && mErrorMsg == null && mPaging != null) {
                loadMore();
            }
        }
    };

    protected void loadMore() {
        if (mLoading) {
            return;
        }
        mLoading = true;
        onLoadingMore(mPaging);
    }

    protected abstract void onLoadingMore(Paging pPaging);

    @Override
    protected View onCreateContentView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        return layoutInflater.inflate(onCreatePagingLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        this.mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        this.mRecyclerView = (VRecyclerView) view.findViewById(R.id.recycler_view);

        this.mSwipeRefreshLayout.setOnRefreshListener(this);

        this.mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = onCreateLayoutManager(view, savedInstanceState);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter = onCreateAdapter(view, savedInstanceState));

        setRecyclerViewPadding(this.mRecyclerView);

        this.mRecyclerView.addOnScrollListener(mOnScrollListener);
        this.mRecyclerView.setScrollViewCallbacks(this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setProgressViewOffset();
        firstRefresh();
    }

    @Override
    public void onDestroyView() {
        this.mRecyclerView.removeOnScrollListener(mOnScrollListener);
        this.mRecyclerView.setScrollViewCallbacks(null);

        super.onDestroyView();
    }

    protected abstract void setProgressViewOffset();

    protected abstract VRecyclerAdapter onCreateAdapter(View view, Bundle savedInstanceState);

    protected abstract RecyclerView.LayoutManager onCreateLayoutManager(View view, Bundle savedInstanceState);

    protected int onCreatePagingLayoutId() {
        return R.layout.fragment_recycler;
    }

    protected void scrollToTopOrRefresh(boolean fromUser) {
        final RecyclerView recyclerView = this.getRecyclerView();

        if (recyclerView != null && recyclerView.getLayoutManager() instanceof LinearLayoutManager && (
                (LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition() != 0) {
            onTopReturn();
        } else {
            if (!mLoading) {
                refresh(fromUser);
            }
        }
    }

    protected void firstRefresh() {
        mSwipeRefreshLayout.post(() -> {
            if (getActivity() != null && isAdded() && !isDetached()) {

                mSwipeRefreshLayout.setRefreshing(true);

                refresh(false);
            }
        });
    }

    private void refresh(boolean fromUser) {
        if (this instanceof FunctionLazyLoad.OnLazyLoadListener) {
            FunctionLazyLoad lazyLoadFunc = ((FunctionLazyLoad.OnLazyLoadListener) this).getLazyLoadFunction();
            if (lazyLoadFunc != null && !lazyLoadFunc.isLoaded())
                return;
        }


        mLoading = true;

        mLoadedAll = false;

        mErrorMsg = null;

        mPaging = null;

        if (!this.mSwipeRefreshLayout.isRefreshing()) {
            this.mSwipeRefreshLayout.setRefreshing(true);
        }

        onRefreshing(fromUser);
    }

    abstract protected void onRefreshing(boolean fromUser);

    protected void postRefreshCompleted(T result) {
        postRefreshCompleted(result, false);
    }

    protected void postRefreshCompleted(T result, boolean fromCache) {
        if (getActivity() == null) {
            return;
        }

        mSwipeRefreshLayout.setRefreshing(false);

        if (!fromCache) {
            mLoading = false;
        }

        mIsEmpty = false;

        if (result == null || result.data == null) {
            // 网络加载失败，但原先有缓存的情况，仍然展示缓存
            if (mAdapter.getItemCount() > 0) {
                return;
            }

            // show ErrorCard
            mErrorMsg = getResources().getString(R.string.text_default_error_message);
        } else {
            setPaging(result.paging);

            mLoadedAll = (result.data.size() == 0 || result.paging == null || result.paging.isEnd);

            mErrorMsg = null;
        }

        if (getHeaderItemCount() == 0) {
            mAdapter.clearAllRecyclerItem();
        } else {
            mAdapter.removeListItemsFrom(getHeaderItemCount());
        }

        List<VRecyclerItem> recyclerItemList = toRecyclerItem(result);
        if (recyclerItemList == null) {
            recyclerItemList = new ArrayList<>();
        }
        if (getTopSpaceHeight() > 0) {
            recyclerItemList.add(0, RecyclerItemFactory.createSpaceItemByHeight(getTopSpaceHeight()));
        }

        if (mErrorMsg != null) {
            mIsEmpty = true;
            recyclerItemList.add(createEmptyItem(true));
        } else if ((result != null ? result.data.size() : 0) == 0) {
            // show Empty
            mIsEmpty = true;
            recyclerItemList.add(createEmptyItem(false));
        }

        // 来自缓存的回调不应该添加 progress item
        if (!mLoadedAll && mErrorMsg == null && !fromCache) {
            recyclerItemList.add(RecyclerItemFactory.createProgressItem());
            if (recyclerItemList.size() < 10) {
                loadMore();
            }
        }

        mAdapter.addRecyclerItemList(recyclerItemList);

        addNoMoreTip();
    }

    protected void postRefreshFailed(Throwable exception) {
        postRefreshCompleted(null);
    }

    protected void postLoadMoreCompleted(T pResult) {
        if (getActivity() == null || mAdapter.getItemCount() == 0) {
            return;
        }

        if (pResult == null || pResult.data == null) {
            // show ErrorCard
            mErrorMsg = getResources().getString(R.string.text_default_network_error);
        } else {
            setPaging(pResult.paging);

            mLoadedAll = (pResult.data.size() == 0 || pResult.paging == null || pResult.paging.isEnd);
        }

        mSwipeRefreshLayout.setRefreshing(false);

        mLoading = false;
        mIsEmpty = false;

        final List<VRecyclerItem> recyclerItemList = toRecyclerItem(pResult);

        if (mErrorMsg != null) {
            recyclerItemList.add(RecyclerItemFactory.createErrorItem(new ErrorCardViewHolder.ErrorInfo(mErrorMsg, v -> {
                mErrorMsg = null;


                // add progress
                mAdapter.removeRecyclerItem(mAdapter.getItemCount() - 1);
                mAdapter.addRecyclerItem(RecyclerItemFactory.createProgressItem());

                loadMore();
            }, v -> {
                mAdapter.removeRecyclerItem(mAdapter.getItemCount() - 1);
                v.postDelayed(() -> {
                    mErrorMsg = null;
                    mAdapter.addRecyclerItem(RecyclerItemFactory.createProgressItem());
                }, 500);
            })));
        }

        mAdapter.addRecyclerItemList(mAdapter.getItemCount() - 1, recyclerItemList);

        if (mLoadedAll || mErrorMsg != null) {
            // remove progress
            mAdapter.removeRecyclerItem(mAdapter.getItemCount() - 1);
        }

        addNoMoreTip();
    }

    protected void postLoadMoreFailed(Throwable exception) {
        postLoadMoreCompleted(null);
    }

    protected void addNoMoreTip() {
        if (!mAdapter.containViewType(ViewTypeFactory.VIEW_TYPE_NO_MORE)) {
            return;
        }

        if (mLoadedAll && !mIsEmpty && mErrorMsg == null) {
            mAdapter.addRecyclerItem(mAdapter.getItemCount(), RecyclerItemFactory.createNoMoreTipItem(buildNoMoreTip
                    ()));
        }
    }

    protected NoMoreTipViewHolder.Tip buildNoMoreTip() {
        return new NoMoreTipViewHolder.Tip(DisplayUtils.dpToPixel(getContext(), 72.0f), getString(R.string
                .no_more_content_tip));
    }

    private VRecyclerItem createEmptyItem(boolean hasError) {
        if (hasError) {
            return RecyclerItemFactory.createEmptyItem(new EmptyViewHolder.EmptyInfo(mErrorMsg, R.drawable
                    .ic_network_error,
                    getEmptyViewHeight(), R.string.text_default_retry, v -> {
                mAdapter.clearAllRecyclerItem();
                mSwipeRefreshLayout.setRefreshing(true);
                refresh(true);
            }));
        } else {
            return RecyclerItemFactory.createEmptyItem(getEmptyInfo());
        }
    }

    protected abstract List<VRecyclerItem> toRecyclerItem(T result);

    public void onTopReturn() {
        if (this.mRecyclerView == null) {
            return;
        }

        if (mRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            int item = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            if (item > 10) {
                mRecyclerView.scrollToPosition(10);
                mRecyclerView.smoothScrollToPosition(0);
            } else {
                mRecyclerView.smoothScrollToPosition(0);
            }
        }
    }

    @Override
    protected void onToolbarClick() {
        onTopReturn();
    }

    @Override
    public void onRefresh() {
        refresh(true);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    public void setRecyclerViewPadding(VRecyclerView recyclerViewPadding) {
    }

    public void setPaging(Paging paging) {
        this.mPaging = paging;
    }

    public int getHeaderItemCount() {
        return 0;
    }

    public int getTopSpaceHeight() {
        return 0;
    }

    public int getEmptyViewHeight() {
        return getRecyclerView().getHeight() - getTopSpaceHeight() - getRecyclerView().getPaddingTop() -
                getRecyclerView().getPaddingBottom() - (DisplayUtils.dpToPixel(getContext(), 48.0f));
    }

    protected EmptyViewHolder.EmptyInfo getEmptyInfo() {
        return new EmptyViewHolder.EmptyInfo(R.string.text_default_empty, R.drawable.ic_empty, getEmptyViewHeight());
    }
}
