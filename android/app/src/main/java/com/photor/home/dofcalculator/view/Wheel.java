package com.photor.home.dofcalculator.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.photor.R;


/**
 * Created by xujian on 2017/12/28.
 */

// 1. 创建OnGestureListener监听函数，使用了构造类的方式
public class Wheel extends View implements GestureDetector.OnGestureListener {

    public Wheel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    public interface WheelAdapter {
        int getCount();
        String getItemText(int position);
    }
    // 对应显示Wheel控件中的Wheel个数（有多少节）以及每一节上对应的数字是什么
    public class DummyAdapter implements WheelAdapter {

        // Wheel控件中的Wheel个数（有多少节）
        @Override
        public int getCount() {
            return 10;
        }

        // Wheel控件中的Wheel每一节上对应的数字是什么（这时候没有带上单位）
        @Override
        public String getItemText(int position) {
            return String.format("%d", position); //
        }
    }
    private WheelAdapter mAdapter;
    public void setAdapter(WheelAdapter adapter) {
        mAdapter = adapter;
        if (mAdapter == null) {
            mAdapter = new DummyAdapter();
        }
        // 重绘Wheel控件
        invalidate();
    }


    public interface OnScrollListener {
        void onWheelScroll(int position, float offset);
    }
    private OnScrollListener mScrollListener;
    public void setOnScrollListener(OnScrollListener listener) {
        mScrollListener = listener;
    }

    private GestureDetector mGestureDetector;

    // 用于记录标准的固定位置（屏幕正中间）相对于Wheel第一节的起始位置移动的距离（像素计量）
    private float mFirstOffset = 0.f;
    // 自定义Wheel控件的自定义·属性值
    private float mNodeWidth;  // Wheel中一个Wheel控件宽度
    private float mTextSize; // Wheel 中字符的大小

    private Paint mPainter = new Paint(Paint.ANTI_ALIAS_FLAG);

    public void init(Context context, AttributeSet attrs) {
        // 在<declare-styleable>中定义的<attr>在Wheel中需要通过调用theme的obtainStyledAttributes方法来读取解析属性值。
        // 获得 直接在Wheel控件中指定的xml属性（attrs）；或者是在wheel的xml style属性中指明了style文件（R.styleable.Wheel）
        // defStyleAttr 前两种情况下都找不到属性的值，那么在当前Theme中看 defStyleAttr 属性对应的是不是style文件，如果是，那么从这个style文件中找。
        // 一旦指明defStyleAttr，那么就算没有拿到这个属性的值，那么也不会往第四个参数上去找，即使第四个参数有指定。
        // defStyleRes 前三种情况都会走啊不到对应属性值的前提下，查看defStyleRes 对应的style下是否有对应的 属性值
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Wheel,
                0,0);

        try {
            // 获取自定义控件的自定义属性值
            mNodeWidth = typedArray.getDimension(R.styleable.Wheel_nodeWidth, 60);
            mTextSize = typedArray.getDimension(R.styleable.Wheel_nodeTextSize, 20);
        } finally {
            //在使用完typedArray之后，要调用recycle方法回收资源
            typedArray.recycle();
        }

        // 设置自定义画笔操作
        mPainter.setTextAlign(Paint.Align.LEFT);
        mPainter.setTextSize(mTextSize);

        // 设置控件对手势操作的响应
        // 2. 创建GestureDetector的实例
        mGestureDetector = new GestureDetector(context, this);
        mGestureDetector.setIsLongpressEnabled(false); // 设置长按无效


