package com.baidu.idl.face.main.callback;

import android.hardware.Camera;

public interface CameraDataCallback {
    /**
     * @param data   预览数据
     * @param camera 相机设备
     * @param width  预览宽
     * @param height 预览高
     */
    void onGetCameraData(byte[] data, Camera camera, int width, int height);
}
