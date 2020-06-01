package com.zistone.blecontrol.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zistone.blecontrol.R;
import com.zistone.blecontrol.dialogfragment.DialogFragment_ParamSetting;
import com.zistone.blecontrol.util.BluetoothListener;
import com.zistone.blecontrol.util.BluetoothUtil;
import com.zistone.blecontrol.util.MyConvertUtil;
import com.zistone.blecontrol.util.DialogFragmentListener;
import com.zistone.blecontrol.util.ProgressDialogUtil;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;

public class CommandTest extends AppCompatActivity implements View.OnClickListener, BluetoothListener {
    private static final String TAG = "CommandTest";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_1 = 1;
    private static final int MESSAGE_2 = 2;
    private static final int MESSAGE_ERROR_1 = -1;
    private static final int SEND_SET_CONTROLPARAM = 87;
    private static final int RECEIVE_SEARCH_CONTROLPARAM = 8602;

    private ImageButton _btnReturn, _btnClear, _btnTop, _btnBottom;
    private TextView _txt;
    private Button _btn6, _btn7, _btn8, _btn9, _btn10, _btn11, _btn12, _btn13, _btn14;
    private BluetoothDevice _bluetoothDevice;
    private StringBuffer _stringBuffer = new StringBuffer();
    private Map<String, UUID> _uuidMap;
    private DialogFragment_ParamSetting _paramSetting;
    //是否连接成功、是否打开参数设置界面
    private boolean _connectedSuccess = false, _isOpenParamSetting = false;
    private FragmentManager _fragmentManager;
    private DialogFragmentListener _dialogFragmentListener;
    private int _nextEvent = 0;
    //读取内部事件的线程开关
    private volatile boolean _isEventReadThread = false, _isEventReadOver = false;
    private Myhandler _myhandler;

    static class Myhandler extends Handler {
        WeakReference<CommandTest> _weakReference;
        CommandTest _commandTest;

        public Myhandler(CommandTest activity) {
            _weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            if (_weakReference.get() == null)
                return;
            _commandTest = _weakReference.get();
            String result = (String) message.obj;
            switch (message.what) {
                //连接成功
                case MESSAGE_1: {
                    _commandTest._btn6.setEnabled(true);
                    _commandTest._btn7.setEnabled(true);
                    _commandTest._btn8.setEnabled(true);
                    _commandTest._btn9.setEnabled(true);
                    _commandTest._btn10.setEnabled(true);
                    _commandTest._btn11.setEnabled(true);
                    _commandTest._btn12.setEnabled(true);
                    _commandTest._btn13.setEnabled(true);
                    _commandTest._btn14.setEnabled(true);
                    ProgressDialogUtil.Dismiss();
                    _commandTest._connectedSuccess = true;
                }
                break;
                //显示解析内容
                case MESSAGE_2: {
                    _commandTest._txt.append("\r\n" + result);
                    //定位到最后一行
                    int offset = _commandTest._txt.getLineCount() * _commandTest._txt.getLineHeight();
                    //如果文本的高度大于ScrollView的,就自动滑动
                    if (offset > _commandTest._txt.getHeight()) {
                        _commandTest._txt.scrollTo(0, offset - _commandTest._txt.getHeight());
                    }
                    _commandTest._isEventReadOver = false;
                }
                break;
                case MESSAGE_ERROR_1:
                    _commandTest._connectedSuccess = false;
                    ProgressDialogUtil.Dismiss();
                    ProgressDialogUtil.ShowWarning(_commandTest, "警告", "该设备的连接已断开！");
                    break;
                //解析查询到的内部控制参数
                case RECEIVE_SEARCH_CONTROLPARAM: {
                    byte[] bytes = MyConvertUtil.HexStrToByteArray(result);
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
                    //锁检测开关（锁上开路）
                    String bitStr2 = String.valueOf(bitStr.charAt(1));
                    //门检测开关（关门开路）
                    String bitStr1 = String.valueOf(bitStr.charAt(0));
                    Log.i(TAG, String.format("收到查询到的参数（Bit）：\n门检测开关（关门开路）%s\n锁检测开关（锁上开路）" + "%s\n正常开锁不告警%s\n有外电可以进入维护方式%s\n启用软关机%s\n不检测强磁%s\n使用低磁检测阀值%s\n启用DEBUG软串口%s", bitStr1, bitStr2, bitStr3, bitStr4, bitStr5, bitStr6, bitStr7, bitStr8));
                    //打开控制参数修改界面的时候将查询结果传递过去
                    if (_commandTest._isOpenParamSetting) {
                        if (_commandTest._paramSetting == null) {
                            _commandTest._paramSetting = DialogFragment_ParamSetting.newInstance(new String[]{bitStr1, bitStr2, bitStr3, bitStr4,
                                                                                                              bitStr5, bitStr6, bitStr7,
                                                                                                              bitStr8}, _commandTest._dialogFragmentListener);
                            _commandTest._paramSetting.setCancelable(false);
                        }
                        _commandTest._paramSetting.show(_commandTest._fragmentManager, "DialogFragment_ParamSetting");
                    }
                }
                break;
                //修改内部控制参数
                case SEND_SET_CONTROLPARAM: {
                    Log.i(TAG, "发送参数设置：" + result);
                    BluetoothUtil.SendComm(result);
                }
                break;
            }
        }
    }

