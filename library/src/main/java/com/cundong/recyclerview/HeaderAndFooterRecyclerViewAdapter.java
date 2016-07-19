package com.cundong.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by cundong on 2015/10/9.
 * <p/>
 * RecyclerView.Adapter with Header and Footer
 */
public class HeaderAndFooterRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * HeaderView类型总和的上限
     */
    private static final int TYPE_HEADER_VIEW_MAX = 999;
    /**
     * HeaderView 的基数值
     */
    private static final int TYPE_HEADER_VIEW_BASE = Integer.MIN_VALUE;
    /**
     * FooterView 的基数值
     */
    private static final int TYPE_FOOTER_VIEW_BASE = TYPE_HEADER_VIEW_MAX + 1;
    /**
     * InnerAdapter ViewType 的起始基数值
     */
    private static final int TYPE_INNER_ADAPTER_VIEW_BASE = Integer.MAX_VALUE / 2;

    /**
     * RecyclerView使用的，真正的Adapter
     */
    private RecyclerView.Adapter<RecyclerView.ViewHolder> mInnerAdapter;

    private ArrayList<View> mHeaderViews = new ArrayList<>();
    private ArrayList<View> mFooterViews = new ArrayList<>();

    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {

        @Override
        public void onChanged() {
            super.onChanged();
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            notifyItemRangeChanged(positionStart + getHeaderViewsCount(), itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            notifyItemRangeInserted(positionStart + getHeaderViewsCount(), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            notifyItemRangeRemoved(positionStart + getHeaderViewsCount(), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            int headerViewsCountCount = getHeaderViewsCount();
            notifyItemRangeChanged(fromPosition + headerViewsCountCount, toPosition + headerViewsCountCount + itemCount);
        }
    };

    public HeaderAndFooterRecyclerViewAdapter() {
    }

    public HeaderAndFooterRecyclerViewAdapter(RecyclerView.Adapter innerAdapter) {
        setAdapter(innerAdapter);
    }

    /**
     * 设置adapter
     *
     * @param adapter
     */
    public void setAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {

        if (mInnerAdapter != null) {
            notifyItemRangeRemoved(getHeaderViewsCount(), mInnerAdapter.getItemCount());
            mInnerAdapter.unregisterAdapterDataObserver(mDataObserver);
        }

        this.mInnerAdapter = adapter;
        mInnerAdapter.registerAdapterDataObserver(mDataObserver);
        notifyItemRangeInserted(getHeaderViewsCount(), mInnerAdapter.getItemCount());
    }

    public RecyclerView.Adapter getInnerAdapter() {
        return mInnerAdapter;
    }

    public void addHeaderView(View header) {

        if (header == null) {
            throw new RuntimeException("header is null, not supported");
        }
        if (mHeaderViews.size() == TYPE_HEADER_VIEW_MAX) {
            throw new RuntimeException("your HeaderView count must < " + TYPE_HEADER_VIEW_MAX);
        }

        mHeaderViews.add(header);
        this.notifyDataSetChanged();
    }

    public void addFooterView(View footer) {

        if (footer == null) {
            throw new RuntimeException("footer is null, not supported");
        }

        mFooterViews.add(footer);
        this.notifyDataSetChanged();
    }

    /**
     * 返回第一个FooterView
     *
     * @return
     */
    public View getFooterView() {
        return getFooterViewsCount() > 0 ? mFooterViews.get(0) : null;
    }

    /**
     * 返回第一个HeaderView
     *
     * @return
     */
    public View getHeaderView() {
        return getHeaderViewsCount() > 0 ? mHeaderViews.get(0) : null;
    }

    public void removeHeaderView(View view) {
        mHeaderViews.remove(view);
        this.notifyDataSetChanged();
    }

    public void removeFooterView(View view) {
        mFooterViews.remove(view);
        this.notifyDataSetChanged();
    }

    public int getHeaderViewsCount() {
        return mHeaderViews.size();
    }

    public int getFooterViewsCount() {
        return mFooterViews.size();
    }

    public boolean isHeader(int position) {
        return getHeaderViewsCount() > 0 && position == 0;
    }

    public boolean isFooter(int position) {
        int lastPosition = getItemCount() - 1;
        return getFooterViewsCount() > 0 && position == lastPosition;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int headerViewsCountCount = getHeaderViewsCount();
        if (viewType < TYPE_HEADER_VIEW_BASE + headerViewsCountCount) {
            //viewType是HeaderView
            return new ViewHolder(mHeaderViews.get(viewType - TYPE_HEADER_VIEW_BASE));
        } else if (viewType >= TYPE_INNER_ADAPTER_VIEW_BASE) {
            //innerAdapter 的 viewType是从 Integer.MAX_VALUE / 2 开始的
            int realViewType = viewType - TYPE_INNER_ADAPTER_VIEW_BASE;
            return mInnerAdapter.onCreateViewHolder(parent, realViewType);
        } else{
            //viewType是FooterView
            return new ViewHolder(mFooterViews.get(viewType - TYPE_FOOTER_VIEW_BASE));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //bindViewHolder，刷新ViewHolder中的内容
        int headerViewsCountCount = getHeaderViewsCount();
        if (position >= headerViewsCountCount && position < headerViewsCountCount + mInnerAdapter.getItemCount()) {
            //position在innerAdapter范围
            mInnerAdapter.onBindViewHolder(holder, position - headerViewsCountCount);
        } else {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                ((StaggeredGridLayoutManager.LayoutParams) layoutParams).setFullSpan(true);
            }
        }
    }

    @Override
    public int getItemCount() {
        return getHeaderViewsCount() + getFooterViewsCount() + mInnerAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        int innerCount = mInnerAdapter.getItemCount();
        int headerViewsCount = getHeaderViewsCount();
        if (position < headerViewsCount) {
            //position 小于 header 总数
            return TYPE_HEADER_VIEW_BASE + position;
        } else if (position >= (headerViewsCount + innerCount)) {
            //position > (headViewCount + innerAdapterCount) ,被视为 footer类型
            return TYPE_FOOTER_VIEW_BASE + position - headerViewsCount - innerCount;
        } else {
            //其他都是 innerAdapter 的类型
            int innerItemViewType = mInnerAdapter.getItemViewType(position - headerViewsCount);
            if (innerItemViewType >= (Integer.MAX_VALUE - TYPE_INNER_ADAPTER_VIEW_BASE)) {
                throw new IllegalArgumentException("your adapter's return value of getViewTypeCount() must < "+(Integer.MAX_VALUE - TYPE_INNER_ADAPTER_VIEW_BASE));
            }
            return innerItemViewType + TYPE_INNER_ADAPTER_VIEW_BASE;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
