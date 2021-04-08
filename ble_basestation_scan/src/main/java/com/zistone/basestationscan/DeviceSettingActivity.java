package com.zistone.basestationscan;

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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
    //查询相对位置
    private static final String CMD_23 = "68000000000000682300E316";
    //关机
    private static final String CMD_25 = "680000000000006825045AA555AAE316";
    //查询IP地址及端口
    private static final String CMD_30 = "68000000000000683000E316";
    //查询WIFI名称
    private static final String CMD_31 = "68000000000000683100E316";
    //查询WIFI密码
    private static final String CMD_32 = "68000000000000683200E316";
    //查询允许上报的标签首地址
    private static final String CMD_2B = "68000000000000682B00E316";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static int TASK_TIME = 1 * 1000;

    private ImageButton _btnReturn, _btnTop, _btnBottom, _btnClear;
    private TextView _txtDebug;
    private EditText _edtIp, _edtPort, _edtWifiName, _edtWifiPwd, _edtX, _edtY, _edtZ, _edtMac1, _edtMac2, _edtMac3, _edtMac4, _edtMac5;
    private Button _btnSetIpPort, _btnSetWifiName, _btnSetWifiPwd, _btnSetLocation, _btnOff, _btnReStart, _btnMac;
    private BluetoothDevice _bluetoothDevice;
    private Map<String, UUID> _uuidMap;
    private Toolbar _toolbar;
    private MyHandler _myHandler;
    private LinearLayout _llDebug;
    private Spinner _spinner;
    private String _locationUnitStr = "01";
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
                        //相对坐标
                        case 23: {
                            int type = 0;
                            if (!"FF".equals(strArray[10].toUpperCase())) {
                                type = Integer.parseInt(strArray[10]);
                                type -= 1;
                            }
                            _deviceSettingActivity._spinner.setSelection(type);
                            String xStr = strArray[11] + strArray[12];
                            int x = Integer.parseInt(xStr, 16);
                            String yStr = strArray[13] + strArray[14];
                            int y = Integer.parseInt(yStr, 16);
                            String zStr = strArray[15] + strArray[16];
                            int z = Integer.parseInt(zStr, 16);
                            _deviceSettingActivity._edtX.setText(x + "");
                            _deviceSettingActivity._edtY.setText(y + "");
                            _deviceSettingActivity._edtZ.setText(z + "");
                            String cmdStr = Arrays.toString(strArray).replaceAll("[\\s|\\[|\\]|,]", "");
                            cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                            String logStr = "相对坐标\n" + cmdStr + "\nX：" + x + "，Y：" + y + "，Z：" + z + "\n\n";
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
                        //允许上报的标签首地址，数据本身就是16进制，这里不做转换
                        case 43: {
                            int len1 = Integer.valueOf(strArray[9], 16);
                            String hexMac = "";
                            for (int i = 0; i < len1; i++) {
                                hexMac += strArray[10 + i];
                            }
                            int len2 = Integer.valueOf(strArray[1], 16);
                            if (len2 > 0) {
                                for (int i = 0; i < len2; i++) {
                                    hexMac += strArray[2 + i];
                                }
                            }
                            String[] macArray = MyConvertUtil.StrAddCharacter(hexMac, 4, " ").split(" ");
                            if (macArray.length > 0)
                                _deviceSettingActivity._edtMac1.setText(macArray[0]);
                            if (macArray.length > 1)
                                _deviceSettingActivity._edtMac2.setText(macArray[1]);
                            if (macArray.length > 2)
                                _deviceSettingActivity._edtMac3.setText(macArray[2]);
                            if (macArray.length > 3)
                                _deviceSettingActivity._edtMac4.setText(macArray[3]);
                            if (macArray.length > 4)
                                _deviceSettingActivity._edtMac5.setText(macArray[4]);
                            String cmdStr = Arrays.toString(strArray).replaceAll("[\\s|\\[|\\]|,]", "");
                            cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                            String logStr = "允许上报的标签首地址\n" + cmdStr + "\n" + MyConvertUtil.StrAddCharacter(hexMac, 4, " ") + "\n\n";
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
            MyBleUtil.SendComm(CMD_2B);
            Thread.sleep(100);
            MyBleUtil.SendComm(CMD_23);
            Thread.sleep(100);
            MyBleUtil.SendComm(CMD_30);
            Thread.sleep(100);
            MyBleUtil.SendComm(CMD_31);
            Thread.sleep(100);
            MyBleUtil.SendComm(CMD_32);
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
                if (strArray.length < 14) {
                    String losStr = "指令长度" + strArray.length + "错误，不予解析！";
                    _myHandler.obtainMessage(-99, "\n" + losStr + "\n").sendToTarget();
                    return;
                }
                String type = strArray[8].toUpperCase();
                //允许上报的标签首地址
                if ("AB".equals(type)) {
                    _myHandler.obtainMessage(43, strArray).sendToTarget();
                } else if ("EB".equals(type)) {
                    String logStr = "允许上报的标签首地址响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, "\n" + logStr + "\n").sendToTarget();
                }
                //相对坐标
                else if ("A3".equals(type)) {
                    _myHandler.obtainMessage(23, strArray).sendToTarget();
                } else if ("E3".equals(type)) {
                    String logStr = "相对坐标响应错误，不予解析！";
                    Log.e(TAG, logStr);
                    _myHandler.obtainMessage(-99, "\n" + logStr + "\n").sendToTarget();
                }
                //IP地址和端口
                else if ("B0".equals(type)) {
                    _myHandler.obtainMessage(30, strArray).sendToTarget();
                } else if ("F0".equals(type)) {
                    String logStr = "IP地址和端口响应错误，不予解析！";
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
                case R.id.btn_mac_setting: {
                    String mac1 = _edtMac1.getText().toString();
                    String mac2 = _edtMac2.getText().toString();
                    String mac3 = _edtMac3.getText().toString();
                    String mac4 = _edtMac4.getText().toString();
                    String mac5 = _edtMac5.getText().toString();
                    String mac = mac1 + mac2 + mac3 + mac4 + mac5;
                    int len = mac.length() / 4;
                    if (len < 1) {
                        Toast.makeText(this, "请输入正确的标签首地址", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] hexMacArray = new String[5];
                    hexMacArray[0] = mac1;
                    hexMacArray[1] = mac2;
                    hexMacArray[2] = mac3;
                    hexMacArray[3] = mac4;
                    hexMacArray[4] = mac5;
                    String data = "";
                    for (int i = 0; i < len; i++) {
                        if (i < 4) {
                            data += hexMacArray[i];
                        }
                    }
                    String cmd = "";
                    if (len < 5) {
                        String lenStr = MyConvertUtil.AddZeroForNum(len * 2 + "", 2, true);
                        cmd += "68000000000000682B" + lenStr + data + "E316";
                    } else {
                        cmd += "6802" + hexMacArray[4] + "000000682B08" + data + "E316";
                    }
                    String checkCode = MyBleUtil.SendComm(cmd);
                    String logStr = "设置允许上报的标签首址：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ") + "，CRC=" + checkCode;
                    Log.i(TAG, logStr);
                    _txtDebug.append(logStr + "\n\n");
                }
                break;
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
                case R.id.btn_location_setting: {
                    try {
                        int x = Integer.parseInt(_edtX.getText().toString());
                        int y = Integer.parseInt(_edtY.getText().toString());
                        int z = Integer.parseInt(_edtZ.getText().toString());
                        if (x > 65535 || y > 65535 || z > 65535) {
                            Toast.makeText(this, "请输入正确的坐标参数", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String hexX = MyConvertUtil.AddZeroForNum(MyConvertUtil.IntToHexStr(x), 4, true);
                        String hexY = MyConvertUtil.AddZeroForNum(MyConvertUtil.IntToHexStr(y), 4, true);
                        String hexZ = MyConvertUtil.AddZeroForNum(MyConvertUtil.IntToHexStr(z), 4, true);
                        String cmd = "68000000000000682307" + _locationUnitStr + hexX + hexY + hexZ + "E316";
                        String checkCode = MyBleUtil.SendComm(cmd);
                        String logStr = "设置相对坐标：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ") + "，CRC=" + checkCode;
                        Log.i(TAG, logStr);
                        _txtDebug.append(logStr + "\n\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "请输入正确的坐标参数", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
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
                    String checkCode = MyBleUtil.SendComm(cmd);
                    String logStr = "设置IP和端口：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ") + "，CRC=" + checkCode;
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
                            String checkCode = MyBleUtil.SendComm(cmd);
                            String logStr = "设置WIFI名称：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ") + "，CRC=" + checkCode;
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
                            String checkCode = MyBleUtil.SendComm(cmd);
                            String logStr = "设置WIFI名称：" + MyConvertUtil.StrAddCharacter(cmd, 2, " ") + "，CRC=" + checkCode;
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
                case R.id.btn_off_setting:
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
        _edtIp = findViewById(R.id.edt_ip_setting);
        _edtPort = findViewById(R.id.edt_port_setting);
        _edtWifiName = findViewById(R.id.edt_wifi_name_setting);
        _edtWifiPwd = findViewById(R.id.edt_wifi_pwd_setting);
        _edtX = findViewById(R.id.edt_x_setting);
        _edtY = findViewById(R.id.edt_y_setting);
        _edtZ = findViewById(R.id.edt_z_setting);
        _edtMac1 = findViewById(R.id.edt_mac1_setting);
        _edtMac2 = findViewById(R.id.edt_mac2_setting);
        _edtMac3 = findViewById(R.id.edt_mac3_setting);
        _edtMac4 = findViewById(R.id.edt_mac4_setting);
        _edtMac5 = findViewById(R.id.edt_mac5_setting);
        _txtDebug = findViewById(R.id.txt_debug_setting);
        _txtDebug.setMovementMethod(ScrollingMovementMethod.getInstance());
        _btnSetIpPort = findViewById(R.id.btn_ip_port_setting);
        _btnSetIpPort.setOnClickListener(this::onClick);
        _btnSetWifiName = findViewById(R.id.btn_wifi_name_setting);
        _btnSetWifiName.setOnClickListener(this::onClick);
        _btnSetWifiPwd = findViewById(R.id.btn_wifi_pwd_setting);
        _btnSetWifiPwd.setOnClickListener(this::onClick);
        _btnSetLocation = findViewById(R.id.btn_location_setting);
        _btnSetLocation.setOnClickListener(this::onClick);
        _btnOff = findViewById(R.id.btn_off_setting);
        _btnOff.setOnClickListener(this::onClick);
        _btnReStart = findViewById(R.id.btn_restart_setting);
        _btnReStart.setOnClickListener(this::onClick);
        _btnMac = findViewById(R.id.btn_mac_setting);
        _btnMac.setOnClickListener(this::onClick);
        _spinner = findViewById(R.id.spinner_location);
        _spinner.setSelection(0, true);
        _spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        _locationUnitStr = "01";
                        break;
                    case 1:
                        _locationUnitStr = "02";
                        break;
                    case 2:
                        _locationUnitStr = "03";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        InitListener();
        //蓝牙监听
        MyBleUtil.SetMessageListener(_messageListener);
        Task();
    }

}
