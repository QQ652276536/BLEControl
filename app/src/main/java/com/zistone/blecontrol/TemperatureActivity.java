package com.zistone.blecontrol;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.zistone.blecontrol.baidutts.InitConfig;
import com.zistone.blecontrol.baidutts.MySyntherizer;
import com.zistone.blecontrol.baidutts.NonBlockSyntherizer;
import com.zistone.blecontrol.baidutts.util.Auth;
import com.zistone.blecontrol.baidutts.util.IOfflineResourceConst;
import com.zistone.blecontrol.baidutts.util.MessageListener;
import com.zistone.blecontrol.baidutts.util.OfflineResource;
import com.zistone.blecontrol.controls.AmountView;
import com.zistone.blecontrol.util.BleListener;
import com.zistone.blecontrol.util.MyBleUtil;
import com.zistone.blecontrol.util.MyConvertUtil;
import com.zistone.blecontrol.util.MyDeviceFilterShared;
import com.zistone.blecontrol.util.MyProgressDialogUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class TemperatureActivity extends AppCompatActivity implements View.OnClickListener, BleListener {
    private static final String TAG = "TemperatureActivity";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String SEARCH_TEMPERATURE_COMM1 = "680000000000006810000180E616";
    private static final String SEARCH_TEMPERATURE_COMM2 = "680000000000006810000181E116";
    private static final int MESSAGE_ERROR_1 = -1;
    private static final int MESSAGE_ERROR_2 = -2;
    private static final int MESSAGE_ERROR_3 = -3;
    private static final int MESSAGE_1 = 100;
    private static final int RECEIVE = 8002;

    private BluetoothDevice _bluetoothDevice;
    private Toolbar _toolbar;
    private ImageButton _btnReturn;
    private TextView _txt1, _txt2, _txt3, _txt4, _txt5;
    private StringBuffer _stringBuffer = new StringBuffer();
    private Map<String, UUID> _uuidMap;
    //是否连接成功,是否检测到人脸
    private boolean _connectedSuccess = false, _isCheckedFace = false;
    private Timer _refreshTimer;
    private TimerTask _refreshTask;
    private AmountView _amountView;
    private AmountView.OnAmountChangeListener _onAmountChangeListener;
    //统计的所有温度,测量温度
    private double _measuringValue = 0.0;
    //TTS语音部分
    //发布时请替换成自己申请的_appId、_appKey和_secretKey
    //注意如果需要离线合成功能,请在您申请的应用中填写包名
    //发布时请替换成自己申请的appId、appKey和secretKey,注意如果需要离线合成功能,请在您申请的应用中填写包名
    private String _appId = "18730922";
    private String _appKey = "Dqm6IyZ47QXlX0WvHnrZKmsF";
    private String _secretKey = "UXM4raYA21UA7m48b49lGdEGLZOpIK3w";
    //纯离线合成SDK授权码;离在线合成SDK免费,没有此参数
    private String _sn;
    private TtsMode _ttsMode = IOfflineResourceConst.DEFAULT_OFFLINE_TTS_MODE;
    private String _offlineVoice = OfflineResource.VOICE_MALE;
    //主控制类,所有合成控制方法从这个类开始
    private MySyntherizer _mySyntherizer;
    //语音合成状态:-1表示合成或播放过程中出现错误,1表示合成结束,2表示开始播放,3表示播放结束
    private int _speechState = 3;
    private MediaPlayer _mediaPlayer1, _mediaPlayer2;
    private MyHandler _myHandler;

    static class MyHandler extends Handler {
        private WeakReference<TemperatureActivity> _weakReference;
        private TemperatureActivity _temperatureActivity;

        public MyHandler(TemperatureActivity activity) {
            _weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            if (_weakReference.get() == null)
                return;
            _temperatureActivity = _weakReference.get();
            String result = (String) message.obj;
            switch (message.what) {
                case MESSAGE_1:
                    _temperatureActivity.Speak("设备已连接");
                    //连接成功后再显示人脸检测
                    //                    _cameraView.setVisibility(View.VISIBLE);
                    _temperatureActivity._refreshTimer = new Timer();
                    _temperatureActivity._refreshTask = new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                MyBleUtil.SendComm(SEARCH_TEMPERATURE_COMM1);
                                Log.i(TAG, "发送查询温度的指令...");
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    //任务、延迟执行时间、重复调用间隔,Timer和TimerTask在调用cancel()取消后不能再执行schedule语句
                    _temperatureActivity._refreshTimer.schedule(_temperatureActivity._refreshTask, 0, 1 * 1000);
                    break;
                case RECEIVE:
                    String strs[] = result.split(",");
                    //最高温度
                    double value1 = Double.valueOf(strs[0]) / 100;
                    //最低温度
                    double value2 = Double.valueOf(strs[1]) / 100;
                    //环境温度
                    double value3 = Double.valueOf(strs[2]) / 100;
                    //平均温度
                    double value4 = Double.valueOf(strs[3]) / 100;
                    _temperatureActivity._txt1.setText(value1 + "℃");
                    _temperatureActivity._txt1.setTextColor(Color.RED);
                    _temperatureActivity._txt2.setText(value2 + "℃");
                    _temperatureActivity._txt2.setTextColor(Color.YELLOW);
                    _temperatureActivity._txt3.setText(value3 + "℃");
                    _temperatureActivity._txt3.setTextColor(Color.BLUE);
                    _temperatureActivity._txt4.setText(value4 + "℃");
                    _temperatureActivity._txt4.setTextColor(Color.CYAN);
                    _temperatureActivity.ReadySpeak(value1, value2, value3, value4);
                    break;
            }
        }
    }

    private Handler _speechStateHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            _speechState = message.what;
        }
    };

    /**
     * 准备语音合成的内容
     *
     * @param value1 最高温度
     * @param value2 最低温度
     * @param value3 环境温度
     * @param value4 平均温度
     */
    private void ReadySpeak(double value1, double value2, double value3, double value4) {
        _measuringValue = value1 + _amountView.getCurrent();
        //只保留两位小数
        String strAvValue = new DecimalFormat("0.0").format(_measuringValue);
        Log.i(TAG, String.format("最高温度:%s℃ 最低温度:%s℃ 环境温度:%s℃ 平均温度:%s℃ 测量温度:%s℃", value1, value2, value3, value4, strAvValue));
        //检测到人脸才统计温度
        if (_isCheckedFace) {
            if (_measuringValue >= 34 && _measuringValue <= 37.5) {
                //                _mediaPlayer1.start();
            } else if (_measuringValue > 37.5) {
                //                _mediaPlayer2.start();
            }
            //上一条语音播放完毕才播放下一条
            if (_speechState == 3)
                Speak(strAvValue + "度");
            _isCheckedFace = false;
        }
        _txt5.setText(strAvValue + "℃");
        _txt5.setTextColor(Color.GREEN);
    }

    /**
     * 初始化引擎,需要的参数均在InitConfig类里
     */
    private void InitTTS() {
        //日志打印在logcat中
        LoggerProxy.printable(true);
        //语音合成时的日志
        MessageListener messageListener = new MessageListener(_speechStateHandler);
        SpeechSynthesizerListener listener = messageListener;
        //设置初始化参数
        InitConfig config = GetInitConfig(listener);
        _mySyntherizer = new NonBlockSyntherizer(TemperatureActivity.this, config);
    }

    /**
     * 合成的参数,可以初始化时填写,也可以在合成前设置.
     *
     * @return 合成参数
     */
    private Map<String, String> GetParams() {
        //以下参数均为选填
        Map<String, String> params = new HashMap<>();
        //设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>, 其它发音人见文档
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        //设置合成的音量,0-15 ,默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "15");
        //设置合成的语速,0-15 ,默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        //设置合成的语调,0-15 ,默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");
        //MIX_MODE_DEFAULT                          默认 ,wifi状态下使用在线,非wifi离线.在线状态下,请求超时6s自动转离线
        //MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI       wifi状态下使用在线,非wifi离线.在线状态下, 请求超时1.2s自动转离线
        //MIX_MODE_HIGH_SPEED_NETWORK               3G 4G wifi状态下使用在线,其它状态离线.在线状态下,请求超时1.2s自动转离线
        //MIX_MODE_HIGH_SPEED_SYNTHESIZE            2G 3G 4G wifi状态下使用在线,其它状态离线.在线状态下,请求超时1.2s自动转离线
        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        //离在线模式,强制在线优先.在线请求后超时2秒后,转为离线合成.
        //params.put(SpeechSynthesizer.PARAM_MIX_MODE_TIMEOUT, SpeechSynthesizer.PARAM_MIX_TIMEOUT_TWO_SECOND);
        //离线资源文件, 从assets目录中复制到临时目录,需要在initTTs方法前完成
        OfflineResource offlineResource = CreateOfflineResource(_offlineVoice);
        //声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, offlineResource.getModelFilename());
        return params;
    }

    private InitConfig GetInitConfig(SpeechSynthesizerListener listener) {
        Map<String, String> params = GetParams();
        //添加你自己的参数
        InitConfig initConfig;
        if (_sn == null) {
            initConfig = new InitConfig(_appId, _appKey, _secretKey, _ttsMode, params, listener);
        } else {
            initConfig = new InitConfig(_appId, _appKey, _secretKey, _sn, _ttsMode, params, listener);
        }
        //上线时请删除AutoCheck的调用
        //       AutoCheck.getInstance(getApplicationContext()).check(initConfig, new Handler() {
        //           @Override
        //           public void handleMessage(Message msg) {
        //               if (msg.what == 100) {
        //                   AutoCheck autoCheck = (AutoCheck) msg.obj;
        //                   synchronized (autoCheck) {
        //                       String message = autoCheck.obtainDebugMessage();
        //                       Log.i(TAG, message);
        //                   }
        //               }
        //           }
        //
        //       });
        return initConfig;
    }

    /**
     * 复制assets里的离线资源文件到设备的/sdcard/baituTTS/
     *
     * @param voiceType
     * @return
     */
    private OfflineResource CreateOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this, voiceType);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "复制assets里的离线资源文件到设备路径的/sdcard/baituTTS/失败!!!\n" + e.getMessage());
        }
        return offlineResource;
    }

    /**
     * Speak实际上是调用Synthesize后获取音频流,然后播放.
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     *
     * @param text 需要合成的文本,长度不能超过1024个GBK字节.
     */
    private void Speak(String text) {
        int result = _mySyntherizer.Speak(text);
        CheckResult(result, "Speak()");
    }


    /**
     * 合成但是不播放
     * 音频流保存为文件的方法可以参见SaveFileActivity及FileSaveListener
     *
     * @param text 需要合成的文本,长度不能超过1024个GBK字节.
     */
    private void Synthesize(String text) {
        int result = _mySyntherizer.Synthesize(text);
        CheckResult(result, "Synthesize()");
    }

    /**
     * 批量播放
     */
    private void BatchSpeak() {
        List<Pair<String, String>> texts = new ArrayList<>();
        texts.add(new Pair<>("已成功连接设(she4)备(bei4)", "a0"));
        texts.add(new Pair<>("重(zhong4)量这个是多音字示例", "a1"));
        int result = _mySyntherizer.BatchSpeak(texts);
        CheckResult(result, "BatchSpeak()");
    }

    /**
     * 切换离线发音,引擎在合成时该方法不能调用
     *
     * @param mode
     */
    private void LoadModel(String mode) {
        _offlineVoice = mode;
        OfflineResource offlineResource = CreateOfflineResource(_offlineVoice);
        Log.i(TAG, "切换离线语音:" + offlineResource.getModelFilename());
        int result = _mySyntherizer.LoadModel(offlineResource.getModelFilename(), offlineResource.getTextFilename());
        CheckResult(result, "LoadModel()");
    }

    private void CheckResult(int result, String method) {
        if (result != 0)
            Log.e(TAG, String.format("方法%s执行失败,错误代码:%s", method, result));
    }

    /**
     * 暂停播放,仅调用Speak()后生效
     */
    private void Pause() {
        int result = _mySyntherizer.Pause();
        CheckResult(result, "Pause()");
    }

    /**
     * 继续播放,仅调用Speak()后再调用Pause()生效
     */
    private void Resume() {
        int result = _mySyntherizer.Resume();
        CheckResult(result, "Resume()");
    }

    /**
     * 停止合成引擎,即停止播放、合成、清空内部合成队列
     */
    private void Stop() {
        int result = _mySyntherizer.Stop();
        CheckResult(result, "Stop()");
    }

    /**
     * 断开与BLE设备的连接
     */
    private void DisConnect() {
        MyBleUtil.DisConnGatt();
        _txt1.setText("Null");
        _txt1.setTextColor(Color.GRAY);
        _txt2.setText("Null");
        _txt2.setTextColor(Color.GRAY);
        _txt3.setText("Null");
        _txt3.setTextColor(Color.GRAY);
        _txt4.setText("Null");
        _txt4.setTextColor(Color.GRAY);
        _txt5.setText("Null");
        _txt5.setTextColor(Color.GRAY);
        Speak("设备已断开");
        if (_refreshTask != null)
            _refreshTask.cancel();
        if (_refreshTimer != null)
            _refreshTimer.cancel();
    }

    /**
     * 解析硬件返回的数据
     *
     * @param data
     */
    private void Resolve(String data) {
        Log.i(TAG, "共接收:" + data);
        String[] strArray = data.split(" ");
        String indexStr = strArray[12];
        int what = 0;
        String obj = "";
        switch (indexStr) {
            case "80":
                byte[] bytes1 = MyConvertUtil.HexStrToByteArray(strArray[13]);
                String bitStr = MyConvertUtil.ByteToBit(bytes1[0]);
                String doorState1 = String.valueOf(bitStr.charAt(7));
                String lockState1 = String.valueOf(bitStr.charAt(6));
                String doorState2 = String.valueOf(bitStr.charAt(5));
                String lockState2 = String.valueOf(bitStr.charAt(4));
                //强磁开关状态
                String magneticState = String.valueOf(bitStr.charAt(3));
                //外接电源状态
                String outsideState = String.valueOf(bitStr.charAt(2));
                //内部电池充电状态
                String insideState = String.valueOf(bitStr.charAt(1));
                //电池电量(平均温度)
                int battery = Integer.parseInt(strArray[14] + strArray[15], 16);
                //下端磁强(最低温度)
                int magneticDown = Integer.parseInt(strArray[16] + strArray[17], 16);
                //上端磁强(最高温度)
                int magneticUp = Integer.parseInt(strArray[2] + strArray[3], 16);
                //前端磁强(环境温度)
                int magneticBefore = Integer.parseInt(strArray[4] + strArray[5], 16);
                what = RECEIVE;
                //注意几个温度的顺序,最高->最低->环境->平均,后面解析也是这个顺序来的
                obj = magneticUp + "," + magneticDown + "," + magneticBefore + "," + battery;
                break;
        }
        _myHandler.obtainMessage(what, obj).sendToTarget();
    }

    @Override
    public void OnScanLeResult(ScanResult result) {
    }

    @Override
    public void OnConnected() {
    }

    @Override
    public void OnConnecting() {
    }

    @Override
    public void OnDisConnected() {
    }

    @Override
    public void OnWriteSuccess(byte[] byteArray) {
        String result = MyConvertUtil.ByteArrayToHexStr(byteArray);
        result = MyConvertUtil.StrAddCharacter(result, 2, " ");
        String[] strArray = result.split(" ");
        String indexStr = strArray[11];
        switch (indexStr) {
        }
    }

    @Override
    public void OnReadSuccess(byte[] byteArray) {
        String result = MyConvertUtil.ByteArrayToHexStr(byteArray);
        result = MyConvertUtil.StrAddCharacter(result, 2, " ");
        //Log.i(TAG, "接收:" + result);
        String[] strArray = result.split(" ");
        //一个包(20个字节)
        if (strArray[0].equals("68") && strArray[strArray.length - 1].equals("16")) {
            Resolve(result);
            //清空缓存
            _stringBuffer = new StringBuffer();
        }
        //分包
        else {
            if (!strArray[strArray.length - 1].equals("16")) {
                _stringBuffer.append(result + " ");
            }
            //最后一个包
            else {
                _stringBuffer.append(result);
                result = _stringBuffer.toString();
                Resolve(result);
                //清空缓存
                _stringBuffer = new StringBuffer();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
            this.finish();
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_return_temperature: {
                finish();
            }
            break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _myHandler = new MyHandler(this);
        setContentView(R.layout.activity_temperature);
        Intent intent = getIntent();
        _bluetoothDevice = intent.getParcelableExtra(ARG_PARAM1);
        _uuidMap = (Map<String, UUID>) intent.getSerializableExtra(ARG_PARAM2);
        _mediaPlayer1 = MediaPlayer.create(this, R.raw.dingdong);
        _mediaPlayer2 = MediaPlayer.create(this, R.raw.didi);
        //初始化百度语音TTS引擎
        InitTTS();
        try {
            Auth.getInstance(this);
        } catch (Auth.AuthCheckException e) {
            Log.e(TAG, e.getMessage());
            return;
        }
        //Toolbar
        _toolbar = findViewById(R.id.toolbar_temperature);
        _toolbar.setTitle("");
        setSupportActionBar(_toolbar);
        _btnReturn = findViewById(R.id.btn_return_temperature);
        _txt1 = findViewById(R.id.txt1_temperature);
        _txt2 = findViewById(R.id.txt2_temperature);
        _txt3 = findViewById(R.id.txt3_temperature);
        _txt4 = findViewById(R.id.txt4_temperature);
        _txt5 = findViewById(R.id.txt5_temperature);
        _btnReturn.setOnClickListener(this::onClick);
        _amountView = findViewById(R.id.amountView_temperature);
        _amountView.setMaxMinStep(10, -10, 0.1);
        _amountView.setCurrent(Double.valueOf(MyDeviceFilterShared.GetTemperatureParam(getApplicationContext())));
        //初始化完再设置监听
        _amountView.setLister(_onAmountChangeListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (_mySyntherizer != null) {
            _mySyntherizer.Stop();
            _mySyntherizer.Release();
        }
        if (_refreshTask != null)
            _refreshTask.cancel();
        if (_refreshTimer != null)
            _refreshTimer.cancel();
        if (_mediaPlayer1 != null)
            _mediaPlayer1.release();
        if (_mediaPlayer2 != null)
            _mediaPlayer2.release();
        MyBleUtil.DisConnGatt();
        _bluetoothDevice = null;
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

}