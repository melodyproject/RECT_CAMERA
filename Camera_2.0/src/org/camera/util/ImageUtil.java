package org.camera.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class ImageUtil {
	/**
	 * ��תBitmap
	 * @param b
	 * @param rotateDegree
	 * @return
	 */
	public static Bitmap getRotateBitmap(Bitmap b, float rotateDegree){
		Matrix matrix = new Matrix();
		matrix.postRotate((float)rotateDegree);
		Bitmap rotaBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, false);
		return rotaBitmap;
	}
	
	public static int getSampleSize(String fileSrc,int defaultW,int defaultH){
		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inJustDecodeBounds=true;	//Ϊtrue ,��ʾ�����ѯͼƬ���ǰ������ط�����ڴ��
		BitmapFactory.decodeFile(fileSrc, options);
		int outW=options.outWidth;
		int outH=options.outHeight;
		int sampleSize=1;
		while((outW/defaultW>sampleSize)||(outH/defaultH>sampleSize)){
			sampleSize*=2;
		}
		return sampleSize;
	}
	/**
	 * ȡ�ò���֮���ͼƬ
	 * @param fileSrc
	 * @param sampleSize
	 * @return
	 */
	public static Bitmap getSubBitmap(String fileSrc,int sampleSize){
		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inJustDecodeBounds=false;
		options.inSampleSize=sampleSize;
		Bitmap bmp=BitmapFactory.decodeFile(fileSrc, options);
		return bmp;
	}
}
