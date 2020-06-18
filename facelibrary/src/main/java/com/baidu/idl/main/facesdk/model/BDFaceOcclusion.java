package com.baidu.idl.main.facesdk.model;

public class BDFaceOcclusion {
    public float leftEye;    // 左眼遮挡置信度
    public float rightEye;   // 右眼遮挡置信度
    public float nose;        // 鼻子遮挡置信度
    public float mouth;       // 嘴巴遮挡置信度
    public float leftCheek;  // 左脸遮挡置信度
    public float rightCheek; // 右脸遮挡置信度
    public float chin;        // 下巴遮挡置信度

    public BDFaceOcclusion(float leftEye, float rightEye, float nose,
                           float mouth, float leftCheek,
                           float rightCheek, float chin) {
        this.leftEye = leftEye;
        this.rightEye = rightEye;
        this.nose = nose;
        this.mouth = mouth;
        this.leftCheek = leftCheek;
        this.rightCheek = rightCheek;
        this.chin = chin;
    }
}
