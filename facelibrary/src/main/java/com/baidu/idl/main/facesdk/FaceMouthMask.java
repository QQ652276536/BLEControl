package com.baidu.idl.main.facesdk;

import android.content.Context;
import android.util.Log;

import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceInstance;
import com.baidu.idl.main.facesdk.utils.FileUitls;

/**
 * Created by taotianran on 2020/02/18.
 */

public class FaceMouthMask {

    private static final String TAG = FaceMouthMask.class.getSimpleName();
    private BDFaceInstance bdFaceInstance;

    public FaceMouthMask(BDFaceInstance thisBdFaceInstance) {
        if (thisBdFaceInstance == null) {
            return;
        }
        bdFaceInstance = thisBdFaceInstance;
    }

    /**
     * 默认 instance
     */
    public FaceMouthMask() {
        bdFaceInstance = new BDFaceInstance();
        bdFaceInstance.getDefautlInstance();
    }

    public void initModel(final Context context,
                          final String mouthMaskModel,
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
                int status = -1;
                byte[] maskModelContent = FileUitls.getModelContent(context, mouthMaskModel);
                if (maskModelContent.length != 0) {
                    status = nativeInitModel(instanceIndex, maskModelContent);
                    if (status != 0) {
                        callback.onResponse(status, "口罩检测模型加载失败");
                        return;
                    }
                }


                if (status == 0) {
                    callback.onResponse(0, "口罩检测模型加载成功");
                } else {
                    callback.onResponse(1, "口罩检测模型加载失败");
                }
                Log.e("bdface", "FaceMouthMask initModel");
            }
        };
        FaceQueue.getInstance().execute(runnable);
    }

    public float[] checkMask(BDFaceImageInstance bdFaceImageInstance, FaceInfo[] faceInfos) {
        if (bdFaceImageInstance == null || faceInfos == null) {
            Log.v(TAG, "Parameter is null");
            return null;
        }
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return null;
        }
        return nativeCheckMask(instanceIndex, bdFaceImageInstance, faceInfos);
    }

    public int uninitModel() {
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return -1;
        }
        return nativeUninitModel(bdFaceInstance.getIndex());
    }

    private native float[] nativeCheckMask(long bdFaceInstanceIndex, BDFaceImageInstance imageInstance,
                                       FaceInfo[] faceInfos);

    private native int nativeInitModel(long bdFaceInstanceIndex, byte[] modelContent);

    private native int nativeUninitModel(long bdFaceInstanceIndex);
}