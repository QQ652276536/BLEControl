package com.zistone.blelocation;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.zistone.libble.controls.MyBarChartView;
import com.zistone.libble.util.BleEvent;
import com.zistone.libble.util.MyBleConnectListener;
import com.zistone.libble.util.MyBleMessageListener;
import com.zistone.libble.util.MyBleUtil;
import com.zistone.libble.util.MyConvertUtil;
import com.zistone.libble.util.MyFileUtil;
import com.zistone.libble.util.MyProgressDialogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class DeviceInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DeviceInfoActivity";
    private static int SPRING_GREEN = Color.parseColor("#3CB371");
    //查询版本信息及内部时间
    private static final String CMD_20 = "680000000000006820000000E316";
    //电池电量
    private static final String CMD_21 = "680000000000006821000000E316";
    //设备状态、连接的标签数、信号强度、基站类型
    private static final String CMD_22 = "680000000000006822000000E316";
    //查询IMEI
    private static final String CMD_26 = "68000000000000682600E316";
    //卫星信号强度（GPS开始查询）
    private static final String CMD_29_GPS_START = "6800000000000068290100E316";
    //卫星信号强度（GPS继续查询）
    private static final String CMD_29_GPS_CONTINUE = "6800000000000068290180E316";
    //卫星信号强度（北斗开始查询）
    private static final String CMD_29_BD_START = "6800000000000068290101E316";
    //卫星信号强度（北斗继续查询）
    private static final String CMD_29_BD_CONTINUE = "6800000000000068290181E316";
    //内部日志（开始查询）
    private static final String CMD_2A_START = "68000000000000682A0100E316";
    //内部日志（继续查询）
    private static final String CMD_2A_CONTINUE = "68000000000000682A0101E316";
    //GPS位置
    private static final String CMD_33 = "680000000000006833000000E316";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static int TASK_TIME1 = 1 * 1000, TASK_TIME2 = 10 * 1000;

    private TextView _txtVersion, _txtTime, _txtElec, _txtVol, _txtElecState, _txtDeviceState, _txtRssi, _txtLat, _txtLot, _txtType, _txtHeight,
            _txtName, _txtMac, _txtImei;
    private BluetoothDevice _bluetoothDevice;
    private Map<String, UUID> _uuidMap;
    private Toolbar _toolbar;
    private Timer _timer;
    private TimerTask _timerTask1, _timerTask2;
    private MyHandler _myHandler;
    private boolean _taskIsRuning = false, _isConnected = false, _isVer = false, _isImei = false, _isElec = false, _isState = false;
    private LinearLayout _llConnectState1, _llConnectState2;
    private MyBleConnectListener _connectListener;
    private MyBleMessageListener _messageListener;
    private MyBarChartView _myBarCharView;
    //GPS信号强度
    private Map<String, Integer> _gpsRssiMap = new HashMap<>();
    private DeviceInfoDebugDialog _deviceInfoDebugDialog;
    private DeviceInfoDebugDialog.DeviceInfoDebugListener _deviceInfoDebugListener;
    //查询GPS信号强度的时候设备会锁住3秒、这时不要发其它不相关的命令、否则会返回错误码，查询内部日志的时候不要发其它不相关的命令
    private boolean _isSearchGpsRssi = false;
    //内部日志线程
    private boolean _searchLogThreadFlag = false;
    private Thread _searchLogThread;
    private FileOutputStream _logFileOutputStream;
    private OutputStreamWriter _logOutputStreamWriter;
    private int _currentLogtCount = 0, _totalLogCount = 0, _logStartOrContinue = 0, _gpsRssiStartOrContinue = 0;

    private static class MyHandler extends Handler {

        WeakReference<DeviceInfoActivity> _weakReference;
        DeviceInfoActivity _deviceInfoActivity;

        public MyHandler(DeviceInfoActivity activity) {
            _weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            if (_weakReference.get() == null)
                return;
            String cmdStr = "";
            try {
                _deviceInfoActivity = _weakReference.get();
                if (message.what == -99) {
                    _deviceInfoActivity.AppendDeviceInfoDebugTxt(message.obj + "\n");
                } else {
                    String[] strArray = (String[]) message.obj;
                    String type = strArray[8].toUpperCase();
                    cmdStr = Arrays.toString(strArray).replaceAll("[\\s|\\[|\\]|,]", "");
                    cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                    switch (type) {
                        //版本信息及内部时间
                        case "A0": {
                            String versionStr = MyConvertUtil.HexStrToStr(strArray[10] + strArray[11] + strArray[12] + strArray[13]);
                            versionStr = MyConvertUtil.StrAddCharacter(versionStr, 1, ".").replaceFirst("\\.", "");
                            _deviceInfoActivity._txtVersion.setText(versionStr);
                            String yearStr = "20" + strArray[14];
                            int month = Integer.parseInt(strArray[15]);
                            int day = Integer.parseInt(strArray[16]);
                            int hour = Integer.parseInt(strArray[17]);
                            int minute = Integer.parseInt(strArray[2]);
                            int second = Integer.parseInt(strArray[3]);
                            String timeStr = yearStr + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
                            _deviceInfoActivity._txtTime.setText(timeStr);
                            String logStr = "版本信息及内部时间\n" + cmdStr + "\n版本：" + versionStr + "，内部时间：" + timeStr + "\n";
                            Log.i(TAG, logStr);
                            _deviceInfoActivity.AppendDeviceInfoDebugTxt(logStr);
                            _deviceInfoActivity._isVer = true;
                        }
                        break;
                        case "E0": {
                            String logStr = cmdStr + "（版本信息及内部时间响应错误！）\n";
                            Log.e(TAG, logStr);
                            _deviceInfoActivity.AppendDeviceInfoDebugTxt(logStr);
                        }
                        break;
                        //电池电量
                        case "A1": {
                            int elec = Integer.parseInt(strArray[10], 16);
                            _deviceInfoActivity._txtElec.setText(elec + "%");
                            double voltage = (double) Integer.parseInt(strArray[11] + strArray[12], 16) / 1000;
                            _deviceInfoActivity._txtVol.setText(voltage + "V");
                            //0没有外电、1正在充电、2充电完毕
                            int state = Integer.parseInt(strArray[13], 16);
                            String stateStr = "";
                            if (state == 0)
                                stateStr = "没有外电";
                            else if (state == 1)
                                stateStr = "正在充电";
                            else if (state == 2)
                                stateStr = "充电完毕";
                            _deviceInfoActivity._txtElecState.setText(stateStr);
                            double temp = (Integer.valueOf(strArray[14] + strArray[15], 16) & 0xFFFF) / 10.0;
                            String logStr = "电池电量\n" + cmdStr + "电量：" + elec + "%，电压：" + voltage + "mV，充电状态：" + stateStr + "，温度：" + temp + "\n";
                            Log.i(TAG, logStr);
                            _deviceInfoActivity.AppendDeviceInfoDebugTxt(logStr);
                        }
                        break;
                        case "E1": {
                            String logStr = cmdStr + "（电池电量响应错误！）\n";
                            Log.e(TAG, logStr);
                            _deviceInfoActivity.AppendDeviceInfoDebugTxt(logStr);
                        }
                        break;
                        //状态、连接的标签数、信号强度、基站类型
                        case "A2": {
                            int state = Integer.parseInt(strArray[10], 16);
                            String stateStr = "";
                            if (state == 0)
                                stateStr = "设备初始化";
                            else if (state == 1)
                                stateStr = "设备关机";
                            else if (state == 2)
                                stateStr = "正在连接";
                            else if (state == 3)
                                stateStr = "已连接AP";
                            else if (state == 4)
                                stateStr = "正在连接IP";
                            else if (state == 5)
                                stateStr = "已连接IP";
                            else if (state == 6)
                                stateStr = "正在鉴权";
                            else if (state == 7)
                                stateStr = "鉴权成功";
                            else if (state == 8)
                                stateStr = "连接成功";
                            _deviceInfoActivity._txtDeviceState.setText(stateStr);
                            int rssi = Integer.parseInt(strArray[13], 16);
                            _deviceInfoActivity._txtRssi.setText(rssi + "");
                            //0定位基站、1测量基站、2其它
                            int slType = Integer.parseInt(strArray[14], 16);
                            String logStr = "状态、信号强度、基站类别\n" + cmdStr + "连接状态：" + stateStr + "，信号强度：" + rssi + "，基站类型：" + slType + "\n";
                            Log.i(TAG, logStr);
                            _deviceInfoActivity.AppendDeviceInfoDebugTxt(logStr);
                        }
                        break;
                        case "E2": {
                            String logStr = cmdStr + "（设备状态及连接的标签数量响应错误！）\n";
                            Log.e(TAG, logStr);
                            _deviceInfoActivity.AppendDeviceInfoDebugTxt(logStr);
                        }
                        break;
                        //查询IMEI
                        case "A6": {
                            int len = Integer.parseInt(strArray[9], 16);
                            if (len >= 8) {
                                String imei1 = strArray[10];
                                String imei2 = strArray[11];
                                String imei3 = strArray[12];
                                String imei4 = strArray[13];
                                String imei5 = strArray[14];
                                String imei6 = strArray[15];
                                String imei7 = strArray[16];
                                String imei8 = strArray[17].replace("F", "");
                                String imeiStr = imei1 + imei2 + imei3 + imei4 + imei5 + imei6 + imei7 + imei8;
                                _deviceInfoActivity._txtImei.setText(imeiStr);
                                String logStr = "IMEI\n" + cmdStr + "IMEI：" + imeiStr + "\n";
                                Log.i(TAG, logStr);
                                _deviceInfoActivity.AppendDeviceInfoDebugTxt(logStr);
                                _deviceInfoActivity._isImei = true;
                            } else {
                                String logStr = "IMEI\n" + cmdStr + "（IMEI：null）\n";
                                Log.i(TAG, logStr);
                                _deviceInfoActivity.AppendDeviceInfoDebugTxt(logStr);
                            }
                        }
                        break;
                        case "E6": {
                            String logStr = cmdStr + "（查询IMEI响应错误！）\n";
                            Log.e(TAG, logStr);
                            _deviceInfoActivity.AppendDeviceInfoDebugTxt(logStr);
                        }
                        break;
                        //GPS信号强度
                        case "A9": {
                            int len = Integer.parseInt(strArray[9], 16);
                            int st = Integer.parseInt(strArray[10], 16);
                            String stStr = MyConvertUtil.ByteToBitBig((byte) st);
                            //有后续数据1，无后续数据0
                            String bit1 = stStr.charAt(0) + "";
                            //共5位，表示可见的卫星数量（0~31）
                            String bit6 = stStr.charAt(5) + "";
                            String bit5 = stStr.charAt(4) + "";
                            String bit4 = stStr.charAt(3) + "";
                            String bit3 = stStr.charAt(2) + "";
                            String bit2 = stStr.charAt(1) + "";
                            //二进制转十进制
                            int num = Integer.parseInt(bit2 + bit3 + bit4 + bit5 + bit6, 2);
                            //定位系统，0表示GPS，1表示北斗
                            String bit7 = stStr.charAt(6) + "";
                            String bit8 = stStr.charAt(7) + "";
                            String gpsType = "";
                            Log.i(TAG, "bit1=" + bit1 + "，bit2=" + bit2 + "，bit3=" + bit3 + "，bit4=" + bit4 + "，bit5=" + bit5 + "，bit6=" + bit6 +
                                    "，bit7=" + bit7 + "，bit8=" + bit8);
                            if ("00".equals(bit7 + bit8)) {
                                gpsType = "GPS";
                                //继续查询GPS
                                if ("1".equals(bit1)) {
                                    Log.i(TAG, "GPS有后续数据...");
                                    _deviceInfoActivity._gpsRssiStartOrContinue = 2;
                                }
                                //开始查询北斗
                                else {
                                    Log.i(TAG, "GPS无后续数据，即将开始查询北斗数据...");
                                    _deviceInfoActivity._gpsRssiStartOrContinue = 3;
                                }
                            } else if ("01".equals(bit7 + bit8)) {
                                gpsType = "北斗";
                                //继续查询北斗
                                if ("1".equals(bit1)) {
                                    Log.i(TAG, "北斗有后续数据...");
                                    _deviceInfoActivity._gpsRssiStartOrContinue = 4;
                                }
                                //北斗无后续数据
                                else {
                                    //停止查询GPS信号强度
                                    _deviceInfoActivity._isSearchGpsRssi = false;
                                    Log.i(TAG, "北斗无后续数据");
                                    _deviceInfoActivity._gpsRssiStartOrContinue = 1;
                                }
                            }
                            String logStr = "GPS信号强度\n" + cmdStr + "，系统类型：" + gpsType + "，卫星数量：" + num;
                            //卫星标号及信号强度（最多6组）
                            int index1, rssi1;
                            int index2, rssi2;
                            int index3, rssi3;
                            int index4, rssi4;
                            int index5, rssi5;
                            int index6, rssi6;
                            if (len > 1) {
                                index1 = Integer.parseInt(strArray[11], 16);
                                if (index1 != 0) {
                                    rssi1 = Integer.parseInt(strArray[12], 16);
                                    logStr += "，卫星" + index1 + "的信号强度" + rssi1;
                                    _deviceInfoActivity._gpsRssiMap.put(gpsType + index1, rssi1);
                                }
                            }
                            if (len > 3) {
                                index2 = Integer.parseInt(strArray[13], 16);
                                if (index2 != 0) {
                                    rssi2 = Integer.parseInt(strArray[14], 16);
                                    logStr += "，卫星" + index2 + "的信号强度" + rssi2;
                                    _deviceInfoActivity._gpsRssiMap.put(gpsType + index2, rssi2);
                                }
                            }
                            if (len > 5) {
                                index3 = Integer.parseInt(strArray[15], 16);
                                if (index3 != 0) {
                                    rssi3 = Integer.parseInt(strArray[16], 16);
                                    logStr += "，卫星" + index3 + "的信号强度" + rssi3;
                                    _deviceInfoActivity._gpsRssiMap.put(gpsType + index3, rssi3);
                                }
                                index4 = Integer.parseInt(strArray[1], 16);
                                if (index4 != 0) {
                                    rssi4 = Integer.parseInt(strArray[2], 16);
                                    logStr += "，卫星" + index4 + "的信号强度" + rssi4;
                                    _deviceInfoActivity._gpsRssiMap.put(gpsType + index4, rssi4);
                                }
                                index5 = Integer.parseInt(strArray[3], 16);
                                if (index5 != 0) {
                                    rssi5 = Integer.parseInt(strArray[4], 16);
                                    logStr += "，卫星" + index5 + "的信号强度" + rssi5;
                                    _deviceInfoActivity._gpsRssiMap.put(gpsType + index5, rssi5);
                                }
                                index6 = Integer.parseInt(strArray[5], 16);
                                if (index6 != 0) {
                                    rssi6 = Integer.parseInt(strArray[6], 16);
                                    logStr += "，卫星" + index6 + "的信号强度" + rssi6;
                                    _deviceInfoActivity._gpsRssiMap.put(gpsType + index6, rssi6);
                                }
                            }
                            List<MyBarChartView.BarData> gpsData = new ArrayList<>();
                            List<MyBarChartView.BarData> bdData = new ArrayList<>();
                            Iterator<Map.Entry<String, Integer>> iterator = _deviceInfoActivity._gpsRssiMap.entrySet().iterator();
                            while (iterator.hasNext()) {
                                Map.Entry<String, Integer> entry = iterator.next();
                                String key = entry.getKey();
                                int value = entry.getValue();
                                if (key.contains("GPS")) {
                                    gpsData.add(new MyBarChartView.BarData(value, key));
                                } else if (key.contains("北斗")) {
                                    bdData.add(new MyBarChartView.BarData(value, key));
                                }
                            }
                            //排序
                            Collections.sort(gpsData);
                            Collections.sort(bdData);
                            List<MyBarChartView.BarData> data = new ArrayList<>();
                            data.addAll(bdData);
                            data.addAll(gpsData);
                            _deviceInfoActivity._myBarCharView.setBarChartData(data);
                            logStr += "\n";
                            Log.i(TAG, logStr);
                            _deviceInfoActivity.AppendDeviceInfoDebugTxt(logStr);
                        }
                        break;
                        case "E9": {
                            String logStr = cmdStr;
                            _deviceInfoActivity._gpsRssiStartOrContinue = 1;
                            _deviceInfoActivity._isSearchGpsRssi = false;
                            logStr += cmdStr + "（GPS信号强度响应错误，结束本次查询！）\n";
                            Log.e(TAG, logStr);
                            _deviceInfoActivity.AppendDeviceInfoDebugTxt(logStr);
                        }
                        break;
                        //内部日志
                        case "AA": {
                            try {
                                int len = Integer.parseInt(strArray[9], 16);
                                String logStr = "内部日志\n" + cmdStr;
                                int result = Integer.parseInt(strArray[10], 16);
                                //没有后续记录，已读完
                                if (result == 0) {
                                    logStr += "，无后续数据！\n";
                                    Log.i(TAG, logStr);
                                    try {
                                        //线程结束的时候流也关闭了，这里判断线程是否还存在写入
                                        if (_deviceInfoActivity._searchLogThreadFlag) {
                                            _deviceInfoActivity._logOutputStreamWriter.write("无后续数据\n");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    MyProgressDialogUtil.DismissAlertDialog();
                                    return;
                                }
                                //流水号
                                int index = Integer.parseInt(strArray[11] + strArray[12], 16);
                                //前面补零
                                String indexStr = MyConvertUtil.AddZeroForNum(index + "", 4, true);
                                logStr += "流水号（Hex）：" + strArray[11] + strArray[12] + "（" + index + "）";
                                //事件产生时间
                                String yearStr = "20" + strArray[13];
                                int month = Integer.parseInt(strArray[14]);
                                //前面补零
                                String monthStr = MyConvertUtil.AddZeroForNum(month + "", 2, true);
                                int day = Integer.parseInt(strArray[15]);
                                //前面补零
                                String dayStr = MyConvertUtil.AddZeroForNum(day + "", 2, true);
                                int hour = Integer.parseInt(strArray[16]);
                                //前面补零
                                String hourStr = MyConvertUtil.AddZeroForNum(hour + "", 2, true);
                                int minute = Integer.parseInt(strArray[17]);
                                //前面补零
                                String minuteStr = MyConvertUtil.AddZeroForNum(minute + "", 2, true);
                                int second = Integer.parseInt(strArray[2]);
                                //前面补零
                                String secondStr = MyConvertUtil.AddZeroForNum(second + "", 2, true);
                                String timeStr = yearStr + "-" + monthStr + "-" + dayStr + " " + hourStr + ":" + minuteStr + ":" + secondStr;
                                logStr += "，产生时间：" + timeStr;
                                //事件
                                int eventIndex = Integer.parseInt(strArray[3], 16);
                                logStr += "，事件索引（Hex）：" + strArray[3] + "（" + eventIndex + "）";
                                String eventStr = "未知";
                                if (eventIndex < BleEvent.BLE_EVENT_ARRAY.length) {
                                    eventStr = BleEvent.BLE_EVENT_ARRAY[eventIndex];
                                }
                                logStr += "，事件：" + eventStr;
                                //事件产生时的参数
                                int param = Integer.parseInt(strArray[4], 16);
                                logStr += "，事件参数（Hex）：" + strArray[4] + "（" + param + "）";
                                //事件总数
                                _deviceInfoActivity._totalLogCount = Integer.parseInt(strArray[5] + strArray[6], 16);
                                logStr += "，有后续记录，目前记录数：" + _deviceInfoActivity._currentLogtCount + "，总数：" + _deviceInfoActivity._totalLogCount +
                                        "\n";
                                Log.i(TAG, logStr);
                                MyProgressDialogUtil.SetCircleProgressCurrent(_deviceInfoActivity._currentLogtCount);
                                MyProgressDialogUtil.SetCircleProgressMax(_deviceInfoActivity._totalLogCount);
                                _deviceInfoActivity.AppendDeviceInfoDebugTxt(logStr);
                                try {
                                    //线程结束的时候流也关闭了，这里判断线程是否还存在写入
                                    if (_deviceInfoActivity._searchLogThreadFlag) {
                                        _deviceInfoActivity._logOutputStreamWriter.write(indexStr + "    " + timeStr + "    " + strArray[4] + " " + " " + "  " + eventStr + "\n");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                _deviceInfoActivity._currentLogtCount++;
                                _deviceInfoActivity._logStartOrContinue = 2;
                                MyProgressDialogUtil.SetDialogTitleTxt("导出第" + _deviceInfoActivity._currentLogtCount + "条日志...");
                            } catch (Exception e) {
                                try {
                                    //线程结束的时候流也关闭了，这里判断线程是否还存在写入
                                    if (_deviceInfoActivity._searchLogThreadFlag) {
                                        _deviceInfoActivity._logOutputStreamWriter.write("解析错误，数据：" + cmdStr + "\n");
                                    }
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }
                        break;
                        case "EA": {
                            String logStr = cmdStr;
                            logStr += cmdStr + "（内部日志响应错误，结束本次查询！）\n";
                            Log.e(TAG, logStr);
                            _deviceInfoActivity.AppendDeviceInfoDebugTxt(logStr);
                            try {
                                //线程结束的时候流也关闭了，这里判断线程是否还存在写入
                                if (_deviceInfoActivity._searchLogThreadFlag) {
                                    _deviceInfoActivity._logOutputStreamWriter.write("内部日志响应错误，已停止！\n");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            MyProgressDialogUtil.DismissAlertDialog();
                        }
                        break;
                        //GPS位置
                        case "B3": {
                            int len = Integer.parseInt(strArray[9], 16);
                            if (len < 7) {
                                String logStr = "GPS位置\n" + cmdStr + "（长度错误！）\n";
                                Log.i(TAG, logStr);
                                _deviceInfoActivity.AppendDeviceInfoDebugTxt(logStr);
                                return;
                            }
                            String lotStr = strArray[13] + strArray[12] + strArray[11] + strArray[10];
                            double lot = Double.valueOf(Integer.valueOf(lotStr, 16)) / 1000000;
                            String latStr = strArray[17] + strArray[16] + strArray[15] + strArray[14];
                            double lat = Double.valueOf(Integer.valueOf(latStr, 16)) / 1000000;
                            String heightStr = strArray[3] + strArray[2];
                            int height = Integer.parseInt(heightStr, 16);
                            int state = Integer.parseInt(strArray[4]);
                            String stateBisStr = MyConvertUtil.ByteToBitBig((byte) state);
                            String gpsType = stateBisStr.charAt(0) + "";
                            String success = stateBisStr.charAt(7) + "";
                            _deviceInfoActivity._txtType.setText("0".equals(gpsType) ? "GPS定位" : "LBS定位");
                            _deviceInfoActivity._txtLot.setText(lat + "");
                            _deviceInfoActivity._txtLat.setText(lot + "");
                            _deviceInfoActivity._txtHeight.setText(height + "");
                            String logStr = "GPS位置\n" + cmdStr + "经度：" + lot + "，纬度：" + lat + "，高度：" + height + "\n";
                            Log.i(TAG, logStr);
                            _deviceInfoActivity.AppendDeviceInfoDebugTxt(logStr);
                        }
                        break;
                        case "F3": {
                            String logStr = cmdStr + "（GPS位置响应错误！）\n";
                            Log.e(TAG, logStr);
                            _deviceInfoActivity.AppendDeviceInfoDebugTxt(logStr);
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                _deviceInfoActivity.AppendDeviceInfoDebugTxt("\n" + e.toString() + "\n数据：" + cmdStr + "\n\n");
            }
        }

    }

    private void InitListener() {
        _deviceInfoDebugListener = flag -> {
            if (flag) {
                DeviceInfoActivity.this.StartTimerTask();
            } else {
                CancelTimerTask();
                _taskIsRuning = false;
            }
        };

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
                runOnUiThread(() -> MyProgressDialogUtil.ShowWarning(DeviceInfoActivity.this, "知道了", "警告", "连接已断开，请检查设备然后重新连接！", false, () -> {
                    Intent intent = new Intent(DeviceInfoActivity.this, ListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }));
            }
        };

        _messageListener = new MyBleMessageListener() {
            @Override
            public void OnWriteSuccess(byte[] byteArray) {
            }

            public void OnReadSuccess(byte[] byteArray) {
                String result = MyConvertUtil.ByteArrayToHexStr(byteArray);
                result = MyConvertUtil.StrAddCharacter(result, 2, " ");
                Log.i(TAG, "接收：" + result);
                String[] strArray = result.split(" ");
                //目前最短的指令为12位
                if (strArray.length < 11) {
                    String logStr = "收到" + result + "，长度" + strArray.length + "错误，不予解析！";
                    _myHandler.obtainMessage(-99, logStr + "\n").sendToTarget();
                } else {
                    _myHandler.obtainMessage(0, strArray).sendToTarget();
                }
            }
        };
    }

    private void CancelTimerTask() {
        if (null != _timer)
            _timer.cancel();
        if (null != _timerTask1)
            _timerTask1.cancel();
        if (null != _timerTask2)
            _timerTask2.cancel();
    }

    private void AppendDeviceInfoDebugTxt(String str) {
        if (null != _deviceInfoDebugDialog) {
            _deviceInfoDebugDialog.AppendTxt(str);
            Log.i(TAG, str);
        }
    }

    /**
     * 重新启动定时任务
     */
    private void StartTimerTask() {
        //避免重复启动定时任务
        if (!_taskIsRuning && _isConnected) {
            CancelTimerTask();
            _timer = new Timer();
            _timerTask1 = new TimerTask() {
                @Override
                public void run() {
                    Task();
                }
            };
            _timerTask2 = new TimerTask() {
                @Override
                public void run() {
                    _isElec = false;
                    _isState = false;
                }
            };
            _timer.schedule(_timerTask1, 0, TASK_TIME1);
            _timer.schedule(_timerTask2, 0, TASK_TIME2);
            _taskIsRuning = true;
        }
    }

    private void Task() {
        if (!_isConnected)
            return;
        try {
            //没有查询内部日志和其它查询时才做GPS信号强度查询
            if (_isSearchGpsRssi && !_searchLogThreadFlag) {
                Log.i(TAG, "查询GPS信号强度...");
                switch (_gpsRssiStartOrContinue) {
                    //开始查询GPS
                    case 1:
                        _gpsRssiStartOrContinue = -1;
                        MyBleUtil.SendComm(CMD_29_GPS_START);
                        AppendDeviceInfoDebugTxt("开始查询GPS" + MyConvertUtil.StrAddCharacter(CMD_29_GPS_START, 2, " ") + "\n");
                        break;
                    //继续查询GPS
                    case 2:
                        _gpsRssiStartOrContinue = -1;
                        MyBleUtil.SendComm(CMD_29_GPS_CONTINUE);
                        AppendDeviceInfoDebugTxt("继续查询GPS" + MyConvertUtil.StrAddCharacter(CMD_29_GPS_CONTINUE, 2, " ") + "\n");
                        break;
                    //开始查询北斗
                    case 3:
                        _gpsRssiStartOrContinue = -1;
                        MyBleUtil.SendComm(CMD_29_BD_START);
                        AppendDeviceInfoDebugTxt("开始查询北斗" + MyConvertUtil.StrAddCharacter(CMD_29_BD_START, 2, " ") + "\n");
                        break;
                    //继续查询北斗
                    case 4:
                        _gpsRssiStartOrContinue = -1;
                        MyBleUtil.SendComm(CMD_29_BD_CONTINUE);
                        AppendDeviceInfoDebugTxt("继续查询北斗" + MyConvertUtil.StrAddCharacter(CMD_29_BD_CONTINUE, 2, " ") + "\n");
                        Log.i(TAG, "查询GPS信号强度结束");
                        break;
                }
            }
            //没有查询GPS信号强度和内部日志时才做其它查询
            if (!_isSearchGpsRssi && !_searchLogThreadFlag) {
                //每隔10秒查询一次电量
                if (!_isElec) {
                    Log.i(TAG, "查询电池电量...");
                    _isElec = true;
                    MyBleUtil.SendComm(CMD_21);
                    Thread.sleep(50);
                }
                //每隔10秒查询一次状态
                if (!_isState) {
                    Log.i(TAG, "查询设备状态...");
                    _isState = true;
                    MyBleUtil.SendComm(CMD_22);
                    Thread.sleep(50);
                }
                //查询到版本信息就不再查了
                if (!_isVer) {
                    Log.i(TAG, "查询基本信息...");
                    MyBleUtil.SendComm(CMD_20);
                    Thread.sleep(50);
                }
                //查询到IMEI就不再查了
                if (!_isImei) {
                    Log.i(TAG, "查询IMEI...");
                    MyBleUtil.SendComm(CMD_26);
                    Thread.sleep(50);
                }
                MyBleUtil.SendComm(CMD_33);
                Log.i(TAG, "查询GPS位置信息...");
                Thread.sleep(50);
                //即将开始查询GPS信号强度
                _isSearchGpsRssi = true;
                Log.i(TAG, "查询基本信息结束，即将开始查询GPS信号强度...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SetConnectSuccess(boolean flag) {
        runOnUiThread(() -> {
            _isConnected = flag;
            if (flag) {
                _llConnectState1.setBackgroundColor(SPRING_GREEN);
                _llConnectState2.setBackgroundColor(SPRING_GREEN);
                _txtRssi.setTextColor(SPRING_GREEN);
                _txtLat.setTextColor(SPRING_GREEN);
                _txtLot.setTextColor(SPRING_GREEN);
                _txtType.setTextColor(SPRING_GREEN);
                _txtHeight.setTextColor(SPRING_GREEN);
                _txtName.setTextColor(SPRING_GREEN);
                _txtMac.setTextColor(SPRING_GREEN);
                _txtImei.setTextColor(SPRING_GREEN);
            } else {
                _llConnectState1.setBackgroundColor(Color.GRAY);
                _llConnectState2.setBackgroundColor(Color.GRAY);
                _txtRssi.setTextColor(Color.GRAY);
                _txtLat.setTextColor(Color.GRAY);
                _txtLot.setTextColor(Color.GRAY);
                _txtType.setTextColor(Color.GRAY);
                _txtHeight.setTextColor(Color.GRAY);
                _txtName.setTextColor(Color.GRAY);
                _txtMac.setTextColor(Color.GRAY);
                _txtImei.setTextColor(Color.GRAY);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (!_isConnected) {
            Toast.makeText(this, "请先连接终端", Toast.LENGTH_SHORT).show();
            return false;
        }
        switch (item.getItemId()) {
            case R.id.item1_menu:
                startActivityForResult(new Intent(this, DeviceSettingActivity.class), 101);
                break;
            case R.id.item2_menu:
                if (null == _deviceInfoDebugDialog) {
                    _deviceInfoDebugDialog = new DeviceInfoDebugDialog(this, _deviceInfoDebugListener);
                    _deviceInfoDebugDialog.setCanceledOnTouchOutside(true);
                    _deviceInfoDebugDialog.setOnCancelListener(dialog -> StartTimerTask());
                }
                _deviceInfoDebugDialog.show();
                break;
            case R.id.item3_menu:
                //TODO:不加这一句下次弹出窗口会有异常，为啥？
                MyProgressDialogUtil.DismissAlertDialog();
                //每次导出都要重置状态
                _searchLogThreadFlag = true;
                _taskIsRuning = false;
                _logStartOrContinue = 1;
                _currentLogtCount = 0;
                _totalLogCount = 0;
                //取消定时查询任务
                CancelTimerTask();
                Calendar calendar = Calendar.getInstance();
                String logPath =
                        "/sdcard/基站扫描内部日志" + calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日.txt";
                try {
                    MyFileUtil.DeleteFile(logPath);
                    MyFileUtil.MakeFile(logPath);
                    _logFileOutputStream = new FileOutputStream(new File(logPath), true);
                    _logOutputStreamWriter = new OutputStreamWriter(_logFileOutputStream, "UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                    MyProgressDialogUtil.ShowWarning(this, "知道了", "错误", e.getMessage(), false, null);
                    return true;
                }
                //弹出提示窗口
                MyProgressDialogUtil.ShowCircleProgressDialog(this, true, () -> {
                    try {
                        if (null != _logOutputStreamWriter) {
                            _logOutputStreamWriter.flush();
                            _logOutputStreamWriter.close();
                        }
                        if (_currentLogtCount >= _totalLogCount)
                            MyProgressDialogUtil.ShowWarning(this, "知道了", "提示", "日志成功导出！\n路径：" + logPath, false, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        MyProgressDialogUtil.DismissAlertDialog();
                        MyProgressDialogUtil.ShowWarning(this, "知道了", "错误", e.getMessage(), false, null);
                    } finally {
                        //停止导出日志的线程
                        _searchLogThreadFlag = false;
                        //恢复定时查询任务
                        StartTimerTask();
                    }
                }, "正在导出内部日志...\n点击可停止");
                _searchLogThread = new Thread(() -> {
                    while (_searchLogThreadFlag && _isConnected) {
                        //                        try {
                        //                            Thread.sleep(100);
                        //                        } catch (InterruptedException e) {
                        //                            e.printStackTrace();
                        //                        }
                        switch (_logStartOrContinue) {
                            case 1:
                                _logStartOrContinue = 2;
                                MyBleUtil.SendComm(CMD_2A_START);
                                Log.i(TAG, "开始查询内部日志...");
                                AppendDeviceInfoDebugTxt("开始查询内部日志" + MyConvertUtil.StrAddCharacter(CMD_2A_START, 2, " ") + "\n");
                                break;
                            case 2:
                                _logStartOrContinue = -1;
                                MyBleUtil.SendComm(CMD_2A_CONTINUE);
                                Log.i(TAG, "继续查询内部日志...");
                                AppendDeviceInfoDebugTxt("继续查询内部日志" + MyConvertUtil.StrAddCharacter(CMD_2A_CONTINUE, 2, " ") + "\n");
                                break;
                        }
                    }
                });
                _searchLogThread.start();
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_info_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null == data) {
            return;
        }
        switch (requestCode) {
            case 101:
                if (resultCode == RESULT_OK) {
                    String str = data.getStringExtra(ARG_PARAM1);
                    Log.i(TAG, "返回时携带的数据：" + str);
                    if ("已执行关机指令".equals(str)) {
                        MyBleUtil.DisConnGatt();
                        SetConnectSuccess(false);
                    }
                }
                break;
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
        if (null != _deviceInfoDebugDialog)
            _deviceInfoDebugDialog.dismiss();
        CancelTimerTask();
        _taskIsRuning = false;
        _isConnected = false;
        MyBleUtil.DisConnGatt();
        _bluetoothDevice = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        CancelTimerTask();
        _taskIsRuning = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            MyBleUtil.DisConnGatt();
            finish();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        _myHandler = new MyHandler(this);
        Intent intent = getIntent();
        _bluetoothDevice = intent.getParcelableExtra(ARG_PARAM1);
        _uuidMap = (Map<String, UUID>) intent.getSerializableExtra(ARG_PARAM2);
        //Toolbar
        _toolbar = findViewById(R.id.toolbar_info);
        _toolbar.setTitle("");
        setSupportActionBar(_toolbar);
        //Toolbar的icon点击事件要设置在setSupportActionBar()的后面
        _toolbar.setNavigationOnClickListener(v -> {
            CancelTimerTask();
            _taskIsRuning = false;
            MyBleUtil.DisConnGatt();
            finish();
        });
        _llConnectState1 = findViewById(R.id.ll_connect_state1_info);
        _llConnectState2 = findViewById(R.id.ll_connect_state2_info);
        _txtVersion = findViewById(R.id.txt_version_info);
        _txtTime = findViewById(R.id.txt_time_info);
        _txtElec = findViewById(R.id.txt_elec_info);
        _txtVol = findViewById(R.id.txt_vol_info);
        _txtElecState = findViewById(R.id.txt_elec_state_info);
        _txtDeviceState = findViewById(R.id.txt_device_state_info);
        _txtRssi = findViewById(R.id.txt_rssi_info);
        _txtName = findViewById(R.id.txt_name_info);
        _txtName.setText(_bluetoothDevice.getName());
        _txtMac = findViewById(R.id.txt_mac_info);
        _txtMac.setText(_bluetoothDevice.getAddress());
        _txtImei = findViewById(R.id.txt_imei_info);
        _txtLat = findViewById(R.id.txt_lat_info);
        _txtLot = findViewById(R.id.txt_lot_info);
        _txtType = findViewById(R.id.txt_gps_type_info);
        _txtHeight = findViewById(R.id.txt_height_info);
        _myBarCharView = findViewById(R.id.mbcv_info);
        InitListener();
        MyBleUtil.SetConnectListener(_connectListener);
        Log.i(TAG, "开始连接蓝牙...");
        MyBleUtil.ConnectDevice(_bluetoothDevice, _uuidMap);
    }

}
