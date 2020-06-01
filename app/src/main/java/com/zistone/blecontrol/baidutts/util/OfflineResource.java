package com.zistone.blecontrol.baidutts.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;

public class OfflineResource implements IOfflineResourceConst {
    private static final String TAG = "OfflineResource";
    private static HashMap<String, Boolean> mapInitied = new HashMap<String, Boolean>();

    private AssetManager assets;
    private String destPath;
    private String textFilename;
    private String modelFilename;

    public OfflineResource(Context context, String voiceType) throws IOException {
        context = context.getApplicationContext();
        this.assets = context.getApplicationContext().getAssets();
        this.destPath = FileUtil.createTmpDir(context);
        setOfflineVoiceType(voiceType);
    }

    public String getModelFilename() {
        return modelFilename;
    }

    public String getTextFilename() {
        return textFilename;
    }

    public void setOfflineVoiceType(String voiceType) throws IOException {
        String text = TEXT_MODEL;
        String model;
        if (VOICE_MALE.equals(voiceType)) {
            model = VOICE_MALE_MODEL;
        } else if (VOICE_FEMALE.equals(voiceType)) {
            model = VOICE_FEMALE_MODEL;
        } else if (VOICE_DUXY.equals(voiceType)) {
            model = VOICE_DUXY_MODEL;
        } else if (VOICE_DUYY.equals(voiceType)) {
            model = VOICE_DUYY_MODEL;
        } else {
            throw new RuntimeException("没有该声音类型");
        }
        textFilename = copyAssetsFile(text);
        modelFilename = copyAssetsFile(model);
    }


    private String copyAssetsFile(String sourceFilename) throws IOException {
        String destFilename = destPath + "/" + sourceFilename;
        boolean recover = false;
        Boolean existed = mapInitied.get(sourceFilename); // 启动时完全覆盖一次
        if (existed == null || !existed) {
            recover = true;
        }
        FileUtil.copyFromAssets(assets, sourceFilename, destFilename, recover);
        Log.i(TAG, "文件复制成功：" + destFilename);
        return destFilename;
    }


}
