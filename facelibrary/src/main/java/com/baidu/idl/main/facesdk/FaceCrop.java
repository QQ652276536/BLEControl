package com.baidu.idl.main.facesdk;

import android.util.Log;

import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceInstance;

import java.util.concurrent.atomic.AtomicInteger;

public class FaceCrop {
    private static final String TAG = FaceCrop.class.getSimpleName();
    private BDFaceInstance bdFaceInstance;

    public FaceCrop(BDFaceInstance thisBdFaceInstance) {
        if (thisBdFaceInstance == null) {
            return;
        }
        bdFaceInstance = thisBdFaceInstance;
    }

    /**
     * 默认instance
     */
    public FaceCrop() {
        bdFaceInstance = new BDFaceInstance();
        bdFaceInstance.getDefautlInstance();
    }

    public void initFaceCrop(final Callback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long instanceIndex = bdFaceInstance.getIndex();
                if (instanceIndex == 0) {
                    callback.onResponse(-1, "抠图能力加载失败 instanceIndex=0");
                    return;
                }

                int status = nativeCropImageInit(instanceIndex);
                if (status == 0) {
                    callback.onResponse(status, "抠图能力加载成功");
                } else {
                    callback.onResponse(status, "抠图能力加载失败: " + status);
                    return;
                }
            }
        };
        FaceQueue.getInstance().execute(runnable);
    }

    public int uninitFaceCrop() {
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return -1;
        }

        return nativeUnInitCropImage(instanceIndex);
    }

    public BDFaceImageInstance cropFaceByBox(BDFaceImageInstance imageInstance,
                                             FaceInfo faceinfo,
                                             float enlargeRatio,
                                             AtomicInteger isOutofBoundary) {
        if (imageInstance == null || faceinfo == null || isOutofBoundary == null) {
            Log.v(TAG, "Parameter is null");
            return null;
        }
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return null;
        }

        int[] isOutofBoundary_temp = new int[1];
        BDFaceImageInstance img_crop = nativeCropFaceByBox(instanceIndex, imageInstance, faceinfo,
                enlargeRatio, isOutofBoundary_temp);

        isOutofBoundary.set(isOutofBoundary_temp[0]);
        return img_crop;
    }

    public BDFaceImageInstance cropFaceByLandmark(BDFaceImageInstance imageInstance,
                                                  float[] landmark,
                                                  float enlargeRatio,
                                                  boolean correction,
                                                  AtomicInteger isOutofBoundary) {
        if (imageInstance == null || isOutofBoundary == null || landmark.length < 0) {
            Log.v(TAG, "Parameter is null");
            return null;
        }
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return null;
        }

        int[] isOutofBoundary_temp = new int[1];
        BDFaceImageInstance img_crop = nativeCropFaceByLandmark(instanceIndex, imageInstance, landmark,
                enlargeRatio, correction, isOutofBoundary_temp);
        isOutofBoundary.set(isOutofBoundary_temp[0]);
        return img_crop;
    }

    private native BDFaceImageInstance nativeCropFaceByBox(long bdFaceInstanceIndex,
                                                           BDFaceImageInstance imageInstance,
                                                           FaceInfo faceinfo,
                                                           float enlargeRatio,
                                                           int[] isOutofBoundary);

    private native BDFaceImageInstance nativeCropFaceByLandmark(long bdFaceInstanceIndex,
                                                                BDFaceImageInstance imageInstance,
                                                                float[] landmark,
                                                                float enlargeRatio,
                                                                boolean correction,
                                                                int[] isOutofBoundary);

    private native int nativeCropImageInit(long bdFaceInstanceIndex);

    private native int nativeUnInitCropImage(long bdFaceInstanceIndex);

}
