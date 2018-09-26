package com.photor.dofcalculator.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.photor.R;
import com.photor.dofcalculator.util.UtilManager;

/**
 * Created by xujian on 2017/12/29.
 */

public class DepthOfFieldView extends View {

    // 自定义控件DepthOfFieldView的自定义属性
    private float mTargetRatio; // 指明的是物体占整个景深显示部分的百分比
    private float mFontSize; // 指明景深显示的字体的大小信息

    // 控件中的照相机图标信息
    private Drawable mCameraLogo;

    // 控件中物品的图标信息
    private Drawable mTargetLogo;

    // 画笔信息
    private Paint mTextPainter;
    private Paint mLinePainter;

    // 控件显示景深信息相关变量
    private double mDepthOfField = 0.4; // 景深长度
    private double mDistance = 1.0; // 被摄物体距离
    private double mNearLimit = 0.8; // 前景深距离
    private double mFarLimit = 1.2; // 后景深距离

    public DepthOfFieldView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    // 对自定义控件进行一些初始化操作，比如加载自定义属性等等
    public void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DepthOfFieldView,
                0,0);

        try{
            mTargetRatio = typedArray.getFloat(R.styleable.DepthOfFieldView_targetPosition, 0.333f);
            mFontSize = typedArray.getDimension(R.styleable.DepthOfFieldView_textSize, 20);
        } finally {
            typedArray.recycle();
        }

        // 加载照相机图标信息，加载Drawable目录下图标信息的方法
        mCameraLogo = ContextCompat.getDrawable(getContext(), R.drawable.camera_logo);

        // 加载物品图标信息
        mTargetLogo = ContextCompat.getDrawable(getContext(), R.drawable.tree);

        // 设置画笔操作
        mTextPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPainter.setTextSize(mFontSize);
        mTextPainter.setTextAlign(Paint.Align.CENTER);

        mLinePainter = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePainter.setStrokeWidth(10.f);
    }


    // 设置照相机图标在自定义控件中的位置
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // 调试信息
        Log.d("Trace", String.format("left=%d, top=%d, right=%d, bottom=%d",
                left, top, right, bottom));
        Log.d("Trace", String.format("leftPadding=%d, rightPadding=%d, " +
                        "topPadding=%d, bottomPadding=%d",
                getLeftPaddingOffset(), getRightPaddingOffset(),
                getTopPaddingOffset(), getBottomPaddingOffset()));
    }

    /**
     * 关于自定义控件的重绘流程：Measure (onMeasure) -> Layout (onLayout) -> draw (onDraw)
     * 所以说可以在onLayout中先确定drawable的绘画区域，然后在onDraw中再进行实际上的绘画
     */
    // 自定义控件的重绘操作
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 对与照相机图片进行缩放，将其放置于控件的最左部分的中央位置
        int cameraSize = getHeight() / 4; // 在一个正方形的区域里面显示照相机的图片信息
        // 设置mCameraLogo 将要被绘画的矩形区域，但是此时没有进行绘画，只是指明了绘画的区域
        mCameraLogo.setBounds(0, getHeight() / 2 - cameraSize / 2,
                cameraSize, getHeight() / 2 + cameraSize / 2);
        mCameraLogo.draw(canvas); // 这个其实在onLayout里面重绘也是可以的

        // 获得景深表示图形的开始以及结束的横坐标
        float beginLine = mCameraLogo.getBounds().right; // 开始的x坐标
        float endLine = getWidth(); // 结束的x坐标

        // 关于景深显示的线条x坐标
        float nearLine; // 前景深所在的x坐标信息
        float farLine;  // 后景深所在的x坐标信息
        float targetLine; // 物体显示所在的x坐标信息

        targetLine = (endLine - beginLine) * mTargetRatio + beginLine;

        // 绘制被摄物品图标
        int targetSize = getHeight() / 2;
        mTargetLogo.setBounds((int)targetLine - targetSize / 2, getHeight() / 2 - targetSize / 2,
                (int)targetLine + targetSize / 2, getHeight() / 2 + targetSize / 2);
        mTargetLogo.draw(canvas);

        nearLine = (float)(((targetLine - beginLine) * mNearLimit) / mDistance + beginLine);
        if (mFarLimit == Float.POSITIVE_INFINITY || mFarLimit > mDistance / mTargetRatio) {
            // ...
            farLine = endLine;
        } else {
            farLine = (float)((targetLine - beginLine) * mFarLimit / mDistance + beginLine);
        }

        // 绘制 照相机 至 前向景深起始点 之间的线条
        mLinePainter.setColor(Color.RED);
        // 起始条竖线
        canvas.drawLine(beginLine, getHeight() / 2,
                beginLine, getHeight() / 2 + targetSize / 2, mLinePainter);
        // 横线条
        canvas.drawLine(beginLine, getHeight() / 2 + targetSize / 2,
                nearLine, getHeight() / 2 + targetSize / 2, mLinePainter);

        // 绘制 前向景深起始点 至 被拍摄物体之间 的线条
