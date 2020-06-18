package com.baidu.idl.main.facesdk;

import com.baidu.idl.main.facesdk.model.BDFaceOcclusion;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.BDFaceEmotionEnum;

public class FaceInfo {

    /**
     * ---------detect---------**
     * 人脸索引值，标记连续视频帧追踪中人脸ID
     */
    public int faceID;

    /**
     * 人脸中心点x坐标
     */
    public float centerX;

    /**
     * 人脸中心点y坐标
     */
    public float centerY;

    /**
     * 人脸宽度
     */
    public float width;

    /**
     * 人脸高度
     */
    public float height;

    /**
     * 人脸角度
     */
    public float angle;

    /**
     * 人脸置信度
     */
    public float score;

    /**
     * 人脸72个关键点数据（鼻子，眼镜，嘴巴，眉毛）
     */
    public float[] landmarks;

    /** -------- config head pose ture------**/

    /**
     * 人脸左右偏转角
     */
    public float yaw;

    /**
     * 人脸平行平面内的头部旋转角
     */
    public float roll;

    /**
     * 人脸上下偏转角
     */
    public float pitch;

    /** ---------config quality blur illum occluton ture---------**/

    /**
     * 人脸模糊度信息
     */
    public float bluriness;

    /**
     * 人脸光照信息
     */
    public int illum;

    /**
     * 人脸遮挡信息
     */
    public BDFaceOcclusion occlusion;

    /**---------config Attribute ture-------**/

    /**
     * 人脸年龄
     */
    public int age;

    /**
     * 人脸种族（黄，白，黑，印度）
     */
    public BDFaceSDKCommon.BDFaceRace race;

    /**
     * 人脸佩戴眼镜状态（无眼镜，有眼镜，墨镜）
     */
    public BDFaceSDKCommon.BDFaceGlasses glasses;

    /**
     * 人脸性别（男女）状态
     */
    public BDFaceSDKCommon.BDFaceGender gender;

    /**
     * 人脸3种情绪(中性，微笑，大笑)
     */
    public BDFaceSDKCommon.BDFaceEmotion emotionThree;

    /**---------config emotion ture-------**/

    /**
     * 人脸7种情绪（生气，恶心，害怕，开心，伤心，惊讶，无情绪）
     */
    public BDFaceSDKCommon.BDFaceEmotionEnum emotionSeven;


    /**---------config isMouthClose ture-------**/

    /**
     * 嘴巴闭合置信度
     */
    public float mouthclose;

    /**---------config isEyeClose ture-------**/

    /**
     * 左眼闭合的置信度
     */
    public float leftEyeclose;
    /**
     * 右眼闭合的置信度
     */
    public float rightEyeclose;

    public FaceInfo(int faceID, float[] box, float[] landmarks) {
        this.faceID = faceID;
        if (box != null && box.length == 6) {
            this.centerX = box[0];
            this.centerY = box[1];
            this.width = box[2];
            this.height = box[3];
            this.angle = box[4];
            this.score = box[5];
        }

        this.landmarks = landmarks;
    }

    public FaceInfo(int faceID, float[] box, float[] landmarks,
                    float[] headpose, float[] quality, int[] attr, float[] faceclose) {
        this.faceID = faceID;
        if (box != null && box.length == 6) {
            this.centerX = box[0];
            this.centerY = box[1];
            this.width = box[2];
            this.height = box[3];
            this.angle = box[4];
            this.score = box[5];
        }

        this.landmarks = landmarks;

        if (headpose != null && headpose.length == 3) {
            this.yaw = headpose[0];
            this.roll = headpose[1];
            this.pitch = headpose[2];
        }

        if (quality != null && quality.length == 9) {
            this.occlusion = new BDFaceOcclusion(quality[0], quality[1], quality[2],
                    quality[3], quality[4], quality[5], quality[6]);
            this.illum = (int) quality[7];
            this.bluriness = quality[8];
        }

        if (attr != null && attr.length == 6) {
            this.age = attr[0];
            this.race = BDFaceSDKCommon.BDFaceRace.values()[attr[1]];
            this.emotionThree = BDFaceSDKCommon.BDFaceEmotion.values()[attr[2]];
            this.glasses = BDFaceSDKCommon.BDFaceGlasses.values()[attr[3]];
            this.gender = BDFaceSDKCommon.BDFaceGender.values()[attr[4]];
            this.emotionSeven = BDFaceEmotionEnum.values()[attr[5]];
        }

        if (faceclose != null && faceclose.length == 3) {
            leftEyeclose = faceclose[0];
            rightEyeclose = faceclose[1];
            mouthclose = faceclose[2];
        }
    }
}
