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
import com.zistone.blecontrol.util.ConvertUtil;
import com.zistone.blecontrol.util.DialogFragmentListener;
import com.zistone.blecontrol.util.ProgressDialogUtil;

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
    private Button _btn1, _btn2, _btn3, _btn4, _btn5, _btn6, _btn7, _btn8, _btn9, _btn10, _btn11, _btn12;
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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            String result = (String) message.obj;
            switch (message.what) {
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
                    Log.i(TAG, String.format("收到查询到的参数(Bit):\n门检测开关(关门开路)%s\n锁检测开关(锁上开路)%s\n正常开锁不告警%s\n有外电可以进入维护方式%s\n启用软关机%s\n不检测强磁%s\n使用低磁检测阀值%s\n启用DEBUG软串口%s", bitStr1, bitStr2, bitStr3, bitStr4, bitStr5, bitStr6, bitStr7, bitStr8));
                    //打开控制参数修改界面的时候将查询结果传递过去,此时可以不输出调试信息
                    if (_isOpenParamSetting) {
                        if (_paramSetting == null) {
                            _paramSetting = DialogFragment_ParamSetting.newInstance(new String[]{bitStr1, bitStr2, bitStr3, bitStr4, bitStr5, bitStr6,
                                                                                                 bitStr7, bitStr8}, _dialogFragmentListener);
                            _paramSetting.setCancelable(false);
                        }
                        _paramSetting.show(_fragmentManager, "DialogFragment_ParamSetting");
                    }
                }
                break;
                //修改内部控制参数
                case SEND_SET_CONTROLPARAM: {
                    Log.i(TAG, "发送参数设置:" + result);
                    BluetoothUtil.SendComm(result);
                    int offset = _txt.getLineCount() * _txt.getLineHeight();
                    if (offset > _txt.getHeight()) {
                        _txt.scrollTo(0, offset - _txt.getHeight());
                    }
                }
                break;
                case MESSAGE_ERROR_1:
                    _connectedSuccess = false;
                    ProgressDialogUtil.Dismiss();
                    ProgressDialogUtil.ShowWarning(CommandTest.this, "警告", "该设备的连接已断开,如需再次连接请重试!");
                    break;
                case MESSAGE_1: {
                    _btn1.setEnabled(true);
                    _btn2.setEnabled(true);
                    _btn3.setEnabled(true);
                    _btn4.setEnabled(true);
                    _btn5.setEnabled(true);
                    _btn6.setEnabled(true);
                    _btn7.setEnabled(true);
                    _btn8.setEnabled(true);
                    _btn9.setEnabled(true);
                    _btn10.setEnabled(true);
                    _btn11.setEnabled(true);
                    _btn12.setEnabled(true);
                    ProgressDialogUtil.Dismiss();
                    _connectedSuccess = true;
                }
                break;
                case MESSAGE_2: {
                    _txt.append("\r\n" + result);
                    //定位到最后一行
                    int offset = _txt.getLineCount() * _txt.getLineHeight();
                    //如果文本的高度大于ScrollView的,就自动滑动
                    if (offset > _txt.getHeight()) {
                        _txt.scrollTo(0, offset - _txt.getHeight());
                    }
                    _isEventReadOver = false;
                }
                break;
            }
        }
    };

    private void InitListener() {
        _dialogFragmentListener = new DialogFragmentListener() {
            @Override
            public void OnDismiss(String tag) {
            }

            @Override
            public void OnComfirm(String tag, String str) {
                Message message = handler.obtainMessage(SEND_SET_CONTROLPARAM, str);
                handler.sendMessage(message);
                //发送内部参数以后关闭设置窗口
                _paramSetting.dismiss();
                _paramSetting = null;
            }
        };
    }

    /**
     * 内部事件
     *
     * @param data
     */
    private void Resolve(String data) {
        Log.i(TAG, "共接收:" + data);
        String[] strArray = data.split(" ");
        //目前最短的指令是开门指令,为16位
        if (strArray.length < 15) {
            Log.e(TAG, "指令长度" + strArray.length + "错误,不予解析!");
            _isEventReadThread = true;
            return;
        }
        String receive = "";
        /*
         * 特殊处理:读取内部存储的事件记录的通信协议和之前的协议不一样,需要留意
         * 指令示例:68 03 00 00 14 00 01 68 A0 0B 06 20 04 25 23 26 40 1A 85 16
         * 68
         * 03(多余的字符个数,这个字节不计入DAT_R位数里)
         * 00 00 14 00 01(DAT_R的第13位)
         * 68
         * A0
         * 0B(Len)
         * 06(事件)
         * 20(年)04(月)25(日)23(时)26(分)40(秒)
         * 1A(DAT_R的第8位)
         * 85(校验位)16
         * 当事件为窃电时,时间后面的4个字节分别表示SN和SR,SR[0]的0、1、2分别表示门、锁、摄像头,SR[1]的0、1、2分别表示第一路、第二路、两路同时触发
         * 当事件为振动时,时间后面的6个字节分别表示XYZ方向的加速度
         *
         * 执行读取事件记录的时候_btn12控件禁用,这个可以用来判断是否正在执行读取事件记录
         *
         * */
        if (strArray[11].equals("20") && !_btn12.isEnabled()) {
            //超出8个字节后多出来的字节个数
            int excessNum = Integer.parseInt(strArray[1], 16);
            //数据长度
            int len = Integer.parseInt(strArray[9], 16);
            if (len == 0) {
                _isEventReadThread = true;
                return;
            }
            _nextEvent++;
            receive = "第" + _nextEvent + "条内部存储的事件记录";
            switch (strArray[10]) {
                case "01":
                    receive += " 开锁";
                    break;
                case "02":
                    receive += " 关锁";
                    break;
                case "03":
                    receive += " 开门";
                    break;
                case "04":
                    receive += " 关门";
                    break;
                case "05":
                    receive += " 窃电";
                    //窃电时间来源:strArray[17] + strArray[2]
                    //窃电电路数:strArray[3] + strArray[4]
                    switch (Integer.parseInt(strArray[17], 16)) {
                        case 0:
                            receive += " 来源:门";
                            break;
                        case 1:
                            receive += " 来源:锁";
                            break;
                        case 2:
                            receive += " 来源:摄像头";
                            break;
                    }
                    switch (Integer.parseInt(strArray[2], 16)) {
                        case 0:
                            receive += " 电路数:第一路";
                            break;
                        case 1:
                            receive += " 电路数:第二路";
                            break;
                        case 2:
                            receive += " 电路数:两路同时触发";
                            break;
                    }
                    break;
                case "06":
                    receive += " 振动";
                    //XYZ方向的加速度:strArray[17] + strArray[2] + strArray[3] + strArray[4] + strArray[5] + strArray[6]
                    receive += " X:" + strArray[17] + strArray[2] + ", Y:" + strArray[3] + strArray[4] + ", Z:" + strArray[5] + strArray[6];
                    break;
            }
            String yearStr = " 20" + strArray[11] + "/";
            String monthStr = strArray[12] + "/";
            String dayStr = strArray[13] + " ";
            String hourStr = strArray[14] + ":";
            String minuteStr = strArray[15] + ":";
            String secondStr = strArray[16];
            receive += yearStr + monthStr + dayStr + hourStr + minuteStr + secondStr + "\r\n";
        } else {
            String indexStr = strArray[12];
            switch (indexStr) {
                //开门
                case "00":
                    break;
                //读卡
                case "01":
                    break;
                //电池电压
                case "02":
                    break;
                //磁场强度
                case "03":
                    String responseValue = strArray[9].equals("00") ? "OK" : "Fail";
                    //                responseValue += " " + ConvertUtil.HexStrToStr(strArray[14] + strArray[15] + strArray[16] + strArray[17] + strArray[18] + strArray[19] + strArray[20] + strArray[21] + strArray[22] + strArray[23] + strArray[24]);
                    break;
                //测量门状态
                case "04":
                    break;
                //综合测试A:68,04,07,5F,06,C3,01,68,10,00,07,00,80,03,0C,BF,07,57,72,16
                case "80": {
                    //全部门锁状态
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
                }
                break;
                //开一号门锁:68,00,00,00,00,00,01,68,10,00,03,0E,81,03,76,16
                case "81": {
                    String result = strArray[13];
                    byte[] bytes = ConvertUtil.HexStrToByteArray(result);
                    String bitStr = ConvertUtil.ByteToBit(bytes[0]);
                    String doorState1 = String.valueOf(bitStr.charAt(7));
                    String lockState1 = String.valueOf(bitStr.charAt(6));
                }
                break;
                //开二号门锁:68,00,00,00,00,00,01,68,10,00,03,0E,82,03,77,16
                case "82": {
                    String result = strArray[13];
                    byte[] bytes = ConvertUtil.HexStrToByteArray(result);
                    String bitStr = ConvertUtil.ByteToBit(bytes[0]);
                    String doorState1 = String.valueOf(bitStr.charAt(7));
                    String lockState1 = String.valueOf(bitStr.charAt(6));
                }
                break;
                //开全部门锁:68,00,00,00,00,00,01,68,10,00,03,0E,83,03,78,16
                case "83": {
                    String result = strArray[13];
                    byte[] bytes = ConvertUtil.HexStrToByteArray(result);
                    String bitStr = ConvertUtil.ByteToBit(bytes[0]);
                    String doorState1 = String.valueOf(bitStr.charAt(7));
                    String lockState1 = String.valueOf(bitStr.charAt(6));
                    String doorState2 = String.valueOf(bitStr.charAt(5));
                    String lockState2 = String.valueOf(bitStr.charAt(4));
                }
                break;
                //查询内部控制参数:68,00,00,00,00,00,01,68,10,00,06,00,86,00,00,00,00,6D,16
                case "86": {
                    //打开控制参数修改界面的时候将查询结果传递过去,此时可以不输出调试信息
                    if (_isOpenParamSetting) {
                        Message message = handler.obtainMessage(RECEIVE_SEARCH_CONTROLPARAM, strArray[13]);
                        handler.sendMessage(message);
                        return;
                    }
                    byte[] bytes = ConvertUtil.HexStrToByteArray(strArray[13]);
                    String bitStr = ConvertUtil.ByteToBit(bytes[0]);
                    //门检测开关(关门开路)
                    String str1 = String.valueOf(bitStr.charAt(7));
                    //锁检测开关(锁上开路)
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
                    if (str1.equalsIgnoreCase("1")) {
                        stringBuffer.append("\r\n门检测开关(关门开路)【启用】\n");
                    } else {
                        stringBuffer.append("\r\n门检测开关(关门开路)【禁用】\n");
                    }
                    if (str2.equalsIgnoreCase("1")) {
                        stringBuffer.append("锁检测开关(锁上开路)【启用】\n");
                    } else {
                        stringBuffer.append("锁检测开关(锁上开路)【禁用】\n");
                    }
                    if (str3.equalsIgnoreCase("1")) {
                        stringBuffer.append("正常开锁不告警【启用】\n");
                    } else {
                        stringBuffer.append("正常开锁不告警【禁用】\n");
                    }
                    if (str4.equalsIgnoreCase("1")) {
                        stringBuffer.append("有外电可以进入维护方式【启用】\n");
                    } else {
                        stringBuffer.append("有外电可以进入维护方式【禁用】\n");
                    }
                    if (str5.equalsIgnoreCase("1")) {
                        stringBuffer.append("启用软关机【启用】\n");
                    } else {
                        stringBuffer.append("启用软关机【禁用】\n");
                    }
                    if (str6.equalsIgnoreCase("1")) {
                        stringBuffer.append("不检测强磁【启用】\n");
                    } else {
                        stringBuffer.append("不检测强磁【禁用】\n");
                    }
                    if (str7.equalsIgnoreCase("1")) {
                        stringBuffer.append("使用低磁检测阀值【启用】\n");
                    } else {
                        stringBuffer.append("使用低磁检测阀值【禁用】\n");
                    }
                    if (str8.equalsIgnoreCase("1")) {
                        stringBuffer.append("启用DEBUG软串口【启用】\n");
                    } else {
                        stringBuffer.append("启用DEBUG软串口【禁用】\n");
                    }
                    receive = stringBuffer.toString() + "\r\n";
                }
                break;
                //修改内部控制参数:68000000000001681000020087EB16
                case "87":
                    break;
            }
        }
        Message message = handler.obtainMessage(MESSAGE_2, "接收:" + receive);
        handler.sendMessage(message);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            this.finish();
        }
        return false;
    }

    @Override
    public void OnConnected() {
        Log.i(TAG, "成功建立连接!");
        Message message = handler.obtainMessage(MESSAGE_1, "");
        handler.sendMessage(message);
        //返回时告知该设备已成功连接
        setResult(2, new Intent());
    }

    @Override
    public void OnConnecting() {
        ProgressDialogUtil.ShowProgressDialog(CommandTest.this, false, "正在连接...");
    }

    @Override
    public void OnDisConnected() {
        Log.i(TAG, "连接已断开!");
        Message message = handler.obtainMessage(MESSAGE_ERROR_1, "");
        handler.sendMessage(message);
    }

    @Override
    public void OnWriteSuccess(byte[] byteArray) {
        String result = ConvertUtil.ByteArrayToHexStr(byteArray);
        result = ConvertUtil.HexStrAddCharacter(result, " ");
        String[] strArray = result.split(" ");
        String sendResult = "";
        /*
         * 特殊处理:读取内部存储的事件记录的通信协议和之前的协议不一样,需要留意
         * 1、指令为13个字节
         * 2、执行读取事件记录的时候_btn12控件禁用
         * 以上2个条件可以用来判断是否正在执行读取事件记录
         *
         * */
        if (strArray[8].equals("20") && byteArray.length == 13 && !_btn12.isEnabled()) {
            sendResult = "读取内部存储的事件记录";
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
            Message message = handler.obtainMessage(MESSAGE_2, "发送:" + ConvertUtil.StrArrayToStr(strArray));
            handler.sendMessage(message);
        }
        Log.i(TAG, "成功发送'" + sendResult + "'的指令");
    }

    @Override
    public void OnReadSuccess(byte[] byteArray) {
        String result = ConvertUtil.ByteArrayToHexStr(byteArray);
        result = ConvertUtil.HexStrAddCharacter(result, " ");
        Log.i(TAG, "接收:" + result);
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
            //开门
            case R.id.button1: {
                hexStr = "680000000000006810000100E116";
                logStr = "发送'开门'指令:" + hexStr;
            }
            break;
            //读卡
            case R.id.btn2: {
                hexStr = "680000000000006810000101E216";
                logStr = "发送'读卡'指令:" + hexStr;
            }
            break;
            //测量电池电压
            case R.id.btn3: {
                hexStr = "680000000000006810000102E316";
                logStr = "发送'测量电池电压'指令:" + hexStr;
            }
            break;
            //测量磁场强度
            case R.id.btn4: {
                hexStr = "680000000000006810000103E416";
                logStr = "发送'测量磁场强度'指令:" + hexStr;
            }
            break;
            //测量门状态
            case R.id.btn5: {
                hexStr = "680000000000006810000104E516";
                logStr = "发送'测量门状态'指令:" + hexStr;
            }
            break;
            //综合测试A
            case R.id.btn6: {
                hexStr = "680000000000006810000180E616";
                logStr = "发送'综合测试A'指令:" + hexStr;
            }
            break;
            //开一号门锁
            case R.id.btn7: {
                hexStr = "680000000000006810000181E716";
                logStr = "发送'开一号门锁'指令:" + hexStr;
            }
            break;
            //开二号门锁
            case R.id.btn8: {
                hexStr = "680000000000006810000182E816";
                logStr = "发送'开二号门锁'指令:" + hexStr;
            }
            break;
            //开全部门锁
            case R.id.btn9: {
                hexStr = "680000000000006810000183E916";
                logStr = "发送'开全部门锁'指令:" + hexStr;
            }
            break;
            //查询内部控制参数
            case R.id.btn10: {
                _isOpenParamSetting = false;
                hexStr = "680000000000006810000186EA16";
                logStr = "发送'查询内部控制参数'指令:" + hexStr;
            }
            break;
            //修改内部控制参数
            case R.id.btn11: {
                _isOpenParamSetting = true;
                //先查询内部控制参数,再打开修改参数的界面
                hexStr = "680000000000006810000186EA16";
                logStr = "发送'修改内部控制参数'指令:" + hexStr;
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
                                        if (_nextEvent > 0) {
                                            BluetoothUtil.SendComm("6800000000000068200101EC16");
                                            Log.i(TAG, "发送'读取内部存储的下一条记录'指令:6800000000000068200101EC16");
                                        } else {
                                            BluetoothUtil.SendComm("6800000000000068200100EC16");
                                            Log.i(TAG, "发送'读取内部存储的事件记录'指令:6800000000000068200100EC16");
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
        _btn1 = findViewById(R.id.btn1);
        _btn1.setOnClickListener(this);
        _btn2 = findViewById(R.id.btn2);
        _btn2.setOnClickListener(this);
        _btn3 = findViewById(R.id.btn3);
        _btn3.setOnClickListener(this);
        _btn4 = findViewById(R.id.btn4);
        _btn4.setOnClickListener(this);
        _btn5 = findViewById(R.id.btn5);
        _btn5.setOnClickListener(this);
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
        BluetoothUtil.Init(CommandTest.this, this);
        if (_bluetoothDevice != null) {
            Log.i(TAG, "开始连接...");
            BluetoothUtil.ConnectDevice(_bluetoothDevice, _uuidMap);
        } else {
            ProgressDialogUtil.ShowWarning(CommandTest.this, "警告", "未获取到蓝牙,请重试!");
        }
        InitListener();
    }

}
