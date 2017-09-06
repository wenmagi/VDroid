package com.magi.vdroid.view.widget.factory;

import com.magi.base.widget.recycler.VRecyclerItem;
import com.magi.vdroid.view.widget.holder.EmptyViewHolder;
import com.magi.vdroid.view.widget.holder.ErrorCardViewHolder;
import com.magi.vdroid.view.widget.holder.NoMoreTipViewHolder;

/**
 * 当前 Recycler Item 对应的 ViewType 和 绑定的数据
 *
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-26-2017
 */

public class RecyclerItemFactory {

    public static VRecyclerItem<Integer> createSpaceItemByHeight(final int pHeight) {
        return new VRecyclerItem<>(ViewTypeFactory.VIEW_TYPE_SPACE, pHeight);
    }

    public static VRecyclerItem<Integer> createProgressItem() {
        return new VRecyclerItem<>(ViewTypeFactory.VIEW_TYPE_PROGRESS, null);
    }

    public static VRecyclerItem<EmptyViewHolder.EmptyInfo> createEmptyItem(final EmptyViewHolder.EmptyInfo pEmptyInfo) {
        return new VRecyclerItem<>(ViewTypeFactory.VIEW_TYPE_EMPTY, pEmptyInfo);
    }

    public static VRecyclerItem<NoMoreTipViewHolder.Tip> createNoMoreTipItem(final NoMoreTipViewHolder.Tip pTip) {
        return new VRecyclerItem<>(ViewTypeFactory.VIEW_TYPE_NO_MORE, pTip);
    }

    public static VRecyclerItem<ErrorCardViewHolder.ErrorInfo> createErrorItem(final ErrorCardViewHolder.ErrorInfo pErrorInfo) {
        return new VRecyclerItem<>(ViewTypeFactory.VIEW_TYPE_ERROR, pErrorInfo);
    }

}
