package com.magi.base.widget.recycler;

import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.magi.base.widget.recycler.VViewHolder.OnRecyclerItemClickListener;
import static com.magi.base.widget.recycler.VViewHolder.OnRecyclerItemLongClickListener;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-24-2017
 */

public abstract class VRecyclerAdapter extends RecyclerView.Adapter<VViewHolder> {

    public static class AdapterListener {

        public void onCreate(final VViewHolder viewHolder) {
        }

        public void onPreBind(final VViewHolder viewHolder, int position) {

        }

        public void onBind(final VViewHolder viewHolder, int position) {
        }

        public void onUnbind(final VViewHolder viewHolder) {
        }

        public void onAttachedToWindow(final VViewHolder viewHolder) {
        }

        public void onDetachedFromWindow(final VViewHolder viewHolder) {
        }
    }

    private final ArrayMap<Integer, VViewType> mViewTypes = new ArrayMap<>();

    private final List<VRecyclerItem> mItems = new ArrayList<>();

    private AdapterListener mAdapterListener;

    private OnRecyclerItemClickListener mItemOnClickListener;

    private OnRecyclerItemLongClickListener mItemLongClickListener;

    public VRecyclerAdapter() {
        super();

        final List<VViewType> viewTypes = this.onCreateViewTypes();

        for (final VViewType viewType : viewTypes) {
            this.mViewTypes.put(viewType.getTypeId(), viewType);
        }
    }

    public VRecyclerAdapter(OnRecyclerItemClickListener itemOnClickListener) {
        this();

        this.mItemOnClickListener = itemOnClickListener;
    }

    public void setItemOnClickListener(OnRecyclerItemClickListener itemOnClickListener) {
        mItemOnClickListener = itemOnClickListener;
    }

    public void setItemLongClickListener(OnRecyclerItemLongClickListener itemLongClickListener) {
        mItemLongClickListener = itemLongClickListener;
    }

    public void setRecyclerItem(final int position, final VRecyclerItem recyclerItem) {
        this.mItems.set(position, recyclerItem);
        this.notifyItemChanged(position);
    }

    public void addRecyclerItem(final int position, final VRecyclerItem recyclerItems) {
        this.mItems.add(position, recyclerItems);

        this.notifyItemInserted(position);
    }

    public void addRecyclerItem(final VRecyclerItem... recyclerItems) {
        final int oldItemCount = this.getItemCount();

        Collections.addAll(this.mItems, recyclerItems);

        this.notifyItemRangeInserted(oldItemCount, recyclerItems.length);
    }

    public void addRecyclerItemList(final List<VRecyclerItem> recyclerItemList) {
        final int oldItemCount = this.getItemCount();

        this.mItems.addAll(recyclerItemList);

        this.notifyItemRangeInserted(oldItemCount, recyclerItemList.size());
    }

    public void addRecyclerItemList(final int position, final List<VRecyclerItem> recyclerItemList) {
        this.mItems.addAll(position, recyclerItemList);

        this.notifyItemRangeInserted(position, recyclerItemList.size());
    }

    public VRecyclerItem getRecyclerItem(final int position) {
        return this.mItems.get(position);
    }

    public List<VRecyclerItem> getRecyclerItems() {
        return this.mItems;
    }

    public void changeRecyclerItem(final VRecyclerItem recyclerItem, final int position) {
        this.mItems.set(position, recyclerItem);

        this.notifyItemChanged(position);
    }

    public void removeData(Object data) {
        for (VRecyclerItem item : mItems) {
            if (item.getData() == data) {
                removeRecyclerItem(item);
                break;
            }
        }
    }

    public void removeRecyclerItem(final int position) {
        this.mItems.remove(position);

        this.notifyItemRemoved(position);
    }

    public void removeRecyclerItem(final VRecyclerItem recyclerItem) {
        final int index = this.mItems.indexOf(recyclerItem);

        this.removeRecyclerItem(index);
    }

    public void removeRecyclerItem(final int position, final int count) {
        this.mItems.subList(position, position + count).clear();

        this.notifyItemRangeRemoved(position, count);
    }

    // 这个方法只供 BaseAdvancePagingFragment 及其子类在数据第一次刷新的时候用，当页面有头部的时候，
    // 如果用 RecyclerView.Adapter#notifyItemRangeRemoved() 更新数据会导致闪屏；
    // (fromIndex < 0 || toIndex > size) -> IndexOutOfBoundsException
    // (fromIndex > toIndex) -> IllegalArgumentException
    // https://developer.android.com/reference/java/util/AbstractList.html#subList(int, int)
    public void removeListItemsFrom(final int position) {
        // if (position > getItemCount()) {
        //     return;
        // }

        this.mItems.subList(position, getItemCount()).clear();

        this.notifyDataSetChanged();
    }

    public void clearAllRecyclerItem() {
        this.mItems.clear();

        this.notifyDataSetChanged();
    }

    // 判断是否包含指定的 ViewType
    public boolean containViewType(final int typeId) {
        return mViewTypes != null && mViewTypes.containsKey(typeId);
    }

    protected abstract List<VViewType> onCreateViewTypes();

    @Override
    public VViewHolder onCreateViewHolder(final ViewGroup parentView, final int type) {
        final VViewType viewType = this.mViewTypes.get(type);

        if (viewType != null) {
            final Class<? extends VViewHolder> viewHolderClass = viewType.getViewHolderClass();

            final View view = LayoutInflater.from(parentView.getContext()).inflate(viewType.getLayoutResourceId(),
                    parentView, false);

            try {
                final VViewHolder viewHolder = viewHolderClass.getDeclaredConstructor(View.class).newInstance(view);

                if (this.mAdapterListener != null) {
                    this.mAdapterListener.onCreate(viewHolder);
                }

                if (mItemOnClickListener != null) {
                    viewHolder.setOnClickListener(this.mItemOnClickListener);
                }

                if (mItemLongClickListener != null) {
                    viewHolder.setOnLongClickListener(this.mItemLongClickListener);
                }

                viewHolder.setAdapter(this);

                return viewHolder;
            } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException |
                    InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        } else {
            throw new IllegalStateException("Can't find ViewHolder Class for viewType: " + type);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(final VViewHolder holder, final int position) {
        if (this.mAdapterListener != null) {
            this.mAdapterListener.onPreBind(holder, position);
        }

        holder.onBindData(this.mItems.get(position).getData());

        if (this.mAdapterListener != null) {
            this.mAdapterListener.onBind(holder, position);
        }
    }

    @Override
    public void onViewRecycled(final VViewHolder viewHolder) {
        super.onViewRecycled(viewHolder);

        viewHolder.onUnbind();

        if (this.mAdapterListener != null) {
            this.mAdapterListener.onUnbind(viewHolder);
        }
    }

    @Override
    public int getItemViewType(final int position) {
        return this.mItems.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return this.mItems.size();
    }

    @Override
    public void onViewAttachedToWindow(final VViewHolder viewHolder) {
        super.onViewAttachedToWindow(viewHolder);

        viewHolder.onAttachedToWindow();

        if (this.mAdapterListener != null) {
            this.mAdapterListener.onAttachedToWindow(viewHolder);
        }
    }

    @Override
    public void onViewDetachedFromWindow(final VViewHolder viewHolder) {
        super.onViewDetachedFromWindow(viewHolder);

        viewHolder.onDetachedFromWindow();

        if (this.mAdapterListener != null) {
            this.mAdapterListener.onDetachedFromWindow(viewHolder);
        }
    }

    public void setAdapterListener(final AdapterListener adapterListener) {
        this.mAdapterListener = adapterListener;
    }

}
