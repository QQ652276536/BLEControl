/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.face.main.api;

import android.graphics.Bitmap;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.face.main.manager.FaceSDKManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FaceApi {
    private static FaceApi instance;
    private ExecutorService es = Executors.newSingleThreadExecutor();
    private Future future;

    private int mUserNum;
    private boolean isinitSuccess = false;


    private FaceApi() {

    }

    public static synchronized FaceApi getInstance() {
        if (instance == null) {
            instance = new FaceApi();
        }
        return instance;
    }

    /**
     * 是否是有效姓名
     *
     * @param username 用户名
     * @return 有效或无效信息
     */
    public String isValidName(String username) {
        if (username == null || "".equals(username.trim())) {
            return "姓名为空";
        }

        // 姓名过长
        if (username.length() > 10) {
            return "姓名过长";
        }

        // 含有特殊符号
        String regex = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）—" + "—+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(username);
        if (m.find()) {
            return "姓名中含有特殊符号";
        }
        return "0";
    }

    /**
     * 提取特征值
     */
    public float getFeature(Bitmap bitmap, byte[] feature, BDFaceSDKCommon.FeatureType featureType) {
        if (bitmap == null) {
            return -1;
        }
        BDFaceImageInstance imageInstance = new BDFaceImageInstance(bitmap);
        // 最大检测人脸，获取人脸信息
        FaceInfo[] faceInfos = FaceSDKManager.getInstance().getFaceDetect().detect(BDFaceSDKCommon.DetectType.DETECT_VIS, imageInstance);
        float ret = -1;
        if (faceInfos != null && faceInfos.length > 0) {
            FaceInfo faceInfo = faceInfos[0];
            // 人脸识别，提取人脸特征值
            ret = FaceSDKManager.getInstance().getFaceFeature().feature(featureType, imageInstance, faceInfo.landmarks, feature);
        }
        imageInstance.destory();
        return ret;
    }

}
