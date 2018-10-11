package com.photor.album.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class PagerRecyclerView extends RecyclerView {

    private int currPosition = -1;

    public PagerRecyclerView(Context context) {
        super(context);
        if (!isInEditMode()) {
            init();
        }
    }

    public PagerRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init();
        }
    }

    public PagerRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            init();
        }
    }

    //RecyclerView在24.2.0版本中新增了SnapHelper这个辅助类，
    // 用于辅助RecyclerView在滚动结束时将Item对齐到某个位置。特别是列表横向滑动时，
    // 很多时候不会让列表滑到任意位置，而是会有一定的规则限制，这时候就可以通过SnapHelper来定义对齐规则了。
    private void init() {
        new PagerSnapHelper().attachToRecyclerView(this);
    }

    /**
     * 滚动照片时发生的回调信息
     */
    public interface OnPageChangeListener {
        void onPageChanged(int oldPosition, int newPosition);
    }

    public void setOnPageChangeListener(final OnPageChangeListener onPageChangeListener) {
        if (onPageChangeListener == null) return;
        if (currPosition == -1) {
            currPosition = ((LinearLayoutManager)getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            if (currPosition == -1) currPosition = 0;
            addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int oldPosition = currPosition;
                    int newPosition = ((LinearLayoutManager) getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                    if (newPosition != -1) currPosition = newPosition;
                    if (currPosition != oldPosition) onPageChangeListener.onPageChanged(oldPosition, currPosition);
                }
            });
        }
    }
}
