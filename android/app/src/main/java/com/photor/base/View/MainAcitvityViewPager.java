package com.photor.base.View;


import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MainAcitvityViewPager extends ViewPager {

    private static final String TAG = "MAIN_ACTIVITY_VIEW_PAGER";
    private boolean isCanScroll = false;

    public MainAcitvityViewPager(Context context) {
        super(context);
    }

    public MainAcitvityViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCanScroll(boolean canScroll) {
        isCanScroll = canScroll;
    }

    // 为了解决 ViewPager 滑动到 Camera 选项卡的时候，而发生的Camera页面手势失效的问题
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return this.isCanScroll && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.isCanScroll && super.onInterceptTouchEvent(ev);
    }
}
