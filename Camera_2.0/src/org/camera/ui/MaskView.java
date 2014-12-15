package org.camera.ui;

import org.AppStart;
import org.camera.util.DisplayUtil;
import org.yanzi.playcamera.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class MaskView extends ImageView {
	private static final String TAG = "MaskView";
	// private Paint mLinePaint;
	// 网格画笔
	private Paint mLinePaint;
	// 阴影区域的画笔
	private Paint mAreaPaint;
	// 绿色边角的画笔
	private Paint mBlueAreaPaint;
	// 中间矩形
	private Rect mCenterRect = null;
	// 上下文
	private Context mContext;
	// 屏幕的宽度
	private int mWidthScreen;
	// 屏幕的高度
	private int mHeightScreen;
	// 四个绿色边角对应的长度
	private int ScreenRate;
	// 四个绿色边角对应的宽度
	private static final int CORNER_WIDTH = 5;
	// 文字的内边距
	private static final int TEXT_PADDING_TOP = 30;
	// 文字大小
	private static final int TEXT_SIZE = 16;
	// 文字的画笔
	private Paint mTextPain;
	// 阴影区域的颜色值
	private int mMaskColor;
	
	public MaskView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint();
		mContext = context;
		Point p = DisplayUtil.getScreenMetrics(mContext);
		mWidthScreen = p.x;
		mHeightScreen = p.y;
		ScreenRate = AppStart.mDenisty * 15;
		mMaskColor = getResources().getColor(R.color.viewfinder_mask);

	}

	private void initPaint() {
		
		// 设置网格画线的颜色
		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLinePaint.setColor(Color.WHITE);
		mLinePaint.setStyle(Style.STROKE);
		mLinePaint.setStrokeWidth(1f);
		mLinePaint.setAlpha(65);

		// 绘制四周阴影区域
		mAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mAreaPaint.setColor(mMaskColor);
		mAreaPaint.setStyle(Style.FILL);
		mAreaPaint.setAlpha(180);

		// 绿色边角的颜色
		mBlueAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBlueAreaPaint.setColor(Color.GRAY);
		mBlueAreaPaint.setStyle(Style.FILL);
		mBlueAreaPaint.setAlpha(180);

		mTextPain = new Paint(Paint.ANTI_ALIAS_FLAG);
	}

	public void setCenterRect(Rect r) {
		Log.i(TAG, "setCenterRect...");
		this.mCenterRect = r;
		postInvalidate();
	}

	public void clearCenterRect(Rect r) {
		this.mCenterRect = null;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Log.i(TAG, "onDraw...");
		if (mCenterRect == null)
			return;

		// 绘制四周阴影区域
		canvas.drawRect(0, 0, mWidthScreen, mCenterRect.top, mAreaPaint);
		canvas.drawRect(0, mCenterRect.bottom, mWidthScreen, mHeightScreen,
				mAreaPaint);
		canvas.drawRect(0, mCenterRect.top, mCenterRect.left,
				mCenterRect.bottom, mAreaPaint);
		canvas.drawRect(mCenterRect.right, mCenterRect.top, mWidthScreen,
				mCenterRect.bottom, mAreaPaint);

		/******************************************************************/

		// 绿色的拐角
		// 画扫描框边上的角，总共8个部分
		mBlueAreaPaint.setColor(Color.GREEN);
		canvas.drawRect(mCenterRect.left, mCenterRect.top, mCenterRect.left
				+ ScreenRate, mCenterRect.top + CORNER_WIDTH, mBlueAreaPaint);
		canvas.drawRect(mCenterRect.left, mCenterRect.top, mCenterRect.left
				+ CORNER_WIDTH, mCenterRect.top + ScreenRate, mBlueAreaPaint);
		canvas.drawRect(mCenterRect.right - ScreenRate, mCenterRect.top,
				mCenterRect.right, mCenterRect.top + CORNER_WIDTH,
				mBlueAreaPaint);
		canvas.drawRect(mCenterRect.right - CORNER_WIDTH, mCenterRect.top,
				mCenterRect.right, mCenterRect.top + ScreenRate, mBlueAreaPaint);
		canvas.drawRect(mCenterRect.left, mCenterRect.bottom - CORNER_WIDTH,
				mCenterRect.left + ScreenRate, mCenterRect.bottom,
				mBlueAreaPaint);
		canvas.drawRect(mCenterRect.left, mCenterRect.bottom - ScreenRate,
				mCenterRect.left + CORNER_WIDTH, mCenterRect.bottom,
				mBlueAreaPaint);
		canvas.drawRect(mCenterRect.right - ScreenRate, mCenterRect.bottom
				- CORNER_WIDTH, mCenterRect.right, mCenterRect.bottom,
				mBlueAreaPaint);
		canvas.drawRect(mCenterRect.right - CORNER_WIDTH, mCenterRect.bottom
				- ScreenRate, mCenterRect.right, mCenterRect.bottom,
				mBlueAreaPaint);

		/*********************************************************************/
		
		// 画水平的网格
		for (int i = 0; i < (mCenterRect.bottom - mCenterRect.top); i += AppStart.mAverage) {
			canvas.drawLine(mCenterRect.left, mCenterRect.top + i,
					mCenterRect.right, mCenterRect.top + i, mLinePaint);
		}
		
		// 画竖直的网格
		for (int i = 0; i < (mCenterRect.right - mCenterRect.left); i += AppStart.mAverage) {
			canvas.drawLine(mCenterRect.left + i, mCenterRect.top,
					mCenterRect.left + i, mCenterRect.bottom, mLinePaint);
		}
		
		// 画扫描框上面的字
		mTextPain.setColor(Color.WHITE);
		mTextPain.setTextSize(TEXT_SIZE * AppStart.mDenisty);
		mTextPain.setAlpha(0x40);
		mTextPain.setTypeface(Typeface.create("System", Typeface.BOLD));
		String text = getResources().getString(R.string.scan_text);
		float textWidth = mTextPain.measureText(text);
		canvas.drawText(text, (mWidthScreen - textWidth) / 2,
				(float) (mCenterRect.top - (float) TEXT_PADDING_TOP* AppStart.mDenisty), mTextPain);
		
		super.onDraw(canvas);
	}
}