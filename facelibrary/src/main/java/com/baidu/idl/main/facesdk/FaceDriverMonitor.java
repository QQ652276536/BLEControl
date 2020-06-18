package com.baidu.idl.main.facesdk;

import android.content.Context;

import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceDriverMonitorInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceInstance;
import com.baidu.idl.main.facesdk.utils.FileUitls;

public class FaceDriverMonitor {
    private BDFaceInstance bdFaceInstance;

    public FaceDriverMonitor(BDFaceInstance thisBdFaceInstance) {
        if (thisBdFaceInstance == null) {
            return;
        }
        bdFaceInstance = thisBdFaceInstance;
    }

    /**
     * 默认instance
     */
    public FaceDriverMonitor() {
        bdFaceInstance = new BDFaceInstance();
        bdFaceInstance.getDefautlInstance();
    }

    public void initDriverMonitor(final Context context, final String driverMonitorModel, final Callback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (context == null) {
                    callback.onResponse(1, "没有初始化上下文");
                    return;
                }
                long instanceIndex = bdFaceInstance.getIndex();
                if (instanceIndex == 0) {
                    callback.onResponse(-1, "驾驶行为监测能力加载失败 instanceIndex=0");
                    return;
                }

                int status = -1;
                byte[] modelContent = FileUitls.getModelContent(context, driverMonitorModel);
                if (modelContent.length != 0) {
                    status = nativeDriverMonitorInit(instanceIndex, modelContent);
                    if (status != 0) {
                        callback.onResponse(status, "驾驶行为监测模型加载失败");
                        return;
                    }
                }
                if (status == 0) {
                    callback.onResponse(0, "驾驶行为监测模型加载成功");
                } else {
                    callback.onResponse(1, "驾驶行为监测模型加载失败");
                }
            }
        };
        FaceQueue.getInstance().execute(runnable);
    }

    public int uninitDriverMonitor() {
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return -1;
        }

        return nativeUnInitDriverMonitor(instanceIndex);
    }

    public BDFaceDriverMonitorInfo driverMonitor(BDFaceImageInstance imageInstance,
                                                 FaceInfo faceinfo) {
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0 || imageInstance == null || faceinfo == null) {
            return null;
        }

        return nativeDriverMonitor(instanceIndex, imageInstance, faceinfo);
    }

    public native int nativeDriverMonitorInit(long bdFaceInstanceIndex, byte[] modelContent);

    public native int nativeUnInitDriverMonitor(long bdFaceInstanceIndex);

    public native BDFaceDriverMonitorInfo nativeDriverMonitor(long bdFaceInstanceIndex,
                                                              BDFaceImageInstance imageInstance,
                                                              FaceInfo faceinfo);
}
