package org;

import android.app.Application;

public class AppStart extends Application {
	//�ļ������λ��
	public static String mSaveFilePath="";
	//�ܶ�
	public static int mDenisty;
	//�ȷݷ���
	public static int mAverage;
	//����Ԥ��ͼƬ��λ��
	public static String mSavePriViewPath;
	
	private static AppStart mInstance;
	
	public static AppStart getInstance(){
		return mInstance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mInstance=this;
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}
}