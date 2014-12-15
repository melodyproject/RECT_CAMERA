package org.camera.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.camera.gesture.GestureImageView;
import org.camera.util.FileUtil;
import org.camera.util.ImageUtil;
import org.yanzi.playcamera.R;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.Window;

public class CameraResultActivity extends BaseActivity {
	private GestureImageView gim;
	private String savePath = FileUtil.initPath() + File.separator
			+ System.currentTimeMillis() + "_clothes.jpg";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 隐藏状态栏

		setContentView(R.layout.preview);
		gim = (GestureImageView) findViewById(R.id.iv_preview);

		String filePath = getIntent().getStringExtra("filePath");

		Bitmap bitmap = ImageUtil.getSubBitmap(filePath, 1);

		Bitmap mc = BitmapFactory.decodeResource(getResources(), R.drawable.mc);
		
		mc=Bitmap.createScaledBitmap(mc, bitmap.getWidth(), bitmap.getHeight(), false);
		
		Bitmap alBitmap=combineBitmap(bitmap, mc);
		
		/**
		 * Canvas画:带网格的图片
		 */
		/*
		TODO:画笔画上去的。。。。
		// 创建一个新的空白的Bitmap对象
		Bitmap alBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), bitmap.getConfig());
		// 创建一个空白的画布
		Canvas canvas = new Canvas(alBitmap);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK); // 设置画笔的颜色

		Matrix matrix = new Matrix();
		canvas.drawBitmap(bitmap, matrix, paint);
		paint.setStrokeWidth(1f);
		paint.setColor(Color.WHITE);
		paint.setAlpha(65);

		int wpre = bitmap.getWidth() * (AppStart.mAverage)
				/ (DisplayUtil.getScreenMetrics(this).x);
		int hpre = bitmap.getHeight() * (AppStart.mAverage)
				/ (DisplayUtil.getScreenMetrics(this).x);

		for (int i = 0; i < bitmap.getWidth(); i += wpre) {
			canvas.drawLine(i, 0, i, bitmap.getHeight(), paint);
		}

		for (int i = 0; i < bitmap.getHeight(); i += hpre) {
			canvas.drawLine(0, i, bitmap.getWidth(), i, paint);
		}*/
		
		gim.setImageBitmap(alBitmap);
		saveBitmap(alBitmap, savePath);

		FileUtil.deleteFile(filePath);
	}

	private void saveBitmap(Bitmap alBitmap, String path) {
		OutputStream stream = null;
		try {
			if (!"".equals(path)) {
				stream = new FileOutputStream(new File(path));
				alBitmap.compress(CompressFormat.JPEG, 100, stream);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (null != stream) {
				try {
					stream.close();
					stream = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Bitmap combineBitmap(Bitmap background, Bitmap foreground) {
		if (background == null) {
			return null;
		}
		
		int bgWidth = background.getWidth();
		int bgHeight = background.getHeight();
		Bitmap newmap = Bitmap.createBitmap(bgWidth, bgHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas(newmap);
		canvas.drawBitmap(background, 0, 0, null);
		canvas.drawBitmap(foreground,0,0, null);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return newmap;
	}
}