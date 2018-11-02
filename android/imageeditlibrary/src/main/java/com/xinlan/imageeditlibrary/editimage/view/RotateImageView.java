package com.xinlan.imageeditlibrary.editimage.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.xinlan.imageeditlibrary.editimage.utils.PaintUtil;

/**
 * 旋转图片
 * 
 * @author 潘易
 * xujian2018/10/23
 * 
 */
public class RotateImageView extends View {
	private Rect srcRect;
	private RectF dstRect;
	private Rect maxRect;// 最大限制矩形框

	private Bitmap bitmap;
	private Matrix matrix = new Matrix();// 辅助计算矩形

	private float scale;// 缩放比率
	private int rotateAngle;  // 当前的旋转角度

	private RectF wrapRect = new RectF();// 图片包围矩形
	private Paint bottomPaint;
	private RectF originImageRect;

	public RotateImageView(Context context) {
		super(context);
		init(context);
	}

	public RotateImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public RotateImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		srcRect = new Rect();
		dstRect = new RectF();
		maxRect = new Rect();
		// 旋转图片背景的白底色信息
		bottomPaint = PaintUtil.newRotateBottomImagePaint();
		originImageRect = new RectF();
	}

	public void addBit(Bitmap bit, RectF imageRect) {
		bitmap = bit;
		srcRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());  // 当前原图像的大小矩形信息
		dstRect = imageRect; // 当前图像所处的位置矩形

		originImageRect.set(0, 0, bit.getWidth(), bit.getHeight());  // 原图像的大小矩形信息
		this.invalidate();
	}

	public void rotateImage(int angle) {
		rotateAngle = angle;
		this.invalidate();
	}

	public void reset() {
		rotateAngle = 0;
		scale = 1;
		invalidate();
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		if (bitmap == null)
			return;
		maxRect.set(0, 0, getWidth(), getHeight());// 最大边界矩形

		calculateWrapBox();
		scale = 1;
		if (wrapRect.width() > getWidth()) {
			scale = getWidth() / wrapRect.width();
		}

		// 绘制图形
		canvas.save();
		canvas.scale(scale, scale, canvas.getWidth() >> 1,
				canvas.getHeight() >> 1);
		// 绘制旋转界面的底部区域wrapRect
		canvas.drawRect(wrapRect, bottomPaint);
		// 设置当前的旋转角度
		canvas.rotate(rotateAngle, canvas.getWidth() >> 1,
				canvas.getHeight() >> 1);
		canvas.drawBitmap(bitmap, srcRect, dstRect, null);
		canvas.restore();
	}

	/**
	 * 计算出wrapRect
	 */
	private void calculateWrapBox() {
		wrapRect.set(dstRect);
		matrix.reset();// 重置矩阵为单位矩阵
		int centerX = getWidth() >> 1;
		int centerY = getHeight() >> 1;
		matrix.postRotate(rotateAngle, centerX, centerY);// 旋转后的角度
		 System.out.println("旋转之前-->" + wrapRect.left + "    " + wrapRect.top
		 + "    " + wrapRect.right + "   " + wrapRect.bottom);
		matrix.mapRect(wrapRect);
		 System.out.println("旋转之后-->" + wrapRect.left + "    " + wrapRect.top
		 + "    " + wrapRect.right + "   " + wrapRect.bottom);
	}

	/**
	 * 取得旋转后新图片的大小
	 * 
	 * @return
	 */
	public RectF getImageNewRect() {
		Matrix m = new Matrix();
		m.postRotate(this.rotateAngle, originImageRect.centerX(),
				originImageRect.centerY());
		m.mapRect(originImageRect);
		return originImageRect;
	}

	/**
	 * 缩放比率
	 * @return
	 */
	public synchronized float getScale() {
		return scale;
	}

	/**
	 * 旋转角度
	 * @return
	 */
	public synchronized int getRotateAngle() {
		return rotateAngle;
	}
}// end class
