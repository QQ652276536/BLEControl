package com.zistone.tts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.SynthesizerTool;
import com.baidu.tts.client.TtsMode;
import com.zistone.blecontrol.R;
import com.zistone.tts.control.InitConfig;
import com.zistone.tts.control.MySyntherizer;
import com.zistone.tts.control.NonBlockSyntherizer;
import com.zistone.tts.listener.UiMessageListener;
import com.zistone.tts.util.Auth;
import com.zistone.tts.util.AutoCheck;
import com.zistone.tts.util.FileUtil;
import com.zistone.tts.util.IOfflineResourceConst;
import com.zistone.tts.util.OfflineResource;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

/**
 * 合成demo。
 * 离在线合成SDK含在线和离线模型，纯离线合成SDK额外含有纯离线功能。
 * <p>
 * <p>
 * TtsMode.MIX 离在线合成根据网络状况优先走在线，在线时访问服务器失败后转为离线。
 * TtsMode.ONLINE 一直在线合成
 * TtsMode.OFFLINE 纯离线合成，一直离线。需要纯离线SDK
 */
public class SynthActivity extends BaseActivity implements View.OnClickListener {

    // ================== 完整版初始化参数设置开始 ==========================
    /**
     * 发布时请替换成自己申请的appId appKey 和 secretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
     * 本demo的包名是com.baidu.tts.sample，定义在build.gradle中。
     */
    protected String appId;

    protected String appKey;

    protected String secretKey;

    protected String sn; // 纯离线合成SDK授权码；离在线合成SDK免费，没有此参数

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； TtsMode.OFFLINE 纯离线合成，需要纯离线SDK
    protected TtsMode ttsMode = IOfflineResourceConst.DEFAULT_OFFLINE_TTS_MODE;

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_vXXXXXXX.dat为离线男声模型文件；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_vXXXXX.dat为离线女声模型文件;
    // assets目录下bd_etts_common_speech_yyjw_mand_eng_high_am-mix_vXXXXX.dat 为度逍遥模型文件;
    // assets目录下bd_etts_common_speech_as_mand_eng_high_am_vXXXX.dat 为度丫丫模型文件;
    protected String offlineVoice = OfflineResource.VOICE_MALE;

    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================

    // 主控制类，所有合成控制方法从这个类开始
    protected MySyntherizer synthesizer;

    protected int descTextId = R.raw.sync_activity_description;