        // 设置控件对于焦点的处理
        setFocusable(true);  // 主要用于视图的焦点事件，其可以通过触摸模式（即：手触摸屏幕中的视图）和键盘模式（即：使用小键盘的上/下/左/右键来选择屏幕中的视图）来获取焦点；
        setFocusableInTouchMode(true); // 主要用于视图的焦点事件，其只能通过触摸模式（即：手触摸屏幕中的视图）来获取焦点；
    }

    /**
    * 在onTouch()方法中，我们调用GestureDetector的onTouchEvent()方法，将捕捉到的MotionEvent交给GestureDetector
    * 来分析是否有合适的callback函数来处理用户的手势
    */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event); // 将捕捉到的MotionEvent交给GestureDetector来分析是否有合适的callback函数来处理用户的手势
        return super.onTouchEvent(event);
    }

    // 记录当前滑动到了第几个Wheel小节，返回的是小节的下标（以正中央为基准）
    public int getPosition() {
        int position = (int)Math.floor(-mFirstOffset / mNodeWidth);
        return position;
    }

    // 获得在当前的停止位置在当前的Wheel小节中所占的百分比 数值在[0.0, 1.0]
    public float getOffset() {
        double value = -mFirstOffset / mNodeWidth;
        return (float)(value - Math.floor(value));
    }

    /**
     * 触发顺序：
     * 点击一下非常快的（不滑动）Touchup：
     * onDown->onSingleTapUp->onSingleTapConfirmed
     * 点击一下稍微慢点的（不滑动）Touchup：
     * onDown->onShowPress->onSingleTapUp->onSingleTapConfirmed
     */

    // 用户按下屏幕就会触发；
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    // 如果是按下的时间超过瞬间，而且在按下的时候没有松开或者是拖动的，那么onShowPress就会执行。
    @Override
    public void onShowPress(MotionEvent e) {

    }

    // 一次单独的轻击抬起操作,也就是轻击一下屏幕，立刻抬起来，才会有这个触发，
    // 当然,如果除了Down以外还有其它操作,那就不再算是Single操作了,所以也就不会触发这个事件
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    /**
     * @param e1 The first down motion event that started the scrolling.
     * @param e2 The move motion event that triggered the current onScroll.
     * @param distanceX The distance along the X axis(轴) that has been scrolled since the last call to onScroll. This is NOT the distance between e1 and e2.
     * @param distanceY The distance along the Y axis that has been scrolled since the last call to onScroll. This is NOT the distance between e1 and e2.
     * 无论是用手拖动view，或者是以抛的动作滚动，都会多次触发 ,这个方法在ACTION_MOVE动作发生时就会触发
     **/
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // distanceX: 上一次Scroll事件对应的点的x坐标减去当前Scroll事件对应的x坐标（向左划为正，向右划为负）
        // distanceY: 上一次Scroll事件对应的点的y坐标减去当前Scroll事件对应的y坐标（向上划为正，向下划为负）
        mFirstOffset -= distanceX;  // 向左滑动distanceX是正数，向右划是负数
        if (mFirstOffset > 0) {
            // 已经滑动到了最初始的位置，不能再继续向右划
            mFirstOffset = 0;
        }

        // 向左划的最大滑动距离
        int maxOffset = (int)((mAdapter.getCount() - 1) * mNodeWidth);
        if (-mFirstOffset > maxOffset) {
            // 已经滑动到了最尾端的位置，不能再继续向左划
            mFirstOffset = -maxOffset;
        }

        // 使得实现 onWheelScroll 的Wheel实例，能够让mDepthOfFieldCalculator记录下当前的位置
        if (mScrollListener != null) {
            mScrollListener.onWheelScroll(getPosition(), getOffset());
        }

        invalidate(); // 每次滑动，Wheel对应的每一节的刻度位置都会发生变化，需要重绘Wheel控件
        return true;
    }


    // 用户长按触摸屏，由多个MotionEvent ACTION_DOWN触发
    /**
     * 触发顺序：
     * onDown->onShowPress->onLongPress
     */
    @Override
    public void onLongPress(MotionEvent e) {

    }


    /**
     * 滑屏：手指触动屏幕后，稍微滑动后立即松开
     * onDown-----》onScroll----》onScroll----》onScroll----》………----->onFling
     * 拖动
     * onDown------》onScroll----》onScroll------》onFiling
     * 可见，无论是滑屏，还是拖动，影响的只是中间OnScroll触发的数量多少而已，最终都会触发onFling事件！
     */

    // 滑屏，用户按下触摸屏、快速移动后松开，由1个MotionEvent ACTION_DOWN, 多个ACTION_MOVE, 1个ACTION_UP触发
    /**
     * 参数解释：
     * e1：第1个ACTION_DOWN MotionEvent
     * e2：最后一个ACTION_MOVE MotionEvent
     * velocityX：X轴上的移动速度，像素/秒
     * velocityY：Y轴上的移动速度，像素/秒
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // Wheel控件的宽度被默认设置为 layout_width = match_parent 即整个屏幕的宽度
        // 以控件的正中央（基准标线--指明刻度）为基准
        float mLeftBoundary = - getWidth() / 2.f;
        float mRightBoundary = getWidth() / 2.f;

        // 在Wheel正中间的位置设置一条小竖线，用于指明当前的刻度
        canvas.drawLine(getWidth() / 2.f, 0, getWidth() / 2.f, (getHeight() - mTextSize) / 4.f, mPainter);

        // 确定屏幕的左边界以及右边界对应着Wheel的小节的下标
        int firstShowPosition = (int)Math.floor((-mFirstOffset + mLeftBoundary) / mNodeWidth);
        int lastShowPosition = (int)Math.floor((-mFirstOffset + mRightBoundary) / mNodeWidth);
        firstShowPosition = Math.max(0, firstShowPosition);
        lastShowPosition = Math.min(mAdapter.getCount() - 1, lastShowPosition);

        for (int i = firstShowPosition; i <= lastShowPosition; i++) {
            String text = mAdapter.getItemText(i); // 是带了单位的刻度文字信息
            float x = mFirstOffset + getWidth() / 2.f + i * mNodeWidth;
            canvas.drawText(text, x, getHeight() / 2.f + mTextSize / 2.f, mPainter);
            canvas.drawLine(x, 0, x, getHeight(), mPainter);
        }

        // 绘制控件的上下边界线条
        canvas.drawLine(0,0, getWidth(), 0, mPainter);
        canvas.drawLine(0, getHeight(), getWidth(), getHeight(), mPainter);

        super.onDraw(canvas);
    }
}
