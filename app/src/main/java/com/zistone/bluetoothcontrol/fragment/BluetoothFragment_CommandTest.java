package com.zistone.bluetoothcontrol.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zistone.bluetoothcontrol.R;
import com.zistone.bluetoothcontrol.util.BTListener;
import com.zistone.bluetoothcontrol.util.BTUtil;
import com.zistone.bluetoothcontrol.util.ConvertUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

public class BluetoothFragment_CommandTest extends Fragment implements View.OnClickListener, BTListener
{
    private static final String TAG = "BluetoothFragment_CommandTest";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_1 = 1;
    private static final int MESSAGE_2 = 2;
    private static final int MESSAGE_ERROR_1 = -1;
    private OnFragmentInteractionListener _onFragmentInteractionListener;
    private Context _context;
    private View _view;
    private ImageButton _btnReturn;
    private TextView _textView;
    private Button _button0, _button1, _button2, _button3, _button4, _button5, _button6, _button7, _button8, _button9, _button10, _button11;
    private ProgressBar _progressBar;
    private BluetoothDevice _bluetoothDevice;
    private StringBuffer _stringBuffer = new StringBuffer();
    private Map<String, UUID> _uuidMap;

    @Override
    public void OnConnected()
    {
        Log.d(TAG, ">>>成功建立连接!");
        Message message = new Message();
        message.what = MESSAGE_1;
        handler.sendMessage(message);
    }

    @Override
    public void OnConnecting()
    {
    }

    @Override
    public void OnDisConnected()
    {
        Log.d(TAG, ">>>连接已断开!");
        Message message = new Message();
        message.what = MESSAGE_ERROR_1;
        handler.sendMessage(message);
    }

    @Override
    public void OnWriteSuccess(byte[] byteArray)
    {
        String result = ConvertUtil.ByteArrayToHexStr(byteArray);
        result = ConvertUtil.HexStrAddCharacter(result, " ");
        String[] strArray = result.split(" ");
        String sendResult = "";
        String indexStr = strArray[11];
        switch(indexStr)
        {
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
        Log.d(TAG, ">>>" + sendResult);
        Message message = new Message();
        message.what = MESSAGE_2;
        //                    message.obj = sendResult;
        message.obj = "发送:" + ConvertUtil.StrArrayToStr(strArray);
        handler.sendMessage(message);
    }

    @Override
    public void OnReadSuccess(byte[] byteArray)
    {
        String result = ConvertUtil.ByteArrayToHexStr(byteArray);
        result = ConvertUtil.HexStrAddCharacter(result, " ");
        Log.d(TAG, ">>>接收:" + result);
        String[] strArray = result.split(" ");
        //一个包(20个字节)
        if(strArray[0].equals("68") && strArray[strArray.length - 1].equals("16"))
        {
            Resolve(result);
            //清空缓存
            _stringBuffer = new StringBuffer();
        }
        //分包
        else
        {
            if(!strArray[strArray.length - 1].equals("16"))
            {
                _stringBuffer.append(result + " ");
            }
            //最后一个包
            else
            {
                _stringBuffer.append(result);
                result = _stringBuffer.toString();
                Resolve(result);
                //清空缓存
                _stringBuffer = new StringBuffer();
            }
        }
    }

    public static BluetoothFragment_CommandTest newInstance(BluetoothDevice bluetoothDevice, Map<String, UUID> map)
    {
        BluetoothFragment_CommandTest fragment = new BluetoothFragment_CommandTest();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, bluetoothDevice);
        args.putSerializable(ARG_PARAM2, (Serializable) map);
        fragment.setArguments(args);
        return fragment;
    }

