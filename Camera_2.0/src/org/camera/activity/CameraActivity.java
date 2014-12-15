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
	private int DST_CENTER_RECT_WIDTH; //��λ��dip
	private int DST_CENTER_RECT_HEIGHT;//��λ��dip
	private Point mRectPictureSize = null;
	private int mImageButtonW;//�ֶ���������ImageButton�Ĵ�С
	private int mImageButtonH;//�ֶ���������ImageButton�Ĵ�С
	private WindowManager.LayoutParams layout;
	private ProgressDialog mDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//��ť���
		mImageButtonW=DisplayUtil.dip2px(this, 80);
		mImageButtonH=DisplayUtil.dip2px(this, 80);
		
		//Ԥ����Ŀ�͸�;
		DST_CENTER_RECT_WIDTH=DisplayUtil.getScreenMetrics(this).x;
		DST_CENTER_RECT_HEIGHT=DisplayUtil.getScreenMetrics(this).x;
		
		setContentView(R.layout.activity_camera);
		initUI();
		initViewParams();
		//����
		mShutterBtn.setOnClickListener(new BtnListeners());
		//��ȡ��Ļ��ǰ�Ĳ���ֵ
		layout= getWindow().getAttributes();
	}
	
	private void initUI(){
		mSurfaceView = (CameraSurfaceView)findViewById(R.id.camera_surfaceview);
		mShutterBtn = (ImageButton)findViewById(R.id.btn_shutter);
		mMaskView = (MaskView)findViewById(R.id.view_mask);
		mPreViewBtn=(ImageButton) findViewById(R.id.btn_preview);
		mLightBtn=(ImageButton) findViewById(R.id.btn_light);
		//Ԥ�� ...
		mPreViewBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.out.println("preview......");
				if ("".equals(AppStart.mSaveFilePath)) {
					Toast.makeText(CameraActivity.this, "���������֮������...", 0).show();
					return;
				}
				Intent intent=new Intent(CameraActivity.this,CameraResultActivity.class);
				intent.putExtra("filePath",AppStart.mSaveFilePath);
				startActivity(intent);
			}
		});
		//������Ļ���� ....
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
		mPreviewRate = DisplayUtil.getScreenRate(this); //Ĭ��ȫ���ı���Ԥ��
		mSurfaceView.setLayoutParams(params);
		
		//�ֶ���������ImageButton�Ĵ�СΪ120dip��120dip,ԭͼƬ��С��64��64
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
				Log.d(TAG, "���������x���y��:"+mRectPictureSize.x+","+mRectPictureSize.y);
				
				CameraInterface.getInstance().doTakePicture(mRectPictureSize.x, mRectPictureSize.y,mCameraPictureCallback);
				
				break;
			default:break;
			}
		}
	}
	
	/**�������պ�ͼƬ���м���εĿ�Ⱥ͸߶�
	 * @param w ��Ļ�ϵľ��ο�ȣ���λpx
	 * @param h ��Ļ�ϵľ��θ߶ȣ���λpx
	 * @return
	 */
	private Point createCenterPictureRect(int w, int h){
		
		int wScreen =DisplayUtil.getScreenMetrics(this).x;
		int hScreen = DisplayUtil.getScreenMetrics(this).y;
		
		int wSavePicture = CameraInterface.getInstance().doGetPrictureSize().y; //��ΪͼƬ��ת�ˣ����Դ˴���߻�λ
		int hSavePicture = CameraInterface.getInstance().doGetPrictureSize().x; //��ΪͼƬ��ת�ˣ����Դ˴���߻�λ
		
		float wRate = (float)(wSavePicture) / (float)(wScreen);
		float hRate = (float)(hSavePicture) / (float)(hScreen);
		
		float rate = (wRate <= hRate) ? wRate : hRate;//Ҳ���԰�����С���ʼ���
		
		int wRectPicture = (int)( w * wRate);
		int hRectPicture = (int)( h * hRate);
		
		return new Point(wRectPicture, hRectPicture);
	}
	
	/**
	 * ������Ļ�м�ľ���
	 * @param w Ŀ����εĿ��,��λpx
	 * @param h	Ŀ����εĸ߶�,��λpx
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
		//��ֹ������޷���ʾԤ����������,�Ѿ����˷ǿմ���
		CameraInterface.getInstance().doStopCamera();
		Log.i(TAG, "����openCmera...");
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
	
	//��������
	private void setFullBrightness(float lightBrightness) {
		layout.screenBrightness = lightBrightness;
		getWindow().setAttributes(layout);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		FileUtil.deleteFile(AppStart.mSaveFilePath);
	}
	
	/**���յĻص�*/
	CameraPictureCallback mCameraPictureCallback=new CameraPictureCallback() {
		@Override
		public void onStart() {
			mDialog=new ProgressDialog(CameraActivity.this);
			mDialog.setCancelable(true);
			mDialog.setMessage("���ڴ���ͼƬ....");
			mDialog.show();
		}
		@Override
		public void onResult(boolean success) {
			mDialog.dismiss();
			mDialog=null;
		}
	};
}