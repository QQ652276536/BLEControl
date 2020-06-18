package com.baidu.idl.main.facesdk.model;

/**
 * 接口配置信息
 * Created by litonghui on 2018/9/5.
 */

public class BDFaceSDKConfig {

    /**
     * 输入图像的缩放系数
     */
    public float scaleRatio = -1;

    /**
     * 需要检测的最大人脸数目
     */
    public int maxDetectNum = 10;

    /**
     * 需要检测的最小人脸大小
     */
    public int minFaceSize = 0;

    /**
     * 人脸置信度阈值（检测分值大于该阈值认为是人脸）
     * RGB
     */
    public float notRGBFaceThreshold = 0.5f;
    /**
     * 人脸置信度阈值（检测分值大于该阈值认为是人脸）
     * NIR
     */
    public float notNIRFaceThreshold = 0.5f;

    /**
     * 未跟踪到人脸前的检测时间间隔
     */
    public float detectInterval = 0;

    /**
     * 已跟踪到人脸后的检测时间间隔
     */
    public float trackInterval = 500;

    /**
     * 质量检测模糊，默认不做质量检测
     */
    public boolean isCheckBlur = false;

    /**
     * 质量检测遮挡，默认不做质量检测
     */
    public boolean isOcclusion = false;

    /**
     * 质量检测光照，默认不做质量检测
     */
    public boolean isIllumination = false;

    /**
     * 姿态角检测，获取yaw(左右偏转角)，roll(人脸平行平面内的头部旋转角)，pitch(上下偏转角),默认不检测
     */
    public boolean isHeadPose = false;

    /**
     * 属性检查，获取年龄，种族，是否戴眼镜等信息，默认不检测
     */
    public boolean isAttribute = false;

    /**
     * 7种情绪信息获取，默认不检测
     */
    private boolean isEmotion = false;

    /**
     * 是否扣图，默认不扣图
     */
    public boolean isCropFace = false;

    /**
     * 是否检测眼睛闭合，默认不检测
     */
    public boolean isEyeClose = false;

    /**
     * 是否检测嘴巴闭合，默认不检测
     */
    public boolean isMouthClose = false;

}
