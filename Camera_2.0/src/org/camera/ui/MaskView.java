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
	// ���񻭱�
	private Paint mLinePaint;
	// ��Ӱ����Ļ���
	private Paint mAreaPaint;
	// ��ɫ�߽ǵĻ���
	private Paint mBlueAreaPaint;
	// �м����
	private Rect mCenterRect = null;
	// ������
	private Context mContext;
	// ��Ļ�Ŀ��
	private int mWidthScreen;
	// ��Ļ�ĸ߶�
	private int mHeightScreen;
	// �ĸ���ɫ�߽Ƕ�Ӧ�ĳ���
	private int ScreenRate;
	// �ĸ���ɫ�߽Ƕ�Ӧ�Ŀ��
	private static final int CORNER_WIDTH = 5;
	// ���ֵ��ڱ߾�
	private static final int TEXT_PADDING_TOP = 30;
	// ���ִ�С
	private static final int TEXT_SIZE = 16;
	// ���ֵĻ���
	private Paint mTextPain;
	// ��Ӱ�������ɫֵ
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
		
		// ���������ߵ���ɫ
		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLinePaint.setColor(Color.WHITE);
		mLinePaint.setStyle(Style.STROKE);
		mLinePaint.setStrokeWidth(1f);
		mLinePaint.setAlpha(65);

		// ����������Ӱ����
		mAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mAreaPaint.setColor(mMaskColor);
		mAreaPaint.setStyle(Style.FILL);
		mAreaPaint.setAlpha(180);

		// ��ɫ�߽ǵ���ɫ
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

		// ����������Ӱ����
		canvas.drawRect(0, 0, mWidthScreen, mCenterRect.top, mAreaPaint);
		canvas.drawRect(0, mCenterRect.bottom, mWidthScreen, mHeightScreen,
				mAreaPaint);
		canvas.drawRect(0, mCenterRect.top, mCenterRect.left,
				mCenterRect.bottom, mAreaPaint);
		canvas.drawRect(mCenterRect.right, mCenterRect.top, mWidthScreen,
				mCenterRect.bottom, mAreaPaint);

		/******************************************************************/

		// ��ɫ�Ĺս�
		// ��ɨ�����ϵĽǣ��ܹ�8������
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
		
		// ��ˮƽ������
		for (int i = 0; i < (mCenterRect.bottom - mCenterRect.top); i += AppStart.mAverage) {
			canvas.drawLine(mCenterRect.left, mCenterRect.top + i,
					mCenterRect.right, mCenterRect.top + i, mLinePaint);
		}
		
		// ����ֱ������
		for (int i = 0; i < (mCenterRect.right - mCenterRect.left); i += AppStart.mAverage) {
			canvas.drawLine(mCenterRect.left + i, mCenterRect.top,
					mCenterRect.left + i, mCenterRect.bottom, mLinePaint);
		}
		
		// ��ɨ����������
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