package com.magi.vdroid.view.widget.adapter;

import com.magi.base.widget.recycler.VRecyclerAdapter;
import com.magi.base.widget.recycler.VViewType;
import com.magi.vdroid.view.widget.factory.ViewTypeFactory;

import java.util.List;


/**
 * 应用中使用此 Adapter 即可
 *
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-25-2017
 */

public class BaseRecyclerAdapter extends VRecyclerAdapter {
    @Override
    protected List<VViewType> onCreateViewTypes() {
        return ViewTypeFactory.viewTypes;
    }
}
