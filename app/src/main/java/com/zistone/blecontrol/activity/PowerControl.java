package com.zistone.blecontrol.activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.zistone.blecontrol.R;
import com.zistone.blecontrol.controls.MyScrollView;
import com.zistone.blecontrol.dialogfragment.DialogFragment_OTA;
import com.zistone.blecontrol.dialogfragment.DialogFragment_ParamSetting;
import com.zistone.blecontrol.dialogfragment.DialogFragment_WriteValue;
import com.zistone.blecontrol.util.BluetoothListener;
import com.zistone.blecontrol.util.BluetoothUtil;
import com.zistone.blecontrol.util.ConvertUtil;
import com.zistone.blecontrol.util.DialogFragmentListener;
import com.zistone.blecontrol.util.ProgressDialogUtil;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class PowerControl extends AppCompatActivity implements View.OnClickListener, BluetoothListener {

    private static final String TAG = "PowerControl";
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
    private static final int MESSAGE_ERROR_1 = -1;
    private static final int MESSAGE_ERROR_2 = -2;
    private static final int MESSAGE_ERROR_3 = -3;
    private static final int MESSAGE_1 = 100;
    private static final int RECEIVE_BASEINFO = 21;
    private static final int RECEIVE_LOCATION = 22;
    private static final int RECEIVE_TESTA = 8002;
    private static final int RECEIVE_OPENDOORS1 = 8102;
    private static final int RECEIVE_OPENDOORS2 = 8202;
    private static final int RECEIVE_OPENALLDOORS = 8302;
    private static final int SEND_SEARCH_CONTROLPARAM = 86;
    private static final int RECEIVE_SEARCH_CONTROLPARAM = 8602;
    private static final int SEND_SET_CONTROLPARAM = 87;

    private BluetoothDevice _bluetoothDevice;
    private Toolbar _toolbar;
    private ImageButton _btnReturn, _btnClear;
    private TextView _debugView;
    private Button _btn1, _btn2, _btn3, _btn4;
    private TextView _txt1, _txt2, _txt3, _txt4, _txt5, _txt6, _txt7, _txtVersion;
    private StringBuffer _stringBuffer = new StringBuffer();
    private Timer _refreshTimer;
    private TimerTask _refreshTask;
    private MyScrollView _scrollView;
    private LinearLayout _llPowerControl;
    private DialogFragment_WriteValue _writeValue;
    private DialogFragment_ParamSetting _paramSetting;
    private DialogFragment_OTA _ota;
    //是否连接成功、是否打开参数设置界面
    private boolean _connectedSuccess = false, _isOpenParamSetting = false;
    private Map<String, UUID> _uuidMap;
    private ProgressDialogUtil.Listener _progressDialogUtilListener;
    private DialogFragmentListener _dialogFragmentListener;
    private FragmentManager _fragmentManager;
    private MyHandler _myHandler;

    static class MyHandler extends Handler {
        WeakReference<PowerControl> _weakReference;
        PowerControl _powerControl;

        public MyHandler(PowerControl activity) {
            _weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            if (_weakReference.get() == null) {
                return;
            }
            _powerControl = _weakReference.get();
            String result = (String) message.obj;
            switch (message.what) {
                case MESSAGE_ERROR_1:
                    _powerControl.DisConnect();
                    ProgressDialogUtil.ShowWarning(_powerControl, "警告", "该设备的连接已断开！");
                    break;
                //连接成功
                case MESSAGE_1: {
                    _powerControl._btn2.setEnabled(true);
                    _powerControl._btn3.setEnabled(true);
                    _powerControl._btn4.setEnabled(true);
                    _powerControl._refreshTimer = new Timer();
                    //综合测试
                    _powerControl._refreshTask = new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                BluetoothUtil.SendComm(BASEINFO_COMM);
                                Log.i(TAG, "发送'读取设备基本信息'指令：" + BASEINFO_COMM);
                                Thread.sleep(100);
                                BluetoothUtil.SendComm(LOCATION_COMM);
                                Log.i(TAG, "发送'GPS位置'指令：" + LOCATION_COMM);
                                Thread.sleep(100);
                                BluetoothUtil.SendComm(TESTA);
                                Log.i(TAG, "发送'综合测试'指令：" + TESTA);
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    //任务、延迟执行时间、重复调用间隔，Timer和TimerTask在调用cancel()取消后不能再执行schedule语句
                    _powerControl._refreshTimer.schedule(_powerControl._refreshTask, 0, 2 * 1000);
                    _powerControl._connectedSuccess = true;
                }
                break;
                //设备基本信息
                case RECEIVE_BASEINFO: {
                    String[] strArray = result.split(" ");
                    String versionStr = ConvertUtil.HexStrToStr((strArray[10] + strArray[11] + strArray[12] + strArray[13]).trim());
                    versionStr = ConvertUtil.StrAddCharacter(versionStr, ".");
                    String voltageStr1 = String.valueOf(Integer.valueOf(strArray[14], 16));
                    //不足两位补齐，比如0->0、1->01
                    if (voltageStr1.length() == 1)
                        voltageStr1 = "0" + voltageStr1;
                    String voltageStr2 = String.valueOf(Integer.valueOf(strArray[15], 16));
                    if (voltageStr2.length() == 1)
                        voltageStr2 = "0" + voltageStr2;
                    double voltage = Double.valueOf(voltageStr1 + voltageStr2) / 1000;
                    double temperature = 23.0;
                    try {
                        temperature = 23 + Double.valueOf(strArray[16]) / 2;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    _powerControl._txtVersion.setText(versionStr);
                    _powerControl._txt5.setText(voltage + "V");
                    _powerControl._txt6.setText(temperature + "℃");
                }
                break;
                //设备位置信息
                case RECEIVE_LOCATION: {
                    String[] strArray = result.split(" ");
                    int state = Integer.parseInt(strArray[10], 16);
                    if (state != 1) {
                        _powerControl._txt7.setText("定位失败");
                        return;
                    }
                    String latStr = strArray[11] + strArray[12] + strArray[13] + strArray[14];
                    double latNum = Double.valueOf(Integer.valueOf(latStr, 16)) / 1000000;
                    int len = Integer.parseInt(strArray[1], 16);
                    String lotStr = strArray[15] + strArray[16] + strArray[17] + strArray[2];
                    double lotNum = Double.valueOf(Integer.valueOf(lotStr, 16)) / 1000000;
                    //                    String heightStr = strArray[3] + strArray[4];
                    String heightStr = strArray[3];
                    int height = Integer.parseInt(heightStr, 16);
                    _powerControl._txt7.setText("经度" + latNum + "纬度" + lotNum + "高度" + height);
                }
                break;
                //综合测试
                case RECEIVE_TESTA: {
                    String strArray[] = result.split(",");
                    String doorState1 = strArray[0];
                    if (doorState1.equalsIgnoreCase("1")) {
                        _powerControl._txt1.setText("已开");
                        _powerControl._txt1.setTextColor(Color.GREEN);
                    } else {
                        _powerControl._txt1.setText("已关");
                        _powerControl._txt1.setTextColor(Color.RED);
                    }
                    String lockState1 = strArray[1];
                    if (lockState1.equalsIgnoreCase("1")) {
                        _powerControl._txt2.setText("已开");
                        _powerControl._txt2.setTextColor(Color.GREEN);
                    } else {
                        _powerControl._txt2.setText("已关");
                        _powerControl._txt2.setTextColor(Color.RED);
                    }
                    String doorState2 = strArray[2];
                    if (doorState2.equalsIgnoreCase("1")) {
                        _powerControl._txt3.setText("已开");
                        _powerControl._txt3.setTextColor(Color.GREEN);
                    } else {
                        _powerControl._txt3.setText("已关");
                        _powerControl._txt3.setTextColor(Color.RED);
                    }
                    String lockState2 = strArray[3];
                    if (lockState2.equalsIgnoreCase("1")) {
                        _powerControl._txt4.setText("已开");
                        _powerControl._txt4.setTextColor(Color.GREEN);
                    } else {
                        _powerControl._txt4.setText("已关");
                        _powerControl._txt4.setTextColor(Color.RED);
                    }
                }
                break;
                //一号门锁
                case RECEIVE_OPENDOORS1:
                    break;
                //二号门锁
                case RECEIVE_OPENDOORS2: {
                    byte[] bytes = ConvertUtil.HexStrToByteArray(result);
                    String bitStr = ConvertUtil.ByteToBit(bytes[0]);
                    String doorState2 = String.valueOf(bitStr.charAt(7));
                    if (doorState2.equalsIgnoreCase("1"))
                        _powerControl._txt3.setText("已开");
                    else
                        _powerControl._txt3.setText("已关");
                    String lockState2 = String.valueOf(bitStr.charAt(6));
                    if (lockState2.equalsIgnoreCase("1"))
                        _powerControl._txt4.setText("已开");
                    else
                        _powerControl._txt4.setText("已关");
                }
                break;
                //全部门锁
                case RECEIVE_OPENALLDOORS: {
                    byte[] bytes = ConvertUtil.HexStrToByteArray(result);
                    String bitStr = ConvertUtil.ByteToBit(bytes[0]);
                    String doorState1 = String.valueOf(bitStr.charAt(7));
                    if (doorState1.equalsIgnoreCase("1"))
                        _powerControl._txt1.setText("已开");
                    else
                        _powerControl._txt1.setText("已关");
                    String lockState1 = String.valueOf(bitStr.charAt(6));
                    if (lockState1.equalsIgnoreCase("1"))
                        _powerControl._txt2.setText("已开");
                    else
                        _powerControl._txt2.setText("已关");
                    String doorState2 = String.valueOf(bitStr.charAt(5));
                    if (doorState2.equalsIgnoreCase("1"))
                        _powerControl._txt3.setText("已开");
                    else
                        _powerControl._txt3.setText("已关");
                    String lockState2 = String.valueOf(bitStr.charAt(4));
                    if (lockState2.equalsIgnoreCase("1"))
                        _powerControl._txt4.setText("已开");
                    else
                        _powerControl._txt4.setText("已关");
                }
                break;
                //解析查询到的内部控制参数
                case RECEIVE_SEARCH_CONTROLPARAM: {
                    byte[] bytes = ConvertUtil.HexStrToByteArray(result);
                    String bitStr = ConvertUtil.ByteToBit(bytes[0]);
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
                    Log.
                            i(TAG, String.format("收到查询到的参数(Bit)：\n门检测开关(关门开路)%s\n锁检测开关(锁上开路)%s\n正常开锁不告警%s\n有外电可以进入维护方式%s\n启用软关机%s\n不检测强磁%s\n使用低磁检测阀值%s\n启用DEBUG软串口%s", bitStr1, bitStr2, bitStr3, bitStr4, bitStr5, bitStr6, bitStr7, bitStr8));
                    //打开控制参数修改界面的时候将查询结果传递过去，此时可以不输出调试信息
                    if (_powerControl._isOpenParamSetting) {
                        if (_powerControl._paramSetting == null) {
                            _powerControl._paramSetting = DialogFragment_ParamSetting.newInstance(new String[]{bitStr1, bitStr2, bitStr3, bitStr4,
                                                                                                               bitStr5, bitStr6, bitStr7,
                                                                                                               bitStr8}, _powerControl._dialogFragmentListener);
                            _powerControl._paramSetting.setCancelable(false);
                        }
                        _powerControl._paramSetting.show(_powerControl._fragmentManager, "DialogFragment_ParamSetting");
                        _powerControl.
                                _isOpenParamSetting = false;
                    } else {
                        if (bitStr8.equalsIgnoreCase("1")) {
                            _powerControl._debugView.append("\n收到：\n启用DEBUG软串口【启用】\n");
                        } else {
                            _powerControl._debugView.append("\n收到：\n启用DEBUG软串口【禁用】\n");
                        }
                        if (bitStr7.equalsIgnoreCase("1")) {
                            _powerControl._debugView.append("使用低磁检测阀值【启用】\n");
                        } else {
                            _powerControl._debugView.append("使用低磁检测阀值【禁用】\n");
                        }
                        if (bitStr6.equalsIgnoreCase("1")) {
                            _powerControl._debugView.append("不检测强磁【启用】\n");
                        } else {
                            _powerControl._debugView.append("不检测强磁【禁用】\n");
                        }
                        if (bitStr5.equalsIgnoreCase("1")) {
                            _powerControl._debugView.append("启用软关机【启用】\n");
                        } else {
                            _powerControl._debugView.append("启用软关机【禁用】\n");
                        }
                        if (bitStr4.equalsIgnoreCase("1")) {
                            _powerControl._debugView.append("有外电可以进入维护方式【启用】\n");
                        } else {
                            _powerControl._debugView.append("有外电可以进入维护方式【禁用】\n");
                        }
                        if (bitStr3.equalsIgnoreCase("1")) {
                            _powerControl._debugView.append("正常开锁不告警【启用】\n");
                        } else {
                            _powerControl._debugView.append("正常开锁不告警【禁用】\n");
                        }
                        if (bitStr2.equalsIgnoreCase("1")) {
                            _powerControl._debugView.append("锁检测开关(锁上开路)【启用】\n");
                        } else {
                            _powerControl._debugView.append("锁检测开关(锁上开路)【禁用】\n");
                        }
                        if (bitStr1.equalsIgnoreCase("1")) {
                            _powerControl._debugView.append("门检测开关(关门开路)【启用】\n");
                        } else {
                            _powerControl._debugView.append("门检测开关(关门开路)【禁用】\n");
                        }
                    }
                    //定位到最后一行
                    int offset = _powerControl._debugView.getLineCount() * _powerControl._debugView.getLineHeight();
                    //如果文本的高度大于ScrollView的，就自动滑动
                    if (offset > _powerControl._debugView.getHeight()) {
                        _powerControl._debugView.scrollTo(0, offset - _powerControl._debugView.getHeight());
                    }
                }
                break;
                //修改内部控制参数
                case SEND_SET_CONTROLPARAM: {
                    Log.i(TAG, "发送参数设置：" + result);
                    BluetoothUtil.SendComm(result);
                    _powerControl._debugView.append("发送参数设置指令 ");
                    int offset = _powerControl._debugView.getLineCount() * _powerControl._debugView.getLineHeight();
                    if (offset > _powerControl._scrollView.getHeight()) {
                        _powerControl._debugView.scrollTo(0, offset - _powerControl._scrollView.getHeight());
                    }
                }
                break;
                //发送查询内部控制参数的指令
                case SEND_SEARCH_CONTROLPARAM: {
                    BluetoothUtil.SendComm(SEARCH_CONTROLPARAM_COMM);
                }
                break;
            }
        }
    }

    private void InitListener() {
        _progressDialogUtilListener = new ProgressDialogUtil.Listener() {
            @Override
            public void OnDismiss() {
                if (_btn1.getText().toString().equals("断开") && !_connectedSuccess) {
                    _btn1.setText("连接");
                    DisConnect();
                }
            }

            @Override
            public void OnConfirm() {
                _btn1.setText("连接");
                DisConnect();
                Intent intent = GetAppOpenIntentByPackageName(PowerControl.this, "com.ambiqmicro.android.amota");
                startActivity(intent);
            }

            @Override
            public void OnCancel() {
            }
        };
        _dialogFragmentListener = new DialogFragmentListener() {
            @Override
            public void OnDismiss(String tag) {

            }

            @Override
            public void OnComfirm(String tag, String str) {
                Message message = _myHandler.obtainMessage(SEND_SET_CONTROLPARAM, str);
                _myHandler.sendMessage(message);
                //发送内部参数以后关闭设置窗口
                _paramSetting.dismiss();
                _paramSetting = null;
            }
        };
    }

    /**
     * 根据包名启动APK
     *
     * @param context
     * @param packageName
     * @return
     */
    private Intent GetAppOpenIntentByPackageName(Context context, String packageName) {
        String mainAct = null;
        PackageManager pkgMag = context.getPackageManager();
        //ACTION_MAIN是隐藏启动的action， 你也可以自定义
        Intent intent = new Intent(Intent.ACTION_MAIN);
        //CATEGORY_LAUNCHER有了这个，你的程序就会出现在桌面上
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        //FLAG_ACTIVITY_RESET_TASK_IF_NEEDED 按需启动的关键，如果任务队列中已经存在，则重建程序
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

        @SuppressLint("WrongConstant") List<ResolveInfo> list = pkgMag.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo.packageName.equals(packageName)) {
                mainAct = info.activityInfo.name;
                break;
            }
        }
        if (TextUtils.isEmpty(mainAct))
            return null;
        intent.setComponent(new ComponentName(packageName, mainAct));
        return intent;
    }

    /**
     * 解析硬件返回的数据
     *
     * @param data
     */
    private void Resolve(String data) {
        String[] strArray = data.split(" ");
        Message message = new Message();
        /*
         * 特殊处理：设备基本信息的通信协议和之前的协议不一样，需要留意
         *
         * */
        if (strArray[8].equals("A1") && strArray.length == 19) {
            message.what = RECEIVE_BASEINFO;
            message.obj = data;
        }
        /*
         * 特殊处理：GPS位置信息的通信协议和之前的协议不一样，需要留意
         *
         * */
        else if (strArray[8].equals("A2") && strArray.length == 20) {
            message.what = RECEIVE_LOCATION;
            message.obj = data;
        } else {
            String indexStr = strArray[12];
            switch (indexStr) {
                //全部门锁状态
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
                    message.what = RECEIVE_TESTA;
                    message.
                            obj = doorState1 + "," + lockState1 + "," + doorState2 + "," + lockState2 + "," + battery + "," + magneticDown + "," + magneticUp + "," + magneticBefore;
                }
                break;
                //开一号门锁
                case "81": {
                    message.what = RECEIVE_OPENDOORS1;
                    message.obj = "";
                }
                break;
                //开二号门锁
                case "82": {
                    message.what = RECEIVE_OPENDOORS2;
                    message.obj = strArray[13];
                }
                break;
                //开全部门锁
                case "83": {
                    message.what = RECEIVE_OPENALLDOORS;
                    message.obj = strArray[13];
                }
                break;
                //查询内部控制参数
                case "86": {
                    message.what = RECEIVE_SEARCH_CONTROLPARAM;
                    message.obj = strArray[13];
                }
                break;
                //修改内部控制参数
                case "87": {
                    //先查询内部控制参数
                    message.what = SEND_SEARCH_CONTROLPARAM;
                    message.obj = "";
                }
                break;
            }
        }
        _myHandler.sendMessage(message);
    }

    private void DisConnect() {
        _connectedSuccess = false;
        _btn2.setEnabled(false);
        _btn3.setEnabled(false);
        _btn4.setEnabled(false);
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
        _txt4.setText("Null");
        _txt4.setTextColor(Color.GRAY);
        _txt5.setText("Null");
        _txt6.setText("Null");
        _txt7.setText("Null");
        _txtVersion.setText("Null");
        _txtVersion.setTextColor(Color.GRAY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            this.finish();
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (_connectedSuccess) {
            switch (item.getItemId()) {
                //内部控制参数设置
                case R.id.menu_1_power: {
                    //先查询内部控制参数，再打开修改参数的界面
                    BluetoothUtil.SendComm(SEARCH_CONTROLPARAM_COMM);
                    _isOpenParamSetting = true;
                }
                break;
                //写入指令
                case R.id.menu_2_power: {
                    _writeValue = new DialogFragment_WriteValue();
                    _writeValue.setCancelable(false);
                    _writeValue.show(_fragmentManager, "DialogFragment_WriteValue");
                }
                break;
                //文件传输
                case R.id.menu_3_power: {
                    _ota = DialogFragment_OTA.newInstance(_bluetoothDevice);
                    _ota.setCancelable(false);
                    _ota.show(_fragmentManager, "DialogFragment_OTA");
                }
                break;
                //OTA升级
                case R.id.menu_4_power: {
                    Intent intent = GetAppOpenIntentByPackageName(PowerControl.this, "com.ambiqmicro.android.amota");
                    if (intent != null) {
                        ProgressDialogUtil.ShowConfirm(PowerControl.this, "提示", "使用OTA升级功能会关闭当前与设备的连接");
                    } else {
                        ProgressDialogUtil.ShowWarning(PowerControl.this, "提示", "未安装OTA_ZM301，无法使用该功能！");
                    }
                }
                break;
            }
        } else {
            ProgressDialogUtil.ShowWarning(PowerControl.this, "提示", "请连接蓝牙设备！");
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.powercontrol_menu_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_return: {
                ProgressDialogUtil.Dismiss();
                this.finish();
            }
            break;
            //连接
            case R.id.button1: {
                if (_bluetoothDevice != null) {
                    if (_btn1.getText().toString().equals("连接")) {
                        _btn1.setText("断开");
                        Log.i(TAG, "开始连接...");
                        BluetoothUtil.ConnectDevice(_bluetoothDevice, _uuidMap);
                    } else {
                        _btn1.setText("连接");
                        DisConnect();
                    }
                } else {
                    ProgressDialogUtil.ShowWarning(PowerControl.this, "提示", "未获取到蓝牙，请重试！");
                }
            }
            break;
            //开一号门锁
            case R.id.button2: {
                Log.i(TAG, "发送开一号门锁：" + OPENDOOR1_COMM);
                BluetoothUtil.SendComm(OPENDOOR1_COMM);
            }
            break;
            //开二号门锁
            case R.id.button3: {
                Log.i(TAG, "发送开二号门锁：" + OPENDOOR2_COMM);
                BluetoothUtil.SendComm(OPENDOOR2_COMM);
            }
            break;
            //开全部门锁
            case R.id.button4: {
                Log.i(TAG, "发送开全部门锁：" + OPENDOORS_COMM);
                BluetoothUtil.SendComm(OPENDOORS_COMM);
            }
            break;
            //清屏
            case R.id.btnClear:
                _debugView.setText("");
                break;
        }

    }

    @Override
    public void OnConnected() {
        ProgressDialogUtil.Dismiss();
        Log.i(TAG, "成功建立连接！");
        Message message = _myHandler.obtainMessage(MESSAGE_1, "");
        _myHandler.sendMessage(message);
        //返回时告知该设备已成功连接
        setResult(2, new Intent());
    }

    @Override
    public void OnConnecting() {
        ProgressDialogUtil.ShowProgressDialog(PowerControl.this, true, _progressDialogUtilListener, "正在连接...");
    }

    @Override
    public void OnDisConnected() {
        Log.i(TAG, "连接已断开！");
        Message message = _myHandler.obtainMessage(MESSAGE_ERROR_1, "");
        _myHandler.sendMessage(message);
    }

    @Override
    public void OnWriteSuccess(byte[] byteArray) {
        String result = ConvertUtil.ByteArrayToHexStr(byteArray);
        result = ConvertUtil.HexStrAddCharacter(result, " ");
        String[] strArray = result.split(" ");
        String indexStr = strArray[11];
        String sendResult = "";
        switch (indexStr) {
            //发送综合测量指令
            case "80":
                sendResult = "综合测试A";
                break;
            //发送开一号门锁指令
            case "81":
                sendResult = "开一号门锁";
                break;
            //发送开二号门锁指令
            case "82":
                sendResult = "开二号门锁";
                break;
            //发送开全部门锁指令
            case "83":
                sendResult = "开全部门锁";
                break;
        }
    }

    @Override
    public void OnReadSuccess(byte[] byteArray) {
        String result = ConvertUtil.ByteArrayToHexStr(byteArray);
        result = ConvertUtil.HexStrAddCharacter(result, " ");
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

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _myHandler = new MyHandler(this);
        setContentView(R.layout.activity_power_control);
        _fragmentManager = getSupportFragmentManager();
        Intent intent = getIntent();
        _bluetoothDevice = intent.getParcelableExtra(ARG_PARAM1);
        _uuidMap = (Map<String, UUID>) intent.getSerializableExtra(ARG_PARAM2);
        //Toolbar
        _toolbar = findViewById(R.id.toolbar_powercontrol);
        _toolbar.setTitle("");
        setSupportActionBar(_toolbar);
        _txt1 = findViewById(R.id.txt1);
        _txt2 = findViewById(R.id.txt2);
        _txt3 = findViewById(R.id.txt3);
        _txt4 = findViewById(R.id.txt4);
        _txt5 = findViewById(R.id.txt5);
        _txt6 = findViewById(R.id.txt6);
        _txt7 = findViewById(R.id.txt7);
        _txtVersion = findViewById(R.id.txtVersion);
        _debugView = findViewById(R.id.debug_view);
        _btnReturn = findViewById(R.id.btn_return);
        _btn1 = findViewById(R.id.button1);
        _btn2 = findViewById(R.id.button2);
        _btn3 = findViewById(R.id.button3);
        _btn4 = findViewById(R.id.button4);
        _btnClear = findViewById(R.id.btnClear);
        _scrollView = findViewById(R.id.scrollView);
        _llPowerControl = findViewById(R.id.fragment_bluetooth_powercontrol);
        _btnReturn.setOnClickListener(this::onClick);
        _btnClear.setOnClickListener(this::onClick);
        _btn1.setOnClickListener(this::onClick);
        _btn2.setOnClickListener(this::onClick);
        _btn3.setOnClickListener(this::onClick);
        _btn4.setOnClickListener(this::onClick);
        _debugView.setMovementMethod(ScrollingMovementMethod.getInstance());
        InitListener();
        BluetoothUtil.Init(PowerControl.this, this);
    }

    @Override
    public void onDestroy() {
        ProgressDialogUtil.Dismiss();
        if (_refreshTimer != null)
            _refreshTimer.cancel();
        if (_refreshTask != null)
            _refreshTask.cancel();
        BluetoothUtil.DisConnGatt();
        _bluetoothDevice = null;
        super.onDestroy();
    }

}
