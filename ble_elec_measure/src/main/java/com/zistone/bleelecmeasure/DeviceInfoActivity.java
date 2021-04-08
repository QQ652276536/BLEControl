package com.zistone.bleelecmeasure;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.zistone.libble.controls.MyRemoteControlButton;
import com.zistone.libble.util.MyBleConnectListener;
import com.zistone.libble.util.MyBleMessageListener;
import com.zistone.libble.util.MyBleUtil;
import com.zistone.libble.util.MyConvertUtil;
import com.zistone.libble.util.MyProgressDialogUtil;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class DeviceInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DeviceInfoActivity";
    //电能测量-电压，单位：V
    private static final String CMD_3B_VOLTAGE = "68000000000000683B010000E316";
    //电能测量-电流，单位：A
    private static final String CMD_3B_CURRENT = "68000000000000683B010100E316";
    //电能测量-功率，单位：W
    private static final String CMD_3B_POWER = "68000000000000683B010200E316";
    //电能测量-电能，单位：kWh
    private static final String CMD_3B_ELECTRIC = "68000000000000683B010300E316";
    //电能测量-频率
    private static final String CMD_3B_FREQUENCY = "68000000000000683B010400E316";
    //电能测量-功率因数
    private static final String CMD_3B_FACTOR = "68000000000000683B010500E316";
    //电能测量-时长，单位：秒
    private static final String CMD_3B_TIME = "68000000000000683B011000E316";
    //操作命令-关机
    private static final String CMD_3C_SHUTDOWN = "68000000000000683C015000E316";
    //操作命令-开机
    private static final String CMD_3C_STARTUP = "68000000000000683C015100E316";
    //操作命令-上
    private static final String CMD_3C_UP = "68000000000000683C015200E316";
    //操作命令-下
    private static final String CMD_3C_DOWN = "68000000000000683C015300E316";
    //操作命令-退出/取消
    private static final String CMD_3C_EXIT = "68000000000000683C015400E316";
    //操作命令-确认/进入
    private static final String CMD_3C_ENTER = "68000000000000683C015500E316";
    //操作命令-继续测量（断续测量模式下）
    private static final String CMD_3C_CONTINUE_CONTINUE_TEST = "68000000000000683C015600E316";
    //操作命令-暂停测量
    private static final String CMD_3C_PAUSE_TEST = "68000000000000683C015700E316";
    //操作命令-退出测量
    private static final String CMD_3C_EXIT_MEASURE = "68000000000000683C015800E316";
    //操作命令-关闭蜂鸣器
    private static final String CMD_3C_BUZZER_CLOSE = "68000000000000683C015900E316";
    //操作命令-开启蜂鸣器
    private static final String CMD_3C_BUZZER_OPEN = "68000000000000683C015D00E316";
    //操作命令-开始断续测量
    private static final String CMD_3C_INTERMITTENT_TEST = "68000000000000683C015A00E316";
    //操作命令-开始连续测量
    private static final String CMD_3C_CONTINUE_TEST = "68000000000000683C015B00E316";
    //查询当前测量结果
    private static final String CMD_SEARCH_TEST_RESULT = "68000000000000683D0000E316";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static int TASK_TIME = 1 * 1000;

    private ImageButton _btnTop, _btnBottom, _btnClear;
    private Button _btnElectricDebug, _btnCircuitDebug, _btnContinueTest, _btnIntermittentTest, _btnPauseTest, _btnExitMeasure;
    private TextView _txtName, _txtMac, _txtVoltage, _txtCurrent, _txtPower, _txtElectric, _txtFactor, _txtFrequency, _txtDebug, _txtTest;
    private BluetoothDevice _bluetoothDevice;
    private Map<String, UUID> _uuidMap;
    private Toolbar _toolbar;
    private MyHandler _myHandler;
    private LinearLayout _llDebug;
    private Timer _timer;
    private TimerTask _timerTask;
    private boolean _deviceInfoTaskIsRuning = true, _testResultTaskIsRunning = false, _isConnected = false;
    private MyRemoteControlButton _myRemoteControlButton;
    private String _currentTest = "";
    private MyBleConnectListener _connectListener;
    private MyBleMessageListener _messageListener;

    static class MyHandler extends Handler {
        WeakReference<DeviceInfoActivity> _weakReference;
        DeviceInfoActivity _deviceInfoActivity;

        public MyHandler(DeviceInfoActivity activity) {
            _weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            if (_weakReference.get() == null)
                return;
            try {
                _deviceInfoActivity = _weakReference.get();
                if (message.what == -99) {
                    _deviceInfoActivity._txtDebug.append(message.obj + "\n");
                } else {
                    String[] strArray = (String[]) message.obj;
                    //当前测量结果
                    if (message.what == 189) {
                        String stateHex = strArray[10];
                        String warnHex = strArray[11];
                        String passHex = strArray[12] + strArray[13];
                        String failHex = strArray[14] + strArray[15];
                        //累计的正常/异常次数，仅在断续测量下有数据
                        String passCountHex = strArray[16] + strArray[17];
                        String failCountHex = strArray[2] + strArray[3];
                        int warn = Integer.parseInt(warnHex, 16);
                        String warnBitStr = MyConvertUtil.ByteToBitBig((byte) warn);
                        String[] warnStrArray = MyConvertUtil.StrAddCharacter(warnBitStr, 1, " ").split(" ");
                        String warnStr = "";
                        if (warnStrArray[0].equals("1"))
                            warnStr += "没有交流";
                        if (warnStrArray[7].equals("1"))
                            warnStr += "，温度过高";
                        if (warnStrArray[6].equals("1"))
                            warnStr += "，温度过高";
                        if (warnStrArray[5].equals("1"))
                            warnStr += "，湿度过低";
                        if (warnStrArray[4].equals("1"))
                            warnStr += "，电压过高";
                        int passCount = Integer.parseInt(passCountHex, 16);
                        int failCount = Integer.parseInt(failCountHex, 16);
                        Log.i(TAG, "测量状态（Hex）：" + stateHex + "，告警状态（Hex）：" + warnHex + "（" + warnBitStr + "）" + "，正常次数（Hex）：" + passHex +
                                "，异常次数（Hex）：" + failHex + "，累计正常次数（Hex）：" + passCountHex + "，累计异常次数（Hex）：" + failCountHex);
                        int pass = Integer.parseInt(passHex, 16);
                        int fail = Integer.parseInt(failHex, 16);
                        //断续测量结束后恢复连续测量按钮状态
                        if (_deviceInfoActivity._currentTest.equals("INTERMITTENT_TEST")) {
                            if ("01".equals(stateHex)) {
                                _deviceInfoActivity._txtTest.setText("正在测量...");
                            } else {
                                _deviceInfoActivity._txtTest.setText("测量完成");
                                _deviceInfoActivity._txtTest.append(warnStr + "\n");
                                _deviceInfoActivity._txtTest.append("正常\t" + pass + "\t异常\t" + fail);
                                _deviceInfoActivity._txtTest.append("\n累计\t" + passCount + "\t累计\t" + failCount);
                            }
                        } else {
                            if ("01".equals(stateHex)) {
                                _deviceInfoActivity._txtTest.setText("正在测量...");
                            } else {
                                _deviceInfoActivity._txtTest.setText("测量完成");
                                _deviceInfoActivity._txtTest.append(warnStr + "\n");
                                _deviceInfoActivity._txtTest.append("正常\t" + pass + "\t异常\t" + fail);
                            }
                        }
                    }
                    //电能数据
                    else {
                        String hexStr = strArray[11] + strArray[12] + strArray[13] + strArray[14];
                        double value = Integer.parseInt(hexStr, 16) / 1000.00;
                        String valueStr = String.format("%.2f", value);
                        switch (message.what) {
                            //电压
                            case 0: {
                                String logStr = "电压：" + valueStr + "V\n\n";
                                Log.i(TAG, logStr);
                                _deviceInfoActivity._txtVoltage.setText(valueStr + "V");
                                _deviceInfoActivity._txtDebug.append(logStr);
                            }
                            break;
                            //电流
                            case 1: {
                                String logStr = "电流：" + valueStr + "A\n\n";
                                Log.i(TAG, logStr);
                                _deviceInfoActivity._txtCurrent.setText(valueStr + "V");
                                _deviceInfoActivity._txtDebug.append(logStr);
                            }
                            break;
                            //功率
                            case 2: {
                                String logStr = "功率：" + valueStr + "W\n\n";
                                Log.i(TAG, logStr);
                                _deviceInfoActivity._txtPower.setText(valueStr + "W");
                                _deviceInfoActivity._txtDebug.append(logStr);
                            }
                            break;
                            //电能
                            case 3: {
                                String logStr = "电能：" + valueStr + "kWh\n\n";
                                Log.i(TAG, logStr);
                                _deviceInfoActivity._txtElectric.setText(valueStr + "kWh");
                                _deviceInfoActivity._txtDebug.append(logStr);
                            }
                            break;
                            //频率
                            case 4: {
                                String logStr = "频率：" + valueStr + "Hz\n\n";
                                Log.i(TAG, logStr);
                                _deviceInfoActivity._txtFrequency.setText(valueStr + "Hz");
                                _deviceInfoActivity._txtDebug.append(logStr);
                            }
                            break;
                            //功率因数
                            case 5: {
                                String logStr = "功率因数：" + valueStr + "\n\n";
                                Log.i(TAG, logStr);
                                _deviceInfoActivity._txtFactor.setText(valueStr);
                                _deviceInfoActivity._txtDebug.append(logStr);
                            }
                            break;
                            //测量持续时长
                            case 10: {
                                String logStr = "测量持续时长：" + value + "\n\n";
                                Log.i(TAG, logStr);
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                _deviceInfoActivity._txtDebug.append("\n" + e.toString() + "\n");
            } finally {
                int offset = _deviceInfoActivity._txtDebug.getLineCount() * _deviceInfoActivity._txtDebug.getLineHeight();
                if (offset > _deviceInfoActivity._txtDebug.getHeight()) {
                    _deviceInfoActivity._txtDebug.scrollTo(0, offset - _deviceInfoActivity._txtDebug.getHeight());
                }
            }
        }
    }

    private void SetConnectSuccess(boolean flag) {
        _isConnected = flag;
        runOnUiThread(() -> {
            _btnExitMeasure.setEnabled(flag);
            _btnContinueTest.setEnabled(flag);
            _btnIntermittentTest.setEnabled(flag);
            _btnPauseTest.setEnabled(flag);
            _btnCircuitDebug.setEnabled(flag);
            _btnElectricDebug.setEnabled(flag);
        });
    }

    /**
     * 查询测量结果
     */
    private void TestTask() {
        Log.i(TAG, "执行电路测量指令...");
        try {
            MyBleUtil.SendComm(CMD_SEARCH_TEST_RESULT);
            Thread.sleep(1 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询设备参数
     */
    private void DeviceInfoTask() {
        Log.i(TAG, "依次执行电能测量指令...");
        try {
            MyBleUtil.SendComm(CMD_3B_VOLTAGE);
            Thread.sleep(50);
            MyBleUtil.SendComm(CMD_3B_CURRENT);
            Thread.sleep(50);
            MyBleUtil.SendComm(CMD_3B_POWER);
            Thread.sleep(50);
            MyBleUtil.SendComm(CMD_3B_ELECTRIC);
            Thread.sleep(50);
            MyBleUtil.SendComm(CMD_3B_FACTOR);
            Thread.sleep(50);
            MyBleUtil.SendComm(CMD_3B_FREQUENCY);
            Thread.sleep(50);
            MyBleUtil.SendComm(CMD_3B_TIME);
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void StartTimerTask() {
        if (null != _timer)
            _timer.cancel();
        if (null != _timerTask)
            _timerTask.cancel();
        _timer = new Timer();
        _timerTask = new TimerTask() {
            @Override
            public void run() {
                if (_deviceInfoTaskIsRuning)
                    DeviceInfoTask();
                if (_testResultTaskIsRunning)
                    TestTask();
            }
        };
        _timer.schedule(_timerTask, 0, TASK_TIME);
    }

    private void InitListener() {
        _connectListener = new MyBleConnectListener() {
            @Override
            public void OnConnected() {
                Log.i(TAG, "成功建立连接");
                _isConnected = true;
                MyProgressDialogUtil.DismissAlertDialog();
                SetConnectSuccess(true);
                //返回时告知该设备已成功连接
                setResult(2, new Intent());
                StartTimerTask();
            }

            @Override
            public void OnConnecting() {
                Log.i(TAG, "正在建立连接...");
                MyProgressDialogUtil.ShowWaittingDialog(DeviceInfoActivity.this, true, null, "正在连接...\n如长时间未连\n接请返回重试");
            }

            @Override
            public void OnDisConnected() {
                MyProgressDialogUtil.DismissAlertDialog();
                SetConnectSuccess(false);
                _isConnected = false;
                Log.e(TAG, "连接已断开");
                runOnUiThread(() -> MyProgressDialogUtil.ShowWarning(DeviceInfoActivity.this, "知道了", "警告", "连接已断开，请检查设备然后重新连接！", true, () -> {
//                    //返回时重新刷新
//                    Intent intent = new Intent(DeviceInfoActivity.this, ListActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(intent);
                    finish();
                }));
            }
        };

        _messageListener = new MyBleMessageListener() {
            @Override
            public void OnWriteSuccess(byte[] byteArray) {
                String result = MyConvertUtil.ByteArrayToHexStr(byteArray);
                result = MyConvertUtil.StrAddCharacter(result, 2, " ");
                Log.i(TAG, "发送：" + result);
                String finalResult = result;
                runOnUiThread(() -> _txtDebug.append("发送：" + finalResult + "\n"));
            }

            @Override
            public void OnReadSuccess(byte[] byteArray) {
                String result = MyConvertUtil.ByteArrayToHexStr(byteArray);
                result = MyConvertUtil.StrAddCharacter(result, 2, " ");
                Log.i(TAG, "收到：" + result);
                String finalResult = result;
                runOnUiThread(() -> _txtDebug.append("收到：" + finalResult + "\n"));
                String[] strArray = result.split(" ");
                //目前最短的指令为12位
                if (strArray.length <= 12) {
                    String loStr = "指令长度" + strArray.length + "错误，不予解析！";
                    _myHandler.obtainMessage(-99, loStr + "\n").sendToTarget();
                    return;
                }
                String type = strArray[8].toUpperCase();
                //电能测量数据
                if ("BB".equals(type)) {
                    int index = Integer.parseInt(strArray[10], 16);
                    _myHandler.obtainMessage(index, strArray).sendToTarget();
                } else if ("FB".equals(type)) {
                    String logStr = "电能测量数据响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, "\n" + logStr + "\n").sendToTarget();
                }
                //当前测量结果
                else if ("BD".equals(type)) {
                    _myHandler.obtainMessage(189, strArray).sendToTarget();
                } else if ("FD".equals(type)) {
                    String logStr = "当前测量结果响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, "\n" + logStr + "\n").sendToTarget();
                }
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                MyProgressDialogUtil.DismissAlertDialog();
                finish();
                break;
            case R.id.item1_menu:
                if (item.getTitle().equals("打开调试")) {
                    _llDebug.setVisibility(View.VISIBLE);
                    item.setTitle("关闭调试");
                    if (null != _timer)
                        _timer.cancel();
                    if (null != _timerTask)
                        _timerTask.cancel();
                } else {
                    item.setTitle("打开调试");
                    _llDebug.setVisibility(View.INVISIBLE);
                    StartTimerTask();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View v) {
        try {
            String logStr = null;
            switch (v.getId()) {
                case R.id.btn_continue_test:
                    MyBleUtil.SendComm(CMD_3C_CONTINUE_TEST);
                    logStr = "连续测量";
                    _testResultTaskIsRunning = true;
                    StartTimerTask();
                    _currentTest = "CONTINUE_TEST";
                    //开启连续测量后只能手动停止，禁止断续测量按钮
                    _btnIntermittentTest.setEnabled(false);
                    break;
                case R.id.btn_intermittent_test:
                    MyBleUtil.SendComm(CMD_3C_INTERMITTENT_TEST);
                    logStr = "断续测量";
                    _testResultTaskIsRunning = true;
                    StartTimerTask();
                    _currentTest = "INTERMITTENT_TEST";
                    //断续测量会自动停止，禁止连续测量按钮
                    _btnContinueTest.setEnabled(false);
                    break;
                case R.id.btn_pause_test:
                    //                    if (_btnPauseTest.getText().equals("暂停测量")) {
                    //                        _testResultTaskIsRunning = false;
                    //                        _btnPauseTest.setText("继续测量");
                    //                        MyBleUtil.SendComm(CMD_3C_PAUSE_TEST);
                    //                        logStr = "暂停测量";
                    //恢复测量按钮
                    _btnIntermittentTest.setEnabled(true);
                    _btnContinueTest.setEnabled(true);
                    //                    } else {
                    //                        _testResultTaskIsRunning = true;
                    //                        _btnPauseTest.setText("暂停测量");
                    //                        MyBleUtil.SendComm(CMD_3C_CONTINUE_CONTINUE_TEST);
                    //                        logStr = "继续测量";
                    //                    }
                    _testResultTaskIsRunning = false;
                    MyBleUtil.SendComm(CMD_3C_PAUSE_TEST);
                    logStr = "暂停测量";
                    break;
                case R.id.btn_exit_measure:
                    MyBleUtil.SendComm(CMD_3C_EXIT_MEASURE);
                    logStr = "退出测量";
                    _testResultTaskIsRunning = false;
                    //恢复测量按钮
                    _btnIntermittentTest.setEnabled(true);
                    _btnContinueTest.setEnabled(true);
                    break;
                //电能测量
                case R.id.btn_electric_debug:
                    DeviceInfoTask();
                    break;
                //电路测量
                case R.id.btn_circuit_debug:
                    TestTask();
                    break;
                case R.id.btn_top:
                    _txtDebug.scrollTo(0, 0);
                    break;
                case R.id.btn_bottom:
                    int offset = _txtDebug.getLineCount() * _txtDebug.getLineHeight();
                    if (offset > _txtDebug.getHeight()) {
                        _txtDebug.scrollTo(0, offset - _txtDebug.getHeight());
                    }
                    break;
                case R.id.btn_clear:
                    _txtDebug.setText("");
                    break;
            }
            if (null != logStr && !"".equals(logStr)) {
                Log.i(TAG, logStr);
                _txtDebug.append(logStr + "\n\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            _txtDebug.append("\n" + e.toString() + "\n");
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        MyBleUtil.SetMessageListener(_messageListener);
        StartTimerTask();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != _timer)
            _timer.cancel();
        if (null != _timerTask)
            _timerTask.cancel();
        _isConnected = false;
        MyBleUtil.DisConnGatt();
        _bluetoothDevice = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (null != _timer)
            _timer.cancel();
        if (null != _timerTask)
            _timerTask.cancel();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        _myHandler = new MyHandler(this);
        Intent intent = getIntent();
        _bluetoothDevice = intent.getParcelableExtra(ARG_PARAM1);
        _uuidMap = (Map<String, UUID>) intent.getSerializableExtra(ARG_PARAM2);
        _myRemoteControlButton = findViewById(R.id.mrcb_menu);
        MyRemoteControlButton.RoundMenu myRoundMenu = new MyRemoteControlButton.RoundMenu();
        myRoundMenu.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.down1);
        myRoundMenu.onClickListener = v -> {
            MyBleUtil.SendComm(CMD_3C_UP);
            String logStr = "下/-";
            _txtDebug.append(logStr + "\n\n");
        };
        _myRemoteControlButton.AddRoundMenu(myRoundMenu);
        myRoundMenu = new MyRemoteControlButton.RoundMenu();
        myRoundMenu.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.enter1);
        myRoundMenu.onClickListener = v -> {
            MyBleUtil.SendComm(CMD_3C_ENTER);
            String logStr = "确认/进入";
            _txtDebug.append(logStr + "\n\n");
        };
        _myRemoteControlButton.AddRoundMenu(myRoundMenu);
        myRoundMenu = new MyRemoteControlButton.RoundMenu();
        myRoundMenu.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.up1);
        myRoundMenu.onClickListener = v -> {
            MyBleUtil.SendComm(CMD_3C_DOWN);
            String logStr = "上/+";
            _txtDebug.append(logStr + "\n\n");
        };
        _myRemoteControlButton.AddRoundMenu(myRoundMenu);
        myRoundMenu = new MyRemoteControlButton.RoundMenu();
        myRoundMenu.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.exit1);
        myRoundMenu.onClickListener = v -> {
            MyBleUtil.SendComm(CMD_3C_EXIT);
            String logStr = "退出/取消";
            _txtDebug.append(logStr + "\n\n");
        };
        _myRemoteControlButton.AddRoundMenu(myRoundMenu);
        _myRemoteControlButton.SetCenterButton(null);
        _llDebug = findViewById(R.id.ll_debug);
        _llDebug.setVisibility(View.INVISIBLE);
        _btnElectricDebug = findViewById(R.id.btn_electric_debug);
        _btnElectricDebug.setOnClickListener(this::onClick);
        _btnCircuitDebug = findViewById(R.id.btn_circuit_debug);
        _btnCircuitDebug.setOnClickListener(this::onClick);
        _btnTop = findViewById(R.id.btn_top);
        _btnTop.setOnClickListener(this::onClick);
        _btnBottom = findViewById(R.id.btn_bottom);
        _btnBottom.setOnClickListener(this::onClick);
        _btnClear = findViewById(R.id.btn_clear);
        _btnClear.setOnClickListener(this::onClick);
        _btnExitMeasure = findViewById(R.id.btn_exit_measure);
        _btnExitMeasure.setOnClickListener(this::onClick);
        _btnContinueTest = findViewById(R.id.btn_continue_test);
        _btnContinueTest.setOnClickListener(this::onClick);
        _btnIntermittentTest = findViewById(R.id.btn_intermittent_test);
        _btnIntermittentTest.setOnClickListener(this::onClick);
        _btnPauseTest = findViewById(R.id.btn_pause_test);
        _btnPauseTest.setOnClickListener(this::onClick);
        //Toolbar
        _toolbar = findViewById(R.id.toolbar);
        _toolbar.setTitle("");
        setSupportActionBar(_toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        _txtName = findViewById(R.id.txt_name);
        _txtMac = findViewById(R.id.txt_mac);
        _txtVoltage = findViewById(R.id.txt_voltage);
        _txtCurrent = findViewById(R.id.txt_current);
        _txtPower = findViewById(R.id.txt_power);
        _txtElectric = findViewById(R.id.txt_electric);
        _txtFactor = findViewById(R.id.txt_factor);
        _txtFrequency = findViewById(R.id.txt_frequency);
        _txtDebug = findViewById(R.id.txt_debug);
        _txtDebug.setMovementMethod(ScrollingMovementMethod.getInstance());
        _txtDebug.setScrollbarFadingEnabled(false);
        _txtTest = findViewById(R.id.txt_test);
        InitListener();
        MyBleUtil.SetConnectListener(_connectListener);
        Log.i(TAG, "开始连接蓝牙...");
        MyBleUtil.ConnectDevice(_bluetoothDevice, _uuidMap);
    }

}
