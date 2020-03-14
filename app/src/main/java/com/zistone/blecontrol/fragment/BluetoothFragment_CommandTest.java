package com.zistone.blecontrol.fragment;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zistone.MainActivity;
import com.zistone.blecontrol.R;
import com.zistone.blecontrol.dialogfragment.DialogFragment_ParamSetting;
import com.zistone.blecontrol.util.BTListener;
import com.zistone.blecontrol.util.BTUtil;
import com.zistone.blecontrol.util.ConvertUtil;
import com.zistone.blecontrol.util.ProgressDialogUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

public class BluetoothFragment_CommandTest extends Fragment implements View.OnClickListener, BTListener {
    private static final String TAG = "BluetoothFragment_CommandTest";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_1 = 1;
    private static final int MESSAGE_2 = 2;
    private static final int MESSAGE_ERROR_1 = -1;
    private static final int SEND_SET_CONTROLPARAM = 87;
    private static final int RECEIVE_SEARCH_CONTROLPARAM = 8602;

    private OnFragmentInteractionListener _onFragmentInteractionListener;
    private Context _context;
    private View _view;
    private ImageButton _btnReturn;
    private TextView _txt;
    private Button _btn0, _btn1, _btn2, _btn3, _btn4, _btn5, _btn6, _btn7, _btn8, _btn9, _btn10, _btn11;
    private BluetoothDevice _bluetoothDevice;
    private StringBuffer _stringBuffer = new StringBuffer();
    private Map<String, UUID> _uuidMap;
    private DialogFragment_ParamSetting _paramSetting;
    //是否连接成功、是否打开参数设置界面
    private boolean _connectedSuccess = false, _isOpenParamSetting = false;

    @Override
    public void OnConnected() {
        Log.d(TAG, ">>>成功建立连接!");
        Message message = handler.obtainMessage(MESSAGE_1, "");
        handler.sendMessage(message);
    }

    @Override
    public void OnConnecting() {
        ProgressDialogUtil.ShowProgressDialog(_context, "正在连接...");
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
        String sendResult = "";
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
        Log.d(TAG, ">>>发送:" + sendResult);
        Message message = handler.obtainMessage(MESSAGE_2, "发送:" + ConvertUtil.StrArrayToStr(strArray));
        handler.sendMessage(message);
    }

    @Override
    public void OnReadSuccess(byte[] byteArray) {
        String result = ConvertUtil.ByteArrayToHexStr(byteArray);
        result = ConvertUtil.HexStrAddCharacter(result, " ");
        Log.d(TAG, ">>>接收:" + result);
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

    public static BluetoothFragment_CommandTest newInstance(BluetoothDevice bluetoothDevice, Map<String, UUID> map) {
        BluetoothFragment_CommandTest fragment = new BluetoothFragment_CommandTest();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, bluetoothDevice);
        args.putSerializable(ARG_PARAM2, (Serializable) map);
        fragment.setArguments(args);
        return fragment;
    }

