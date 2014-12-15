package org.camera.camera;

/**
 * 回调相机拍照状态的处理
 */
public interface CameraPictureCallback {
	/** 快门按下之后开始提示 */
	void onStart();

	/** 完成销毁提示信息 */
	void onResult(boolean success);
}