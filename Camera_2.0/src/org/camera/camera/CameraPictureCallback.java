package org.camera.camera;

/**
 * �ص��������״̬�Ĵ���
 */
public interface CameraPictureCallback {
	/** ���Ű���֮��ʼ��ʾ */
	void onStart();

	/** ���������ʾ��Ϣ */
	void onResult(boolean success);
}