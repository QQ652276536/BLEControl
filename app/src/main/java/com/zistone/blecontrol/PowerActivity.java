package com.zistone.blecontrol;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.zistone.blecontrol.dialogfragment.ParamSettingDialogFragment;
import com.zistone.blecontrol.util.MyBleConnectListener;
import com.zistone.blecontrol.util.MyBleMessageListener;
import com.zistone.blecontrol.util.MyProgressDialogListener;
import com.zistone.blecontrol.util.MyBleUtil;
import com.zistone.blecontrol.util.MyConvertUtil;
import com.zistone.blecontrol.util.MyProgressDialogUtil;

import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class PowerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PowerActivity";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //查询内部控制参数
    private static final String SEARCH_CONTROLPARAM_COMM = "680000000000006810000186EA16";
    //读取基本信息：版本，电池电压，内部温度
    private static final String BASEINFO_COMM = "6800000000000068210100EC16";
    //读取GPS位置信息
    private static final String LOCATION_COMM = "6800000000000068220100EC16";
    //综合测试：循环发送检测门的状态
    private static final String TESTA = "680000000000006810000180E616";
    //开一号门锁
    private static final String OPENDOOR1_COMM = "680000000000006810000181E116";
    //开二号门锁
    private static final String OPENDOOR2_COMM = "680000000000006810000182E716";
    //开全部门锁
    private static final String OPENDOORS_COMM = "680000000000006810000183E716";
    //开三号门锁
    private static final String OPENDOOR3_COMM = "680000000000006810000184E716";
    //温度、湿度、烟感、水浸
    private static final String CMD_39 = "680000000000006839000000E316";
    private static final int RECEIVE_BASEINFO = 21;
    private static final int RECEIVE_LOCATION = 22;
    private static final int RECEIVE_TEMPERATURE = 39;
    private static final int RECEIVE_TESTA = 8002;
    private static final int RECEIVE_OPENDOORS1 = 8102;
    private static final int RECEIVE_OPENDOORS2 = 8202;
    private static final int RECEIVE_OPENDOORS3 = 8402;
    private static final int RECEIVE_OPENALLDOORS = 8302;
    private static final int SEND_SEARCH_CONTROLPARAM = 86;
    private static final int RECEIVE_SEARCH_CONTROLPARAM = 8602;
    private static final int SEND_SET_CONTROLPARAM = 87;

    private BluetoothDevice _bluetoothDevice;
    private Toolbar _toolbar;
    private ImageButton _btnReturn, _btnTop, _btnBottom, _btnClear;
    private TextView _debugView;
    private Button _btn2, _btn3, _btn4, _btn5;
    private TextView _txt2, _txt5, _txt6, _txt7, _txt8, _txtVersion, _txtHumidity, _txtSmoke, _txtWater;
    private StringBuffer _stringBuffer = new StringBuffer();
    private MyProgressDialogListener dialogListener;
    private MyHandler _myHandler;
    //是否打开参数设置界面
    private boolean _isOpenParamSetting = false;
    private Timer _refreshTimer = new Timer();
    private ParamSettingDialogFragment _paramSetting;
    private FragmentManager _fragmentManager;
    //定时发送综合测试指令
    private TimerTask _refreshTask = new TimerTask() {
        @Override
        public void run() {
            try {
                MyBleUtil.SendComm(BASEINFO_COMM);
                Log.i(TAG, "发送'读取设备基本信息'指令：" + BASEINFO_COMM);
                Thread.sleep(100);
                MyBleUtil.SendComm(LOCATION_COMM);
                Log.i(TAG, "发送'GPS位置'指令：" + LOCATION_COMM);
                Thread.sleep(100);
                MyBleUtil.SendComm(TESTA);
                Log.i(TAG, "发送'综合测试A'指令：" + TESTA);
                Thread.sleep(100);
                MyBleUtil.SendComm(CMD_39);
                Log.i(TAG, "发送'温/湿度'指令：" + CMD_39);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
    private MyBleConnectListener _connectListener;
    private MyBleMessageListener _messageListener;

    static class MyHandler extends Handler {
        WeakReference<PowerActivity> _weakReference;
        PowerActivity _powerActivity;

        public MyHandler(PowerActivity activity) {
            _weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            if (_weakReference.get() == null)
                return;
            _powerActivity = _weakReference.get();
            String result = (String) message.obj;
            switch (message.what) {
                //设备基本信息
                case RECEIVE_BASEINFO: {
                    String[] strArray = result.split(" ");
                    String versionStr = MyConvertUtil.HexStrToStr((strArray[10] + strArray[11] + strArray[12] + strArray[13]).trim());
                    versionStr = MyConvertUtil.StrAddCharacter(versionStr, 2, ".");
                    double voltage = (double) Integer.valueOf(strArray[14] + strArray[15], 16) / 1000;
                    //解析方式改为和39命令一样
                    //                    double temperature = 23.0;
                    //                    try {
                    //                        temperature = 23 + Double.parseDouble(strArray[16]) / 2;
                    //                    } catch (Exception e) {
                    //                        e.printStackTrace();
                    //                    }
                    BigInteger bigInteger = new BigInteger(strArray[16], 16);
                    double temperature = (double) bigInteger.intValue() / 10;
                    _powerActivity._txtVersion.setText(versionStr);
                    _powerActivity._txt5.setText(voltage + "V");
                    //21命令里的温度是一个字节，39命令里的温度是两个字节，所以解析出来的不一样，这里温度改为使用39命令里的
                    //                    _powerActivity._txt6.setText(temperature + "℃");
                }
                break;
                //设备位置信息
                case RECEIVE_LOCATION: {
                    String[] strArray = result.split(" ");
                    int state = Integer.parseInt(strArray[10], 16);
                    if (state != 1) {
                        _powerActivity._txt7.setText("定位失败");
                        return;
                    }
                    //                String latStr = strArray[11] + strArray[12] + strArray[13] + strArray[14];
                    String latStr = strArray[14] + strArray[13] + strArray[12] + strArray[11];
                    double latNum = Double.valueOf(Integer.valueOf(latStr, 16)) / 1000000;
                    int len = Integer.parseInt(strArray[1], 16);
                    //                String lotStr = strArray[15] + strArray[16] + strArray[17] + strArray[2];
                    String lotStr = strArray[2] + strArray[17] + strArray[16] + strArray[15];
                    double lotNum = Double.valueOf(Integer.valueOf(lotStr, 16)) / 1000000;
                    //                    String heightStr = strArray[3] + strArray[4];
                    String heightStr1 = strArray[4];
                    int height = Integer.parseInt(heightStr1, 16);
                    String heightStr2 = strArray[3];
                    height += Integer.parseInt(heightStr2, 16);
                    _powerActivity._txt7.setText(latNum + "，" + lotNum + "，" + height);
                }
                break;
                //温湿度
                case RECEIVE_TEMPERATURE: {
                    String[] strArray = result.split(" ");
                    if (strArray.length < 14)
                        return;
                    //状态位，Bit0=1表示湿度数据有效，Bit1=1表示温度数据有效
                    byte[] byteArray = MyConvertUtil.HexStrToByteArray(strArray[10]);
                    String bitStr = MyConvertUtil.ByteToBit(byteArray[0]);
                    String[] bitStrArray = MyConvertUtil.StrAddCharacter(bitStr, 1, " ").split(" ");
                    String bit0 = bitStrArray[7];
                    String bit1 = bitStrArray[6];
                    //有符号16进制转10进制
                    //                    int humidity = Integer.valueOf(strArray[11] + strArray[12], 16).shortValue() / 10;
                    //                    int temperature = Integer.valueOf(strArray[13] + strArray[14], 16).shortValue() / 10;
                    BigInteger bigInteger1 = new BigInteger(strArray[11] + strArray[12], 16);
                    BigInteger bigInteger2 = new BigInteger(strArray[13] + strArray[14], 16);
                    double humidity = (double) bigInteger1.intValue() / 10;
                    double temperature = (double) bigInteger2.intValue() / 10;
                    if ("1".equals(bit0)) {
                        _powerActivity._txtHumidity.setText(humidity + "%");
                    }
                    if ("1".equals(bit1)) {
                        //设备基本信息命令里也有温度字段
                        _powerActivity._txt6.setText(temperature + "℃");
                    }
                    //烟感
                    int smoke = Integer.valueOf(strArray[15], 16);
                    //水浸
                    int water = Integer.valueOf(strArray[16], 16);
                    if (smoke == 0) {
                        _powerActivity._txtSmoke.setText("正常");
                        _powerActivity._txtSmoke.setTextColor(Color.GREEN);
                    } else {
                        _powerActivity._txtSmoke.setText("告警");
                        _powerActivity._txtSmoke.setTextColor(Color.RED);
                    }
                    if (water == 0) {
                        _powerActivity._txtWater.setText("正常");
                        _powerActivity._txtWater.setTextColor(Color.GREEN);
                    } else {
                        _powerActivity._txtWater.setText("告警");
                        _powerActivity._txtWater.setTextColor(Color.RED);
                    }
                    String cmdStr = Arrays.toString(strArray).replaceAll("[\\s|\\[|\\]|,]", "");
                    cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                    String logStr =
                            "温湿度\n" + cmdStr + "\nbitStr：" + bitStr + "，bit0：" + bit0 + "，bit1：" + bit1 + "，温度：" + temperature + "℃，湿度：" + humidity + "%，烟感：" + smoke + "，水浸：" + water + "\n\n";
                    Log.i(TAG, logStr);
                }
                break;
                //综合测试
                case RECEIVE_TESTA: {
                    String[] strArray = result.split(" ");
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
                    //电池电量
                    int battery = Integer.parseInt(strArray[14] + strArray[15], 16);
                    //下端磁强
                    int magneticDown = Integer.parseInt(strArray[16] + strArray[17], 16);
                    //上端磁强
                    int magneticUp = Integer.parseInt(strArray[2] + strArray[3], 16);
                    //前端磁强
                    int magneticBefore = Integer.parseInt(strArray[4] + strArray[5], 16);
                    if (doorState1.equals("1")) {
                        _powerActivity._txt2.setText("已开");
                        _powerActivity._txt2.setTextColor(Color.GREEN);
                    } else {
                        _powerActivity._txt2.setText("已关");
                        _powerActivity._txt2.setTextColor(Color.RED);
                    }
                    if (lockState1.equals("1")) {
                        _powerActivity._txt8.setText("已开");
                        _powerActivity._txt8.setTextColor(Color.GREEN);
                    } else {
                        _powerActivity._txt8.setText("已关");
                        _powerActivity._txt8.setTextColor(Color.RED);
                    }
                }
                break;
                //一号门锁
                case RECEIVE_OPENDOORS1:
                    //二号门锁
                case RECEIVE_OPENDOORS2:
                    //开三号门锁
                case RECEIVE_OPENDOORS3: {
                    String[] strArray = result.split(" ");
                    byte[] bytes = MyConvertUtil.HexStrToByteArray(strArray[13]);
                    String bitStr = MyConvertUtil.ByteToBit(bytes[0]);
                    String doorState = String.valueOf(bitStr.charAt(7));
                }
                break;
                //全部门锁
                case RECEIVE_OPENALLDOORS: {
                    //全部（一二号）门锁有响应再发送开三号门锁
                    //                    MyBleUtil.SendComm(OPENDOOR3_COMM);
                    break;
                }
                //发送查询内部控制参数的指令
                case SEND_SEARCH_CONTROLPARAM: {
                    MyBleUtil.SendComm(SEARCH_CONTROLPARAM_COMM);
                }
                break;
                //查询到的内部控制参数
                case RECEIVE_SEARCH_CONTROLPARAM: {
                    String[] strArray = result.split(" ");
                    byte[] bytes = MyConvertUtil.HexStrToByteArray(strArray[16]);
                    String bitStr = MyConvertUtil.ByteToBit(bytes[0]);
                    //启用DEBUG软串口
                    String bitStr8 = String.valueOf(bitStr.charAt(7));
                    //使用低磁检测阀值
                    String bitStr7 = String.valueOf(bitStr.charAt(6));
                    //不检测强磁
                    String bitStr6 = String.valueOf(bitStr.charAt(5));
                    //启用软关机
                    String bitStr5 = String.valueOf(bitStr.charAt(4));
                    //有外电可以进入维护方式
                    String bitStr4 = String.valueOf(bitStr.charAt(3));
                    //正常开锁不告警
                    String bitStr3 = String.valueOf(bitStr.charAt(2));
                    //锁检测开关(锁上开路)
                    String bitStr2 = String.valueOf(bitStr.charAt(1));
                    //门检测开关(关门开路)
                    String bitStr1 = String.valueOf(bitStr.charAt(0));
                    Log.i(TAG, String.format("收到查询到的参数（Bit）：\n门检测开关（关门开路）%s\n锁检测开关（锁上开路）" + "%s\n正常开锁不告警%s\n有外电可以进入维护方式%s\n启用软关机%s\n不检测强磁%s\n" +
                            "使用低磁检测阀值%s\n启用DEBUG软串口%s", bitStr1, bitStr2, bitStr3, bitStr4, bitStr5, bitStr6, bitStr7, bitStr8));
                    //打开控制参数修改界面的时候将查询结果传递过去，此时可以不输出调试信息
                    if (_powerActivity._isOpenParamSetting) {
                        if (_powerActivity._paramSetting == null) {
                            _powerActivity._paramSetting = ParamSettingDialogFragment.NewInstance(new String[]{bitStr1, bitStr2, bitStr3, bitStr4,
                                                                                                               bitStr5, bitStr6, bitStr7,
                                                                                                               bitStr8},
                                    _powerActivity.dialogListener);
                            _powerActivity._paramSetting.setCancelable(false);
                        }
                        _powerActivity._paramSetting.show(_powerActivity._fragmentManager, "ParamSettingDialogFragment");
                        _powerActivity.
                                _isOpenParamSetting = false;
                    } else {
                        if (bitStr8.equals("1"))
                            _powerActivity._debugView.append("\n收到：\n启用DEBUG软串口【启用】\n");
                        else
                            _powerActivity._debugView.append("\n收到：\n启用DEBUG软串口【禁用】\n");
                        if (bitStr7.equals("1"))
                            _powerActivity._debugView.append("使用低磁检测阀值【启用】\n");
                        else
                            _powerActivity._debugView.append("使用低磁检测阀值【禁用】\n");
                        if (bitStr6.equals("1"))
                            _powerActivity._debugView.append("不检测强磁【启用】\n");
                        else
                            _powerActivity._debugView.append("不检测强磁【禁用】\n");
                        if (bitStr5.equals("1"))
                            _powerActivity._debugView.append("启用软关机【启用】\n");
                        else
                            _powerActivity._debugView.append("启用软关机【禁用】\n");
                        if (bitStr4.equals("1"))
                            _powerActivity._debugView.append("有外电可以进入维护方式【启用】\n");
                        else
                            _powerActivity._debugView.append("有外电可以进入维护方式【禁用】\n");
                        if (bitStr3.equals("1"))
                            _powerActivity._debugView.append("正常开锁不告警【启用】\n");
                        else
                            _powerActivity._debugView.append("正常开锁不告警【禁用】\n");
                        if (bitStr2.equals("1"))
                            _powerActivity._debugView.append("锁检测开关(锁上开路)【启用】\n");
                        else
                            _powerActivity._debugView.append("锁检测开关(锁上开路)【禁用】\n");
                        if (bitStr1.equals("1"))
                            _powerActivity._debugView.append("门检测开关(关门开路)【启用】\n");
                        else
                            _powerActivity._debugView.append("门检测开关(关门开路)【禁用】\n");
                    }
                    //定位到最后一行
                    int offset = _powerActivity._debugView.getLineCount() * _powerActivity._debugView.getLineHeight();
                    //如果文本的高度大于ScrollView的，就自动滑动
                    if (offset > _powerActivity._debugView.getHeight())
                        _powerActivity._debugView.scrollTo(0, offset - _powerActivity._debugView.getHeight());
                }
                break;
                //修改内部控制参数
                case SEND_SET_CONTROLPARAM: {
                    Log.i(TAG, "发送参数设置：" + result);
                    MyBleUtil.SendComm(result);
                    _powerActivity._debugView.append("发送参数设置指令 ");
                    int offset = _powerActivity._debugView.getLineCount() * _powerActivity._debugView.getLineHeight();
                    if (offset > _powerActivity._debugView.getHeight())
                        _powerActivity._debugView.scrollTo(0, offset - _powerActivity._debugView.getHeight());
                }
                break;
            }
        }
    }

    /**
     * 解析硬件返回的数据
     *
     * @param data 带空格的16进制字符串
     */
    private void Resolve(String data) {
        String[] strArray = data.split(" ");
        int what = 0;
        /*
         * 设备基本信息的通信协议和之前的开关门协议不一样，需要留意
         *
         * */
        if (strArray[8].equals("A1")) {
            what = RECEIVE_BASEINFO;
        }
        /*
         * GPS位置信息的通信协议和之前的开关门协议不一样，需要留意
         *
         * */
        else if (strArray[8].equals("A2")) {
            what = RECEIVE_LOCATION;
        }
        /*
         * 温湿度
         * */
        else if (strArray[8].equals(("B9"))) {
            what = RECEIVE_TEMPERATURE;
        }
        /*
         * 开关门协议
         *
         * */
        else if (strArray[8].equals("10")) {
            String indexStr = strArray[12];
            switch (indexStr) {
                //全部门锁状态
                case "80":
                    what = RECEIVE_TESTA;
                    break;
                //开一号门锁
                case "81":
                    what = RECEIVE_OPENDOORS1;
                    break;
                //开二号门锁
                case "82":
                    what = RECEIVE_OPENDOORS2;
                    break;
                //开全部门锁
                case "83":
                    what = RECEIVE_OPENALLDOORS;
                    break;
                //查询内部控制参数
                case "86":
                    what = RECEIVE_SEARCH_CONTROLPARAM;
                    break;
                //修改内部控制参数
                case "87":
                    //先查询再修改
                    what = SEND_SEARCH_CONTROLPARAM;
                    break;
            }
        }
        _myHandler.obtainMessage(what, data).sendToTarget();
    }

    private void InitListener() {
        _connectListener = new MyBleConnectListener() {
            @Override
            public void OnConnected() {
            }

            @Override
            public void OnConnecting() {
            }

            @Override
            public void OnDisConnected() {
                Log.e(TAG, "连接已断开");
                runOnUiThread(() -> MyProgressDialogUtil.ShowWarning(PowerActivity.this, "知道了", "警告", "连接已断开，请检查设备然后重新连接！", false, () -> {
                    Intent intent = new Intent(PowerActivity.this, ListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }));
            }
        };
        _messageListener = new MyBleMessageListener() {
            @Override
            public void OnWriteSuccess(byte[] byteArray) {
                String result = MyConvertUtil.ByteArrayToHexStr(byteArray);
                result = MyConvertUtil.StrAddCharacter(result, 2, " ");
                String[] strArray = result.split(" ");
                String indexStr = strArray[11];
                switch (indexStr) {
                    case "80":
                        Log.i(TAG, "发送'综合测试'指令：" + TESTA);
                        break;
                    case "81":
                        Log.i(TAG, "发送开一号门锁：" + OPENDOOR1_COMM);
                        break;
                    case "82":
                        Log.i(TAG, "发送开二号门锁：" + OPENDOOR2_COMM);
                        break;
                    case "83":
                        Log.i(TAG, "发送开全部门锁：" + OPENDOORS_COMM);
                        break;
                    case "84":
                        Log.i(TAG, "发送开三号门锁：" + OPENDOOR3_COMM);
                        break;
                }
            }

            @Override
            public void OnReadSuccess(byte[] byteArray) {
                String result = MyConvertUtil.ByteArrayToHexStr(byteArray);
                result = MyConvertUtil.StrAddCharacter(result, 2, " ");
                Log.i(TAG, "收到：" + result);
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
        };
        dialogListener = new MyProgressDialogListener() {
            @Override
            public void OnDismiss(String tag) {
            }

            @Override
            public void OnComfirm(String tag, String str) {
                _myHandler.obtainMessage(SEND_SET_CONTROLPARAM, str).sendToTarget();
                //发送内部参数以后关闭设置窗口
                _paramSetting.dismiss();
                _paramSetting = null;
            }

            @Override
            public void OnComfirm(String tag, Object[] objectArray) {
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
            this.finish();
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            //内部控制参数设置
            case R.id.item1_power: {
                //先查询内部控制参数，再打开修改参数的界面
                MyBleUtil.SendComm(SEARCH_CONTROLPARAM_COMM);
                _isOpenParamSetting = true;
            }
            break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.power_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnReturn_power: {
                MyProgressDialogUtil.DismissAlertDialog();
                this.finish();
            }
            //回到顶部
            case R.id.btnTop_power:
                _debugView.scrollTo(0, 0);
                break;
            //回到底部
            case R.id.btnBottom_power:
                int offset = _debugView.getLineCount() * _debugView.getLineHeight();
                if (offset > _debugView.getHeight())
                    _debugView.scrollTo(0, offset - _debugView.getHeight());
                break;
            //清屏
            case R.id.btnClear_power:
                _debugView.setText("");
                break;
            //开一号门锁
            case R.id.button2_power: {
                MyBleUtil.SendComm(OPENDOOR1_COMM);
            }
            break;
            //开二号门锁
            case R.id.button3_power: {
                MyBleUtil.SendComm(OPENDOOR2_COMM);
            }
            break;
            //开全部门锁
            case R.id.button4_power: {
                MyBleUtil.SendComm(OPENDOORS_COMM);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MyBleUtil.SendComm(OPENDOOR3_COMM);
            }
            //开三号门锁
            case R.id.button5_power: {
                MyBleUtil.SendComm(OPENDOOR3_COMM);
            }
            break;
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _myHandler = new MyHandler(this);
        setContentView(R.layout.activity_power);
        //任务、延迟执行时间、重复调用间隔，Timer和TimerTask在调用cancel方法取消后不能再执行schedule语句
        _refreshTimer.schedule(_refreshTask, 0, 1 * 1000);
        _fragmentManager = getSupportFragmentManager();
        Intent intent = getIntent();
        _bluetoothDevice = intent.getParcelableExtra(ARG_PARAM1);
        //Toolbar
        _toolbar = findViewById(R.id.toolbar_power);
        _toolbar.setTitle("");
        setSupportActionBar(_toolbar);
        _txt2 = findViewById(R.id.txt2_power);
        _txt5 = findViewById(R.id.txt5_power);
        _txt6 = findViewById(R.id.txt6_power);
        _txt7 = findViewById(R.id.txt7_power);
        _txt8 = findViewById(R.id.txt8_power);
        _txtVersion = findViewById(R.id.txtVersion_power);
        _txtHumidity = findViewById(R.id.txt9_power);
        _txtSmoke = findViewById(R.id.txt10_power);
        _txtWater = findViewById(R.id.txt11_power);
        _debugView = findViewById(R.id.debug_view_power);
        _debugView.setMovementMethod(ScrollingMovementMethod.getInstance());
        _btnReturn = findViewById(R.id.btnReturn_power);
        _btn2 = findViewById(R.id.button2_power);
        _btn3 = findViewById(R.id.button3_power);
        _btn5 = findViewById(R.id.button5_power);
        _btn4 = findViewById(R.id.button4_power);
        _btnTop = findViewById(R.id.btnTop_power);
        _btnBottom = findViewById(R.id.btnBottom_power);
        _btnClear = findViewById(R.id.btnClear_power);
        _btnReturn.setOnClickListener(this::onClick);
        _btnTop.setOnClickListener(this::onClick);
        _btnBottom.setOnClickListener(this::onClick);
        _btnClear.setOnClickListener(this::onClick);
        _btn2.setOnClickListener(this::onClick);
        _btn3.setOnClickListener(this::onClick);
        _btn4.setOnClickListener(this::onClick);
        _btn5.setOnClickListener(this::onClick);
        _debugView.setMovementMethod(ScrollingMovementMethod.getInstance());
        InitListener();
        MyBleUtil.SetConnectListener(_connectListener);
        MyBleUtil.SetMessageListener(_messageListener);
    }

    @Override
    public void onDestroy() {
        _refreshTimer.cancel();
        _refreshTask.cancel();
        _bluetoothDevice = null;
        super.onDestroy();
    }

}