//        canvas.drawLine(nearLine, 0, nearLine, getHeight() - mFontSize, mLinePainter);
        mLinePainter.setColor(Color.GREEN);
        // 起始竖线条
        canvas.drawLine(nearLine, getHeight() / 2,
                nearLine, getHeight() / 2 + targetSize / 2, mLinePainter);
        // 横线条
        canvas.drawLine(nearLine, getHeight() / 2 + targetSize / 2,
                targetLine, getHeight() / 2 + targetSize / 2, mLinePainter);
        // 绘制前向景深的文字
        String nearLimitText = UtilManager.getInstance().getCompatDistanceText(getContext(), mNearLimit);
        Rect nearLimitBounds = new Rect();
        mTextPainter.getTextBounds(nearLimitText, 0, nearLimitText.length(), nearLimitBounds);
        // 根据显示空间决定字体的显示位置
        if (nearLine - beginLine < nearLimitBounds.width()) {
            mTextPainter.setTextAlign(Paint.Align.LEFT);
        } else {
            mTextPainter.setTextAlign(Paint.Align.RIGHT);
        }
        canvas.drawText(nearLimitText, nearLine, getHeight() / 2 + mFontSize / 2, mTextPainter);


        // 绘制远向景深的线条
//        canvas.drawLine(farLine, 0, farLine, getHeight() - mFontSize, mLinePainter);
        mLinePainter.setColor(Color.GREEN);
        // 结束竖线条
        canvas.drawLine(farLine, getHeight() / 2,
                farLine, getHeight() / 2 + targetSize / 2, mLinePainter);
        // 横线条
        canvas.drawLine(targetLine, getHeight() / 2 + targetSize / 2,
                farLine, getHeight() / 2 + targetSize / 2, mLinePainter);
        // 绘制后向景深文字
        String farLimitText = UtilManager.getInstance().getCompatDistanceText(getContext(), mFarLimit);
        Rect farLimitBounds = new Rect();
        mTextPainter.getTextBounds(farLimitText, 0, farLimitText.length(), farLimitBounds);
        // 根据显示空间决定字体的显示位置
        if (endLine - farLine < farLimitBounds.width()) {
            mTextPainter.setTextAlign(Paint.Align.RIGHT);
        } else {
            mTextPainter.setTextAlign(Paint.Align.LEFT);
        }
        canvas.drawText(farLimitText, farLine, getHeight() / 2 + mFontSize / 2, mTextPainter);

        // 绘制物体处的线条
//        canvas.drawLine(targetLine, 0, targetLine, getHeight() - mFontSize, mLinePainter);

        // 绘制景深控件的线条
//        canvas.drawLine(nearLine, getHeight() - mFontSize, farLine, getHeight() - mFontSize, mLinePainter);

        // 设置景深总长度
        mTextPainter.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(UtilManager.getInstance().getCompatDistanceText(getContext(), mDepthOfField),
                (nearLine + farLine) / 2.f, getHeight(), mTextPainter);
    }


    // 为景深自定义控件设置数据，并且重绘
    public void setData(double depthOfField, double distance, double nearLimit, double farLimit) {
        mDepthOfField = depthOfField;
        mDistance = distance;
        mNearLimit = nearLimit;
        mFarLimit = farLimit;

        invalidate();
    }
}
