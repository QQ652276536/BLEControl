package com.baidu.idl.main.facesdk;

import android.content.Context;
import android.util.Log;

import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceDetectListConf;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.model.BDFaceSDKConfig;
import com.baidu.idl.main.facesdk.utils.FileUitls;

import java.util.concurrent.locks.ReentrantLock;

public class FaceDetect {

    private static final String TAG = FaceDetect.class.getSimpleName();
    private BDFaceInstance bdFaceInstance;
    private ReentrantLock lock = null;

    public FaceDetect(BDFaceInstance thisBdFaceInstance) {
        if (thisBdFaceInstance == null) {
            return;
        }
        bdFaceInstance = thisBdFaceInstance;
        lock = new ReentrantLock();
    }

    /**
     * 默认instance
     */
    public FaceDetect() {
        bdFaceInstance = new BDFaceInstance();
        bdFaceInstance.getDefautlInstance();
        lock = new ReentrantLock();
    }

    public void initModel(final Context context,
                          final String visModel,
                          final String nirModel,
                          final String alignModel,
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
                int statusVis = -1;
                byte[] visModelContent = FileUitls.getModelContent(context, visModel);
                if (visModelContent.length != 0) {
                    statusVis = nativeDetectModelInit(instanceIndex, visModelContent,
                            BDFaceSDKCommon.DetectType.DETECT_VIS.ordinal());
                    if (statusVis != 0) {
                        callback.onResponse(statusVis, "Vis检测模型加载失败");
                        return;
                    }
                }

                int statusNir = -1;
                byte[] nirModelContent = FileUitls.getModelContent(context, nirModel);
                if (nirModelContent.length != 0) {
                    statusNir = nativeDetectModelInit(instanceIndex, nirModelContent,
                            BDFaceSDKCommon.DetectType.DETECT_NIR.ordinal());
                    if (statusNir != 0) {
                        callback.onResponse(statusNir, "Nir检测模型加载失败");
                        return;
                    }
                }

                int statusAlign = -1;
                byte[] alignModelContent = FileUitls.getModelContent(context, alignModel);
                if (alignModelContent.length != 0) {
                    statusAlign = nativeAlignModelInit(instanceIndex,
                            BDFaceSDKCommon.DetectType.DETECT_VIS.ordinal(),
                            BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_ACCURATE.ordinal(),
                            alignModelContent);
                    if (statusAlign != 0) {
                        callback.onResponse(statusAlign, "对齐模型加载失败");
                        return;
                    }
                }
                int statusTrack = nativeLoadTrack(instanceIndex,
                        BDFaceSDKCommon.DetectType.DETECT_VIS.ordinal(),
                        BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_ACCURATE.ordinal());
                if (statusTrack != 0) {
                    callback.onResponse(statusTrack, "跟踪能力加载失败");
                    return;
                }
                if ((statusVis == 0 || statusNir == 0) && statusAlign == 0) {
                    callback.onResponse(0, "检测对齐模型加载成功");
                } else {
                    callback.onResponse(1, "检测对齐模型加载失败");
                }
                Log.e(TAG, "FaceDetect initModel");
            }
        };
        FaceQueue.getInstance().execute(runnable);
    }

    public void initModel(final Context context,
                          final String detectModel,
                          final String alignModel,
                          final BDFaceSDKCommon.DetectType detectType,
                          final BDFaceSDKCommon.AlignType alignType,
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
                int statusDetect = -1;
                byte[] visModelContent = FileUitls.getModelContent(context, detectModel);
                if (visModelContent.length != 0) {
                    statusDetect = nativeDetectModelInit(instanceIndex, visModelContent,
                            detectType.ordinal());
                    if (statusDetect != 0) {
                        callback.onResponse(statusDetect, "检测模型加载失败");
                        return;
                    }
                }

                int statusAlign = -1;
                byte[] alignModelContent = FileUitls.getModelContent(context, alignModel);
                if (alignModelContent.length != 0) {
                    statusAlign = nativeAlignModelInit(instanceIndex, detectType.ordinal(),
                            alignType.ordinal(), alignModelContent);
                    if (statusAlign != 0) {
                        callback.onResponse(statusAlign, "对齐模型加载失败");
                        return;
                    }
                }

                int statusTrack = nativeLoadTrack(instanceIndex, detectType.ordinal(),
                        alignType.ordinal());
                if (statusTrack != 0) {
                    callback.onResponse(statusTrack, "跟踪能力加载失败");
                    return;
                }
                if (statusDetect == 0 && statusAlign == 0) {
                    callback.onResponse(0, "检测对齐模型加载成功");
                } else {
                    callback.onResponse(1, "检测对齐模型加载失败");
                }
                Log.e(TAG, "FaceDetect initModel");
            }
        };
        FaceQueue.getInstance().execute(runnable);
    }

    public void initQuality(final Context context,
                            final String blurModel,
                            final String occlurModel,
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
                int statusblur = -1;
                byte[] blurModelContent = FileUitls.getModelContent(context, blurModel);
                if (blurModelContent.length != 0) {
                    statusblur = nativeQualityModelInit(instanceIndex, blurModelContent,
                            BDFaceSDKCommon.FaceQualityType.BLUR.ordinal());
                    if (statusblur != 0) {
                        callback.onResponse(statusblur, "模糊模型加载失败");
                        return;
                    }
                }

                int statusocclur = -1;
                byte[] occlurModelContent = FileUitls.getModelContent(context, occlurModel);
                if (occlurModelContent.length != 0) {
                    statusocclur = nativeQualityModelInit(instanceIndex, occlurModelContent,
                            BDFaceSDKCommon.FaceQualityType.OCCLUSION.ordinal());
                    if (statusocclur != 0) {
                        callback.onResponse(statusocclur, "遮挡模型加载失败");
                        return;
                    }
                }
                if (statusblur == 0 || statusocclur == 0) {
                    callback.onResponse(0, "质量模型加载成功");
                } else {
                    callback.onResponse(1, "质量模型加载失败");
                }
                Log.e(TAG, "FaceDetect initQuality");
            }
        };
        FaceQueue.getInstance().execute(runnable);
    }

    public void initAttrEmo(final Context context,
                            final String atttibuteModel,
                            final String emotionModel,
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
                int statusAttr = -1;
                byte[] atttibuteModelContent = FileUitls.getModelContent(context, atttibuteModel);
                if (atttibuteModelContent.length != 0) {
                    statusAttr = nativeAttributeModelInit(instanceIndex, atttibuteModelContent);
                    if (statusAttr != 0) {
                        callback.onResponse(statusAttr, "属性模型加载失败");
                        return;
                    }
                }

                int statusEmo = -1;
                byte[] emotionModelContent = FileUitls.getModelContent(context, emotionModel);
                if (emotionModelContent.length != 0) {
                    statusEmo = nativeEmotionsModelInit(instanceIndex, emotionModelContent);
                    if (statusEmo != 0) {
                        callback.onResponse(statusEmo, "情绪模型加载失败");
                        return;
                    }
                }
                if (statusAttr == 0 || statusEmo == 0) {
                    callback.onResponse(0, "属性模型加载成功");
                } else {
                    callback.onResponse(1, "属性模型加载失败");
                }
                Log.e("bdface", "FaceAttributes initModel");
            }
        };
        FaceQueue.getInstance().execute(runnable);
    }

    public void initFaceClose(final Context context,
                              final String eyecloseModel,
                              final String mouthcloseModel,
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
                int statusEyeClose = -1;
                byte[] eyecloseModelContent = FileUitls.getModelContent(context, eyecloseModel);
                if (eyecloseModelContent.length != 0) {
                    statusEyeClose = nativeFaceCloseModelInit(instanceIndex, eyecloseModelContent, 0);
                    if (statusEyeClose != 0) {
                        callback.onResponse(statusEyeClose, "眼睛闭合模型加载失败");
                        return;
                    }
                }

                int statusMouthClose = -1;
                byte[] mouthcloseModelContent = FileUitls.getModelContent(context, mouthcloseModel);
                if (mouthcloseModelContent.length != 0) {
                    statusMouthClose = nativeFaceCloseModelInit(instanceIndex, mouthcloseModelContent, 1);
                    if (statusMouthClose != 0) {
                        callback.onResponse(statusMouthClose, "嘴巴闭合模型加载失败");
                        return;
                    }
                }
                if (statusEyeClose == 0 || statusMouthClose == 0) {
                    callback.onResponse(0, "闭眼闭嘴模型加载成功");
                } else {
                    callback.onResponse(1, "闭眼闭嘴模型加载失败");
                }
                Log.e("bdface", "FaceClose initModel");
            }
        };
        FaceQueue.getInstance().execute(runnable);
    }

    public void loadConfig(BDFaceSDKConfig config) {
        if (bdFaceInstance == null) {
            return;
        }
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return;
        }
        nativeLoadConfig(instanceIndex, config);
    }

    public FaceInfo[] detect(BDFaceSDKCommon.DetectType type, BDFaceImageInstance imageInstance) {
        if (type == null || imageInstance == null) {
            Log.v(TAG, "Parameter is null");
            return null;
        }
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return null;
        }

        if (!lock.tryLock()) {
            return null;
        }

        FaceInfo[] faceInfosTemp = nativeDetect(instanceIndex, type.ordinal(),
                BDFaceSDKCommon.AlignType.BDFACE_ALIGN_TYPE_RGB_ACCURATE.ordinal(), imageInstance);

        lock.unlock();
        return faceInfosTemp;
    }

    public FaceInfo[] detect(BDFaceSDKCommon.DetectType detectType,
                             BDFaceSDKCommon.AlignType alignType,
                             BDFaceImageInstance imageInstance,
                             FaceInfo[] faceInfos,
                             BDFaceDetectListConf bdFaceDetectListConfig) {

        if (detectType == null || imageInstance == null || alignType == null) {
            Log.v(TAG, "Parameter is null");
            return null;
        }
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return null;
        }
        if (!lock.tryLock()) {
            return null;
        }

        FaceInfo[] faceInfosTemp = nativeFlexibleDetect(instanceIndex, detectType.ordinal(),
                alignType.ordinal(), imageInstance, faceInfos, bdFaceDetectListConfig);
        lock.unlock();

        return faceInfosTemp;
    }

    public FaceInfo[] track(BDFaceSDKCommon.DetectType detectType, BDFaceImageInstance imageInstance) {
        if (detectType == null || imageInstance == null) {
            Log.v(TAG, "Parameter is null");
            return null;
        }
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return null;
        }
        if (!lock.tryLock()) {
            return null;
        }

        FaceInfo[] faceInfosTemp = nativeTrack(instanceIndex, detectType.ordinal(), imageInstance);

        lock.unlock();
        return faceInfosTemp;
    }

    public FaceInfo[] track(BDFaceSDKCommon.DetectType detectType, BDFaceSDKCommon.AlignType alignType,
                            BDFaceImageInstance imageInstance) {
        if (detectType == null || imageInstance == null || alignType == null) {
            Log.v(TAG, "Parameter is null");
            return null;
        }
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return null;
        }
        FaceInfo[] faceInfosTemp = nativeFastTrack(instanceIndex,
                detectType.ordinal(), alignType.ordinal(), imageInstance);
        return faceInfosTemp;
    }

    public BDFaceImageInstance cropFace(BDFaceImageInstance imageInstance, float[] landmark) {
        if (imageInstance == null || landmark == null) {
            Log.v(TAG, "Parameter is null");
            return null;
        }
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return null;
        }
        return nativeCropFace(instanceIndex, imageInstance, landmark);
    }

    public int uninitModel() {
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return -1;
        }
        return nativeUninitModel(instanceIndex);
    }

    private native int nativeDetectModelInit(long bdFaceInstanceIndex, byte[] modelContent, int type);

    private native int nativeAlignModelInit(long bdFaceInstanceIndex, int track_type, int align_type,
                                            byte[] modelContent);

    private native int nativeLoadTrack(long bdFaceInstanceIndex, int detectType, int alignType);

    private native int nativeQualityModelInit(long bdFaceInstanceIndex, byte[] modelContent, int type);

    private native int nativeAttributeModelInit(long bdFaceInstanceIndex, byte[] modelContent);

    private native int nativeEmotionsModelInit(long bdFaceInstanceIndex, byte[] modelContent);

    private native int nativeFaceCloseModelInit(long bdFaceInstanceIndex, byte[] modelContent, int type);

    private native void nativeLoadConfig(long bdFaceInstanceIndex, BDFaceSDKConfig config);

    private native FaceInfo[] nativeTrack(long bdFaceInstanceIndex,
                                          int detectType, BDFaceImageInstance imageInstance);

    private native FaceInfo[] nativeFastTrack(long bdFaceInstanceIndex, int detectType, int alignType,
                                              BDFaceImageInstance imageInstance);

    private native FaceInfo[] nativeDetect(long bdFaceInstanceIndex, int detectType, int alignType,
                                           BDFaceImageInstance imageInstance);

    private native FaceInfo[] nativeFlexibleDetect(long bdFaceInstanceIndex, int detectType, int alignType,
                                                   BDFaceImageInstance imageInstance, FaceInfo[] faceInfos,
                                                   BDFaceDetectListConf detectListConf);

    private native BDFaceImageInstance nativeCropFace(long bdFaceInstanceIndex, BDFaceImageInstance imageInstance,
                                                      float[] landmark);

    private native int nativeUninitModel(long bdFaceInstanceIndex);

}
