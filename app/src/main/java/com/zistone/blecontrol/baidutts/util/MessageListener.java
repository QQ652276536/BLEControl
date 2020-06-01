package com.zistone.blecontrol.baidutts.util;

import android.os.Handler;
import android.util.Log;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizerListener;

/**
 * SpeechSynthesizerListener简单地实现,用于记录日志和播放状态的监听回调
 */
public class MessageListener implements SpeechSynthesizerListener {
    private static final String TAG = "MessageListener";

    private Handler _handler;

    public MessageListener(Handler _handler) {
        this._handler = _handler;
    }

    /**
     * 播放开始,每句播放开始都会回调
     *
     * @param utteranceId
     */
    @Override
    public void onSynthesizeStart(String utteranceId) {
        SendMessage("准备开始合成,序列号:" + utteranceId, true);
    }

    /**
     * 语音流
     * 16K采样率
     * 16bits编码
     * 单声道
     *
     * @param utteranceId
     * @param bytes       二进制语音,注意可能有空data的情况,可以忽略
     * @param progress    合成进度,与播放到哪个字无关,如合成“百度语音问题”这6个字,progress肯定是从0开始,到6结束
     */
    @Override
    public void onSynthesizeDataArrived(String utteranceId, byte[] bytes, int progress, int engineType) {
        SendMessage("合成进度回调, progress:" + progress + ";序列号:" + utteranceId, true);
    }

    /**
     * 合成正常结束,每句合成正常结束都会回调,如果过程中出错,则回调onError,不再回调此接口
     *
     * @param utteranceId
     */
    @Override
    public void onSynthesizeFinish(String utteranceId) {
        SendMessage("合成结束回调, 序列号:" + utteranceId, true);
        _handler.sendMessage(_handler.obtainMessage(1));
    }

    /**
     * 播放开始
     *
     * @param utteranceId
     */
    @Override
    public void onSpeechStart(String utteranceId) {
        SendMessage("播放开始回调, 序列号:" + utteranceId, true);
        _handler.sendMessage(_handler.obtainMessage(2));
    }

    /**
     * 播放进度回调接口,分多次回调
     *
     * @param utteranceId
     * @param progress    合成进度,与播放到哪个字无关,如合成“百度语音问题”这6个字, progress肯定是从0开始,到6结束
     */
    @Override
    public void onSpeechProgressChanged(String utteranceId, int progress) {
        //SendMessage("播放进度回调, progress:" + progress + ";序列号:" + utteranceId,true);
    }

    /**
     * 播放正常结束,每句播放正常结束都会回调,如果过程中出错,则回调onError,不再回调此接口
     *
     * @param utteranceId
     */
    @Override
    public void onSpeechFinish(String utteranceId) {
        SendMessage("播放结束回调, 序列号:" + utteranceId, true);
        _handler.sendMessage(_handler.obtainMessage(3));
    }

    /**
     * 当合成或者播放过程中出错时回调此接口
     *
     * @param utteranceId
     * @param speechError 包含错误码和错误信息
     */
    @Override
    public void onError(String utteranceId, SpeechError speechError) {
        SendMessage("错误发生:" + speechError.description + ",错误编码:" + speechError.code + ",序列号:" + utteranceId, false);
        _handler.sendMessage(_handler.obtainMessage(-1));
    }

    private void SendMessage(String message, boolean noError) {
        if (noError) {
            Log.i(TAG, message);
        } else {
            Log.e(TAG, message);
        }
    }

}
