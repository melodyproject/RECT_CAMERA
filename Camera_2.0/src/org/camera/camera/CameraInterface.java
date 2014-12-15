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
	 * 打开Camera
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
	 * 使用Surfaceview开启预览
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
	 * 使用TextureView预览Camera
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
	 * 停止预览，释放Camera
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
	 * 拍照
	 */
	public void doTakePicture() {
		if (isPreviewing && (mCamera != null)) {
			mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
		}
	}

	public void doTakePicture(int w, int h,CameraPictureCallback callback) {
		if (isPreviewing && (mCamera != null)) {
			Log.i(TAG, "矩形拍照尺寸:width = " + w + " h = " + h);
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

			Log.i(TAG, "矩形区域尺寸1--With = " + mParams.getPictureSize().width
					+ "Height = " + mParams.getPictureSize().height);

			mParams.setPictureFormat(PixelFormat.JPEG);// 设置拍照后存储的图片格式
			
			// 设置PreviewSize和PictureSize
			//预览支持的尺寸
			Size previewSize = CamParaUtil.getInstance().getPropPreviewSize(
					mParams.getSupportedPreviewSizes(),
					previewRate,
					DisplayUtil.getScreenMetrics(AppStart.getInstance()
							.getApplicationContext()).y);
			
			Log.i(TAG, "previewSize:__width:" + previewSize.width
					+ ";previewSize:___height:" + previewSize.height);

			mParams.setPreviewSize(previewSize.width, previewSize.height);
			
			//拍完图片支持的尺寸
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
			mCamera.startPreview();// 开启预览

			isPreviewing = true;
			mPreviwRate = previewRate;

			mParams = mCamera.getParameters(); // 重新get一次
			Log.i(TAG, "预览的尺寸2--With = " + mParams.getPreviewSize().width
					+ "Height = " + mParams.getPreviewSize().height);
			Log.i(TAG, "矩形区域尺寸2--With = " + mParams.getPictureSize().width
					+ "Height = " + mParams.getPictureSize().height);
		}
	}

	/* 为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量 */
	ShutterCallback mShutterCallback = new ShutterCallback() {
		// 快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
		public void onShutter() {
			Log.i(TAG, "myShutterCallback:onShutter...");
			mCallback.onStart();
		}
	};

	PictureCallback mRawCallback = new PictureCallback() {
		// 拍摄的未压缩原数据的回调,可以为null
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.i(TAG, "myRawCallback:onPictureTaken...");
		}
	};

	/**
	 * 常规拍照
	 */
	PictureCallback mJpegPictureCallback = new PictureCallback() {
		// 对jpeg图像数据的回调,最重要的一个回调
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.i(TAG, "myJpegCallback:onPictureTaken...");
			Bitmap b = null;
			if (null != data) {
				b = BitmapFactory.decodeByteArray(data, 0, data.length);// data是字节数据，将其解析成位图
				mCamera.stopPreview();
				isPreviewing = false;
			}
			// 保存图片到sdcard
			if (null != b) {
				// 设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation",90)失效。
				// 图片竟然不能旋转了，故这里要旋转下
				Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);
				FileUtil.saveBitmap(rotaBitmap);
			}
			// 再次进入预览
			mCamera.startPreview();
			isPreviewing = true;
		}
	};

	/**
	 * 拍摄指定区域的Rect....
	 */
	PictureCallback mRectJpegPictureCallback = new PictureCallback() {
		// 对jpeg图像数据的回调,最重要的一个回调
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.i(TAG, "myJpegCallback:onPictureTaken...");
			Bitmap b = null;
			if (null != data) {
				b = BitmapFactory.decodeByteArray(data, 0, data.length);// data是字节数据，将其解析成位图
				mCamera.stopPreview();
				isPreviewing = false;
			}
			// 保存图片到sdcard
			if (null != b) {
				
				mCallback.onResult(true);
				
				// 设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation",
				// 90)失效。
				// 图片竟然不能旋转了，故这里要旋转下
				Bitmap rotateBitmap = ImageUtil.getRotateBitmap(b, 90.0f);
				// rotateBitmap:是整个surfaceView捕获到的画面

				// x轴坐标
				int x = rotateBitmap.getWidth() / 2 - DST_RECT_WIDTH / 2;
				// y轴坐标
				int y = rotateBitmap.getHeight() / 2 - DST_RECT_HEIGHT / 2;
				Log.i(TAG,
						"rotaBitmap.getWidth() = " + rotateBitmap.getWidth()
								+ " rotaBitmap.getHeight() = "
								+ rotateBitmap.getHeight());
				//初始化矩阵
				Matrix matrix=new Matrix();
				//设置缩放率1/2
				matrix.setScale(0.5f, 0.5f);
				// 在rotateBitmap上重新创建一个在第一个像素是x的起点第一个像素点是y的起点坐标且宽度为DST_RECT_WIDTH,高度为DST_RECT_HEIGHT
				Bitmap rectBitmap = Bitmap.createBitmap(rotateBitmap, x, y,
						DST_RECT_WIDTH, DST_RECT_HEIGHT,matrix,false);
				// 保存
				FileUtil.saveBitmap(rectBitmap);
				
				// 释放bitmap
				if (rotateBitmap.isRecycled()) {
					rotateBitmap.recycle();
					rotateBitmap = null;
				}
				if (rectBitmap.isRecycled()) {
					rectBitmap.recycle();
					rectBitmap = null;
				}
			}else{
				//失败...
				mCallback.onResult(false);
			}
			// 再次进入预览...
			mCamera.startPreview();
			isPreviewing = true;
			
			// 释放资源...
			if (null!=b && !b.isRecycled()) {
				b.recycle();
				b = null;
			}
		}
	};
}
