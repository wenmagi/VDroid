package com.magi.vdroid.view.fragment

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.magi.base.widget.recycler.VRecyclerAdapter
import com.magi.base.widget.recycler.VRecyclerItem
import com.magi.modle.Paging
import com.magi.modle.TestResponseList


/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 09-27-2017
 */
class FeedsFragment : BaseRecyclerFragment<TestResponseList>(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onLoadingMore(pPaging: Paging?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setProgressViewOffset() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateAdapter(view: View?, savedInstanceState: Bundle?): VRecyclerAdapter {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateLayoutManager(view: View?, savedInstanceState: Bundle?): RecyclerView.LayoutManager {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRefreshing(fromUser: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toRecyclerItem(result: TestResponseList?): MutableList<VRecyclerItem<Any>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}