package com.photor.base.adapters.event;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by xujian on 2018/2/28.
 */

public class PhotoItemClickListener implements RecyclerView.OnItemTouchListener {

    private OnItemClickListener mItemClickListener;
    private GestureDetector mGestureDetector;

    public PhotoItemClickListener(Context context, OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
        this.mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

        View childView = rv.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mGestureDetector != null && mGestureDetector.onTouchEvent(e)) {
            mItemClickListener.onItemClick(childView, rv.getChildLayoutPosition(childView));
            return true;
        }

        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

}
