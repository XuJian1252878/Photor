package com.photor.album.views;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;  // 当前布局中一共有多少列
    private int spacing;
    private boolean includeEdge;

    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildAdapterPosition(view);  // 获取当前RecyclerView中Item在第几个位置
        int column = position % spanCount; // 获得当前的item处于第几列

        if (includeEdge) {
            // 最开始和最结尾是有间距的
            outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)
            // 在 Grid的第一行加上上间距信息
            if (position < spanCount) {
                outRect.top = spacing;
            }
            outRect.bottom = spacing;
        } else {
            // 最开始和最结尾是没有间距的
            outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
            outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            if (position >= spanCount) {
                outRect.top = spacing;
            }
        }
    }
}
