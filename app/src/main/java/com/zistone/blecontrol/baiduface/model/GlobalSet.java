package com.zistone.blecontrol.baiduface.model;

public class GlobalSet {

    // 模型在asset 下path 为空
    public static final String PATH = "";
    // 模型在SD 卡下写对应的绝对路径
    // public static final String PATH = "/storage/emulated/0/baidu_face/model/";

    public static final int FEATURE_SIZE = 512;

    public static final String TIME_TAG = "face_time";

    // 遮罩比例
    public static final float SURFACE_RATIO = 0.6f;

    public static final String DETECT_VIS_MODEL = PATH + "detect/detect_rgb-customized-pa-faceid4_0.model.int8-0.0.6.1";
    public static final String DETECT_NIR_MODE = PATH + "detect/detect_nir-faceboxes-pa-faceid4_0.model.int8-0.0.4.2";
    public static final String ALIGN_RGB_MODEL = PATH + "align/align_rgb-customized-ca-paddle_6_4_0_1.model.float32-6.4.0.2";
    public static final String ALIGN_NIR_MODEL = PATH + "align/align-mobilenet-pa-nir_faceid4_0.model.int8-0.7.4.2";
    public static final String ALIGN_TRACK_MODEL = PATH + "align/align-customized-pa-mobile.model.float32-0.7.5.2";
    public static final String LIVE_VIS_MODEL = PATH + "silent_live/liveness_rgb-customized-pa-mobile.model.float32-4.1.10.1";
    public static final String LIVE_NIR_MODEL = PATH + "silent_live/liveness_nir-customized-pa-paddle_lite.model.int8-4.1.5.1";
    public static final String LIVE_DEPTH_MODEL = PATH + "silent_live/liveness_depth-customized-pa-autodl_mobile.model.int8-4.1.7.1.lite";
    public static final String RECOGNIZE_VIS_MODEL = PATH + "feature/feature_live-mnasnet-pa-mnasv4_int8.model.int8-1.0.27.1";
    public static final String RECOGNIZE_IDPHOTO_MODEL = PATH + "feature/feature_id-mnasnet-pa-dynamic_norm.model.float32-1.0.16.1";
    public static final String RECOGNIZE_NIR_MODEL = PATH + "feature/feature_nir-customized-pa-nir_91p5.model.float32-1.0.11.2";
    public static final String RECOGNIZE_RGBD_MODEL = PATH + "feature/feature_rgbd-mnasnet-pa-RGBD98p54.model.float32-1.0.15.2";
    public static final String OCCLUSION_MODEL = PATH + "occlusion/occlusion-customized-pa-mobile.model.float32-2.0.4.1";
    public static final String BLUR_MODEL = PATH + "blur/blur-customized-pa-server.model.float32-3.0.2.1.lite";

    public static final String ATTRIBUTE_MODEL = PATH + "attribute/attribute-customized-pa-mobile.model.float32-1.0.9.2";
    public static final String EMOTION_MODEL = PATH + "";
    public static final String GAZE_MODEL = PATH + "gaze/gaze-customized-pa-model.model.float32-1.0.3.1";
    public static final String MOUTH_MASK = PATH + "mouth_mask/mouth_mask-customized-pa-paddle_lite.model.float32-1.0.6.1";
    // 图片尺寸限制大小
    public static final int PICTURE_SIZE = 1000000;

    // 摄像头类型
    public static final String TYPE_CAMERA = "TYPE_CAMERA";
    public static final int ORBBEC = 1;
    public static final int IMIMECT = 2;
    public static final int ORBBECPRO = 3;
    public static final int ORBBECPROS1 = 4;
    public static final int ORBBECPRODABAI = 5;
    public static final int ORBBECPRODEEYEA = 6;
    public static final int ORBBECATLAS = 7;

}