    private static final String TAG = "SynthActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Auth.getInstance(this);
        } catch (Auth.AuthCheckException e) {
            mShowText.setText(e.getMessage());
            return;
        }
        mShowText.setText(FileUtil.getResourceText(this, descTextId));

        appId = Auth.getInstance(this).getAppId();
        appKey = Auth.getInstance(this).getAppKey();
        secretKey = Auth.getInstance(this).getSecretKey();
        sn = Auth.getInstance(this).getSn(); // 纯离线合成必须有此参数；离在线合成SDK免费，没有此参数
        initialButtons(); // 配置onclick
        initialTts(); // 初始化TTS引擎
        Log.i("SynthActivity", "so version:" + SynthesizerTool.getEngineInfo());
    }

    /**
     * 界面上的按钮对应方法
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.speak:
                speak(); // 合成并播放
                break;
            case R.id.synthesize:
                synthesize(); // 只合成不播放
                break;
            case R.id.batchSpeak:
                batchSpeak(); //  批量合成并播放
                break;
            case R.id.loadModel:// 切换离线资源
                AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Holo_Light_Dialog);
                builder.setTitle("引擎空闲时切换");
                final Map<String, String> map = new LinkedHashMap<>(4);
                map.put("离线女声", OfflineResource.VOICE_FEMALE);
                map.put("离线男声", OfflineResource.VOICE_MALE);
                map.put("离线度逍遥", OfflineResource.VOICE_DUXY);
                map.put("离线度丫丫", OfflineResource.VOICE_DUYY);
                final String[] keysTemp = new String[4];
                final String[] keys = map.keySet().toArray(keysTemp);
                builder.setItems(keys, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadModel(map.get(keys[which]));
                    }
                });
                builder.show();
                break;
            case R.id.pause:
                pause(); // 播放暂停
                break;
            case R.id.resume:
                resume(); // 播放恢复
                break;
            case R.id.stop:
                stop(); // 停止合成引擎
                break;
            case R.id.help:
                mShowText.setText(FileUtil.getResourceText(this, descTextId));
                break;
            default:
                break;
        }
    }

    /**
     * 初始化引擎，需要的参数均在InitConfig类里
     * <p>
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     */
    protected void initialTts() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new UiMessageListener(mainHandler);
        InitConfig config = getInitConfig(listener);
        synthesizer = new NonBlockSyntherizer(this, config, mainHandler); // 此处可以改为MySyntherizer 了解调用过程
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return 合成参数Map
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>, 其它发音人见文档
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "15");
        // 设置合成的语速，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // params.put(SpeechSynthesizer.PARAM_MIX_MODE_TIMEOUT, SpeechSynthesizer.PARAM_MIX_TIMEOUT_TWO_SECOND);
        // 离在线模式，强制在线优先。在线请求后超时2秒后，转为离线合成。

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.getModelFilename());
        return params;
    }

    protected InitConfig getInitConfig(SpeechSynthesizerListener listener) {
        Map<String, String> params = getParams();
        // 添加你自己的参数
        InitConfig initConfig;
        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        if (sn == null) {
            initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);
        } else {
            initConfig = new InitConfig(appId, appKey, secretKey, sn, ttsMode, params, listener);
        }
        // 如果您集成中出错，请将下面一段代码放在和demo中相同的位置，并复制InitConfig 和 AutoCheck到您的项目中
        // 上线时请删除AutoCheck的调用
        AutoCheck.getInstance(getApplicationContext()).check(initConfig, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainDebugMessage();
                        toPrint(message); // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
            }

        });
        return initConfig;
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            toPrint("【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }

    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    private void speak() {
        mShowText.setText("");
        String text = mInput.getText().toString();
        // 需要合成的文本text的长度不能超过1024个GBK字节。
        if (TextUtils.isEmpty(mInput.getText())) {
            text = "百度语音，面向广大开发者永久免费开放语音合成技术。";
            mInput.setText(text);
        }
        // 合成前可以修改参数：
        Map<String, String> params = getParams();
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "3"); // 设置为度逍遥
        synthesizer.setParams(params);
        int result = synthesizer.speak(text);
        checkResult(result, "speak");
    }

    /**
     * 合成但是不播放，
     * 音频流保存为文件的方法可以参见SaveFileActivity及FileSaveListener
     */
    private void synthesize() {
        mShowText.setText("");
        String text = this.mInput.getText().toString();
        if (TextUtils.isEmpty(mInput.getText())) {
            text = "欢迎使用百度语音合成SDK,百度语音为你提供支持。";
            mInput.setText(text);
        }
        int result = synthesizer.synthesize(text);
        checkResult(result, "synthesize");
    }

    /**
     * 批量播放
     */
    private void batchSpeak() {
        mShowText.setText("");
        List<Pair<String, String>> texts = new ArrayList<>();
        texts.add(new Pair<>("开始批量播放，", "a0"));
        texts.add(new Pair<>("123456，", "a1"));
        texts.add(new Pair<>("欢迎使用百度语音，，，", "a2"));
        texts.add(new Pair<>("重(chong2)量这个是多音字示例", "a3"));
        int result = synthesizer.batchSpeak(texts);
        checkResult(result, "batchSpeak");
    }

    /**
     * 切换离线发音。注意需要添加额外的判断：引擎在合成时该方法不能调用
     */
    private void loadModel(String mode) {
        offlineVoice = mode;
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        toPrint("切换离线语音：" + offlineResource.getModelFilename());
        int result = synthesizer.loadModel(offlineResource.getModelFilename(), offlineResource.getTextFilename());
        checkResult(result, "loadModel");
    }

    private void checkResult(int result, String method) {
        if (result != 0) {
            toPrint("error code :" + result + " method:" + method);
        }
    }

    /**
     * 暂停播放。仅调用speak后生效
     */
    private void pause() {
        int result = synthesizer.pause();
        checkResult(result, "pause");
    }

    /**
     * 继续播放。仅调用speak后生效，调用pause生效
     */
    private void resume() {
        int result = synthesizer.resume();
        checkResult(result, "resume");
    }

    /*
     * 停止合成引擎。即停止播放，合成，清空内部合成队列。
     */
    private void stop() {
        int result = synthesizer.stop();
        checkResult(result, "stop");
    }

    @Override
    protected void onDestroy() {
        synthesizer.release();
        Log.i(TAG, "释放资源成功");
        super.onDestroy();
    }

    protected void handle(Message msg) {
        if (msg.what == INIT_SUCCESS) {
            for (Button b : buttons) {
                b.setEnabled(true);
            }
            msg.what = PRINT;
        }
        super.handle(msg);
    }

    private void initialButtons() {
        for (Button b : buttons) {
            b.setOnClickListener(this);
            b.setEnabled(false); // 先禁用按钮，等待引擎初始化后打开。
        }
        mHelp.setOnClickListener(this);
    }

}
