package com.baidu.idl.main.facesdk;

import android.content.Context;
import android.util.Log;

import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.FeatureType;
import com.baidu.idl.main.facesdk.model.Feature;
import com.baidu.idl.main.facesdk.utils.FileUitls;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by litonghui on 2018/10/26.
 */

public class FaceFeature {

    private static final String TAG = FaceFeature.class.getSimpleName();
    private BDFaceInstance bdFaceInstance;

    public FaceFeature(BDFaceInstance thisBdFaceInstance) {
        if (thisBdFaceInstance == null) {
            return;
        }
        bdFaceInstance = thisBdFaceInstance;
    }

    /**
     * 默认instance
     */
    public FaceFeature() {
        bdFaceInstance = new BDFaceInstance();
        bdFaceInstance.getDefautlInstance();
    }

    public void initModel(final Context context,
                          final String idPhotoModel,
                          final String visModel,
                          final String nirModel,
                          final Callback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (context == null) {
                    callback.onResponse(1, "没有初始化上下文");
                    return;
                }
                long instanceIndex = bdFaceInstance.getIndex();
                if (instanceIndex == 0) {
                    return;
                }
                int statusId = -1;
                byte[] idPhotoModelContent = FileUitls.getModelContent(context, idPhotoModel);
                if (idPhotoModelContent.length != 0) {
                    statusId = nativeFeatureModelInit(instanceIndex, idPhotoModelContent,
                            FeatureType.BDFACE_FEATURE_TYPE_ID_PHOTO.ordinal());
                    if (statusId != 0) {
                        callback.onResponse(statusId, "证件照识别模型加载失败");
                        return;
                    }
                }

                int statusVis = -1;
                byte[] visModelContent = FileUitls.getModelContent(context, visModel);
                if (visModelContent.length != 0) {
                    statusVis = nativeFeatureModelInit(instanceIndex, visModelContent,
                            FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO.ordinal());
                    if (statusVis != 0) {
                        callback.onResponse(statusVis, "Vis 识别模型加载失败");
                        return;
                    }
                }

                if (statusId == 0 || statusVis == 0) {
                    callback.onResponse(0, "识别模型加载成功");
                } else {
                    callback.onResponse(1, "识别模型加载失败");
                }
                Log.v(TAG, "FaceFeature initModel");
            }
        };
        FaceQueue.getInstance().execute(runnable);
    }

    public void initModel(final Context context,
                          final String idPhotoModel,
                          final String visModel,
                          final String nirModel,
                          final String rgbdModel,
                          final Callback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (idPhotoModel.length() == 0 && visModel.length() == 0 &&
                        nirModel.length() == 0 && rgbdModel.length() == 0) {
                    Log.v(TAG, "FaceFeature未设置模型路径");
                    callback.onResponse(1, "FaceFeature未设置模型路径");
                    return;
                }


                if (context == null) {
                    callback.onResponse(1, "没有初始化上下文");
                    return;
                }
                long instanceIndex = bdFaceInstance.getIndex();
                if (instanceIndex == 0) {
                    return;
                }

                int statusIdPhoto = -1;
                byte[] modelContent;

                if (idPhotoModel.length() != 0) {
                    modelContent = FileUitls.getModelContent(context, idPhotoModel);
                    if (modelContent.length == 0) {
                        Log.v(TAG, "证件照识别模型读取失败");
                        callback.onResponse(statusIdPhoto, "证件照识别模型读取失败");
                        return;
                    }
                    statusIdPhoto = nativeFeatureModelInit(instanceIndex, modelContent,
                            FeatureType.BDFACE_FEATURE_TYPE_ID_PHOTO.ordinal());
                    if (statusIdPhoto != 0) {
                        Log.v(TAG, "证件照识别模型加载失败: " + statusIdPhoto);
                        callback.onResponse(statusIdPhoto, "证件照识别模型加载失败");
                        return;
                    }
                }
                int statusLivePhoto = -1;
                if (visModel.length() != 0) {
                    modelContent = FileUitls.getModelContent(context, visModel);
                    if (modelContent.length == 0) {
                        Log.v(TAG, "生活照识别模型读取失败");
                        callback.onResponse(statusLivePhoto, "生活照识别模型读取失败");
                        return;
                    }
                    statusLivePhoto = nativeFeatureModelInit(instanceIndex, modelContent,
                            FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO.ordinal());
                    if (statusLivePhoto != 0) {
                        Log.v(TAG, "生活照识别模型加载失败: " + statusLivePhoto);
                        callback.onResponse(statusLivePhoto, "生活照识别模型加载失败");
                        return;
                    }
                }

                int statusNirPhoto = -1;
                if (nirModel.length() != 0) {
                    modelContent = FileUitls.getModelContent(context, nirModel);
                    if (modelContent.length == 0) {
                        Log.v(TAG, "Nir识别模型读取失败");
                        callback.onResponse(statusNirPhoto, "Nir识别模型读取失败");
                        return;
                    }
                    statusNirPhoto = nativeFeatureModelInit(instanceIndex, modelContent,
                            FeatureType.BDFACE_FEATURE_TYPE_NIR.ordinal());
                    if (statusNirPhoto != 0) {
                        Log.v(TAG, "Nir识别模型加载失败: " + statusNirPhoto);
                        callback.onResponse(statusNirPhoto, "Nir识别模型加载失败");
                        return;
                    }
                }
                int statusRGBDPhoto = -1;
                if (rgbdModel.length() != 0) {
                    modelContent = FileUitls.getModelContent(context, rgbdModel);
                    if (modelContent.length == 0) {
                        Log.v(TAG, "rgbd识别模型读取失败");
                        callback.onResponse(statusRGBDPhoto, "rgbd识别模型读取失败");
                        return;
                    }
                    statusRGBDPhoto = nativeFeatureModelInit(instanceIndex, modelContent,
                            FeatureType.BDFACE_FEATURE_TYPE_RGBD.ordinal());
                    if (statusRGBDPhoto != 0) {
                        Log.v(TAG, "rgbd识别模型加载失败: " + statusRGBDPhoto);
                        callback.onResponse(statusRGBDPhoto, "rgbd识别模型加载失败");
                        return;
                    }
                }

                Log.v(TAG, "FaceFeature initModel");
                callback.onResponse(0, "识别模型加载成功");

            }
        };
        FaceQueue.getInstance().execute(runnable);
    }

    /**
     * 人脸特征提取
     *
     * @param featureType
     * @param feature
     * @return
     */
    public float feature(FeatureType featureType, BDFaceImageInstance imageInstance,
                         float[] landmarks, byte[] feature) {
        if (featureType == null || landmarks == null || feature == null || imageInstance == null
                || landmarks.length < 0) {
            Log.v(TAG, "Parameter is null");
            return -1;
        }
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return -1;
        }
        return nativeFeature(instanceIndex, featureType.ordinal(), imageInstance, landmarks, feature);
    }

    public float featureRGBD(FeatureType featureType, BDFaceImageInstance imageInstance,
                             BDFaceImageInstance imageInstance_depth, float[] landmarks, byte[] feature) {
        if (featureType == null || landmarks == null || feature == null || imageInstance == null
                || imageInstance_depth == null || landmarks.length < 0) {
            Log.v(TAG, "Parameter is null");
            return -1;
        }
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return -1;
        }
        return nativeRGBDFeature(instanceIndex, featureType.ordinal(), imageInstance,
                imageInstance_depth, landmarks, feature);
    }

    /**
     * 人脸特征比对,并且映射到0--100
     *
     * @param featureType
     * @param feature1
     * @param feature2
     * @return
     */
    public float featureCompare(FeatureType featureType, byte[] feature1, byte[] feature2, boolean isPercent) {
        if (featureType == null || feature1 == null || feature2 == null) {
            Log.v(TAG, "Parameter is null");
            return -1;
        }
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return -1;
        }
        return nativeFeatureCompare(instanceIndex, featureType.ordinal(), feature1, feature2, isPercent ? 1 : 0);
    }

    /**
     * 1:N特征设置
     * 特征集合预加载接口，继承Feature，必须初始化id 和 feature 字段，用于1：N 内部实现和数据返回。
     *
     * @param features
     * @return
     */
    public int featurePush(List<? extends Feature> features) {
        return nativefeaturePush(features);
    }

    /**
     * 1:N特征比对
     * 当前feature和预加载Feature 集合比对，返回预加载Feature集合中命中的id，
     * feature 字段和比对分值score；用户可以通过id 在数据库中查找全量信息。
     *
     * @param firstFaceFeature
     * @param featureType
     * @param topNum
     * @return
     */
    public ArrayList<Feature> featureSearch(byte[] firstFaceFeature, FeatureType featureType,
                                            int topNum, boolean isPercent) {
        if (featureType == null) {
            Log.v(TAG, "Parameter is null");
            return null;
        }
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return null;
        }
        return nativeFeatureSearch(instanceIndex, firstFaceFeature, featureType.ordinal(),
                topNum, isPercent ? 1 : 0);
    }

    public int uninitModel() {
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return -1;
        }
        return nativeUninitModel(instanceIndex);
    }


    private native int nativeFeatureModelInit(long bdFaceInstanceIndex, byte[] modelContent, int featureType);

    private native float nativeFeatureCompare(long bdFaceInstanceIndex, int featureType, byte[] firstFaceFeature,
                                              byte[] secondFaceFeature, int isPercent);

    private native float nativeFeature(long bdFaceInstanceIndex, int featureType, BDFaceImageInstance imageInstance,
                                       float[] landmarks, byte[] feature);

    private native float nativeRGBDFeature(long bdFaceInstanceIndex, int featureType, BDFaceImageInstance imageInstance,
                                           BDFaceImageInstance imageInstance_depth, float[] landmarks, byte[] feature);

    private native int nativefeaturePush(List<? extends Feature> features);

    private native ArrayList<Feature> nativeFeatureSearch(long bdFaceInstanceIndex, byte[] firstFaceFeature,
                                                          int featureType, int topNum, int isPercent);

    private native int nativeUninitModel(long bdFaceInstanceIndex);

}