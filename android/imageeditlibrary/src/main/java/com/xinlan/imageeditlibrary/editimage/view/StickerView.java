package com.xinlan.imageeditlibrary.editimage.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.xinlan.imageeditlibrary.editimage.utils.RectUtil;

import java.util.LinkedHashMap;

/**
 * 贴图操作控件
 *
 * @author panyi
 * xujian 2018/10/14
 */
public class StickerView extends View {
    private static int STATUS_IDLE = 0;
    private static int STATUS_MOVE = 1;// 移动状态
    private static int STATUS_DELETE = 2;// 删除状态
    private static int STATUS_ROTATE = 3;// 图片旋转状态

    private int imageCount;// 已加入照片的数量
    private Context mContext;
    private int currentStatus;// 当前状态
    private StickerItem currentItem;// 当前操作的贴图数据
    private float oldx, oldy;  // 移动、旋转的之后记录下之前被点击的位置

    private Paint rectPaint = new Paint();
    private Paint boxPaint = new Paint();

    private LinkedHashMap<Integer, StickerItem> bank = new LinkedHashMap<Integer, StickerItem>();// 存贮每层贴图数据

    private Point mPoint = new Point(0 , 0);

    public StickerView(Context context) {
        super(context);
        init(context);
    }

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        currentStatus = STATUS_IDLE;

        rectPaint.setColor(Color.RED);
        rectPaint.setAlpha(100);

    }

    public void addBitImage(final Bitmap addBit) {
        StickerItem item = new StickerItem(this.getContext());
        item.init(addBit, this);
        if (currentItem != null) {
            currentItem.isDrawHelpTool = false;
        }
        bank.put(++imageCount, item);
        this.invalidate();// 重绘视图
    }

    /**
     * 绘制客户页面
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // System.out.println("on draw!!~");
        for (Integer id : bank.keySet()) {
            StickerItem item = bank.get(id);
            item.draw(canvas);  // 绘制每一个StickerItem
        }// end for each
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // System.out.println(w + "   " + h + "    " + oldw + "   " + oldh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);// 是否向下传递事件标志 true为消耗

        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                int deleteId = -1;
                for (Integer id : bank.keySet()) {
                    StickerItem item = bank.get(id);
                    if (item.detectDeleteRect.contains(x, y)) {// 删除模式
                        // ret = true;
                        deleteId = id;
                        currentStatus = STATUS_DELETE;
                    } else if (item.detectRotateRect.contains(x, y)) {// 点击了旋转按钮
                        ret = true;
                        if (currentItem != null) {
                            // 如果之前已经有被选择的图案，那么消除之前图案的边框框
                            currentItem.isDrawHelpTool = false;
                        }
                        currentItem = item;  // 记录当前被点击的新图案，被点击的新图案需要被设置边缘框
                        currentItem.isDrawHelpTool = true;
                        currentStatus = STATUS_ROTATE;
                        oldx = x;
                        oldy = y;
                    } else if (detectInItemContent(item , x , y)) {// 移动模式
                        // 被选中一张贴图
                        ret = true;
                        if (currentItem != null) {
                            currentItem.isDrawHelpTool = false;
                        }
                        currentItem = item;
                        currentItem.isDrawHelpTool = true;
                        currentStatus = STATUS_MOVE;
                        oldx = x;
                        oldy = y;
                    }// end if
                }// end for each

                if (!ret && currentItem != null && currentStatus == STATUS_IDLE) {// 没有贴图被选择（点击到了全部贴图的范围外）
                    currentItem.isDrawHelpTool = false;
                    currentItem = null;
                    invalidate();
                }

                if (deleteId > 0 && currentStatus == STATUS_DELETE) {// 删除选定贴图
                    bank.remove(deleteId);
                    currentStatus = STATUS_IDLE;// 返回空闲状态
                    invalidate();
                }// end if

                break;
            case MotionEvent.ACTION_MOVE:
                ret = true;
                if (currentStatus == STATUS_MOVE) {// 移动贴图
                    float dx = x - oldx;
                    float dy = y - oldy;
                    if (currentItem != null) {
                        currentItem.updatePos(dx, dy);
                        invalidate();
                    }// end if
                    oldx = x;
                    oldy = y;
                } else if (currentStatus == STATUS_ROTATE) {// 旋转 缩放图片操作
                    // System.out.println("旋转");
                    float dx = x - oldx;
                    float dy = y - oldy;
                    if (currentItem != null) {
                        currentItem.updateRotateAndScale(oldx, oldy, dx, dy);// 旋转
                        invalidate();
                    }// end if
                    oldx = x;
                    oldy = y;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                ret = false;
                currentStatus = STATUS_IDLE;
                break;
        }// end switch
        return ret;
    }

    /**
     * 判定点击点是否在内容范围之内  需考虑旋转
     * @param item
     * @param x
     * @param y
     * @return
     */
    private boolean detectInItemContent(StickerItem item , float x , float y){
        //reset
        mPoint.set((int)x , (int)y);
        // 具体实现中，贴图的位置矩形distRect(以及helpBox)只是体现了平移、缩放，并没有体现旋转。因此当前的触点要经过反旋转之后，再判断是否在helpBox的范围之内
        // 具体的StickItem使用Matrix来重绘保证正确；helpBox通过让Canvas旋转roatetAngle角度保证重绘正确
        // deleteRect，rotateRect（位置体现）只体现了平移，所以也是通过让Canvas旋转roatetAngle角度保证重绘正确
        RectUtil.rotatePoint(mPoint , item.helpBox.centerX() , item.helpBox.centerY() , -item.roatetAngle);
        return item.helpBox.contains(mPoint.x, mPoint.y);
    }

    public LinkedHashMap<Integer, StickerItem> getBank() {
        return bank;
    }

    public void clear() {
        bank.clear();
        this.invalidate();
    }
}// end class
