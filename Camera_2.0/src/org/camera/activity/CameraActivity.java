package org.camera.activity;

import org.AppStart;
import org.camera.camera.CameraInterface;
import org.camera.camera.CameraInterface.CamOpenOverCallback;
import org.camera.camera.CameraPictureCallback;
import org.camera.camera.preview.CameraSurfaceView;
import org.camera.ui.MaskView;
import org.camera.util.DisplayUtil;
import org.camera.util.FileUtil;
import org.yanzi.playcamera.R;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.Toast;

public class CameraActivity extends BaseActivity implements CamOpenOverCallback {
	private static final String TAG = "CameraActivity";
	private CameraSurfaceView mSurfaceView = null;
	private ImageButton mShutterBtn;
	private ImageButton mLightBtn;
	private ImageButton mPreViewBtn;
	private MaskView mMaskView = null;
	private float mPreviewRate = -1f;
	private int DST_CENTER_RECT_WIDTH; //单位是dip
	private int DST_CENTER_RECT_HEIGHT;//单位是dip
	private Point mRectPictureSize = null;
	private int mImageButtonW;//手动设置拍照ImageButton的大小
	private int mImageButtonH;//手动设置拍照ImageButton的大小
	private WindowManager.LayoutParams layout;
	private ProgressDialog mDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//按钮宽高
		mImageButtonW=DisplayUtil.dip2px(this, 80);
		mImageButtonH=DisplayUtil.dip2px(this, 80);
		
		//预览框的宽和高;
		DST_CENTER_RECT_WIDTH=DisplayUtil.getScreenMetrics(this).x;
		DST_CENTER_RECT_HEIGHT=DisplayUtil.getScreenMetrics(this).x;
		
