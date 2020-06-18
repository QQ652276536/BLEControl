package com.baidu.idl.main.facesdk.model;

/**
 * Created by v_shishuaifeng on 2019/11/8.
 */

public class BDFaceDetectListConf {
     /**
     * 检测（在track 精度不足时开启）
     */
    public boolean usingDetect = false;
    /**
     * 检测（在track 精度不足时开启）
     */
    public boolean usingAlign = true;
    /**
     * 质量检测模糊，默认不做质量检测
     */
    public boolean usingQuality = false;
    /**
     * 姿态角检测，获取yaw(左右偏转角)，roll(人脸平行平面内的头部旋转角)，pitch(上下偏转角),默认不检测
     */
    public boolean usingHeadPose = false;
    /**
     * 属性检查，获取年龄，种族，是否戴眼镜等信息，默认不检测
     */
    public boolean usingAttribute = false;
    /**
     * 7种情绪信息获取，默认不检测
     */
    public boolean usingEmotion = false;
    /**
     * 是否检测眼睛闭合，默认不检测
     */
    public boolean usingEyeClose = false;
    /**
     * 是否检测嘴巴闭合，默认不检测
     */
    public boolean usingMouthClose = false;

}