    /**
     * 解析硬件返回的数据
     *
     * @param data 带空格的16进制
     */
    private void Resolve(String data) {
        Log.i(TAG, "共接收：" + data);
        String[] strArray = data.split(" ");
        //目前最短的指令是开门指令,为16位
        if (strArray.length < 15) {
            Log.e(TAG, "指令长度" + strArray.length + "错误，不予解析！");
            _isEventReadThread = true;
            return;
        }
        String receive = data.trim();
        /*
         * GPS位置信息的通信协议
         *
         * 指令示例：68 03 00 37 00 00 01 68 A2 0B 00 00 00 00 00 00 00 00 B8 16
         * 0B（Len）
         * 00（定位状态，1表示定位成功）
         * 00 00 00 00经度
         * 00 00 00 00纬度
         * 37 00（高度）
         *
         * */
        if (strArray[8].equals("A2")) {
            receive = data + "\r\n解析：";
            int state = Integer.parseInt(strArray[10], 16);
            if (state == 1) {
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
                receive += "经度" + latNum + "纬度" + lotNum + "高度" + height;
            } else {
                receive += "定位失败";
            }
        }
        /*
         * 设备基本信息的通信协议
         *
         * 指令示例：68 00 00 00 00 00 01 68 A1 07 56 33 32 36 0E 00 04 7C 16
         * 07（Len）
         * 56 33 32 36（版本）
         * 0E 00（电池电压）
         * 04（内部温度，为256补码，-127~127，实际温度数据为23+temperature/2）
         * 00（连接状态，0待机，1已连接主机）
         * 00（连接类型，0未定，1软件串口，2SPI，3硬件串口）
         * 00（远程下载百分比）
         *
         * */
        else if (strArray[8].equals("A1")) {
            receive = data;
            String versionStr = MyConvertUtil.HexStrToStr((strArray[10] + strArray[11] + strArray[12] + strArray[13]).trim());
            versionStr = MyConvertUtil.StrAddCharacter(versionStr, ".");
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
            String connectState = Integer.parseInt(strArray[2], 16) == 0 ? "待机" : "已连接主机";
            String connectType = "未定";
            switch (Integer.parseInt(strArray[3], 16)) {
                case 0:
                    connectType = "未定";
                    break;
                case 1:
                    connectType = "软件串口";
                    break;
                case 2:
                    connectType = "SPI";
                    break;
                case 3:
                    connectType = "硬件串口";
                    break;
            }
            int downloadPer = Integer.parseInt(strArray[4], 16);
            receive += String.format("\r\n解析：版本号[%s] 电池电压[%sV] 内部温度[%s℃] 连接状态[%s] 连接类型[%s] 远程下载百分比[%s%%]", versionStr, voltage, temperature, connectState, connectType, downloadPer);
        }
        /*
         * 读取内部存储的事件记录的通信协议
         *
         * 指令示例：68 03 00 00 14 00 01 68 A0 0B 06 20 04 25 23 26 40 1A 85 16
         * 68
         * 03（多余的字符个数,这个字节不计入DAT_R位数里）
         * 00 00 14 00 01（DAT_R的第13位）
         * 68
         * A0
         * 0B（Len）
         * 06（事件）
         * 20（年）04（月）25（日）23（时）26（分）40（秒）
         * 1A（DAT_R的第8位）
         * 85（校验位）16
         * 当事件为窃电时,时间后面的4个字节分别表示SN和SR,SR[0]的0、1、2分别表示门、锁、摄像头,SR[1]的0、1、2分别表示第一路、第二路、两路同时触发
         * 当事件为振动时,时间后面的6个字节分别表示XYZ方向的加速度
         *
         * 执行读取事件记录的时候_btn12控件禁用,这个可以用来判断是否正在执行读取事件记录
         *
         * */
        else if (strArray[8].equals("A0") && !_btn12.isEnabled()) {
            //超出8个字节后多出来的字节个数
            int excessNum = Integer.parseInt(strArray[1], 16);
            //数据长度
            int len = Integer.parseInt(strArray[9], 16);
            if (len == 0) {
                _isEventReadThread = true;
                return;
            }
            _nextEvent++;
            receive = data + "\r\n解析：事件" + _nextEvent;
            switch (strArray[10]) {
                case "01":
                    receive += "【开锁】 ";
                    break;
                case "02":
                    receive += "【关锁】 ";
                    break;
                case "03":
                    receive += "【开门】 ";
                    break;
                case "04":
                    receive += "【关门】 ";
                    break;
                case "05":
                    receive += "【窃电】 ";
                    //窃电时间来源：strArray[17] + strArray[2]
                    //窃电电路数：strArray[3] + strArray[4]
                    switch (Integer.parseInt(strArray[17], 16)) {
                        case 0:
                            receive += "来源【门】 ";
                            break;
                        case 1:
                            receive += "来源【锁】 ";
                            break;
                        case 2:
                            receive += "来源【摄像头】 ";
                            break;
                    }
                    switch (Integer.parseInt(strArray[2], 16)) {
                        case 0:
                            receive += "电路数【第一路】";
                            break;
                        case 1:
                            receive += "电路数【第二路】";
                            break;
                        case 2:
                            receive += "电路数【两路同时触发】";
                            break;
                    }
                    break;
                case "06":
                    receive += "振动【";
                    //XYZ方向的加速度
                    receive += " X：" + strArray[17] + strArray[2] + ", Y：" + strArray[3] + strArray[4] + ", Z：" + strArray[5] + strArray[6] + "】";
                    break;
            }
            String yearStr = " 20" + strArray[11] + "/";
            String monthStr = strArray[12] + "/";
            String dayStr = strArray[13] + " ";
            String hourStr = strArray[14] + ":";
            String minuteStr = strArray[15] + ":";
            String secondStr = strArray[16];
            receive += yearStr + monthStr + dayStr + hourStr + minuteStr + secondStr;
        }
        /*
         * 开关门协议
         *
         * */
        else if (strArray[8].equals("10")) {
            String indexStr = strArray[12];
            switch (indexStr) {
                //综合测试A：全部门锁状态+强磁开关状态+外接电源状态+内部电池充电状态+电池电量+磁强
                case "80": {
                    receive = data + "\r\n解析：";
                    byte[] bytes1 = MyConvertUtil.HexStrToByteArray(strArray[13]);
                    String bitStr = MyConvertUtil.ByteToBit(bytes1[0]);
                    String doorState1 = String.valueOf(bitStr.charAt(7));
                    receive += doorState1.equals("1") ? "门锁【已开】 " : "门锁【已关】 ";
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
                }
                break;
                //开一号门锁
                case "81": {
                    receive = data + "\r\n解析：";
                    String result = strArray[13];
                    byte[] bytes = MyConvertUtil.HexStrToByteArray(result);
                    String bitStr = MyConvertUtil.ByteToBit(bytes[0]);
                    String doorState = String.valueOf(bitStr.charAt(7));
                    String lockState = String.valueOf(bitStr.charAt(6));
                    receive += doorState.equals("1") ? "门一【已开】 " : "门一【已关】 ";
                    receive += lockState.equals("1") ? "锁一【已开】 " : "锁一【已关】 ";
                }
                break;
                //开二号门锁
                case "82": {
                    receive = data + "\r\n解析：";
                    String result = strArray[13];
                    byte[] bytes = MyConvertUtil.HexStrToByteArray(result);
                    String bitStr = MyConvertUtil.ByteToBit(bytes[0]);
                    String doorState = String.valueOf(bitStr.charAt(7));
                    String lockState = String.valueOf(bitStr.charAt(6));
                    receive += doorState.equals("1") ? "门二【已开】 " : "门二【已关】 ";
                    receive += lockState.equals("1") ? "锁二【已开】" : "锁二【已关】";
                }
                break;
                //开全部门锁
                case "83": {
                    receive = data + "\r\n解析：";
                    byte[] bytes = MyConvertUtil.HexStrToByteArray(strArray[13]);
                    String bitStr = MyConvertUtil.ByteToBit(bytes[0]);
                    String doorState1 = String.valueOf(bitStr.charAt(7));
                    receive += doorState1.equals("1") ? "门一【已开】 " : "门一【已关】 ";
                    String lockState1 = String.valueOf(bitStr.charAt(6));
                    receive += lockState1.equals("1") ? "锁一【已开】 " : "锁一【已关】 ";
                    String doorState2 = String.valueOf(bitStr.charAt(5));
                    receive += doorState2.equals("1") ? "门二【已开】 " : "门二【已关】 ";
                    String lockState2 = String.valueOf(bitStr.charAt(4));
                    receive += lockState2.equals("1") ? "锁二【已开】" : "锁二【已关】";
                }
                break;
                //查询内部控制参数
                case "86": {
                    //打开控制参数修改界面的时候将查询结果传递过去,此时可以不输出调试信息
                    if (_isOpenParamSetting) {
                        _myhandler.obtainMessage(RECEIVE_SEARCH_CONTROLPARAM, strArray[13]).sendToTarget();
                        return;
                    }
                    receive = data;
                    byte[] bytes = MyConvertUtil.HexStrToByteArray(strArray[13]);
                    String bitStr = MyConvertUtil.ByteToBit(bytes[0]);
                    //门检测开关（关门开路）
                    String str1 = String.valueOf(bitStr.charAt(7));
                    //锁检测开关（锁上开路）
                    String str2 = String.valueOf(bitStr.charAt(6));
                    //正常开锁不告警
                    String str3 = String.valueOf(bitStr.charAt(5));
                    //有外电可以进入维护方式
                    String str4 = String.valueOf(bitStr.charAt(4));
                    //启用软关机
                    String str5 = String.valueOf(bitStr.charAt(3));
                    //不检测强磁
                    String str6 = String.valueOf(bitStr.charAt(2));
                    //使用低磁检测阀值
                    String str7 = String.valueOf(bitStr.charAt(1));
                    //启用DEBUG软串口
                    String str8 = String.valueOf(bitStr.charAt(0));
                    StringBuffer stringBuffer = new StringBuffer();
                    if (str1.equals("1"))
                        stringBuffer.append("\r\n门检测开关（关门开路）【启用】\n");
                    else
                        stringBuffer.append("\r\n门检测开关（关门开路）【禁用】\n");
                    if (str2.equals("1"))
                        stringBuffer.append("锁检测开关（锁上开路）【启用】\n");
                    else
                        stringBuffer.append("锁检测开关（锁上开路）【禁用】\n");
                    if (str3.equals("1"))
                        stringBuffer.append("正常开锁不告警【启用】\n");
                    else
                        stringBuffer.append("正常开锁不告警【禁用】\n");
                    if (str4.equals("1"))
                        stringBuffer.append("有外电可以进入维护方式【启用】\n");
                    else
                        stringBuffer.append("有外电可以进入维护方式【禁用】\n");
                    if (str5.equals("1"))
                        stringBuffer.append("启用软关机【启用】\n");
                    else
                        stringBuffer.append("启用软关机【禁用】\n");
                    if (str6.equals("1"))
                        stringBuffer.append("不检测强磁【启用】\n");
                    else
                        stringBuffer.append("不检测强磁【禁用】\n");
                    if (str7.equals("1"))
                        stringBuffer.append("使用低磁检测阀值【启用】\n");
                    else
                        stringBuffer.append("使用低磁检测阀值【禁用】\n");
                    if (str8.equals("1"))
                        stringBuffer.append("启用DEBUG软串口【启用】\n");
                    else
                        stringBuffer.append("启用DEBUG软串口【禁用】");
                    receive += "\r\n解析：" + stringBuffer.toString();
                }
                break;
            }
        }
        _myhandler.obtainMessage(MESSAGE_2, "接收：" + receive).sendToTarget();
    }

