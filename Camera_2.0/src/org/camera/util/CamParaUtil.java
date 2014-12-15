package org.camera.util;

import java.util.List;

import org.AppStart;

import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;

public class CamParaUtil {
	private static final String TAG = "CamParaUtil";
	private static CamParaUtil myCamPara = null;

	private CamParaUtil() {

	}

	public static CamParaUtil getInstance() {
		if (myCamPara == null) {
			myCamPara = new CamParaUtil();
			return myCamPara;
		} else {
			return myCamPara;
		}
	}
	
	/**ȡ��Ԥ���ĳߴ�**/
	public Size getPropPreviewSize(List<Camera.Size> list, float th,
			int minHeight) {
		return getOptimalPreviewSize(list, th, minHeight);
		//return getCurrentScreenSize(list, minHeight);
	}

	/** ��ȡ���ŵ�Ԥ���ĳߴ� **/
	private Size getOptimalPreviewSize(List<Camera.Size> sizes, float w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}
	
	 /*private Size getCurrentScreenSize(List<Camera.Size> sizeList, int n) {  
	        if (sizeList != null && sizeList.size() > 0) {  
	            int screenWidth = DisplayUtil.getScreenMetrics(AppStart.getInstance().getApplicationContext()).x;
	            int[] arry = new int[sizeList.size()];  
	            int temp = 0;  
	            for (Size size : sizeList) {  
	                arry[temp++] = Math.abs(size.width - screenWidth);  
	            }  
	            temp = 0;  
	            int index = 0;  
	            for (int i = 0; i < arry.length; i++) {  
	                if (i == 0) {  
	                    temp = arry[i];  
	                    index = 0;  
	                } else {  
	                    if (arry[i] < temp) {  
	                        index = i;  
	                        temp = arry[i];  
	                    }  
	                }  
	            }  
	            return sizeList.get(index);  
	        }  
	        return null;  
	    }  */
	
	
	
	/**ȡ��ͼƬ�ߴ�**/
	public Size getPropPictureSize(List<Camera.Size> list, float th,
			int minHeight, Camera mCamera, Size previewSize) {
		return getOptimalPictureSize(list, th, minHeight, mCamera, previewSize);
	}

	/*** ��ȡ���ŵ�ͼƬ�ߴ� */
	private Size getOptimalPictureSize(List<Camera.Size> list, float th,
			int minHeight, Camera mCamera, Size previewSize) {
		if (mCamera == null)
			return null;

		List<Size> cameraSizes = mCamera.getParameters()
				.getSupportedPictureSizes();
		Size optimalSize = mCamera.new Size(0, 0);
		double previewRatio = (double) previewSize.width / previewSize.height;
		
		for (Size size : cameraSizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - previewRatio) > 0.01f)
				continue;
			if (size.height > optimalSize.height) {
				optimalSize = size;
			}
		}

		if (optimalSize.height == 0) {
			for (Size size : cameraSizes) {
				if (size.height > optimalSize.height) {
					optimalSize = size;
				}
			}
		}
		return optimalSize;
	}

	/**
	 * ��ӡ֧�ֵ�previewSizes
	 * 
	 * @param params
	 */
	public void printSupportPreviewSize(Camera.Parameters params) {
		List<Size> previewSizes = params.getSupportedPreviewSizes();
		for (int i = 0; i < previewSizes.size(); i++) {
			Size size = previewSizes.get(i);
			Log.i(TAG, "previewSizes:width = " + size.width + " height = "
					+ size.height);
		}

	}

	/**
	 * ��ӡ֧�ֵ�pictureSizes
	 * 
	 * @param params
	 */
	public void printSupportPictureSize(Camera.Parameters params) {
		List<Size> pictureSizes = params.getSupportedPictureSizes();
		for (int i = 0; i < pictureSizes.size(); i++) {
			Size size = pictureSizes.get(i);
			Log.i(TAG, "pictureSizes:width = " + size.width + " height = "
					+ size.height);
		}
	}

	/**
	 * ��ӡ֧�ֵľ۽�ģʽ
	 * 
	 * @param params
	 */
	public void printSupportFocusMode(Camera.Parameters params) {
		List<String> focusModes = params.getSupportedFocusModes();
		for (String mode : focusModes) {
			Log.i(TAG, "focusModes--" + mode);
		}
	}
}
