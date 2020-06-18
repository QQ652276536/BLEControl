package com.baidu.idl.main.facesdk;

import android.content.Context;
import android.util.Log;

import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.utils.FileUitls;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by v_shishuaifeng on 2019/11/13.
 */

public class FaceActionLive {
    private static final String TAG = FaceActionLive.class.getSimpleName();
    private BDFaceInstance bdFaceInstance;
    private int[] isExist = new int[1];

    public FaceActionLive(BDFaceInstance thisBdFaceInstance) {
        if (thisBdFaceInstance == null) {
            return;
        }
        bdFaceInstance = thisBdFaceInstance;
    }

    /**
     * 默认 instance
     */
    public FaceActionLive() {
        bdFaceInstance = new BDFaceInstance();
        bdFaceInstance.getDefautlInstance();
    }

    public void initActionLiveModel(final Context context, final String eyecloseModel,
                                    final String mouthcloseModel, final Callback callback) {
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
                int statusClose = -1;
                byte[] eyecloseModelContent = FileUitls.getModelContent(context, eyecloseModel);
                byte[] mouthcloseModelContent = FileUitls.getModelContent(context, mouthcloseModel);
                if (eyecloseModelContent.length != 0 && mouthcloseModelContent.length != 0) {
                    statusClose = nativeActionLiveModelInit(instanceIndex,
                            eyecloseModelContent, mouthcloseModelContent);
                    if (statusClose == 0) {
                        callback.onResponse(statusClose, "动作活体模型加载成功");
                    } else {
                        callback.onResponse(statusClose, "动作活体模型加载失败");
                        return;
                    }
                }
            }
        };
        FaceQueue.getInstance().execute(runnable);
    }

    public int actionLive(BDFaceSDKCommon.BDFaceActionLiveType type,
                          BDFaceImageInstance imageInstance, float[] landmarks, AtomicInteger exist) {
        if (imageInstance == null || landmarks == null || type == null || exist == null) {
            Log.v(TAG, "Parameter is null");
            return -1;
        }
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return -1;
        }
        int status = nativeActionLive(instanceIndex, type.ordinal(), imageInstance, landmarks, isExist);
        exist.set(isExist[0]);
        return status;
    }

    public int clearHistory() {
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return -1;
        }
        return nativeClearHistory(instanceIndex);
    }

    public int uninitActionLiveModel() {
        long instanceIndex = bdFaceInstance.getIndex();
        if (instanceIndex == 0) {
            return -1;
        }
        return nativeUninitActionLiveModel(instanceIndex);
    }


    private native int nativeActionLiveModelInit(long bdFaceInstanceIndex,
                                                 byte[] eyecloseModelContent,
                                                 byte[] mouthcloseModelContent);

    private native int nativeActionLive(long bdFaceInstanceIndex, int type,
                                        BDFaceImageInstance imageInstance, float[] landmarks, int[] isexist);

    private native int nativeClearHistory(long bdFaceInstanceIndex);

    private native int nativeUninitActionLiveModel(long bdFaceInstanceIndex);
}
