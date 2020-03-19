package com.zistone.blecontrol.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zistone.blecontrol.R;
import com.zistone.blecontrol.util.BluetoothListener;
import com.zistone.blecontrol.util.BluetoothUtil;
import com.zistone.blecontrol.util.ConvertUtil;
import com.zistone.blecontrol.util.MyActivityManager;
import com.zistone.blecontrol.util.ProgressDialogUtil;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class TemperatureMeasure extends AppCompatActivity implements View.OnClickListener, BluetoothListener {
    private static final String TAG = "TemperatureMeasure";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String SEARCH_CONTROLPARAM_COMM = "680000000000006810000186EA16";
    private static final int MESSAGE_ERROR_1 = -1;
    private static final int MESSAGE_ERROR_2 = -2;
    private static final int MESSAGE_ERROR_3 = -3;
    private static final int MESSAGE_1 = 100;
    private static final int RECEIVE = 8002;

    private BluetoothDevice _bluetoothDevice;
    private Context _context;
    private Toolbar _toolbar;
    private ImageButton _btnReturn;
    private Button _btn1, _btn2;
    private TextView _txt1, _txt2, _txt3;
    private CheckBox _chk1, _chk2;
    private StringBuffer _stringBuffer = new StringBuffer();
    private Timer _refreshTimer;
    private TimerTask _refreshTask;
    private Map<String, UUID> _uuidMap;
    private ProgressDialogUtil.Listener _progressDialogUtilListener;
    //是否连接成功
    private boolean _connectedSuccess = false;

    private void InitListener() {
        _progressDialogUtilListener = new ProgressDialogUtil.Listener() {
            @Override
            public void OnDismiss() {
                DisConnect();
            }
        };
    }

    private void DisConnect() {
        _connectedSuccess = false;
        _btn1.setEnabled(false);
        if (_refreshTask != null) {
            _refreshTask.cancel();
        }
        if (_refreshTimer != null) {
            _refreshTimer.cancel();
        }
        BluetoothUtil.DisConnGatt();
        _txt1.setText("Null");
        _txt1.setTextColor(Color.GRAY);
        _txt2.setText("Null");
        _txt2.setTextColor(Color.GRAY);
        _txt3.setText("Null");
        _txt3.setTextColor(Color.GRAY);
    }

    /**
     * 解析硬件返回的数据
     *
     * @param data
     */
    private void Resolve(String data) {
        //Log.d(TAG, ">>>共接收:" + data);
        String[] strArray = data.split(" ");
        String indexStr = strArray[12];
        Message message = new Message();
        switch (indexStr) {
            case "80": {
                byte[] bytes1 = ConvertUtil.HexStrToByteArray(strArray[13]);
                String bitStr = ConvertUtil.ByteToBit(bytes1[0]);
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
                //电池电量
                int battery = Integer.parseInt(strArray[14] + strArray[15], 16);
                //下端磁强
                int magneticDown = Integer.parseInt(strArray[16] + strArray[17], 16);
                //上端磁强
                int magneticUp = Integer.parseInt(strArray[2] + strArray[3], 16);
                //前端磁强
                int magneticBefore = Integer.parseInt(strArray[4] + strArray[5], 16);
                message.what = RECEIVE;
                message.obj = doorState1 + "," + lockState1 + "," + doorState2 + "," + lockState2 + "," + battery + "," + magneticDown + "," + magneticUp + "," + magneticBefore;
            }
            break;
        }
        handler.sendMessage(message);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            String result = (String) message.obj;
            switch (message.what) {
                case MESSAGE_ERROR_1:
                    DisConnect();
                    ProgressDialogUtil.ShowWarning(_context, "警告", "该设备的连接已断开,如需再次连接请重试!");
                    break;
                case MESSAGE_1: {
                    _btn1.setEnabled(true);
                    _refreshTimer = new Timer();
                    _refreshTask = new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(() -> {
                                try {
                                    String hexStr = "680000000000006810000180E616";
                                    BluetoothUtil.SendComm(hexStr);
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    };
                    //任务、延迟执行时间、重复调用间隔,Timer和TimerTask在调用cancel()取消后不能再执行schedule语句
                    _refreshTimer.schedule(_refreshTask, 0, 2 * 1000);
                    _connectedSuccess = true;
                }
                break;
                case RECEIVE: {
                    String strs[] = result.split(",");
                    Log.d(TAG, String.format("最高温度:%s℃ 最低温度:%s℃ 平均温度:%s℃", strs[5], strs[6], strs[7]));
                    _txt1.setText(strs[5] + "℃");
                    _txt1.setTextColor(Color.RED);
                    _txt2.setText(strs[6] + "℃");
                    _txt2.setTextColor(Color.BLUE);
                    _txt3.setText(strs[7] + "℃");
                    _txt3.setTextColor(Color.GREEN);
                }
                break;
            }
        }
    };

    @Override
    public void OnConnected() {
        ProgressDialogUtil.Dismiss();
        Log.d(TAG, ">>>成功建立连接!");
        //轮询
        Message message = handler.obtainMessage(MESSAGE_1, "");
        handler.sendMessage(message);
        //连接成功的回调
        startActivityForResult(null, 2);
    }

    @Override
    public void OnConnecting() {
        ProgressDialogUtil.ShowProgressDialog(_context, _progressDialogUtilListener, "正在连接...");
    }

    @Override
    public void OnDisConnected() {
        Log.d(TAG, ">>>连接已断开!");
        Message message = handler.obtainMessage(MESSAGE_ERROR_1, "");
        handler.sendMessage(message);
    }

    @Override
    public void OnWriteSuccess(byte[] byteArray) {
        String result = ConvertUtil.ByteArrayToHexStr(byteArray);
        result = ConvertUtil.HexStrAddCharacter(result, " ");
        String[] strArray = result.split(" ");
        String indexStr = strArray[11];
        switch (indexStr) {
        }
    }

    @Override
    public void OnReadSuccess(byte[] byteArray) {
        String result = ConvertUtil.ByteArrayToHexStr(byteArray);
        result = ConvertUtil.HexStrAddCharacter(result, " ");
        //Log.d(TAG, ">>>接收:" + result);
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
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            this.finish();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn2_temperature:
            case R.id.btn_return_temperature: {
                finish();
            }
            break;
            //连接
            case R.id.btn1_temperature: {
                if (_bluetoothDevice != null) {
                    if (_btn1.getText().toString().equals("连接")) {
                        _btn1.setText("断开");
                        Log.d(TAG, ">>>开始连接...");
                        BluetoothUtil.ConnectDevice(_bluetoothDevice, _uuidMap);
                    } else {
                        _btn1.setText("连接");
                        DisConnect();
                    }
                } else {
                    ProgressDialogUtil.ShowWarning(_context, "提示", "未获取到蓝牙,请重试!");
                }
            }
            break;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_measure);
        _context = MyActivityManager.getInstance().GetCurrentActivity();
        Intent intent = getIntent();
        _bluetoothDevice = intent.getParcelableExtra(ARG_PARAM1);
        _uuidMap = (Map<String, UUID>) intent.getSerializableExtra(ARG_PARAM2);
        //Toolbar
        _toolbar = findViewById(R.id.toolbar_temperature);
        _toolbar.setTitle("");
        setSupportActionBar(_toolbar);
        _txt1 = findViewById(R.id.txt1_temperature);
        _txt2 = findViewById(R.id.txt2_temperature);
        _txt3 = findViewById(R.id.txt3_temperature);
        _btnReturn = findViewById(R.id.btn_return_temperature);
        _btn1 = findViewById(R.id.btn1_temperature);
        _btn2 = findViewById(R.id.btn2_temperature);
        _btnReturn.setOnClickListener(this::onClick);
        _btn1.setOnClickListener(this::onClick);
        _btn2.setOnClickListener(this::onClick);
        InitListener();
        BluetoothUtil.Init(_context, this);
    }

    @Override
    public void onDestroy() {
        if (_refreshTimer != null)
            _refreshTimer.cancel();
        if (_refreshTask != null)
            _refreshTask.cancel();
        BluetoothUtil.DisConnGatt();
        _bluetoothDevice = null;
        super.onDestroy();
    }

}