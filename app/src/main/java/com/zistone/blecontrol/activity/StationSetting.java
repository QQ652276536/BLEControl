package com.zistone.blecontrol.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zistone.blecontrol.R;
import com.zistone.blecontrol.util.BluetoothListener;
import com.zistone.blecontrol.util.BluetoothUtil;
import com.zistone.blecontrol.util.MyConvertUtil;
import com.zistone.blecontrol.util.ProgressDialogUtil;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;

public class StationSetting extends AppCompatActivity implements View.OnClickListener, BluetoothListener {
    private static final String TAG = "StationSetting";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_1 = 1;
    private static final int MESSAGE_2 = 2;
    private static final int MESSAGE_ERROR_1 = -1;

    private ImageButton _btnReturn, _btnTop, _btnBottom, _btnClear;
    private Button _btn1, _btn2, _btn3, _btn4, _btn5, _btn6;
    private EditText _edt1, _edt2, _edt3, _edt4, _edt5;
    private TextView _txt1;
    private MyHandler _myHandler;
    private StringBuffer _stringBuffer = new StringBuffer();
    private BluetoothDevice _bluetoothDevice;
    private Map<String, UUID> _uuidMap;

    static class MyHandler extends Handler {
        private WeakReference<StationSetting> _weakReference;
        private StationSetting _stationSetting;

