package com.baidu.idl.main.facesdk.model;

import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.BDFaceGazeDirection;

public class BDFaceGazeInfo {

    public float leftEyeConf;
    public float rightEyeConf;

    public BDFaceGazeDirection leftEyeGaze;
    public BDFaceGazeDirection rightEyeGaze;

    public BDFaceGazeInfo(int leftEye, float leftConf, int rightEye, float rightConf) {
        leftEyeGaze = BDFaceGazeDirection.values()[leftEye];
        leftEyeConf = leftConf;
        rightEyeGaze = BDFaceGazeDirection.values()[rightEye];
        rightEyeConf = rightConf;
    }
}
