package com.baidu.idl.main.facesdk.model;

/**
 * Created by litonghui on 2018/9/5.
 */

public class BDFaceSDKCommon {

    public enum BDFaceImageType {
        BDFACE_IMAGE_TYPE_RGB,
        BDFACE_IMAGE_TYPE_BGR,
        BDFACE_IMAGE_TYPE_RGBA,
        BDFACE_IMAGE_TYPE_BGRA,
        BDFACE_IMAGE_TYPE_GRAY,
        BDFACE_IMAGE_TYPE_DEPTH,
        BDFACE_IMAGE_TYPE_YUV_NV21, //  YYYYVUVU
        BDFACE_IMAGE_TYPE_YUV_NV12, // YYYYUVUV
        BDFACE_IMAGE_TYPE_YUV_YV12, // YYYYVVUU
    }

    // 检测类型
    public enum DetectType {
        DETECT_VIS,  // 可见光图像
        DETECT_NIR   // 红外图像
    }

    //  人脸关键点类型枚举
    public enum AlignType {
        BDFACE_ALIGN_TYPE_RGB_ACCURATE,
        BDFACE_ALIGN_TYPE_RGB_FAST,
        BDFACE_ALIGN_TYPE_NIR_ACCURATE,
    }

    /**
     * 图片检测类型，目前支持红外，深度图，可见光
     */
    public enum LiveType {
        BDFACE_SILENT_LIVE_TYPE_RGB,
        BDFACE_SILENT_LIVE_TYPE_NIR,
        BDFACE_SILENT_LIVE_TYPE_DEPTH,
    }

    /**
     * 特征提取图片类型，证件照，可见光，红外
     */
    public enum FeatureType {
        BDFACE_FEATURE_TYPE_LIVE_PHOTO,     // 生活照特征提取
        BDFACE_FEATURE_TYPE_ID_PHOTO,       // 证件照特征提取
        BDFACE_FEATURE_TYPE_NIR,       // 证件照特征提取
        BDFACE_FEATURE_TYPE_RGBD,       // 证件照特征提取
    }

    // 质量检测类型
    public enum FaceQualityType {
        BLUR,         // 模糊
        OCCLUSION,    // 遮挡
        ILLUMINATION  // 光照
    }

    // 表情类型
    public enum BDFaceEmotion {
        BDFACE_EMOTION_FROWN,           // 皱眉
        BDFACE_EMOTION_SMILE,           // 笑
        BDFACE_EMOTION_CALM,            // 平静
    }

    // 情绪
    public enum BDFaceEmotionEnum {
        BDFACE_EMOTIONS_ANGRY,          // 生气
        BDFACE_EMOTIONS_DISGUST,        // 恶心
        BDFACE_EMOTIONS_FEAR,           // 害怕
        BDFACE_EMOTIONS_HAPPY,          // 开心
        BDFACE_EMOTIONS_SAD,            // 伤心
        BDFACE_EMOTIONS_SURPRISE,       // 惊讶
        BDFACE_EMOTIONS_NEUTRAL,        // 无情绪
    }

    // 人脸属性种族
    public enum BDFaceRace {
        BDFACE_RACE_YELLOW,       // 黄种人
        BDFACE_RACE_WHITE,        // 白种人
        BDFACE_RACE_BLACK,        // 黑种人
        BDFACE_RACE_INDIAN,       // 印度人
    }

    // 戴眼镜状态
    public enum BDFaceGlasses {
        BDFACE_NO_GLASSES,   // 无眼镜
        BDFACE_GLASSES,      // 有眼镜
        BDFACE_SUN_GLASSES,  // 墨镜
    }

    // 性别
    public enum BDFaceGender {
        BDFACE_GENDER_FEMALE, // 女性
        BDFACE_GENDER_MALE,   // 男性
    }

    // 凝视方向
    public enum BDFaceGazeDirection {
        BDFACE_GACE_DIRECTION_UP,           // 向上看
        BDFACE_GACE_DIRECTION_DOWN,         // 向下看
        BDFACE_GACE_DIRECTION_RIGHT,        // 向右看
        BDFACE_GACE_DIRECTION_LEFT,         // 向左看
        BDFACE_GACE_DIRECTION_FRONT,        // 向前看
        BDFACE_GACE_DIRECTION_EYE_CLOSE,    // 闭眼
    }

    public enum BDFaceActionLiveType {
        BDFace_ACTION_LIVE_BLINK,           // 眨眨眼
        BDFACE_ACTION_LIVE_OPEN_MOUTH,      // 张张嘴
        BDFACE_ACTION_LIVE_NOD,             // 点点头
        BDFACE_ACTION_LIVE_SHAKE_HEAD,      // 摇摇头
        BDFACE_ACTION_LIVE_LOOK_UP,         // 抬头
        BDFACE_ACTION_LIVE_TURN_LEFT,       // 向左转
        BDFACE_ACTION_LIVE_TURN_RIGHT,      // 向右转
    }

    /**
     * log种类枚举
     */
    public enum BDFaceLogInfo {
        BDFACE_LOG_VALUE_MESSAGE,   // 打印输出值日志
        BDFACE_LOG_ERROR_MESSAGE,   // 打印输出错误日志
        BDFACE_LOG_ALL_MESSAGE,     // 打印所有日志
    }

    public enum BDFaceCoreRunMode {
        BDFACE_LITE_POWER_HIGH,
        BDFACE_LITE_POWER_LOW,
        BDFACE_LITE_POWER_FULL,
        BDFACE_LITE_POWER_NO_BIND,
        BDFACE_LITE_POWER_RAND_HIGH,
        BDFACE_LITE_POWER_RAND_LOW
    }
}
