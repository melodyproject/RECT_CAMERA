package org.camera.camera;

import java.io.IOException;
import java.util.List;

import org.AppStart;
import org.camera.util.CamParaUtil;
import org.camera.util.DisplayUtil;
import org.camera.util.FileUtil;
import org.camera.util.ImageUtil;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;

public class CameraInterface {
	private static final String TAG = "CameraInterface";
	private Camera mCamera;
	private Camera.Parameters mParams;
	private boolean isPreviewing = false;
	private float mPreviwRate = -1f;
	private static CameraInterface mCameraInterface;
	private int DST_RECT_WIDTH;
	private int DST_RECT_HEIGHT;
	
	private CameraPictureCallback mCallback;
	
	private Size mPriViewSize;
	private Size mPictureSize;

	public Size getmPriViewSize() {
		return mPriViewSize;
	}

	public void setmPriViewSize(Size mPriViewSize) {
		this.mPriViewSize = mPriViewSize;
	}

	public Size getmPictureSize() {
		return mPictureSize;
	}

	public void setmPictureSize(Size mPictureSize) {
		this.mPictureSize = mPictureSize;
	}
	
	public interface CamOpenOverCallback {
		public void cameraHasOpened();
	}

	private CameraInterface() {
	}

	public static synchronized CameraInterface getInstance() {
		if (mCameraInterface == null) {
			mCameraInterface = new CameraInterface();
		}
		return mCameraInterface;
	}

	/**
	 * ��Camera
	 * 
	 * @param callback
	 */
	public void doOpenCamera(CamOpenOverCallback callback) {
		Log.i(TAG, "Camera open....");
		mCamera = Camera.open();
		Log.i(TAG, "Camera open over....");
		callback.cameraHasOpened();
	}

