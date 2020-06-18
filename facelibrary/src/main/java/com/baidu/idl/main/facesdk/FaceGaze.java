package com.baidu.idl.main.facesdk;

import android.content.Context;

import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceGazeInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceInstance;
import com.baidu.idl.main.facesdk.utils.FileUitls;

/**
 * 注意力功能
 */
public class FaceGaze {
    private static final String TAG = FaceGaze.class.getSimpleName();
    private BDFaceInstance bdFaceInstance;

    public FaceGaze(BDFaceInstance thisBdFaceInstance) {
        if (thisBdFaceInstance == null) {
            return;
        }
        bdFaceInstance = thisBdFaceInstance;
    }

    /**
     * 默认instance
     */
    public FaceGaze() {
        bdFaceInstance = new BDFaceInstance();
        bdFaceInstance.getDefautlInstance();
    }

    public void initModel(final Context context, final String gazeModel, final Callback callback) {
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
                byte[] modelContent = FileUitls.getModelContent(context, gazeModel);
                if (modelContent.length != 0) {
                    status = nativeGazeModelInit(instanceIndex, modelContent);
                    if (status != 0) {
                        callback.onResponse(status, "注意力检测模型加载失败");
                        return;
                    }
                }
                if (status == 0) {
                    callback.onResponse(0, "注意力检测模型加载成功");
                } else {
                    callback.onResponse(1, "注意力检测模型加载失败");
                }
            }
        };
        FaceQueue.getInstance().execute(runnable);
    }

    /**
     * 注意力检测函数
     *
     * @param imageInstance 检测图片索引
     * @param landmarks     检测关键点信息
     * @return
     */
    public BDFaceGazeInfo gaze(BDFaceImageInstance imageInstance, float[] landmarks) {
        if (imageInstance == null || landmarks == null || landmarks.length < 0) {
            return null;
        }
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return null;
        }
        return nativeGaze(instanceIndex, imageInstance, landmarks);

    }

    /**
     * 卸载注意力模型
     *
     * @return
     */
    public int uninitGazeModel() {
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return -1;
        }
        return nativeUninitGazeModel(instanceIndex);
    }

    private native int nativeGazeModelInit(long bdFaceInstanceIndex, byte[] modelContent);

    private native BDFaceGazeInfo nativeGaze(long bdFaceInstanceIndex, BDFaceImageInstance imageInstance,
                                             float[] landmarks);

    private native int nativeUninitGazeModel(long bdFaceInstanceIndex);
}
