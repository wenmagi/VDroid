package com.magi.vdroid.view.widget.factory;

import com.magi.base.widget.recycler.VViewType;
import com.magi.vdroid.R;
import com.magi.vdroid.view.widget.holder.EmptyViewHolder;
import com.magi.vdroid.view.widget.holder.ErrorCardViewHolder;
import com.magi.vdroid.view.widget.holder.HeightSpaceViewHolder;
import com.magi.vdroid.view.widget.holder.ProgressViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用中所有 Recycler Item 的卡片类型
 *
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-25-2017
 */

public class ViewTypeFactory {

    private static int sCursor = 0;

    public static final int VIEW_TYPE_SPACE = sCursor++;

    public static final int VIEW_TYPE_PROGRESS = sCursor++;

    public static final int VIEW_TYPE_EMPTY = sCursor++;

    public static final int VIEW_TYPE_ERROR = sCursor++;

    public static final int VIEW_TYPE_NO_MORE = sCursor++;

    public static List<VViewType> viewTypes = new ArrayList<>();

    static {
        // Space with Height
        viewTypes.add(new VViewType(ViewTypeFactory.VIEW_TYPE_SPACE, R.layout.recycler_item_space_by_height,
                HeightSpaceViewHolder.class));

        viewTypes.add(new VViewType(ViewTypeFactory.VIEW_TYPE_EMPTY, R.layout.recycler_item_empty, EmptyViewHolder
                .class));

        viewTypes.add(new VViewType(ViewTypeFactory.VIEW_TYPE_PROGRESS, R.layout.recycler_item_progress, ProgressViewHolder
                    .class));

        viewTypes.add(new VViewType(ViewTypeFactory.VIEW_TYPE_ERROR, R.layout.recycler_item_error_card, ErrorCardViewHolder
                    .class));
    }
}