	/**
	 * ʹ��Surfaceview����Ԥ��
	 * 
	 * @param holder
	 * @param previewRate
	 */
	public void doStartPreview(SurfaceHolder holder, float previewRate) {
		Log.i(TAG, "doStartPreview...");
		if (isPreviewing) {
			mCamera.stopPreview();
			return;
		}
		if (mCamera != null) {
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				e.printStackTrace();
			}
			initCamera(previewRate);
		}
	}

	/**
	 * ʹ��TextureViewԤ��Camera
	 * 
	 * @param surface
	 * @param previewRate
	 */
	@SuppressLint("NewApi")
	public void doStartPreview(SurfaceTexture surface, float previewRate) {
		Log.i(TAG, "doStartPreview...");
		if (isPreviewing) {
			mCamera.stopPreview();
			return;
		}
		if (mCamera != null) {
			try {
				mCamera.setPreviewTexture(surface);
			} catch (IOException e) {
				e.printStackTrace();
			}
			initCamera(previewRate);
		}
	}

	/**
	 * ֹͣԤ�����ͷ�Camera
	 */
	public void doStopCamera() {
		if (null != mCamera) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			isPreviewing = false;
			mPreviwRate = -1f;
			mCamera.release();
			mCamera = null;
		}
	}

	/**
	 * ����
	 */
	public void doTakePicture() {
		if (isPreviewing && (mCamera != null)) {
			mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
		}
	}

	public void doTakePicture(int w, int h,CameraPictureCallback callback) {
		if (isPreviewing && (mCamera != null)) {
			Log.i(TAG, "�������ճߴ�:width = " + w + " h = " + h);
			DST_RECT_WIDTH = w;
			DST_RECT_HEIGHT = h;
			mCallback=callback;
			mCamera.takePicture(mShutterCallback, null,
					mRectJpegPictureCallback);
		}
	}

	public Point doGetPrictureSize() {
		Size s = mCamera.getParameters().getPictureSize();
		return new Point(s.width, s.height);
	}

	private void initCamera(float previewRate) {

		if (mCamera != null) {

			mParams = mCamera.getParameters();

			Log.i(TAG, "��������ߴ�1--With = " + mParams.getPictureSize().width
					+ "Height = " + mParams.getPictureSize().height);

			mParams.setPictureFormat(PixelFormat.JPEG);// �������պ�洢��ͼƬ��ʽ
			
			// ����PreviewSize��PictureSize
			//Ԥ��֧�ֵĳߴ�
			Size previewSize = CamParaUtil.getInstance().getPropPreviewSize(
					mParams.getSupportedPreviewSizes(),
					previewRate,
					DisplayUtil.getScreenMetrics(AppStart.getInstance()
							.getApplicationContext()).y);
			
			Log.i(TAG, "previewSize:__width:" + previewSize.width
					+ ";previewSize:___height:" + previewSize.height);

			mParams.setPreviewSize(previewSize.width, previewSize.height);
			
			//����ͼƬ֧�ֵĳߴ�
			Size pictureSize = CamParaUtil.getInstance().getPropPictureSize(
					mParams.getSupportedPictureSizes(),
					previewRate,
					DisplayUtil.getScreenMetrics(AppStart.getInstance()
							.getApplicationContext()).y, mCamera, previewSize);
			mParams.setPictureSize(pictureSize.width, pictureSize.height);

			Log.i(TAG, "pictureSize:__width:" + pictureSize.width
					+ ";pictureSize:___height:" + pictureSize.height);

			setmPriViewSize(previewSize);

			mCamera.setDisplayOrientation(90);

			List<String> focusModes = mParams.getSupportedFocusModes();
			if (null != focusModes && focusModes.contains("continuous-video")) {
				mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			}

			mCamera.setParameters(mParams);
			mCamera.startPreview();// ����Ԥ��

			isPreviewing = true;
			mPreviwRate = previewRate;

			mParams = mCamera.getParameters(); // ����getһ��
			Log.i(TAG, "Ԥ���ĳߴ�2--With = " + mParams.getPreviewSize().width
					+ "Height = " + mParams.getPreviewSize().height);
			Log.i(TAG, "��������ߴ�2--With = " + mParams.getPictureSize().width
					+ "Height = " + mParams.getPictureSize().height);
		}
	}

	/* Ϊ��ʵ�����յĿ������������ձ�����Ƭ��Ҫ���������ص����� */
	ShutterCallback mShutterCallback = new ShutterCallback() {
		// ���Ű��µĻص������������ǿ����������Ʋ��š����ꡱ��֮��Ĳ�����Ĭ�ϵľ������ꡣ
		public void onShutter() {
			Log.i(TAG, "myShutterCallback:onShutter...");
			mCallback.onStart();
		}
	};

	PictureCallback mRawCallback = new PictureCallback() {
		// �����δѹ��ԭ���ݵĻص�,����Ϊnull
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.i(TAG, "myRawCallback:onPictureTaken...");
		}
	};

	/**
	 * ��������
	 */
	PictureCallback mJpegPictureCallback = new PictureCallback() {
		// ��jpegͼ�����ݵĻص�,����Ҫ��һ���ص�
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.i(TAG, "myJpegCallback:onPictureTaken...");
			Bitmap b = null;
			if (null != data) {
				b = BitmapFactory.decodeByteArray(data, 0, data.length);// data���ֽ����ݣ����������λͼ
				mCamera.stopPreview();
				isPreviewing = false;
			}
			// ����ͼƬ��sdcard
			if (null != b) {
				// ����FOCUS_MODE_CONTINUOUS_VIDEO)֮��myParam.set("rotation",90)ʧЧ��
				// ͼƬ��Ȼ������ת�ˣ�������Ҫ��ת��
				Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);
				FileUtil.saveBitmap(rotaBitmap);
			}
			// �ٴν���Ԥ��
			mCamera.startPreview();
			isPreviewing = true;
		}
	};

	/**
	 * ����ָ�������Rect....
	 */
	PictureCallback mRectJpegPictureCallback = new PictureCallback() {
		// ��jpegͼ�����ݵĻص�,����Ҫ��һ���ص�
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.i(TAG, "myJpegCallback:onPictureTaken...");
			Bitmap b = null;
			if (null != data) {
				b = BitmapFactory.decodeByteArray(data, 0, data.length);// data���ֽ����ݣ����������λͼ
				mCamera.stopPreview();
				isPreviewing = false;
			}
			// ����ͼƬ��sdcard
			if (null != b) {
				
				mCallback.onResult(true);
				
				// ����FOCUS_MODE_CONTINUOUS_VIDEO)֮��myParam.set("rotation",
				// 90)ʧЧ��
				// ͼƬ��Ȼ������ת�ˣ�������Ҫ��ת��
				Bitmap rotateBitmap = ImageUtil.getRotateBitmap(b, 90.0f);
				// rotateBitmap:������surfaceView���񵽵Ļ���

				// x������
				int x = rotateBitmap.getWidth() / 2 - DST_RECT_WIDTH / 2;
				// y������
				int y = rotateBitmap.getHeight() / 2 - DST_RECT_HEIGHT / 2;
				Log.i(TAG,
						"rotaBitmap.getWidth() = " + rotateBitmap.getWidth()
								+ " rotaBitmap.getHeight() = "
								+ rotateBitmap.getHeight());
				//��ʼ������
				Matrix matrix=new Matrix();
				//����������1/2
				matrix.setScale(0.5f, 0.5f);
				// ��rotateBitmap�����´���һ���ڵ�һ��������x������һ�����ص���y����������ҿ��ΪDST_RECT_WIDTH,�߶�ΪDST_RECT_HEIGHT
				Bitmap rectBitmap = Bitmap.createBitmap(rotateBitmap, x, y,
						DST_RECT_WIDTH, DST_RECT_HEIGHT,matrix,false);
				// ����
				FileUtil.saveBitmap(rectBitmap);
				
				// �ͷ�bitmap
				if (rotateBitmap.isRecycled()) {
					rotateBitmap.recycle();
					rotateBitmap = null;
				}
				if (rectBitmap.isRecycled()) {
					rectBitmap.recycle();
					rectBitmap = null;
				}
			}else{
				//ʧ��...
				mCallback.onResult(false);
			}
			// �ٴν���Ԥ��...
			mCamera.startPreview();
			isPreviewing = true;
			
			// �ͷ���Դ...
			if (null!=b && !b.isRecycled()) {
				b.recycle();
				b = null;
			}
		}
	};
}
