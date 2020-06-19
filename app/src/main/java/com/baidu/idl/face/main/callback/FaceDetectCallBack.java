package com.baidu.idl.face.main.callback;

import com.baidu.idl.face.main.model.LivenessModel;

public interface FaceDetectCallBack {
    public void onFaceDetectCallback(LivenessModel livenessModel);

    public void onTip(int code, String msg);

    void onFaceDetectDarwCallback(LivenessModel livenessModel);
}
