package org;

import android.app.Application;

public class AppStart extends Application {
	//文件保存的位置
	public static String mSaveFilePath="";
	//密度
	public static int mDenisty;
	//等份份数
	public static int mAverage;
	//保存预览图片的位置
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