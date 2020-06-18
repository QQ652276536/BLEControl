package com.baidu.idl.main.facesdk;

import android.content.Context;
import android.util.Log;

import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.LiveType;
import com.baidu.idl.main.facesdk.utils.FileUitls;

/**
 * Created by litonghui on 2018/10/26.
 */

public class FaceLive {

    private static final String TAG = FaceLive.class.getSimpleName();
    private BDFaceInstance bdFaceInstance;

    public FaceLive(BDFaceInstance thisBdFaceInstance) {
        if (thisBdFaceInstance == null) {
            return;
        }
        bdFaceInstance = thisBdFaceInstance;
    }

    /**
     * 默认 instance
     */
    public FaceLive() {
        bdFaceInstance = new BDFaceInstance();
        bdFaceInstance.getDefautlInstance();
    }

    public void initModel(final Context context,
                          final String visModel,
                          final String nirModel,
                          final String depthModel,
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
                    statusVis = nativeSilentLiveModelInit(instanceIndex, visModelContent,
                            LiveType.BDFACE_SILENT_LIVE_TYPE_RGB.ordinal());
                    if (statusVis != 0) {
                        callback.onResponse(statusVis, "Vis 活体模型加载失败");
                        return;
                    }
                }

                int statusNir = -1;
                byte[] nirModelContent = FileUitls.getModelContent(context, nirModel);
                if (nirModelContent.length != 0) {
                    statusNir = nativeSilentLiveModelInit(instanceIndex, nirModelContent,
                            LiveType.BDFACE_SILENT_LIVE_TYPE_NIR.ordinal());
                    if (statusNir != 0) {
                        callback.onResponse(statusNir, "Nir 活体模型加载失败");
                        return;
                    }
                }

                int statusDepth = -1;
                byte[] depthModelContent = FileUitls.getModelContent(context, depthModel);
                if (depthModelContent.length != 0) {
                    statusDepth = nativeSilentLiveModelInit(instanceIndex, depthModelContent,
                            LiveType.BDFACE_SILENT_LIVE_TYPE_DEPTH.ordinal());
                    if (statusDepth != 0) {
                        callback.onResponse(statusDepth, "Deep 活体模型加载失败");
                        return;
                    }
                }
                if (statusVis == 0 || statusNir == 0 || statusDepth == 0) {
                    callback.onResponse(0, "活体模型加载成功");
                } else {
                    callback.onResponse(1, "活体模型加载失败");
                }
                Log.e("bdface", "FaceLive initModel");
            }
        };
        FaceQueue.getInstance().execute(runnable);
    }

    public float silentLive(LiveType type, BDFaceImageInstance bdFaceImageInstance, float[] landmarks) {
        if (type == null || bdFaceImageInstance == null || landmarks == null) {
            Log.v(TAG, "Parameter is null");
            return -1;
        }
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return -1;
        }
        return nativeSilentLive(instanceIndex, type.ordinal(), bdFaceImageInstance, landmarks);
    }

    public int uninitModel() {
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return -1;
        }
        return nativeUninitModel(bdFaceInstance.getIndex());
    }

    private native float nativeSilentLive(long bdFaceInstanceIndex, int type, BDFaceImageInstance imageInstance,
                                          float[] landmarks);

    private native int nativeSilentLiveModelInit(long bdFaceInstanceIndex, byte[] modelContent, int type);

    private native int nativeUninitModel(long bdFaceInstanceIndex);
}
