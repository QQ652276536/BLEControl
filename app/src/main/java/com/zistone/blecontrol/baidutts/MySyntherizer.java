package com.zistone.blecontrol.baidutts;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.baidu.tts.client.SpeechSynthesizeBag;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.zistone.blecontrol.baidutts.util.MainHandlerConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 对SpeechSynthesizer的封装
 */
public class MySyntherizer implements MainHandlerConstant {

    private static final String TAG = "MySyntherizer";
    private static volatile boolean _isInitied = false;
    private SpeechSynthesizer _speechSynthesizer;
    private Context _context;

    public MySyntherizer(Context context) {
        //不要连续调用SpeechSynthesizer.getInstance()
        if (_isInitied) {
            throw new RuntimeException("MySynthesizer对象里面的SpeechSynthesizer还未释放,请勿新建一个新对象.如果需要新建,请先调用之前MySynthesizer对象的release()方法.");
        }
        Log.i(TAG, "MySyntherizer已实例化");
        _context = context;
        _isInitied = true;
    }

    public MySyntherizer(Context context, InitConfig initConfig) {
        this(context);
        Init(initConfig);
    }

    /**
     * 注意该方法需要在新线程中调用.且该线程不能结束.详细请参见NonBlockSyntherizer的实现
     *
     * @param config 配置
     * @return 是否初始化成功
     */
    protected boolean Init(InitConfig config) {
        Log.i(TAG, ">>>初始化开始");
        _speechSynthesizer = SpeechSynthesizer.getInstance();
        _speechSynthesizer.setContext(_context);
        Log.i(TAG, ">>>包名:" + _context.getPackageName());
        SpeechSynthesizerListener listener = config.getListener();
        _speechSynthesizer.setSpeechSynthesizerListener(listener);
        //请替换为语音开发者平台上注册应用得到的App ID ,AppKey ,Secret Key ,填写在SynthActivity的开始位置
        _speechSynthesizer.setAppId(config.getAppId());
        _speechSynthesizer.setApiKey(config.getAppKey(), config.getSecretKey());
        //设置播放参数
        SetParams(config.getParams());
        //初始化tts
        int result = _speechSynthesizer.initTts(config.getTtsMode());
        if (result != 0) {
            Log.e(TAG, ">>>初始化失败,错误代码:" + result);
            return false;
        }
        Log.i(TAG, ">>>合成引擎初始化成功");
        return true;
    }

    /**
     * 合成并播放
     *
     * @param text 小于1024 GBK字节,即512个汉字或者字母数字
     * @return 0表示成功
     */
    public int Speak(String text) {
        if (!_isInitied) {
            throw new RuntimeException(">>>TTS还未初始化");
        }
        return _speechSynthesizer.speak(text);
    }

    /**
     * 合成并播放
     *
     * @param text        小于1024 GBK字节,即512个汉字或者字母数字
     * @param utteranceId 用于listener的回调,默认"0"
     * @return 0表示成功
     */
    public int Speak(String text, String utteranceId) {
        if (!_isInitied) {
            throw new RuntimeException(">>>TTS还未初始化");
        }
        return _speechSynthesizer.speak(text, utteranceId);
    }

    /**
     * 只合成不播放
     *
     * @param text 合成的文本
     * @return 0表示成功
     */
    public int Synthesize(String text) {
        if (!_isInitied) {
            //SpeechSynthesizer.getInstance()不要连续调用
            throw new RuntimeException(">>>TTS还未初始化");
        }
        return _speechSynthesizer.synthesize(text);
    }

    public int Synthesize(String text, String utteranceId) {
        if (!_isInitied) {
            //SpeechSynthesizer.getInstance()不要连续调用
            throw new RuntimeException(">>>TTS还未初始化");
        }
        return _speechSynthesizer.synthesize(text, utteranceId);
    }

    public int BatchSpeak(List<Pair<String, String>> texts) {
        if (!_isInitied) {
            throw new RuntimeException(">>>TTS还未初始化");
        }
        List<SpeechSynthesizeBag> bags = new ArrayList<SpeechSynthesizeBag>();
        for (Pair<String, String> pair : texts) {
            SpeechSynthesizeBag speechSynthesizeBag = new SpeechSynthesizeBag();
            speechSynthesizeBag.setText(pair.first);
            if (pair.second != null) {
                speechSynthesizeBag.setUtteranceId(pair.second);
            }
            bags.add(speechSynthesizeBag);
        }
        return _speechSynthesizer.batchSpeak(bags);
    }

    public void SetParams(Map<String, String> params) {
        if (params != null) {
            for (Map.Entry<String, String> e : params.entrySet()) {
                _speechSynthesizer.setParam(e.getKey(), e.getValue());
            }
        }
    }

    public int Pause() {
        return _speechSynthesizer.pause();
    }

    public int Resume() {
        return _speechSynthesizer.resume();
    }

    public int Stop() {
        return _speechSynthesizer.stop();
    }

    /**
     * 引擎在合成时该方法不能调用！！！
     * 注意:只有TtsMode.MIX才可以切换离线发音
     *
     * @return
     */
    public int LoadModel(String modelFilename, String textFilename) {
        int result = _speechSynthesizer.loadModel(modelFilename, textFilename);
        Log.i(TAG, ">>>切换离线发音人成功");
        return result;
    }

    /**
     * 释放资源
     */
    public void Release() {
        Log.i(TAG, ">>>执行释放资源函数Release()");
        if (!_isInitied) {
            //这里报错是因为连续两次new MySyntherizer,必须第一次new之后,调用Release()
            throw new RuntimeException(">>>TTS还未初始化");
        }
        _speechSynthesizer.stop();
        _speechSynthesizer.release();
        _speechSynthesizer = null;
        _isInitied = false;
    }

}
