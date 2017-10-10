package com.magi.vdroid.view.fragment;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.magi.base.widget.recycler.VRecyclerAdapter;
import com.magi.base.widget.recycler.VRecyclerItem;
import com.magi.modle.Paging;
import com.magi.modle.TestResponseList;

import java.util.List;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 09-27-2017
 */

public class TestFragment extends BaseRecyclerFragment<TestResponseList> {
    @Override
    protected void onLoadingMore(Paging pPaging) {

    }

    @Override
    protected void setProgressViewOffset() {

    }

    @Override
    protected VRecyclerAdapter onCreateAdapter(View view, Bundle savedInstanceState) {
        return null;
    }

    @Override
    protected RecyclerView.LayoutManager onCreateLayoutManager(View view, Bundle savedInstanceState) {
        return null;
    }

    @Override
    protected void onRefreshing(boolean fromUser) {

    }

    @Override
    protected List<VRecyclerItem> toRecyclerItem(TestResponseList result) {
        return null;
    }
}