    private View.OnKeyListener backListener = new View.OnKeyListener()
    {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
            {
                BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
                getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
                getFragmentManager().beginTransaction().remove(BluetoothFragment_CommandTest.this).commitNow();
                return true;
            }
            return false;
        }
    };

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            super.handleMessage(message);
            String result = (String) message.obj;
            switch(message.what)
            {
                case MESSAGE_ERROR_1:
                    _progressBar.setVisibility(View.INVISIBLE);
                    ShowWarning(1);
                    break;
                case MESSAGE_1:
                {
                    _button1.setEnabled(true);
                    _button2.setEnabled(true);
                    _button3.setEnabled(true);
                    _button4.setEnabled(true);
                    _button5.setEnabled(true);
                    _button6.setEnabled(true);
                    _button7.setEnabled(true);
                    _button8.setEnabled(true);
                    _button9.setEnabled(true);
                    _button10.setEnabled(true);
                    _button11.setEnabled(true);
                    _button1.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    _button2.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    _button3.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    _button4.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    _button5.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    _button6.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    _button7.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    _button8.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    _button9.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    _button10.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    _button11.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    _progressBar.setVisibility(View.INVISIBLE);
                    break;
                }
                case MESSAGE_2:
                {
                    _textView.append("\r\n" + result);
                    //定位到最后一行
                    int offset = _textView.getLineCount() * _textView.getLineHeight();
                    //如果文本的高度大于ScrollView的,就自动滑动
                    if(offset > _textView.getHeight())
                    {
                        _textView.scrollTo(0, offset - _textView.getHeight());
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    /**
     * Activity中加载Fragment时会要求实现onFragmentInteraction(Uri uri)方法,此方法主要作用是从fragment向activity传递数据
     */
    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    public void onButtonPressed(Uri uri)
    {
        if(_onFragmentInteractionListener != null)
        {
            _onFragmentInteractionListener.onFragmentInteraction(uri);
        }
    }

    private void Resolve(String data)
    {
        Log.d(TAG, ">>>共接收:" + data);
        String[] strArray = data.split(" ");
        String indexStr = strArray[12];
        String receive = ConvertUtil.StrArrayToStr(strArray);
        switch(indexStr)
        {
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
            case "80":
            {
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
                break;
            }
            //开一号门锁:68,00,00,00,00,00,01,68,10,00,03,0E,81,03,76,16
            case "81":
            {
                String result = strArray[13];
                byte[] bytes = ConvertUtil.HexStrToByteArray(result);
                String bitStr = ConvertUtil.ByteToBit(bytes[0]);
                String doorState1 = String.valueOf(bitStr.charAt(7));
                String lockState1 = String.valueOf(bitStr.charAt(6));
                break;
            }
            //开二号门锁:68,00,00,00,00,00,01,68,10,00,03,0E,82,03,77,16
            case "82":
            {
                String result = strArray[13];
                byte[] bytes = ConvertUtil.HexStrToByteArray(result);
                String bitStr = ConvertUtil.ByteToBit(bytes[0]);
                String doorState1 = String.valueOf(bitStr.charAt(7));
                String lockState1 = String.valueOf(bitStr.charAt(6));
                break;
            }
            //开全部门锁:68,00,00,00,00,00,01,68,10,00,03,0E,83,03,78,16
            case "83":
            {
                String result = strArray[13];
                byte[] bytes = ConvertUtil.HexStrToByteArray(result);
                String bitStr = ConvertUtil.ByteToBit(bytes[0]);
                String doorState1 = String.valueOf(bitStr.charAt(7));
                String lockState1 = String.valueOf(bitStr.charAt(6));
                String doorState2 = String.valueOf(bitStr.charAt(5));
                String lockState2 = String.valueOf(bitStr.charAt(4));
                break;
            }
            //查询内部控制参数:68,00,00,00,00,00,01,68,10,00,06,00,86,00,00,00,00,6D,16
            case "86":
            {
                byte[] bytes1 = ConvertUtil.HexStrToByteArray(strArray[13]);
                String bitStr = ConvertUtil.ByteToBit(bytes1[0]);
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
                if(str1.equalsIgnoreCase("1"))
                {
                    stringBuffer.append("\r\n门检测开关(关门开路)【启用】\n");
                }
                else
                {
                    stringBuffer.append("\r\n门检测开关(关门开路)【禁用】\n");
                }
                if(str2.equalsIgnoreCase("1"))
                {
                    stringBuffer.append("锁检测开关(锁上开路)【启用】\n");
                }
                else
                {
                    stringBuffer.append("锁检测开关(锁上开路)【禁用】\n");
                }
                if(str3.equalsIgnoreCase("1"))
                {
                    stringBuffer.append("正常开锁不告警【启用】\n");
                }
                else
                {
                    stringBuffer.append("正常开锁不告警【禁用】\n");
                }
                if(str4.equalsIgnoreCase("1"))
                {
                    stringBuffer.append("有外电可以进入维护方式【启用】\n");
                }
                else
                {
                    stringBuffer.append("有外电可以进入维护方式【禁用】\n");
                }
                if(str5.equalsIgnoreCase("1"))
                {
                    stringBuffer.append("启用软关机【启用】\n");
                }
                else
                {
                    stringBuffer.append("启用软关机【禁用】\n");
                }
                if(str6.equalsIgnoreCase("1"))
                {
                    stringBuffer.append("不检测强磁【启用】\n");
                }
                else
                {
                    stringBuffer.append("不检测强磁【禁用】\n");
                }
                if(str7.equalsIgnoreCase("1"))
                {
                    stringBuffer.append("使用低磁检测阀值【启用】\n");
                }
                else
                {
                    stringBuffer.append("使用低磁检测阀值【禁用】\n");
                }
                if(str8.equalsIgnoreCase("1"))
                {
                    stringBuffer.append("启用DEBUG软串口【启用】\n");
                }
                else
                {
                    stringBuffer.append("启用DEBUG软串口【禁用】\n");
                }
                receive = stringBuffer.toString() + "\r\n";
                break;
            }
            //修改内部控制参数:68,00,00,00,00,00,01,68,10,00,02,00,87,E3,16
            case "87":
            {
                break;
            }
        }
        Message message = new Message();
        message.what = MESSAGE_2;
        message.obj = "接收:" + receive;
        handler.sendMessage(message);
    }

    private void ShowWarning(int param)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setTitle("警告");
        switch(param)
        {
            case 1:
                builder.setMessage("该设备的连接已断开!请重试!");
                builder.setPositiveButton("知道了", (dialog, which) ->
                {

                    BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
                    getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
                    getFragmentManager().beginTransaction().remove(BluetoothFragment_CommandTest.this).commitNow();
                });
                break;
            case 2:
                builder.setMessage("未获取到蓝牙,请重试!");
                builder.setPositiveButton("知道了", (dialog, which) ->
                {

                    BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
                    getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
                    getFragmentManager().beginTransaction().remove(BluetoothFragment_CommandTest.this).commitNow();
                });
                break;
        }
        builder.show();
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.btn_return:
            {
                BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
                getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
                getFragmentManager().beginTransaction().remove(BluetoothFragment_CommandTest.this).commitNow();
                break;
            }
            //清屏
            case R.id.btn0:
                _textView.setText("");
                break;
            //开门
            case R.id.button1:
            {
                String hexStr = "680000000000006810000100E116";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
                break;
            }
            //读卡
            case R.id.btn2:
            {
                String hexStr = "680000000000006810000101E216";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
                break;
            }
            //测量电池电压
            case R.id.btn3:
            {
                String hexStr = "680000000000006810000102E316";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
                break;
            }
            //测量磁场强度
            case R.id.btn4:
            {
                String hexStr = "680000000000006810000103E416";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
                break;
            }
            //测量门状态
            case R.id.btn5:
            {
                String hexStr = "680000000000006810000104E516";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
                break;
            }
            //综合测试A
            case R.id.btn6:
            {
                String hexStr = "680000000000006810000180E616";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
                break;
            }
            //开一号门锁
            case R.id.btn7:
            {
                String hexStr = "680000000000006810000181E716";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
                break;
            }
            //开二号门锁
            case R.id.btn8:
            {
                String hexStr = "680000000000006810000182E816";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
                break;
            }
            //开全部门锁
            case R.id.btn9:
            {
                String hexStr = "680000000000006810000183E916";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
                break;
            }
            //查询内部控制参数
            case R.id.btn10:
            {
                String hexStr = "680000000000006810000186EA16";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
                break;
            }
            //修改内部控制参数
            case R.id.btn11:
            {
                String hexStr = "6800000000000068100005877F000000EA16";
                Log.d(TAG, ">>>发送:" + hexStr);
                BTUtil.SendComm(hexStr);
                break;
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            _bluetoothDevice = getArguments().getParcelable(ARG_PARAM1);
            _uuidMap = (Map<String, UUID>) getArguments().getSerializable(ARG_PARAM2);
        }
        _context = getContext();
        BTUtil.Init(_context, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        _view = inflater.inflate(R.layout.fragment_bluetooth_read_write, container, false);
        //强制获得焦点
        _view.requestFocus();
        _view.setFocusable(true);
        _view.setFocusableInTouchMode(true);
        _view.setOnKeyListener(backListener);
        _btnReturn = _view.findViewById(R.id.btn_return);
        _btnReturn.setOnClickListener(this);
        _textView = _view.findViewById(R.id.textView);
        _textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        _button0 = _view.findViewById(R.id.btn0);
        _button0.setOnClickListener(this);
        _button1 = _view.findViewById(R.id.button1);
        _button1.setOnClickListener(this);
        _button2 = _view.findViewById(R.id.btn2);
        _button2.setOnClickListener(this);
        _button3 = _view.findViewById(R.id.btn3);
        _button3.setOnClickListener(this);
        _button4 = _view.findViewById(R.id.btn4);
        _button4.setOnClickListener(this);
        _button5 = _view.findViewById(R.id.btn5);
        _button5.setOnClickListener(this);
        _button6 = _view.findViewById(R.id.btn6);
        _button6.setOnClickListener(this);
        _button7 = _view.findViewById(R.id.btn7);
        _button7.setOnClickListener(this);
        _button8 = _view.findViewById(R.id.btn8);
        _button8.setOnClickListener(this);
        _button9 = _view.findViewById(R.id.btn9);
        _button9.setOnClickListener(this);
        _button10 = _view.findViewById(R.id.btn10);
        _button10.setOnClickListener(this);
        _button11 = _view.findViewById(R.id.btn11);
        _button11.setOnClickListener(this);
        _progressBar = _view.findViewById(R.id.progressBar);
        _progressBar.setVisibility(View.VISIBLE);
        if(_bluetoothDevice != null)
        {
            Log.d(TAG, ">>>开始连接...");
            BTUtil.ConnectDevice(_bluetoothDevice, _uuidMap);
        }
        else
        {
            ShowWarning(2);
        }
        return _view;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener)
        {
            _onFragmentInteractionListener = (OnFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        _onFragmentInteractionListener = null;
        BTUtil.DisConnGatt();
        _bluetoothDevice = null;
    }
}