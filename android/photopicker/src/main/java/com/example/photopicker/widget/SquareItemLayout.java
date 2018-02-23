package com.example.photopicker.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by xujian on 2018/1/7.
 */

public class SquareItemLayout extends RelativeLayout {

    public SquareItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        /**
         * getDefaultSize
         * Utility to return a default size. Uses the supplied size if the MeasureSpec imposed no
         * constraints. Will get larger if allowed by the MeasureSpec.
         *
         */
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));

        /**
         * int getMeasuredWidth ()
         * Return the full width measurement information for this view as computed by the most
         * recent call to measure(int, int).
         */
        int childWidthSize = getMeasuredWidth();
        /**
         * makeMeasureSpec
         * MeasureSpec是父控件提供给子View的一个参数，作为设定自身大小参考，只是个参考，要多大，还是View自己说了算。
         * Creates a measure specification based on the supplied size and mode
         * EXACTLY
         * Measure specification mode: The parent has determined an exact size for the child.
         * The child is going to be given those bounds regardless of how big it wants to be.
         * MeasureSpec 包括size 还有 model，这里表示父控件为子View指定确切大小，希望子View完全按照自己给定尺寸来处理。
         * 当前View指定layout_width layout_height为matchParent的时候将会是个 边长为 父布局宽度的正方形；
         * 当前view指定 确切的dp的时候，那么将会是个 边长为 dp 的正方形
         */
        heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