    private void InitListener() {
        _dialogFragmentListener = new DialogFragmentListener() {
            @Override
            public void OnDismiss(String tag) {
            }

            @Override
            public void OnComfirm(String tag, String str) {
                _myhandler.obtainMessage(SEND_SET_CONTROLPARAM, str).sendToTarget();
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
            this.finish();
        return false;
    }

    @Override
    public void OnConnected() {
        Log.i(TAG, "成功建立连接！");
        _myhandler.obtainMessage(MESSAGE_1, "").sendToTarget();
        //返回时告知该设备已成功连接
        setResult(2, new Intent());
    }

    @Override
    public void OnConnecting() {
        ProgressDialogUtil.ShowProgressDialog(CommandTest.this, true, "正在连接...");
    }

    @Override
    public void OnDisConnected() {
        Log.i(TAG, "连接已断开！");
        _myhandler.obtainMessage(MESSAGE_ERROR_1, "").sendToTarget();
    }

    @Override
    public void OnWriteSuccess(byte[] byteArray) {
        String result = MyConvertUtil.ByteArrayToHexStr(byteArray);
        result = MyConvertUtil.HexStrAddCharacter(result, " ");
        String[] strArray = result.split(" ");
        String sendResult = "";
        /*
         * 读取内部存储的事件记录的通信协议
         * 1、指令为13个字节
         * 2、执行读取事件记录的时候_btn12控件禁用
         * 以上2个条件可以用来判断是否正在执行读取事件记录
         *
         * */
        if (strArray[8].equals("20") && byteArray.length == 13 && !_btn12.isEnabled()) {
            sendResult = "读取内部存储的事件记录";
        } else if (strArray[8].equals("21")) {
            sendResult = "读取基本信息";
        } else if (strArray[8].equals("22")) {
            sendResult = "读取GPS位置信息";
        } else {
            String indexStr = strArray[11];
            switch (indexStr) {
                case "00":
                    sendResult = "开门";
                    break;
                case "01":
                    sendResult = "读卡";
                    break;
                case "02":
                    sendResult = "测量电池电压";
                    break;
                case "03":
                    sendResult = "测量磁场强度";
                    break;
                case "80":
                    sendResult = "综合测试A";
                    break;
                case "81":
                    sendResult = "开一号门锁";
                    break;
                case "82":
                    sendResult = "开二号门锁";
                    break;
                case "83":
                    sendResult = "开全部门锁";
                    break;
                case "86":
                    sendResult = "查询内部控制参数";
                    break;
                case "87":
                    sendResult = "修改内部控制参数";
                    break;
            }
        }
        _myhandler.obtainMessage(MESSAGE_2, "\r\n发送：" + MyConvertUtil.StrArrayToStr(strArray)).sendToTarget();
        Log.i(TAG, "成功发送'" + sendResult + "'的指令");
    }

    @Override
    public void OnReadSuccess(byte[] byteArray) {
        String result = MyConvertUtil.ByteArrayToHexStr(byteArray);
        result = MyConvertUtil.HexStrAddCharacter(result, " ");
        Log.i(TAG, "接收：" + result);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        String hexStr = "";
        String logStr = "";
        switch (v.getId()) {
            case R.id.btn_return: {
                ProgressDialogUtil.Dismiss();
                this.finish();
            }
            break;
            //回到顶部
            case R.id.btnTop:
                _txt.scrollTo(0, 0);
                break;
            //回到底部
            case R.id.btnBottom:
                int offset = _txt.getLineCount() * _txt.getLineHeight();
                if (offset > _txt.getHeight()) {
                    _txt.scrollTo(0, offset - _txt.getHeight());
                }
                break;
            //清屏
            case R.id.btnClear:
                _txt.setText("");
                break;
            //综合测试A
            case R.id.btn6: {
                hexStr = "680000000000006810000180E616";
                logStr = "发送'综合测试A'指令：" + hexStr;
            }
            break;
            //开一号门锁
            case R.id.btn7: {
                hexStr = "680000000000006810000181E716";
                logStr = "发送'开一号门锁'指令：" + hexStr;
            }
            break;
            //开二号门锁
            case R.id.btn8: {
                hexStr = "680000000000006810000182E816";
                logStr = "发送'开二号门锁'指令：" + hexStr;
            }
            break;
            //开全部门锁
            case R.id.btn9: {
                hexStr = "680000000000006810000183E916";
                logStr = "发送'开全部门锁'指令：" + hexStr;
            }
            break;
            //查询内部控制参数
            case R.id.btn10: {
                _isOpenParamSetting = false;
                hexStr = "680000000000006810000186EA16";
                logStr = "发送'查询内部控制参数'指令：" + hexStr;
            }
            break;
            //修改内部控制参数
            case R.id.btn11: {
                _isOpenParamSetting = true;
                //先查询内部控制参数,再打开修改参数的界面
                hexStr = "680000000000006810000186EA16";
                logStr = "发送'修改内部控制参数'指令：" + hexStr;
            }
            break;
            //读取内部存储的事件记录
            case R.id.btn12: {
                _btn12.setEnabled(false);
                _nextEvent = 0;
                _isEventReadThread = false;
                _isEventReadOver = false;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //说明还有下一条记录
                        while (!_isEventReadThread) {
                            //上一条记录解析完毕再执行读取
                            if (!_isEventReadOver) {
                                _isEventReadOver = true;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (_nextEvent == 0) {
                                            BluetoothUtil.SendComm("6800000000000068200100EC16");
                                            Log.i(TAG, "发送'读取内部存储的事件记录'指令：6800000000000068200100EC16");
                                        } else {
                                            BluetoothUtil.SendComm("6800000000000068200101EC16");
                                            Log.i(TAG, "发送'读取内部存储的【下一条】记录'指令：6800000000000068200101EC16");
                                        }
                                    }
                                });
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                _btn12.setEnabled(true);
                                Log.i(TAG, "事件记录读取完毕");
                            }
                        });
                    }
                }).start();
            }
            break;
            //读取基本信息
            case R.id.btn13: {
                hexStr = "6800000000000068210100EC16";
                logStr = "发送'读取基本信息'指令：" + hexStr;
            }
            break;
            //读取GPS位置信息
            case R.id.btn14: {
                hexStr = "6800000000000068220100EC16";
                logStr = "发送'读取GPS位置信息'指令：" + hexStr;
            }
            break;
        }
        if (!hexStr.equals("")) {
            BluetoothUtil.SendComm(hexStr);
            Log.i(TAG, logStr);
        }
    }

    @Override
    public void onDestroy() {
        _isEventReadThread = true;
        ProgressDialogUtil.Dismiss();
        BluetoothUtil.DisConnGatt();
        _bluetoothDevice = null;
        if (_paramSetting != null) {
            _paramSetting.dismiss();
            _paramSetting = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _myhandler = new Myhandler(this);
        setContentView(R.layout.activity_command_test);
        _fragmentManager = getSupportFragmentManager();
        Intent intent = getIntent();
        _bluetoothDevice = intent.getParcelableExtra(ARG_PARAM1);
        _uuidMap = (Map<String, UUID>) intent.getSerializableExtra(ARG_PARAM2);
        _btnReturn = findViewById(R.id.btn_return);
        _btnReturn.setOnClickListener(this);
        _txt = findViewById(R.id.txt);
        _txt.setMovementMethod(ScrollingMovementMethod.getInstance());
        _btnClear = findViewById(R.id.btnClear);
        _btnClear.setOnClickListener(this);
        _btnTop = findViewById(R.id.btnTop);
        _btnTop.setOnClickListener(this);
        _btnBottom = findViewById(R.id.btnBottom);
        _btnBottom.setOnClickListener(this);
        _btn6 = findViewById(R.id.btn6);
        _btn6.setOnClickListener(this);
        _btn7 = findViewById(R.id.btn7);
        _btn7.setOnClickListener(this);
        _btn8 = findViewById(R.id.btn8);
        _btn8.setOnClickListener(this);
        _btn9 = findViewById(R.id.btn9);
        _btn9.setOnClickListener(this);
        _btn10 = findViewById(R.id.btn10);
        _btn10.setOnClickListener(this);
        _btn11 = findViewById(R.id.btn11);
        _btn11.setOnClickListener(this);
        _btn12 = findViewById(R.id.btn12);
        _btn12.setOnClickListener(this);
        _btn13 = findViewById(R.id.btn13);
        _btn13.setOnClickListener(this);
        _btn14 = findViewById(R.id.btn14);
        _btn14.setOnClickListener(this);
        BluetoothUtil.Init(CommandTest.this, this);
        if (_bluetoothDevice != null) {
            Log.i(TAG, "开始连接...");
            BluetoothUtil.ConnectDevice(_bluetoothDevice, _uuidMap);
        } else {
            ProgressDialogUtil.ShowWarning(CommandTest.this, "警告", "未获取到蓝牙,请重试！");
        }
        InitListener();
    }

}
