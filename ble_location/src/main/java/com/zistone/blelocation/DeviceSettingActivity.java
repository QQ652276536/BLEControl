package com.zistone.blelocation;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.zistone.libble.util.MyBleMessageListener;
import com.zistone.libble.util.MyBleUtil;
import com.zistone.libble.util.MyConvertUtil;
import com.zistone.libble.util.MyProgressDialogUtil;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceSettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DeviceSettingActivity";
    private static int SPRING_GREEN = Color.parseColor("#3CB371");
    //关机
    private static final String CMD_25 = "680000000000006825045AA555AAE316";
    //查询系统控制参数（信号灯）
    private static final String CMD_27_LIGHT = "6800000000000068270100E316";
    //查询系统控制参数（基站定位）
    private static final String CMD_27_BASE = "6800000000000068270101E316";
    //查询系统控制参数（蓝牙控制）
    private static final String CMD_27_BLE = "6800000000000068270102E316";
    //查询系统控制参数（调试开关）
    private static final String CMD_27_DEBUG = "6800000000000068270103E316";
    //设置/查询IP地址及端口
    private static final String CMD_30 = "680000000000006830000000E316";
    //基站位置上报事件间隔及每次工作的最长时间(秒)
    private static final String CMD_35 = "680000000000006835000000E316";
    //每日上报的起始时间
    private static final String CMD_36 = "680000000000006836000000E316";
    //传感器的采样事件间隔及唤醒下的采样次数
    private static final String CMD_37 = "680000000000006837000000E316";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Button _btnIpPort, _btnUp, _btnUpStart, _btnCollect, _btnReStart, _btnOff;
    private Switch _swLight, _swBase, _swBle, _swDebug;
    private ImageButton _btnReturn, _btnTop, _btnBottom, _btnClear;
    private TextView _txtDebug;
    private EditText _edtIp, _edtPort, _edtUpInterval, _edtUpWorkTime, _edtUpStartHourTime, _edtUpStartMinuteTime, _edtUpStartSecondTime,
            _edtCollectInterval, _edtCollectCount;
    private BluetoothDevice _bluetoothDevice;
    private Map<String, UUID> _uuidMap;
    private Toolbar _toolbar;
    private MyHandler _myHandler;
    private LinearLayout _llDebug;
    private MyBleMessageListener _messageListener;
    //防止因为查询结果设置控件初始状态时触发注册的事件
    private boolean _isFirstChanged = true;

    private static class MyHandler extends Handler {

        WeakReference<DeviceSettingActivity> _weakReference;
        DeviceSettingActivity _deviceSettingActivity;

        public MyHandler(DeviceSettingActivity activity) {
            _weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            if (_weakReference.get() == null)
                return;
            try {
                _deviceSettingActivity = _weakReference.get();
                if (message.what == -99) {
                    _deviceSettingActivity._txtDebug.append(message.obj + "\n");
                } else {
                    String[] strArray = (String[]) message.obj;
                    String cmdStr = Arrays.toString(strArray).replaceAll("[\\s|\\[|\\]|,]", "");
                    switch (message.what) {
                        //系统控制参数
                        case 27: {
                            cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                            String logStr = "";
                            int index = Integer.parseInt(strArray[10], 16);
                            int state = Integer.parseInt(strArray[11], 16);
                            switch (index) {
                                //信号灯
                                case 0:
                                    logStr = "系统控制参数\n" + cmdStr + "信号灯：" + (state == 1 ? "开启" : "关闭") + "\n";
                                    if (state == 1)
                                        _deviceSettingActivity._swLight.setChecked(true);
                                    else
                                        _deviceSettingActivity._swLight.setChecked(false);
                                    break;
                                //基站定位
                                case 1:
                                    logStr = "系统控制参数\n" + cmdStr + "基站定位：" + (state == 1 ? "开启" : "关闭") + "\n";
                                    if (state == 1)
                                        _deviceSettingActivity._swBase.setChecked(true);
                                    else
                                        _deviceSettingActivity._swBase.setChecked(false);
                                    break;
                                //蓝牙控制
                                case 2:
                                    logStr = "系统控制参数\n" + cmdStr + "蓝牙控制：" + (state == 1 ? "开启" : "关闭") + "\n";
                                    if (state == 1)
                                        _deviceSettingActivity._swBle.setChecked(true);
                                    else
                                        _deviceSettingActivity._swBle.setChecked(false);
                                    break;
                                //调试开关
                                case 3:
                                    logStr = "系统控制参数\n" + cmdStr + "调试开关：" + (state == 1 ? "开启" : "关闭") + "\n";
                                    if (state == 1)
                                        _deviceSettingActivity._swDebug.setChecked(true);
                                    else
                                        _deviceSettingActivity._swDebug.setChecked(false);
                                    //查询系统参数设置完毕再改变状态
                                    _deviceSettingActivity._isFirstChanged = false;
                                    break;
                            }
                            Log.i(TAG, logStr);
                            _deviceSettingActivity._txtDebug.append(logStr);
                        }
                        break;
                        //IP地址及端口
                        case 30: {
                            int ip1 = Integer.valueOf(strArray[10], 16);
                            int ip2 = Integer.valueOf(strArray[11], 16);
                            int ip3 = Integer.valueOf(strArray[12], 16);
                            int ip4 = Integer.valueOf(strArray[13], 16);
                            int port = Integer.valueOf(strArray[14] + strArray[15], 16);
                            _deviceSettingActivity._edtIp.setText(ip1 + "." + ip2 + "." + ip3 + "." + ip4);
                            _deviceSettingActivity._edtPort.setText(port + "");
                            cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                            String logStr = "IP地址及端口\n" + cmdStr + "IP：" + ip1 + "." + ip2 + "." + ip3 + "." + ip4 + "，端口：" + port + "\n";
                            Log.i(TAG, logStr);
                            _deviceSettingActivity._txtDebug.append(logStr);
                        }
                        break;
                        //上报事件间隔及时长
                        case 35: {
                            String intervalStr = strArray[10] + strArray[11] + strArray[12] + strArray[13];
                            int interval = Integer.parseInt(intervalStr, 16);
                            String timeStr = strArray[14] + strArray[15];
                            int time = Integer.parseInt(timeStr, 16);
                            _deviceSettingActivity._edtUpInterval.setText(interval + "");
                            _deviceSettingActivity._edtUpWorkTime.setText(time + "");
                            cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                            String logStr = "上报事件间隔及时长\n" + cmdStr + "上报间隔：" + interval + "，工作时长：" + time + "\n";
                            Log.i(TAG, logStr);
                            _deviceSettingActivity._txtDebug.append(logStr);
                        }
                        break;
                        //每日上报起始时间
                        case 36: {
                            int hour = Integer.parseInt(strArray[10]);
                            int minute = Integer.parseInt(strArray[11]);
                            int second = Integer.parseInt(strArray[12]);
                            _deviceSettingActivity._edtUpStartHourTime.setText(hour + "");
                            _deviceSettingActivity._edtUpStartMinuteTime.setText(minute + "");
                            _deviceSettingActivity._edtUpStartSecondTime.setText(second + "");
                            cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                            String logStr = "每日上报起始时间\n" + cmdStr + "\n" + hour + "时" + minute + "分" + second + "秒\n";
                            Log.i(TAG, logStr);
                            _deviceSettingActivity._txtDebug.append(logStr);
                        }
                        break;
                        //传感器采样间隔及次数
                        case 37: {
                            int interval = Integer.parseInt(strArray[10] + strArray[11], 16);
                            int count = Integer.parseInt(strArray[12] + strArray[13], 16);
                            _deviceSettingActivity._edtCollectInterval.setText(interval + "");
                            _deviceSettingActivity._edtCollectCount.setText(count + "");
                            cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                            String logStr = "传感器采样间隔及次数\n" + cmdStr + "采样间隔：" + interval + "，次数：" + count + "\n";
                            Log.i(TAG, logStr);
                            _deviceSettingActivity._txtDebug.append(logStr);
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                _deviceSettingActivity._txtDebug.append("\n" + e.toString() + "\n");
            } finally {
                int offset = _deviceSettingActivity._txtDebug.getLineCount() * _deviceSettingActivity._txtDebug.getLineHeight();
                if (offset > _deviceSettingActivity._txtDebug.getHeight()) {
                    _deviceSettingActivity._txtDebug.scrollTo(0, offset - _deviceSettingActivity._txtDebug.getHeight());
                }
            }
        }

    }

    private void Task() {
        Log.i(TAG, "依次执行查询指令...");
        try {
            MyBleUtil.SendComm(CMD_27_LIGHT);
            Thread.sleep(100);
            MyBleUtil.SendComm(CMD_27_BASE);
            Thread.sleep(100);
            MyBleUtil.SendComm(CMD_27_BLE);
            Thread.sleep(100);
            MyBleUtil.SendComm(CMD_27_DEBUG);
            Thread.sleep(100);
            MyBleUtil.SendComm(CMD_30);
            Thread.sleep(100);
            MyBleUtil.SendComm(CMD_35);
            Thread.sleep(100);
            MyBleUtil.SendComm(CMD_36);
            Thread.sleep(100);
            MyBleUtil.SendComm(CMD_37);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void InitListener() {
        _messageListener = new MyBleMessageListener() {
            @Override
            public void OnWriteSuccess(byte[] byteArray) {
            }

            @Override
            public void OnReadSuccess(byte[] byteArray) {
                String result = MyConvertUtil.ByteArrayToHexStr(byteArray);
                result = MyConvertUtil.StrAddCharacter(result, 2, " ");
                Log.i(TAG, "收到：" + result);
                String[] strArray = result.split(" ");
                //目前最短的指令为14位
                if (strArray.length < 11) {
                    String logStr = "收到" + result + "，长度" + strArray.length + "错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, logStr + "\n").sendToTarget();
                    return;
                }
                String type = strArray[8].toUpperCase();
                //版本信息及内部时间
                if ("A0".equals(type)) {
                    _myHandler.obtainMessage(20, strArray).sendToTarget();
                } else if ("E0".equals(type)) {
                    String logStr = "版本信息及内部时间响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, logStr + "\n").sendToTarget();
                }
                //电池电量
                else if ("A1".equals(type)) {
                    _myHandler.obtainMessage(21, strArray).sendToTarget();
                } else if ("E1".equals(type)) {
                    String logStr = "电池电量响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, logStr + "\n").sendToTarget();
                }
                //状态及连接的标签数量
                else if ("A2".equals(type)) {
                    _myHandler.obtainMessage(22, strArray).sendToTarget();
                } else if ("E2".equals(type)) {
                    String logStr = "状态及连接的标签数量响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, logStr + "\n").sendToTarget();
                }
                //设置蓝牙工作模式
                else if ("A7".equals(type)) {
                    _myHandler.obtainMessage(27, strArray).sendToTarget();
                } else if ("E7".equals(type)) {
                    String logStr = "设置蓝牙工作模式响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, logStr + "\n").sendToTarget();
                }
                //是否开启基站定位
                else if ("A8".equals(type)) {
                    _myHandler.obtainMessage(28, strArray).sendToTarget();
                } else if ("E8".equals(type)) {
                    String logStr = "是否开启基站定位响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, logStr + "\n").sendToTarget();
                }
                //IP地址及端口
                else if ("B0".equals(type)) {
                    _myHandler.obtainMessage(30, strArray).sendToTarget();
                } else if ("F0".equals(type)) {
                    String logStr = "IP位置及端口响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, logStr + "\n").sendToTarget();
                }
                //上报事件间隔及时长
                else if ("B5".equals(type)) {
                    _myHandler.obtainMessage(35, strArray).sendToTarget();
                } else if ("F5".equals(type)) {
                    String logStr = "上报事件间隔及时长响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, logStr + "\n").sendToTarget();
                }
                //每日上报起始时间
                else if ("B6".equals(type)) {
                    _myHandler.obtainMessage(36, strArray).sendToTarget();
                } else if ("F6".equals(type)) {
                    String logStr = "每日上报起始时间响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, logStr + "\n").sendToTarget();
                }
                //传感器采样间隔及次数
                else if ("B7".equals(type)) {
                    _myHandler.obtainMessage(37, strArray).sendToTarget();
                } else if ("F7".equals(type)) {
                    String logStr = "传感器采样间隔及次数响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, logStr + "\n").sendToTarget();
                }
            }
        };
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            //是点击的输入框区域，则需要显示键盘，同时显示光标，反之，需要隐藏键盘、光标
            if (ShouldHideInput(v, ev)) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (null != inputMethodManager) {
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    _edtIp.clearFocus();
                    _edtPort.clearFocus();
                    _edtCollectCount.clearFocus();
                    _edtCollectInterval.clearFocus();
                    _edtUpInterval.clearFocus();
                    _edtUpStartHourTime.clearFocus();
                    _edtUpStartMinuteTime.clearFocus();
                    _edtUpStartSecondTime.clearFocus();
                    _edtUpWorkTime.clearFocus();
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        //必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    /**
     * 显示/隐藏键盘
     *
     * @param v
     * @param event
     * @return
     */
    public boolean ShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.item1_menu:
                if (item.getTitle().equals("打开调试")) {
                    _llDebug.setVisibility(View.VISIBLE);
                    item.setTitle("关闭调试");
                } else {
                    item.setTitle("打开调试");
                    _llDebug.setVisibility(View.INVISIBLE);
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_setting_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 销毁时断开只在连接成功的首页面断开
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btn_top_setting:
                    _txtDebug.scrollTo(0, 0);
                    break;
                case R.id.btn_bottom_setting:
                    int offset = _txtDebug.getLineCount() * _txtDebug.getLineHeight();
                    if (offset > _txtDebug.getHeight()) {
                        _txtDebug.scrollTo(0, offset - _txtDebug.getHeight());
                    }
                    break;
                case R.id.btn_clear_setting:
                    _txtDebug.setText("");
                    break;
                case R.id.btn_return_setting:
                    finish();
                    break;
                //设置IP及端口
                case R.id.btn_ip_port_setting: {
                    String ipStr = _edtIp.getText().toString();
                    Pattern pattern = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
                    Matcher matcher = pattern.matcher(ipStr);
                    if (!matcher.matches()) {
                        Toast.makeText(this, "请输入正确的IP地址", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String portStr = _edtPort.getText().toString();
                    pattern = Pattern.compile("\\d{1,5}");
                    matcher = pattern.matcher(portStr);
                    if (!matcher.matches()) {
                        Toast.makeText(this, "请输入正确的端口号", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] ipArray = ipStr.split("\\.");
                    int ip1 = Integer.parseInt(ipArray[0]);
                    int ip2 = Integer.parseInt(ipArray[1]);
                    int ip3 = Integer.parseInt(ipArray[2]);
                    int ip4 = Integer.parseInt(ipArray[3]);
                    String hexIp1 = MyConvertUtil.IntToHexStr(ip1);
                    hexIp1 = MyConvertUtil.AddZeroForNum(hexIp1, 2, true);
                    String hexIp2 = MyConvertUtil.IntToHexStr(ip2);
                    hexIp2 = MyConvertUtil.AddZeroForNum(hexIp2, 2, true);
                    String hexIp3 = MyConvertUtil.IntToHexStr(ip3);
                    hexIp3 = MyConvertUtil.AddZeroForNum(hexIp3, 2, true);
                    String hexIp4 = MyConvertUtil.IntToHexStr(ip4);
                    hexIp4 = MyConvertUtil.AddZeroForNum(hexIp4, 2, true);
                    int port = Integer.parseInt(portStr);
                    String hexPort = MyConvertUtil.IntToHexStr(port);
                    //如果不足2个字节补齐
                    hexPort = MyConvertUtil.AddZeroForNum(hexPort, 4, true);
                    String cmd = "68000000000000683006" + hexIp1 + hexIp2 + hexIp3 + hexIp4 + hexPort + "E316";
                    MyBleUtil.SendComm(cmd);
                    String logStr = "设置IP及端口：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ");
                    Log.i(TAG, logStr);
                    _txtDebug.append(logStr + "\n\n");
                    //查询更新
                    Thread.sleep(100);
                    MyBleUtil.SendComm(CMD_35);
                }
                break;
                //上报事件及时长
                case R.id.btn_up_setting: {
                    String intervalStr = _edtUpInterval.getText().toString();
                    String timeStr = _edtUpWorkTime.getText().toString();
                    Pattern pattern = Pattern.compile("^\\d{1,}");
                    Matcher matcher1 = pattern.matcher(intervalStr);
                    Matcher matcher2 = pattern.matcher(timeStr);
                    if (!matcher1.matches() || !matcher2.matches()) {
                        Toast.makeText(this, "请输入正确的上报事件参数", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int interval = Integer.parseInt(intervalStr);
                    int time = Integer.parseInt(timeStr);
                    String intervalHex = MyConvertUtil.AddZeroForNum(Integer.toHexString(interval), 8, true);
                    String timeHex = MyConvertUtil.AddZeroForNum(Integer.toHexString(time), 4, true);
                    String cmd = "68000000000000683506" + intervalHex + timeHex + "E316";
                    MyBleUtil.SendComm(cmd);
                    String logStr = "上报事件及时长：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ");
                    Log.i(TAG, logStr);
                    _txtDebug.append(logStr + "\n\n");
                    //查询更新
                    Thread.sleep(100);
                    MyBleUtil.SendComm(CMD_35);
                }
                break;
                //每日上报起始时间
                case R.id.btn_up_start_setting: {
                    Pattern pattern = Pattern.compile("^\\d{1,}");
                    String hourStr = _edtUpStartHourTime.getText().toString();
                    String minuteStr = _edtUpStartMinuteTime.getText().toString();
                    String secondStr = _edtUpStartSecondTime.getText().toString();
                    Matcher matcher1 = pattern.matcher(hourStr);
                    Matcher matcher2 = pattern.matcher(minuteStr);
                    Matcher matcher3 = pattern.matcher(secondStr);
                    if (!matcher1.matches() || !matcher2.matches() || !matcher3.matches()) {
                        Toast.makeText(this, "请输入正确的上报起始时间参数", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int hour = Integer.parseInt(hourStr);
                    int minute = Integer.parseInt(minuteStr);
                    int second = Integer.parseInt(secondStr);
                    if (hour < 0 || hour > 24 || minute < 0 || minute > 59 || second < 0 || second > 59) {
                        Toast.makeText(this, "请输入正确的上报起始时间参数", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //                    String hourHex = MyConvertUtil.AddZeroForNum(Integer.toHexString(hour), 2, true);
                    //                    String minuteHex = MyConvertUtil.AddZeroForNum(Integer.toHexString(minute), 2, true);
                    //                    String secondHex = MyConvertUtil.AddZeroForNum(Integer.toHexString(second), 2, true);
                    String hourHex = MyConvertUtil.AddZeroForNum(Integer.toString(hour), 2, true);
                    String minuteHex = MyConvertUtil.AddZeroForNum(Integer.toString(minute), 2, true);
                    String secondHex = MyConvertUtil.AddZeroForNum(Integer.toString(second), 2, true);
                    String cmd = "68000000000000683603" + hourHex + minuteHex + secondHex + "E316";
                    MyBleUtil.SendComm(cmd);
                    String logStr = "每日上报起始时间：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ");
                    Log.i(TAG, logStr);
                    _txtDebug.append(logStr + "\n\n");
                    //查询更新
                    Thread.sleep(100);
                    MyBleUtil.SendComm(CMD_36);
                }
                break;
                //传感器采样间隔及次数
                case R.id.btn_collect_setting: {
                    Pattern pattern = Pattern.compile("^\\d{1,}");
                    String intervalStr = _edtCollectInterval.getText().toString();
                    String countStr = _edtCollectCount.getText().toString();
                    Matcher matcher1 = pattern.matcher(intervalStr);
                    Matcher matcher2 = pattern.matcher(countStr);
                    if (!matcher1.matches() || !matcher2.matches()) {
                        Toast.makeText(this, "请输入正确的采样间隔参数", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int interval = Integer.parseInt(intervalStr);
                    int count = Integer.parseInt(countStr);
                    String intervalHex = MyConvertUtil.AddZeroForNum(Integer.toHexString(interval), 4, true);
                    String countHex = MyConvertUtil.AddZeroForNum(Integer.toHexString(count), 4, true);
                    String cmd = "68000000000000683704" + intervalHex + countHex + "E316";
                    MyBleUtil.SendComm(cmd);
                    String logStr = "传感器采样间隔及次数：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ");
                    Log.i(TAG, logStr);
                    _txtDebug.append(logStr + "\n\n");
                    //查询更新
                    Thread.sleep(100);
                    MyBleUtil.SendComm(CMD_37);
                }
                break;
                //关机
                case R.id.btn_off_setting: {
                    MyProgressDialogUtil.ShowConfirm(this, "是的", "不了", "提示", "确定要对该设备进行关机吗？", true, new MyProgressDialogUtil.ConfirmListener() {
                        @Override
                        public void OnConfirm() {
                            try {
                                MyBleUtil.SendComm(CMD_25);
                                Thread.sleep(1 * 1000);
                                MyProgressDialogUtil.DismissAlertDialog();
                                MyProgressDialogUtil.ShowWarning(DeviceSettingActivity.this, "好的", "提示", "该设备已关机", false, () -> {
                                    Intent intent = new Intent();
                                    intent.putExtra(ARG_PARAM1, "已执行关机指令");
                                    setResult(RESULT_OK, intent);
                                    finish();
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void OnConfirm(String str) {
                        }

                        @Override
                        public void OnCancel() {
                            MyProgressDialogUtil.DismissAlertDialog();
                        }
                    });
                }
                break;
                //重启
                case R.id.btn_restart_setting: {
                    MyProgressDialogUtil.ShowConfirm(this, "是的", "不了", "提示", "确定要对该设备进行重启吗？", true, new MyProgressDialogUtil.ConfirmListener() {
                        @Override
                        public void OnConfirm() {
                            try {
                                MyBleUtil.SendComm("6800000000000068250412345678E316");
                                Thread.sleep(1 * 1000);
                                MyProgressDialogUtil.DismissAlertDialog();
                                MyProgressDialogUtil.ShowWarning(DeviceSettingActivity.this, "好的", "提示", "该设备已关机", false, () -> {
                                    Intent intent = new Intent();
                                    intent.putExtra(ARG_PARAM1, "已执行关机指令");
                                    setResult(RESULT_OK, intent);
                                    finish();
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void OnConfirm(String str) {
                        }

                        @Override
                        public void OnCancel() {
                            MyProgressDialogUtil.DismissAlertDialog();
                        }
                    });
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            _txtDebug.append("\n" + e.toString() + "\n");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_setting);
        _myHandler = new MyHandler(this);
        Intent intent = getIntent();
        _bluetoothDevice = intent.getParcelableExtra(ARG_PARAM1);
        _uuidMap = (Map<String, UUID>) intent.getSerializableExtra(ARG_PARAM2);
        _llDebug = findViewById(R.id.ll_debug_setting);
        _llDebug.setVisibility(View.INVISIBLE);
        _btnReturn = findViewById(R.id.btn_return_setting);
        _btnReturn.setOnClickListener(this::onClick);
        _btnTop = findViewById(R.id.btn_top_setting);
        _btnTop.setOnClickListener(this::onClick);
        _btnBottom = findViewById(R.id.btn_bottom_setting);
        _btnBottom.setOnClickListener(this::onClick);
        _btnClear = findViewById(R.id.btn_clear_setting);
        _btnClear.setOnClickListener(this::onClick);
        //Toolbar
        _toolbar = findViewById(R.id.toolbar_setting);
        _toolbar.setTitle("");
        setSupportActionBar(_toolbar);
        _swLight = findViewById(R.id.sw_light_setting);
        _swLight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (_isFirstChanged)
                return;
            if (isChecked) {
                String cmd = "680000000000006827020001E316";
                String logStr = "设置信号灯开启：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ");
                Log.i(TAG, logStr);
                _txtDebug.append(logStr + "\n\n");
                MyBleUtil.SendComm(cmd);
            } else {
                String cmd = "680000000000006827020000E316";
                String logStr = "设置信号灯关闭：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ");
                Log.i(TAG, logStr);
                _txtDebug.append(logStr + "\n\n");
                MyBleUtil.SendComm(cmd);
            }
        });
        _swBase = findViewById(R.id.sw_base_setting);
        _swBase.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (_isFirstChanged)
                return;
            if (isChecked) {
                String cmd = "680000000000006827020101E316";
                String logStr = "设置基站定位开启：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ");
                Log.i(TAG, logStr);
                _txtDebug.append(logStr + "\n\n");
                MyBleUtil.SendComm(cmd);
            } else {
                String cmd = "680000000000006827020100E316";
                String logStr = "设置基站定位关闭：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ");
                Log.i(TAG, logStr);
                _txtDebug.append(logStr + "\n\n");
                MyBleUtil.SendComm(cmd);
            }
        });
        _swBle = findViewById(R.id.sw_ble_setting);
        _swBle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (_isFirstChanged)
                return;
            if (isChecked) {
                String cmd = "680000000000006827020201E316";
                String logStr = "设置蓝牙控制开启：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ");
                Log.i(TAG, logStr);
                _txtDebug.append(logStr + "\n\n");
                MyBleUtil.SendComm(cmd);
            } else {
                String cmd = "680000000000006827020200E316";
                String logStr = "设置蓝牙控制关闭：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ");
                Log.i(TAG, logStr);
                _txtDebug.append(logStr + "\n\n");
                MyBleUtil.SendComm(cmd);
            }
        });
        _swDebug = findViewById(R.id.sw_debug_setting);
        _swDebug.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (_isFirstChanged)
                return;
            if (isChecked) {
                String cmd = "680000000000006827020301E316";
                String logStr = "设置调试开关开启：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ");
                Log.i(TAG, logStr);
                _txtDebug.append(logStr + "\n\n");
                MyBleUtil.SendComm(cmd);
            } else {
                String cmd = "680000000000006827020300E316";
                String logStr = "设置调试开关关闭：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ");
                Log.i(TAG, logStr);
                _txtDebug.append(logStr + "\n\n");
                MyBleUtil.SendComm(cmd);
            }
        });
        _edtIp = findViewById(R.id.edt_ip_setting);
        _edtPort = findViewById(R.id.edt_port_setting);
        _edtUpInterval = findViewById(R.id.edt_up_interval_setting);
        _edtUpWorkTime = findViewById(R.id.edt_work_time_setting);
        _edtUpStartHourTime = findViewById(R.id.edt_hour_up_time_setting);
        _edtUpStartMinuteTime = findViewById(R.id.edt_minute_up_time_setting);
        _edtUpStartSecondTime = findViewById(R.id.edt_second_up_time_setting);
        _edtCollectInterval = findViewById(R.id.edt_collect_interval_setting);
        _edtCollectCount = findViewById(R.id.edt_collect_count_setting);
        _txtDebug = findViewById(R.id.txt_debug_setting);
        _txtDebug.setMovementMethod(ScrollingMovementMethod.getInstance());
        _btnIpPort = findViewById(R.id.btn_ip_port_setting);
        _btnIpPort.setOnClickListener(this::onClick);
        _btnOff = findViewById(R.id.btn_off_setting);
        _btnOff.setOnClickListener(this::onClick);
        _btnReStart = findViewById(R.id.btn_restart_setting);
        _btnReStart.setOnClickListener(this::onClick);
        _btnUp = findViewById(R.id.btn_up_setting);
        _btnUp.setOnClickListener(this::onClick);
        _btnUpStart = findViewById(R.id.btn_up_start_setting);
        _btnUpStart.setOnClickListener(this::onClick);
        _btnCollect = findViewById(R.id.btn_collect_setting);
        _btnCollect.setOnClickListener(this::onClick);
        InitListener();
        //蓝牙监听
        MyBleUtil.SetMessageListener(_messageListener);
        Task();
    }

}
