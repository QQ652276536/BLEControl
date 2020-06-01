package com.zistone.blecontrol.baidutts;

import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.zistone.blecontrol.baidutts.util.IOfflineResourceConst;

import java.util.Map;

/**
 * 合成引擎的初始化参数
 */
public class InitConfig {
    private String appId;

    private String appKey;

    private String secretKey;

    //纯离线SDK才有的参数,离在线版本没有
    private String sn;

    //纯在线或者离在线融合
    private TtsMode ttsMode;

    //初始化的其它参数,用于setParam
    private Map<String, String> params;

    //合成引擎的回调
    private SpeechSynthesizerListener listener;

    /**
     * 离在线
     *
     * @param appId
     * @param appKey
     * @param secretKey
     * @param ttsMode
     * @param params
     * @param listener
     */
    public InitConfig(String appId, String appKey, String secretKey, TtsMode ttsMode, Map<String, String> params, SpeechSynthesizerListener listener) {
        this.appId = appId;
        this.appKey = appKey;
        this.secretKey = secretKey;
        this.ttsMode = ttsMode;
        this.params = params;
        this.listener = listener;
    }

    /**
     * 纯离线
     *
     * @param appId
     * @param appKey
     * @param secretKey
     * @param sn
     * @param ttsMode
     * @param params
     * @param listener
     */
    public InitConfig(String appId, String appKey, String secretKey, String sn, TtsMode ttsMode, Map<String, String> params, SpeechSynthesizerListener listener) {
        this(appId, appKey, secretKey, ttsMode, params, listener);
        this.sn = sn;
        if (sn != null) {
            params.put(IOfflineResourceConst.PARAM_SN_NAME, sn);
        }
    }

    public SpeechSynthesizerListener getListener() {
        return listener;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public TtsMode getTtsMode() {
        return ttsMode;
    }

    public String getSn() {
        return sn;
    }

}