		setContentView(R.layout.activity_camera);
		initUI();
		initViewParams();
		//拍照
		mShutterBtn.setOnClickListener(new BtnListeners());
		//获取屏幕当前的参数值
		layout= getWindow().getAttributes();
	}
	
	private void initUI(){
		mSurfaceView = (CameraSurfaceView)findViewById(R.id.camera_surfaceview);
		mShutterBtn = (ImageButton)findViewById(R.id.btn_shutter);
		mMaskView = (MaskView)findViewById(R.id.view_mask);
		mPreViewBtn=(ImageButton) findViewById(R.id.btn_preview);
		mLightBtn=(ImageButton) findViewById(R.id.btn_light);
		//预览 ...
		mPreViewBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("preview......");
				if ("".equals(AppStart.mSaveFilePath)) {
					Toast.makeText(CameraActivity.this, "请拍摄完成之后，再来...", 0).show();
					return;
				}
				Intent intent=new Intent(CameraActivity.this,CameraResultActivity.class);
				intent.putExtra("filePath",AppStart.mSaveFilePath);
				startActivity(intent);
			}
		});
		//调节屏幕亮度 ....
		mLightBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setFullBrightness(1F);
			}
		});
	}
	
	private void initViewParams(){
		LayoutParams params = mSurfaceView.getLayoutParams();
		Point p = DisplayUtil.getScreenMetrics(this);
		params.width = p.x;
		params.height = p.y;
		Log.i(TAG, "screen: w = " + p.x + " y = " + p.y);
		mPreviewRate = DisplayUtil.getScreenRate(this); //默认全屏的比例预览
		mSurfaceView.setLayoutParams(params);
		
		//手动设置拍照ImageButton的大小为120dip×120dip,原图片大小是64×64
		LayoutParams p2 = mShutterBtn.getLayoutParams();
		p2.width =mImageButtonW;
		p2.height =mImageButtonH;
		mShutterBtn.setLayoutParams(p2);
	}
	
	@Override
	public void cameraHasOpened() {
		SurfaceHolder holder = mSurfaceView.getSurfaceHolder();
		CameraInterface.getInstance().doStartPreview(holder, mPreviewRate);
		if(mMaskView != null){
			Rect screenCenterRect = createCenterScreenRect(DST_CENTER_RECT_WIDTH,DST_CENTER_RECT_HEIGHT);
			mMaskView.setCenterRect(screenCenterRect);
		}
	}
	
	private class BtnListeners implements OnClickListener{
		
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.btn_shutter:
				if(mRectPictureSize == null){
					mRectPictureSize = createCenterPictureRect(DST_CENTER_RECT_WIDTH,DST_CENTER_RECT_HEIGHT);
				}
				Log.i(TAG, "----------------------------------------------------");
				Log.d(TAG, "矩形区域的x轴和y轴:"+mRectPictureSize.x+","+mRectPictureSize.y);
				
				CameraInterface.getInstance().doTakePicture(mRectPictureSize.x, mRectPictureSize.y,mCameraPictureCallback);
				
				break;
			default:break;
			}
		}
	}
	
	/**生成拍照后图片的中间矩形的宽度和高度
	 * @param w 屏幕上的矩形宽度，单位px
	 * @param h 屏幕上的矩形高度，单位px
	 * @return
	 */
	private Point createCenterPictureRect(int w, int h){
		
		int wScreen =DisplayUtil.getScreenMetrics(this).x;
		int hScreen = DisplayUtil.getScreenMetrics(this).y;
		
		int wSavePicture = CameraInterface.getInstance().doGetPrictureSize().y; //因为图片旋转了，所以此处宽高换位
		int hSavePicture = CameraInterface.getInstance().doGetPrictureSize().x; //因为图片旋转了，所以此处宽高换位
		
		float wRate = (float)(wSavePicture) / (float)(wScreen);
		float hRate = (float)(hSavePicture) / (float)(hScreen);
		
		float rate = (wRate <= hRate) ? wRate : hRate;//也可以按照最小比率计算
		
		int wRectPicture = (int)( w * wRate);
		int hRectPicture = (int)( h * hRate);
		
		return new Point(wRectPicture, hRectPicture);
	}
	
	/**
	 * 生成屏幕中间的矩形
	 * @param w 目标矩形的宽度,单位px
	 * @param h	目标矩形的高度,单位px
	 * @return
	 */
	private Rect createCenterScreenRect(int w, int h){
		//left
		int x1 = DisplayUtil.getScreenMetrics(this).x / 2 - w / 2;
		//top
		int y1 = DisplayUtil.getScreenMetrics(this).y / 2 - h / 2;
		//right
		int x2 = x1 + w;
		//bottom
		int y2 = y1 + h;
		
		Log.i(TAG, "x1:"+x1+",x2:"+x2+",y1:"+y1+",y2:"+y2);
		//Rect
		return new Rect(x1, y1, x2, y2);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//防止会出现无法显示预览界面的情况,已经做了非空处理
		CameraInterface.getInstance().doStopCamera();
		Log.i(TAG, "马上openCmera...");
		Runnable run=new Runnable() {
			public void run() {
				CameraInterface.getInstance().doOpenCamera(CameraActivity.this);
			}
		};
		Thread thread=new Thread(run);
		thread.setName(Thread.currentThread().getName());
		thread.start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "stopCamera....");
		CameraInterface.getInstance().doStopCamera();
	}
	
	//设置亮度
	private void setFullBrightness(float lightBrightness) {
		layout.screenBrightness = lightBrightness;
		getWindow().setAttributes(layout);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		FileUtil.deleteFile(AppStart.mSaveFilePath);
	}
	
	/**拍照的回调*/
	CameraPictureCallback mCameraPictureCallback=new CameraPictureCallback() {
		@Override
		public void onStart() {
			mDialog=new ProgressDialog(CameraActivity.this);
			mDialog.setCancelable(true);
			mDialog.setMessage("正在处理图片....");
			mDialog.show();
		}
		@Override
		public void onResult(boolean success) {
			mDialog.dismiss();
			mDialog=null;
		}
	};
}