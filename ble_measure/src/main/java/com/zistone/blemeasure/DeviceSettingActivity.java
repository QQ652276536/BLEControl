package com.zistone.blemeasure;

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
    //查询版本信息及内部时间
    private static final String CMD_20 = "68000000000000682000E316";
    //关机
    private static final String CMD_25 = "680000000000006825045AA555AAE316";
    //查询电池电量
    private static final String CMD_21 = "68000000000000682100E316";
    //设置/查询IP地址及端口
    private static final String CMD_30 = "68000000000000683000E316";
    //设置/查询WIFI名称
    private static final String CMD_31 = "68000000000000683100E316";
    //设置/查询WIFI密码
    private static final String CMD_32 = "68000000000000683200E316";
    //基站位置上报事件间隔及每次工作的最长时间(秒)
    private static final String CMD_35 = "68000000000000683500E316";
    //每日上报的起始时间
    private static final String CMD_36 = "68000000000000683600E316";
    //传感器的采样事件间隔及唤醒下的采样次数
    private static final String CMD_37 = "68000000000000683700E316";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static int TASK_TIME = 1 * 1000;

    private ImageButton _btnReturn, _btnTop, _btnBottom, _btnClear;
    private TextView _txtVersion, _txtTime, _txtBattery, _txtRssi, _txtDebug;
    private EditText _edtUpInterval, _edtUpWorkTime, _edtUpStartHourTime, _edtUpStartMinuteTime, _edtUpStartSecondTime, _edtCollectInterval,
            _edtCollectCount, _edtIp, _edtPort, _edtWifiName, _edtWifiPwd;
    private Button _btnReStart, _btnUp, _btnUpStart, _btnCollect, _btnSetIpPort, _btnSetWifiName, _btnSetWifiPwd, _btnOff;
    private BluetoothDevice _bluetoothDevice;
    private Map<String, UUID> _uuidMap;
    private Toolbar _toolbar;
    private MyHandler _myHandler;
    private LinearLayout _llDebug;
    private MyBleMessageListener _messageListener;

    static class MyHandler extends Handler {
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
                    switch (message.what) {
                        //版本信息及内部时间
                        case 20: {
                            String versionStr = MyConvertUtil.HexStrToStr(strArray[10] + strArray[11] + strArray[12] + strArray[13]);
                            versionStr = MyConvertUtil.StrAddCharacter(versionStr, 1, ".").replaceFirst("\\.", "");
                            _deviceSettingActivity._txtVersion.setText(versionStr);
                            String yearStr = "20" + strArray[14];
                            int month = Integer.parseInt(strArray[15]);
                            int day = Integer.parseInt(strArray[16]);
                            int hour = Integer.parseInt(strArray[17]);
                            int minute = Integer.parseInt(strArray[2]);
                            int second = Integer.parseInt(strArray[3]);
                            String timeStr = yearStr + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
                            _deviceSettingActivity._txtTime.setText(timeStr);
                            String cmdStr = Arrays.toString(strArray).replaceAll("[\\s|\\[|\\]|,]", "");
                            cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                            String logStr = "版本信息及内部时间\n" + cmdStr + "\n版本：" + versionStr + "，内部时间：" + timeStr + "\n\n";
                            Log.i(TAG, logStr);
                            _deviceSettingActivity._txtDebug.append(logStr);
                        }
                        break;
                        //电池电量
                        case 21: {
                            int voltage = Integer.valueOf(strArray[10], 16);
                            _deviceSettingActivity._txtBattery.setText(voltage + "%");
                            double temperature = (Integer.valueOf(strArray[11] + strArray[12], 16) & 0xFFFF) / 10.0;
                            String cmdStr = Arrays.toString(strArray).replaceAll("[\\s|\\[|\\]|,]", "");
                            cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                            String logStr = "电池电量及CPU温度\n" + cmdStr + "\n电量：" + voltage + "%\n\n";
                            Log.i(TAG, logStr);
                            _deviceSettingActivity._txtDebug.append(logStr);
                        }
                        break;
                        //状态、连接的标签数、信号强度、基站类型
                        case 22: {
                            int rssi = Integer.valueOf(strArray[13], 16).shortValue();
                            if (rssi > 80) {
                                rssi = rssi - 256;
                            }
                            //                            if (rssi > 0 && rssi < 256) {
                            //                                rssi = rssi - 256;
                            //                                _deviceSettingActivity._txtRssi.setText(rssi + "dBm");
                            //                            }
                            _deviceSettingActivity._txtRssi.setText(rssi + "dBm");
                            String cmdStr = Arrays.toString(strArray).replaceAll("[\\s|\\[|\\]|,]", "");
                            cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                            String logStr = "状态、连接的标签数、信号强度、基站类别\n" + cmdStr + "\n信号强度：" + rssi + "\n\n";
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
                            String cmdStr = Arrays.toString(strArray).replaceAll("[\\s|\\[|\\]|,]", "");
                            cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                            String logStr = "IP地址及端口\n" + cmdStr + "\nIP：" + ip1 + "." + ip2 + "." + ip3 + "." + ip4 + "，端口：" + port + "\n\n";
                            Log.i(TAG, logStr);
                            _deviceSettingActivity._txtDebug.append(logStr);
                        }
                        break;
                        //WIFI名称
                        case 31: {
                            int len1 = Integer.valueOf(strArray[9], 16);
                            String hexName = "";
                            for (int i = 0; i < len1; i++) {
                                hexName += strArray[10 + i];
                            }
                            int len2 = Integer.valueOf(strArray[1], 16);
                            if (len2 > 0) {
                                for (int i = 0; i < len2; i++) {
                                    hexName += strArray[2 + i];
                                }
                            }
                            String name = MyConvertUtil.HexStrToStr(hexName);
                            _deviceSettingActivity._edtWifiName.setText(name);
                            String cmdStr = Arrays.toString(strArray).replaceAll("[\\s|\\[|\\]|,]", "");
                            cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                            String logStr = "WIFI名称\n" + cmdStr + "\n" + name + "\n\n";
                            Log.i(TAG, logStr);
                            _deviceSettingActivity._txtDebug.append(logStr);
                        }
                        break;
                        //WIFI密码
                        case 32: {
                            int len1 = Integer.valueOf(strArray[9], 16);
                            String hexPwd = "";
                            for (int i = 0; i < len1; i++) {
                                hexPwd += strArray[10 + i];
                            }
                            int len2 = Integer.valueOf(strArray[1], 16);
                            for (int i = 0; i < len2; i++) {
                                hexPwd += strArray[2 + i];
                            }
                            String pwd = MyConvertUtil.HexStrToStr(hexPwd);
                            _deviceSettingActivity._edtWifiPwd.setText(pwd);
                            String cmdStr = Arrays.toString(strArray).replaceAll("[\\s|\\[|\\]|,]", "");
                            cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                            String logStr = "WIFI密码\n" + cmdStr + "\n" + pwd + "\n\n";
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
                            String cmdStr = Arrays.toString(strArray).replaceAll("[\\s|\\[|\\]|,]", "");
                            cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                            String logStr = "上报事件间隔及时长\n" + cmdStr + "\n上报间隔：" + interval + "，工作时长：" + time + "\n\n";
                            Log.i(TAG, logStr);
                            _deviceSettingActivity._txtDebug.append(logStr);
                        }
                        break;
                        //每日上报起始时间
                        case 36: {
                            int hour = Integer.parseInt(strArray[10], 16);
                            int minute = Integer.parseInt(strArray[11], 16);
                            int second = Integer.parseInt(strArray[12], 16);
                            _deviceSettingActivity._edtUpStartHourTime.setText(hour + "");
                            _deviceSettingActivity._edtUpStartMinuteTime.setText(minute + "");
                            _deviceSettingActivity._edtUpStartSecondTime.setText(second + "");
                            String cmdStr = Arrays.toString(strArray).replaceAll("[\\s|\\[|\\]|,]", "");
                            cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                            String logStr = "每日上报起始时间\n" + cmdStr + "\n" + hour + "时" + minute + "分" + second + "秒\n\n";
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
                            String cmdStr = Arrays.toString(strArray).replaceAll("[\\s|\\[|\\]|,]", "");
                            cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                            String logStr = "传感器采样间隔及次数\n" + cmdStr + "采样间隔：" + interval + "，次数：" + count + "\n\n";
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
            MyBleUtil.SendComm(CMD_20);
            Thread.sleep(50);
            MyBleUtil.SendComm(CMD_21);
            Thread.sleep(50);
            MyBleUtil.SendComm(CMD_30);
            Thread.sleep(50);
            MyBleUtil.SendComm(CMD_31);
            Thread.sleep(50);
            MyBleUtil.SendComm(CMD_32);
            Thread.sleep(50);
            MyBleUtil.SendComm(CMD_35);
            Thread.sleep(50);
            MyBleUtil.SendComm(CMD_36);
            Thread.sleep(50);
            MyBleUtil.SendComm(CMD_37);
            Thread.sleep(50);
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
                if (strArray.length < 14) {
                    String logStr = "指令长度" + strArray.length + "错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, "\n" + logStr + "\n").sendToTarget();
                    return;
                }
                String type = strArray[8].toUpperCase();
                //版本信息及内部时间
                if ("A0".equals(type)) {
                    _myHandler.obtainMessage(20, strArray).sendToTarget();
                } else if ("E0".equals(type)) {
                    String logStr = "版本信息及内部时间响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, "\n" + logStr + "\n").sendToTarget();
                }
                //电池电量
                else if ("A1".equals(type)) {
                    _myHandler.obtainMessage(21, strArray).sendToTarget();
                } else if ("E1".equals(type)) {
                    String logStr = "电池电量响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, "\n" + logStr + "\n").sendToTarget();
                }
                //IP位置及端口
                else if ("B0".equals(type)) {
                    _myHandler.obtainMessage(30, strArray).sendToTarget();
                } else if ("F0".equals(type)) {
                    String logStr = "IP位置及端口响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, "\n" + logStr + "\n").sendToTarget();
                }
                //WIFI名称
                else if ("B1".equals(type)) {
                    _myHandler.obtainMessage(31, strArray).sendToTarget();
                } else if ("F1".equals(type)) {
                    String logStr = "WIFI名称响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, "\n" + logStr + "\n").sendToTarget();
                }
                //WIFI密码
                else if ("B2".equals(type)) {
                    _myHandler.obtainMessage(32, strArray).sendToTarget();
                } else if ("F2".equals(type)) {
                    String logStr = "WIFI密码响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, "\n" + logStr + "\n").sendToTarget();
                }
                //上报事件间隔及时长
                else if ("B5".equals(type)) {
                    _myHandler.obtainMessage(35, strArray).sendToTarget();
                } else if ("F5".equals(type)) {
                    String logStr = "上报事件间隔及时长响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, "\n" + logStr + "\n").sendToTarget();
                }
                //每日上报起始时间
                else if ("B6".equals(type)) {
                    _myHandler.obtainMessage(36, strArray).sendToTarget();
                } else if ("F6".equals(type)) {
                    String logStr = "每日上报起始时间响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, "\n" + logStr + "\n").sendToTarget();
                }
                //传感器采样间隔及次数
                else if ("B7".equals(type)) {
                    _myHandler.obtainMessage(37, strArray).sendToTarget();
                } else if ("F7".equals(type)) {
                    String logStr = "传感器采样间隔及次数响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, "\n" + logStr + "\n").sendToTarget();
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
                    _edtWifiName.clearFocus();
                    _edtWifiPwd.clearFocus();
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
                }
                break;
                case R.id.btn_wifi_name_setting: {
                    String name = _edtWifiName.getText().toString();
                    String hexName = MyConvertUtil.StrToHexStr(name);
                    int nameLen = hexName.length() / 2;
                    if (nameLen > 13) {
                        Toast.makeText(this, "名称最长为13个字节", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        String hexLen1 = MyConvertUtil.AddZeroForNum(nameLen + "", 2, true);
                        //后面最多8位
                        if (nameLen <= 8) {
                            String cmd = "680000000000006831" + hexLen1 + hexName + "E316";
                            MyBleUtil.SendComm(cmd);
                            String logStr = "设置WIFI名称：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ");
                            Log.i(TAG, logStr);
                            _txtDebug.append(logStr + "\n\n");
                        } else {
                            hexLen1 = "08";
                            String hexName1 = hexName.substring(0, 16);
                            //前面最多5位
                            int len2 = nameLen - 8 == 0 ? 5 : nameLen - 8;
                            String hexLen2 = MyConvertUtil.AddZeroForNum(len2 + "", 2, true);
                            String hexName2 = hexName.substring(16, 16 + len2 * 2);
                            for (int i = 0; i < 5 - len2; i++) {
                                hexName2 += "00";
                            }
                            String cmd = "68" + hexLen2 + hexName2 + "6831" + hexLen1 + hexName1 + "E316";
                            MyBleUtil.SendComm(cmd);
                            String logStr = "设置WIFI名称：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ");
                            Log.i(TAG, logStr);
                            _txtDebug.append(logStr + "\n\n");
                        }
                    }
                }
                break;
                case R.id.btn_wifi_pwd_setting: {
                    String pwd = _edtWifiPwd.getText().toString();
                    String hexPwd = MyConvertUtil.StrToHexStr(pwd);
                    int pwdLen = hexPwd.length() / 2;
                    if (pwdLen > 13) {
                        Toast.makeText(this, "密码最长为13个字节", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        String hexLen1 = MyConvertUtil.AddZeroForNum(pwdLen + "", 2, true);
                        //后面最多8位
                        if (pwdLen <= 8) {
                            String cmd = "680000000000006832" + hexLen1 + hexPwd + "E316";
                            MyBleUtil.SendComm(cmd);
                            String logStr = "设置WIFI密码：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ");
                            Log.i(TAG, logStr);
                            _txtDebug.append(logStr + "\n\n");
                        } else {
                            hexLen1 = "08";
                            String hexPwd1 = hexPwd.substring(0, 16);
                            //前面最多5位
                            int len2 = pwdLen - 8 == 0 ? 5 : pwdLen - 8;
                            String hexLen2 = MyConvertUtil.AddZeroForNum(len2 + "", 2, true);
                            String hexPwd2 = hexPwd.substring(16, 16 + len2 * 2);
                            for (int i = 0; i < 5 - len2; i++) {
                                hexPwd2 += "00";
                            }
                            String cmd = "68" + hexLen2 + hexPwd2 + "6832" + hexLen1 + hexPwd1 + "E316";
                            MyBleUtil.SendComm(cmd);
                            String logStr = "设置WIFI密码：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ");
                            Log.i(TAG, logStr);
                            _txtDebug.append(logStr + "\n\n");
                        }
                    }
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
                    String hourHex = MyConvertUtil.AddZeroForNum(Integer.toHexString(hour), 2, true);
                    String minuteHex = MyConvertUtil.AddZeroForNum(Integer.toHexString(minute), 2, true);
                    String secondHex = MyConvertUtil.AddZeroForNum(Integer.toHexString(second), 2, true);
                    String cmd = "68000000000000683603" + hourHex + minuteHex + secondHex + "E316";
                    MyBleUtil.SendComm(cmd);
                    String logStr = "每日上报起始时间：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ");
                    Log.i(TAG, logStr);
                    _txtDebug.append(logStr + "\n\n");
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
                }
                break;
                //关机
                case R.id.btn_off_setting: {
                    MyProgressDialogUtil.ShowWarning(this, "是的", "警告", "确定要对该设备进行关机吗？", true, () -> {
                        try {
                            MyBleUtil.SendComm(CMD_25);
                            Thread.sleep(1 * 1000);
                            MyProgressDialogUtil.DismissAlertDialog();
                            MyProgressDialogUtil.ShowWarning(DeviceSettingActivity.this, "知道了", "警告", "该设备已关机，即将返回", false, () -> {
                                Intent intent = new Intent();
                                intent.putExtra(ARG_PARAM1, "已执行关机指令");
                                setResult(RESULT_OK, intent);
                                finish();
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                }
                break;
                //重启
                case R.id.btn_restart_setting: {
                    MyProgressDialogUtil.ShowConfirm(this, "是", "否", "警告", "确定要对该设备进行重启吗？", false, new MyProgressDialogUtil.ConfirmListener() {
                        @Override
                        public void OnConfirm() {
                            try {
                                MyBleUtil.SendComm("6800000000000068250412345678E316");
                                Thread.sleep(1 * 1000);
                                MyProgressDialogUtil.DismissAlertDialog();
                                MyProgressDialogUtil.ShowWarning(DeviceSettingActivity.this, "知道了", "提示", "该设备已关机，即将返回", false, () -> {
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
        _txtVersion = findViewById(R.id.txt_version_setting);
        _txtTime = findViewById(R.id.txt_time_setting);
        _txtBattery = findViewById(R.id.txt_battery_setting);
        _txtRssi = findViewById(R.id.txt_rssi_setting);
        _edtIp = findViewById(R.id.edt_ip_setting);
        _edtPort = findViewById(R.id.edt_port_setting);
        _edtWifiName = findViewById(R.id.edt_wifi_name_setting);
        _edtWifiPwd = findViewById(R.id.edt_wifi_pwd_setting);
        _edtUpInterval = findViewById(R.id.edt_up_interval_setting);
        _edtUpWorkTime = findViewById(R.id.edt_work_time_setting);
        _edtUpStartHourTime = findViewById(R.id.edt_hour_up_time_setting);
        _edtUpStartMinuteTime = findViewById(R.id.edt_minute_up_time_setting);
        _edtUpStartSecondTime = findViewById(R.id.edt_second_up_time_setting);
        _edtCollectInterval = findViewById(R.id.edt_collect_interval_setting);
        _edtCollectCount = findViewById(R.id.edt_collect_count_setting);
        _txtDebug = findViewById(R.id.txt_debug_setting);
        _txtDebug.setMovementMethod(ScrollingMovementMethod.getInstance());
        _btnSetIpPort = findViewById(R.id.btn_ip_port_setting);
        _btnSetIpPort.setOnClickListener(this::onClick);
        _btnSetWifiName = findViewById(R.id.btn_wifi_name_setting);
        _btnSetWifiName.setOnClickListener(this::onClick);
        _btnSetWifiPwd = findViewById(R.id.btn_wifi_pwd_setting);
        _btnSetWifiPwd.setOnClickListener(this::onClick);
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
