package com.baidu.idl.main.facesdk.test;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.idl.main.facesdk.utils.FileUitls;

public class PaddleLiteTest {

    static {
        try {
            System.loadLibrary("bdface_sdk");
            System.loadLibrary("bd_license");
            System.loadLibrary("aikl_calc_arm");
            System.loadLibrary("aikl_cluster_arm");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Context context;

    public PaddleLiteTest(Context context) {
        this.context = context;
    }


    public void create(String model, String param) {

        if (context == null) {
            return;
        }
        if (TextUtils.isEmpty(model) || TextUtils.isEmpty(param)) {
            return;
        }

        byte[] modelContent = FileUitls.getModelContent(context, model);
        byte[] paramContent = FileUitls.getModelContent(context, param);
        if (modelContent.length != 0 && paramContent.length != 0) {
            nativeCreate(modelContent, paramContent);
        }
    }

    public void run() {
        nativeRun();
    }

    private native int nativeCreate(byte[] modelContent, byte[] paramContent);

    private native int nativeRun();

}