        public MyHandler(StationSetting activity) {
            _weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            if (_weakReference.get() == null)
                return;
            _stationSetting = _weakReference.get();
            String result = (String) message.obj;
            switch (message.what) {
                case MESSAGE_1:
                    _stationSetting.SetButtonEnable(true);
                    ProgressDialogUtil.Dismiss();
                    break;
                case MESSAGE_2: {
                    _stationSetting._txt1.append(result + "\r\n");
                    //定位到最后一行
                    int offset = _stationSetting._txt1.getLineCount() * _stationSetting._txt1.getLineHeight();
                    //如果文本的高度大于ScrollView的,就自动滑动
                    if (offset > _stationSetting._txt1.getHeight()) {
                        _stationSetting._txt1.scrollTo(0, offset - _stationSetting._txt1.getHeight());
                    }
                }
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
        //长度不够后面解析会引发异常
        if (strArray.length < 18) {
            Log.e(TAG, "指令长度" + strArray.length + "错误，不予解析！");
            return;
        }
        String receive = data.trim() + "\r\n解析：";
        //设置IP及端口
        if (strArray[8].equals("30")) {
            receive += "定位失败";
        }
        //设置WIFI名称
        else if (strArray[8].equals("31")) {
            receive = data + "\r\n解析：";
            receive += "定位失败";
        }
        //设置WIFI密码
        else if (strArray[8].equals("32")) {
            receive = data + "\r\n解析：";
            receive += "定位失败";
        }
        //设置基站位置坐标
        else if (strArray[8].equals("33")) {
            receive = data + "\r\n解析：";
            receive += "定位失败";
        }
        //设置基站超时参数
        else if (strArray[8].equals("34")) {
            receive = data + "\r\n解析：";
            receive += "定位失败";
        }
        //查询基站状态
        else if (strArray[8].equals("35")) {
            receive = data + "\r\n解析：";
            receive += "定位失败";
        }
        _myHandler.obtainMessage(MESSAGE_2, "接收：" + receive).sendToTarget();
    }

    private void SetButtonEnable(boolean flag) {
        _btn1.setEnabled(flag);
        _btn2.setEnabled(flag);
        _btn3.setEnabled(flag);
        _btn4.setEnabled(flag);
        _btn5.setEnabled(flag);
        _btn6.setEnabled(flag);
    }

    /**
     * 判断要指令里的数据内容是否超过最大长度
     *
     * @param cmd  指令
     * @param data Str数据
     * @return Hex指令
     */
    private String IfDataIsTooLong(String cmd, String data) {
        String result = "";
        //因为计算校验码不需要68，为了方便计算校验码在后面加上68
        String str1 = "000000000068" + cmd;
        String hexData = MyConvertUtil.StrToHexStr(data);
        //后面最多只能存储8位，超过8位则需要存储在前面，这里计算的是字符串的长度，除以2即字节长度
        int len = hexData.length() / 2;
        //不超过8位则直接在后面拼接，前面部分表示长度的位数为0
        if (len <= 8) {
            String hexLen = MyConvertUtil.IntToHexStr(len);
            String str2 = "00" + str1 + hexLen + data;
            String checkCode = MyConvertUtil.CreateCheckCode(str2);
            result = "68" + str2 + checkCode + "16";
        }
        //超过8位但不超过13位除了后面拼接，前面表示长度的为总长度减8，后面再拼接剩余字段
        else if (len > 8 && len <= 13) {
            String secondLen = MyConvertUtil.IntToHexStr(len - 8);
            //这里是当作字符串来截取，对应下标为8的那一位
            String firstStr = data.substring(0, 16);
            String secondStr = data.substring(16);
            //不足5位后面补0
            int supplement = 5 - secondStr.length() / 2;
            for (int i = 0; i < supplement; i++) {
                secondStr += "00";
            }
            String str2 = secondLen + secondStr + str1 + "08" + firstStr;
            String checkCode = MyConvertUtil.CreateCheckCode(str2);
            result = "68" + str2 + checkCode + "16";
        } else {
            result = "";
        }
        return result;
    }

    @Override
    public void OnConnected() {
        Log.i(TAG, "成功建立连接！");
        _myHandler.obtainMessage(MESSAGE_1, "").sendToTarget();
        //返回时告知该设备已成功连接
        setResult(2, new Intent());
    }

    @Override
    public void OnConnecting() {
        ProgressDialogUtil.ShowProgressDialog(StationSetting.this, true, "正在连接...");
    }

    @Override
    public void OnDisConnected() {
        Log.i(TAG, "连接已断开！");
        _myHandler.obtainMessage(MESSAGE_ERROR_1, "").sendToTarget();
    }

    @Override
    public void OnWriteSuccess(byte[] byteArray) {
        String result = MyConvertUtil.ByteArrayToHexStr(byteArray);
        result = MyConvertUtil.HexStrAddCharacter(result, " ");
        String[] strArray = result.split(" ");
        String sendResult = "";

        String indexStr = strArray[11];
        switch (indexStr) {
            case "30":
                sendResult = "设置IP及端口";
                break;
            case "31":
                sendResult = "设置WIFI名称";
                break;
            case "32":
                sendResult = "设置WIFI密码";
                break;
            case "33":
                sendResult = "设置基站位置坐标";
                break;
            case "34":
                sendResult = "设置基站超时参数";
                break;
            case "35":
                sendResult = "查询基站状态";
                break;
        }
        _myHandler.obtainMessage(MESSAGE_2, "\r\n发送：" + MyConvertUtil.StrArrayToStr(strArray)).sendToTarget();
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
    public void onClick(View v) {
        String hexStr = "";
        String logStr = "";
        String edtStr = "";
        switch (v.getId()) {
            case R.id.btnReturn_station:
                this.finish();
                break;
            case R.id.btnTop_station:
                _txt1.scrollTo(0, 0);
                break;
            case R.id.btnBottom_station:
                int offset = _txt1.getLineCount() * _txt1.getLineHeight();
                if (offset > _txt1.getHeight()) {
                    _txt1.scrollTo(0, offset - _txt1.getHeight());
                }
                break;
            case R.id.btnClear_station:
                _txt1.setText("");
                break;
            /*
             * 设置/查询IP及端口【30】
             *
             * 指令示例：68 03 00 00 00 00 00 68 30 0B 00 00 00 00 00 00 00 00 B8 16
             * 68
             * 03（多余的字符个数，这个字节不计入DAT_R位数里）
             * 00 00 00 00 00（DAT_R的9~13位）
             * 68
             * 30（表示设置/查询IP及端口指令）
             * 0B（Len）
             * 00 00 00 00 00 00 00 00（DAT_R的1~8位）
             * B8（校验码）
             *
             * */
            case R.id.btn1_station:
                edtStr = _edt1.getText().toString();
                if (edtStr.equals("")) {
                    //计算校验码不需要加上68
                    String str1 = "0000000000006830000000000000000000";
                    String checkCode = MyConvertUtil.CreateCheckCode(str1);
                    hexStr = "68" + str1 + checkCode + "16";
                    logStr = "发送'查询IP及端口'指令：" + hexStr;
                } else {
                    String[] edtStrArray = edtStr.split(",");
                    if (edtStrArray.length == 2) {
                        String ip = edtStrArray[0];
                        String[] ipArray = ip.split(".");
                        String ipStr1 = MyConvertUtil.IntToHexStr(Integer.valueOf(ipArray[0]));
                        String ipStr2 = MyConvertUtil.IntToHexStr(Integer.valueOf(ipArray[1]));
                        String ipStr3 = MyConvertUtil.IntToHexStr(Integer.valueOf(ipArray[2]));
                        String ipStr4 = MyConvertUtil.IntToHexStr(Integer.valueOf(ipArray[3]));
                        String port = edtStrArray[1];
                        String[] portArray = MyConvertUtil.StrAddCharacter(port, " ").split(" ");
                        String port1 = MyConvertUtil.IntToHexStr(Integer.valueOf(portArray[0]));
                        String port2 = MyConvertUtil.IntToHexStr(Integer.valueOf(portArray[1]));
                        //计算校验码不需要加上68
                        String str1 = "000000000000683006";
                        String str2 = ipStr1 + ipStr2 + ipStr3 + ipStr4 + port1 + port2 + "00" + "00";
                        String checkCode = MyConvertUtil.CreateCheckCode(str1 + str2);
                        hexStr = "68" + str1 + str2 + checkCode + "16";
                        logStr = "发送'设置IP及端口'指令：" + hexStr;
                    } else {
                        _txt1.append("IP及端口错误，请检查！");
                        return;
                    }
                }
                break;
            /*
             * 设置/查询WIFI名称【31】
             *
             * */
            case R.id.btn2_station:
                edtStr = _edt2.getText().toString();
                if (edtStr.equals("")) {
                    //计算校验码不需要加上68
                    String str1 = "0000000000006831000000000000000000";
                    String checkCode = MyConvertUtil.CreateCheckCode(str1);
                    hexStr = "68" + str1 + checkCode + "16";
                    logStr = "发送'查询WIFI名称'指令：" + hexStr;
                } else {
                    hexStr = IfDataIsTooLong("31", edtStr);
                    if (!hexStr.equals("")) {
                        logStr = "发送'设置WIFI名称'指令：" + hexStr;
                    } else {
                        _txt1.append("WIFI名称错误，尽量以简短英文命名！");
                        return;
                    }
                }
                break;
            /*
             * 设置/查询WIFI密码【32】
             *
             * */
            case R.id.btn3_station:
                edtStr = _edt3.getText().toString();
                if (edtStr.equals("")) {
                    //计算校验码不需要加上68
                    String str1 = "0000000000006832000000000000000000";
                    String checkCode = MyConvertUtil.CreateCheckCode(str1);
                    hexStr = "68" + str1 + checkCode + "16";
                    logStr = "发送'查询WIFI密码'指令：" + hexStr;
                } else {
                    hexStr = IfDataIsTooLong("32", edtStr);
                    if (!hexStr.equals("")) {
                        logStr = "发送'设置WIFI密码'指令：" + hexStr;
                    } else {
                        _txt1.append("WIFI密码错误，请检查！");
                        return;
                    }
                }
                break;
            /*
             * 设置/查询基站位置坐标【33】
             *
             * */
            case R.id.btn4_station:
                edtStr = _edt4.getText().toString();
                if (edtStr.equals("")) {
                    //计算校验码不需要加上68
                    String str1 = "0000000000006833000000000000000000";
                    String checkCode = MyConvertUtil.CreateCheckCode(str1);
                    hexStr = "68" + str1 + checkCode + "16";
                    logStr = "发送'查询基站位置坐标'指令：" + hexStr;
                } else {
                    String[] edtStrArray = edtStr.split(",");
                    if (edtStrArray.length == 4) {
                        String typeStr = MyConvertUtil.IntToHexStr(Integer.valueOf(edtStrArray[0]));
                        String latStr = MyConvertUtil.IntToHexStr(Integer.valueOf(edtStrArray[1]));
                        String lotStr = MyConvertUtil.IntToHexStr(Integer.valueOf(edtStrArray[2]));
                        String altStr = MyConvertUtil.IntToHexStr(Integer.valueOf(edtStrArray[3]));
                    } else {
                        _txt1.append("位置坐标错误，请检查！");
                        return;
                    }
                    hexStr = IfDataIsTooLong("33", edtStr);
                    if (!hexStr.equals("")) {
                        logStr = "发送'设置基站位置坐标'指令：" + hexStr;
                    } else {
                    }
                }
                break;
            /*
             * 设置/查询基站超时参数【34】
             *
             * */
            case R.id.btn5_station:
                edtStr = _edt5.getText().toString();
                break;
            /*
             * 查询基站状态【35】
             *
             * */
            case R.id.btn6_station:
                break;
        }
        if (!hexStr.equals("")) {
            BluetoothUtil.SendComm(hexStr);
            Log.i(TAG, logStr);
        }
    }

    @Override
    public void onDestroy() {
        ProgressDialogUtil.Dismiss();
        BluetoothUtil.DisConnGatt();
        _bluetoothDevice = null;
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _myHandler = new MyHandler(this);
        setContentView(R.layout.activity_station_setting);
        Intent intent = getIntent();
        _bluetoothDevice = intent.getParcelableExtra(ARG_PARAM1);
        _uuidMap = (Map<String, UUID>) intent.getSerializableExtra(ARG_PARAM2);
        _btnReturn = findViewById(R.id.btnReturn_station);
        _btnTop = findViewById(R.id.btnTop_station);
        _btnBottom = findViewById(R.id.btnBottom_station);
        _btnClear = findViewById(R.id.btnClear_station);
        _btn1 = findViewById(R.id.btn1_station);
        _btn2 = findViewById(R.id.btn2_station);
        _btn3 = findViewById(R.id.btn3_station);
        _btn4 = findViewById(R.id.btn4_station);
        _btn5 = findViewById(R.id.btn5_station);
        _btn6 = findViewById(R.id.btn6_station);
        _btnReturn.setOnClickListener(this::onClick);
        _btnTop.setOnClickListener(this::onClick);
        _btnBottom.setOnClickListener(this::onClick);
        _btnClear.setOnClickListener(this::onClick);
        _btn1.setOnClickListener(this::onClick);
        _btn2.setOnClickListener(this::onClick);
        _btn3.setOnClickListener(this::onClick);
        _btn4.setOnClickListener(this::onClick);
        _btn5.setOnClickListener(this::onClick);
        _btn6.setOnClickListener(this::onClick);
        _edt1 = findViewById(R.id.edt1_station);
        _edt2 = findViewById(R.id.edt2_station);
        _edt3 = findViewById(R.id.edt3_station);
        _edt4 = findViewById(R.id.edt4_station);
        _edt5 = findViewById(R.id.edt5_station);
        _txt1 = findViewById(R.id.txt1_station);
        BluetoothUtil.Init(StationSetting.this, this);
        if (_bluetoothDevice != null) {
            Log.i(TAG, "开始连接...");
            BluetoothUtil.ConnectDevice(_bluetoothDevice, _uuidMap);
        } else {
            ProgressDialogUtil.ShowWarning(StationSetting.this, "警告", "未获取到蓝牙,请重试！");
        }
    }

}