    private View.OnKeyListener backListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
                getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
                getFragmentManager().beginTransaction().remove(BluetoothFragment_CommandTest.this).commitNow();
                return true;
            }
            return false;
        }
    };

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
                    Log.d(TAG, String.format(">>>收到查询到的参数(Bit):\n门检测开关(关门开路)%s\n锁检测开关(锁上开路)%s\n正常开锁不告警%s\n有外电可以进入维护方式%s\n启用软关机%s\n不检测强磁%s\n使用低磁检测阀值%s\n启用DEBUG软串口%s", bitStr1, bitStr2, bitStr3, bitStr4, bitStr5, bitStr6, bitStr7, bitStr8));
                    //打开控制参数修改页面的时候将查询结果传递过去,此时可以不输出调试信息
                    if (_isOpenParamSetting) {
                        if (_paramSetting == null) {
                            _paramSetting = DialogFragment_ParamSetting.newInstance(new String[]{
                                    bitStr1, bitStr2, bitStr3, bitStr4, bitStr5, bitStr6, bitStr7, bitStr8
                            });
                            _paramSetting.setTargetFragment(BluetoothFragment_CommandTest.this, 1);
                        }
                        _paramSetting.show(getFragmentManager(), "DialogFragment_ParamSetting");
                    }
                }
                break;
                //修改内部控制参数
                case SEND_SET_CONTROLPARAM: {
                    Log.d(TAG, ">>>发送参数设置:" + result);
                    BTUtil.SendComm(result);
                    int offset = _txt.getLineCount() * _txt.getLineHeight();
                    if (offset > _txt.getHeight()) {
                        _txt.scrollTo(0, offset - _txt.getHeight());
                    }
                }
                break;
                case MESSAGE_ERROR_1:
                    _connectedSuccess = false;
                    ProgressDialogUtil.Dismiss();
                    ProgressDialogUtil.ShowWarning(_context, "警告", "该设备的连接已断开,如需再次连接请重试!");
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
                }
                break;
            }
        }
    };

    /**
     * Activity中加载Fragment时会要求实现onFragmentInteraction(Uri uri)方法,此方法主要作用是从fragment向activity传递数据
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void onButtonPressed(Uri uri) {
        if (_onFragmentInteractionListener != null) {
            _onFragmentInteractionListener.onFragmentInteraction(uri);
        }
    }

    private void Resolve(String data) {
        Log.d(TAG, ">>>共接收:" + data);
        String[] strArray = data.split(" ");
        String indexStr = strArray[12];
        String receive = ConvertUtil.StrArrayToStr(strArray);
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
                //打开控制参数修改页面的时候将查询结果传递过去,此时可以不输出调试信息
                if (_isOpenParamSetting) {
                    Message message = handler.obtainMessage(RECEIVE_SEARCH_CONTROLPARAM, strArray[13]);
                    handler.sendMessage(message);
                    _isOpenParamSetting = false;
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
            //修改内部控制参数:68,00,00,00,00,00,01,68,10,00,02,00,87,E3,16
            case "87":
                break;
        }
        Message message = handler.obtainMessage(MESSAGE_2, "接收:" + receive);
        handler.sendMessage(message);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_return: {
                BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
                getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
                getFragmentManager().beginTransaction().remove(BluetoothFragment_CommandTest.this).commitNow();
            }
            break;
            //清屏
            case R.id.btn0:
                _txt.setText("");
                break;
            //开门
            case R.id.button1: {
                String hexStr = "680000000000006810000100E116";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
            }
            break;
            //读卡
            case R.id.btn2: {
                String hexStr = "680000000000006810000101E216";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
            }
            break;
            //测量电池电压
            case R.id.btn3: {
                String hexStr = "680000000000006810000102E316";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
            }
            break;
            //测量磁场强度
            case R.id.btn4: {
                String hexStr = "680000000000006810000103E416";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
            }
            break;
            //测量门状态
            case R.id.btn5: {
                String hexStr = "680000000000006810000104E516";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
            }
            break;
            //综合测试A
            case R.id.btn6: {
                String hexStr = "680000000000006810000180E616";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
            }
            break;
            //开一号门锁
            case R.id.btn7: {
                String hexStr = "680000000000006810000181E716";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
            }
            break;
            //开二号门锁
            case R.id.btn8: {
                String hexStr = "680000000000006810000182E816";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
            }
            break;
            //开全部门锁
            case R.id.btn9: {
                String hexStr = "680000000000006810000183E916";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
            }
            break;
            //查询内部控制参数
            case R.id.btn10: {
                String hexStr = "680000000000006810000186EA16";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
            }
            break;
            //修改内部控制参数
            case R.id.btn11: {
                //先查询内部控制参数,再打开修改参数的界面
                String hexStr = "680000000000006810000186EA16";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
                _isOpenParamSetting = true;

                //                String hexStr = "6800000000000068100005877F000000EA16";
                //                Log.d(TAG, ">>>发送:" + hexStr);
                //                BTUtil.SendComm(hexStr);
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (_connectedSuccess) {
            switch (requestCode) {
                case MainActivity.ACTIVITYRESULT_WRITEVALUE: {
                    String hexStr = data.getStringExtra("WriteValue");
                }
                break;
                case MainActivity.ACTIVITYRESULT_PARAMSETTING: {
                    String hexStr = data.getStringExtra("ParamSetting");
                    Message message = handler.obtainMessage(SEND_SET_CONTROLPARAM, hexStr);
                    handler.sendMessage(message);
                }
                break;
                case MainActivity.ACTIVITYRESULT_OTA: {
                    String hexStr = data.getStringExtra("OTA");
                }
                break;
            }
        } else {
            ProgressDialogUtil.ShowWarning(_context, "警告", "该设备的连接已断开,如需再次连接请重试!");
        }
        //发送内部参数以后关闭设置窗口
        _paramSetting.dismiss();
        _paramSetting = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            _bluetoothDevice = getArguments().getParcelable(ARG_PARAM1);
            _uuidMap = (Map<String, UUID>) getArguments().getSerializable(ARG_PARAM2);
        }
        _context = getContext();
        BTUtil.Init(_context, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _view = inflater.inflate(R.layout.fragment_bluetooth_cmd, container, false);
        //强制获得焦点
        _view.requestFocus();
        _view.setFocusable(true);
        _view.setFocusableInTouchMode(true);
        _view.setOnKeyListener(backListener);
        _btnReturn = _view.findViewById(R.id.btn_return);
        _btnReturn.setOnClickListener(this);
        _txt = _view.findViewById(R.id.txtView);
        _txt.setMovementMethod(ScrollingMovementMethod.getInstance());
        _btn0 = _view.findViewById(R.id.btn0);
        _btn0.setOnClickListener(this);
        _btn1 = _view.findViewById(R.id.button1);
        _btn1.setOnClickListener(this);
        _btn2 = _view.findViewById(R.id.btn2);
        _btn2.setOnClickListener(this);
        _btn3 = _view.findViewById(R.id.btn3);
        _btn3.setOnClickListener(this);
        _btn4 = _view.findViewById(R.id.btn4);
        _btn4.setOnClickListener(this);
        _btn5 = _view.findViewById(R.id.btn5);
        _btn5.setOnClickListener(this);
        _btn6 = _view.findViewById(R.id.btn6);
        _btn6.setOnClickListener(this);
        _btn7 = _view.findViewById(R.id.btn7);
        _btn7.setOnClickListener(this);
        _btn8 = _view.findViewById(R.id.btn8);
        _btn8.setOnClickListener(this);
        _btn9 = _view.findViewById(R.id.btn9);
        _btn9.setOnClickListener(this);
        _btn10 = _view.findViewById(R.id.btn10);
        _btn10.setOnClickListener(this);
        _btn11 = _view.findViewById(R.id.btn11);
        _btn11.setOnClickListener(this);
        if (_bluetoothDevice != null) {
            Log.d(TAG, ">>>开始连接...");
            BTUtil.ConnectDevice(_bluetoothDevice, _uuidMap);
        } else {
            ProgressDialogUtil.ShowWarning(_context, "警告", "未获取到蓝牙,请重试!");
        }
        return _view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            _onFragmentInteractionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _onFragmentInteractionListener = null;
        BTUtil.DisConnGatt();
        _bluetoothDevice = null;
        if (_paramSetting != null) {
            _paramSetting.dismiss();
            _paramSetting = null;
        }
    }
}