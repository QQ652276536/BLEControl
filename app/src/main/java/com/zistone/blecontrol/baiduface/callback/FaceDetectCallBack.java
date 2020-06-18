package com.zistone.blecontrol.baiduface.callback;

import com.zistone.blecontrol.baiduface.model.LivenessModel;

public interface FaceDetectCallBack {
    public void onFaceDetectCallback(LivenessModel livenessModel);

    public void onTip(int code, String msg);

    void onFaceDetectDarwCallback(LivenessModel livenessModel);
}
