package com.zistone.bluetoothcontrol.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
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
import com.zistone.bluetoothcontrol.util.ConvertUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothFragment_CommandTest extends Fragment implements View.OnClickListener
{
    private static final String TAG = "BluetoothFragment_CommandTest";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_1 = 1;
    private static final int MESSAGE_2 = 2;
    private static UUID SERVICE_UUID;
    private static UUID WRITE_UUID;
    private static UUID READ_UUID;
    private static UUID CONFIG_UUID;
    private OnFragmentInteractionListener m_listener;
    private Context m_context;
    private View m_view;
    private ImageButton m_btnReturn;
    private TextView m_textView;
    private Button m_button0;
    private Button m_button1;
    private Button m_button2;
    private Button m_button3;
    private Button m_button4;
    private Button m_button5;
    private Button m_button6;
    private Button m_button7;
    private Button m_button8;
    private Button m_button9;
    private Button m_button10;
    private Button m_button11;
    private ProgressBar m_progressBar;
    private BluetoothDevice m_bluetoothDevice;
    private BluetoothGatt m_bluetoothGatt;
    private BluetoothGattService m_bluetoothGattService;
    private BluetoothGattCharacteristic m_bluetoothGattCharacteristic_write;
    private BluetoothGattCharacteristic m_bluetoothGattCharacteristic_read;
    private StringBuffer m_stringBuffer = new StringBuffer();
    private Timer m_refreshTimer;
    private TimerTask m_refreshTask;

    public static BluetoothFragment_CommandTest newInstance(BluetoothDevice bluetoothDevice, Map<String, UUID> map)
    {
        BluetoothFragment_CommandTest fragment = new BluetoothFragment_CommandTest();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, bluetoothDevice);
        args.putSerializable(ARG_PARAM2, (Serializable) map);
        fragment.setArguments(args);
        return fragment;
    }

    private View.OnKeyListener backListener = (v, keyCode, event) ->
    {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
            getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
            getFragmentManager().beginTransaction().remove(BluetoothFragment_CommandTest.this).commitNow();
            return true;
        }
        return false;
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
                case MESSAGE_1:
                {
                    m_button1.setEnabled(true);
                    m_button2.setEnabled(true);
                    m_button3.setEnabled(true);
                    m_button4.setEnabled(true);
                    m_button5.setEnabled(true);
                    m_button6.setEnabled(true);
                    m_button7.setEnabled(true);
                    m_button8.setEnabled(true);
                    m_button9.setEnabled(true);
                    m_button10.setEnabled(true);
                    m_button11.setEnabled(true);
                    m_button1.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button2.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button3.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button4.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button5.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button6.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button7.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button8.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button9.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button10.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button11.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_progressBar.setVisibility(View.INVISIBLE);
                    break;
                }
                case MESSAGE_2:
                {
                    m_textView.append("\r\n" + result);
                    //定位到最后一行
                    int offset = m_textView.getLineCount() * m_textView.getLineHeight();
                    //如果文本的高度大于ScrollView的,就自动滑动
                    if(offset > m_textView.getHeight())
                    {
                        m_textView.scrollTo(0, offset - m_textView.getHeight());
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
        if(m_listener != null)
        {
            m_listener.onFragmentInteraction(uri);
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
                responseValue += " " + ConvertUtil.HexStrToStr(strArray[14] + strArray[15] + strArray[16] + strArray[17] + strArray[18] + strArray[19] + strArray[20] + strArray[21] + strArray[22] + strArray[23] + strArray[24]);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
        builder.setTitle("警告");
        switch(param)
        {
            case 1:
                builder.setMessage("该设备的连接已断开!,请重试!");
                builder.setPositiveButton("知道了", (dialog, which) ->
                {
                    BluetoothFragment_List bluetoothFragment_list = BluetoothFragment_List.newInstance("", "");
                    getFragmentManager().beginTransaction().replace(R.id.fragment_bluetooth, bluetoothFragment_list, "bluetoothFragment_list").commitNow();
                });
                break;
            case 2:
                builder.setMessage("未获取到蓝牙,请重试!");
                builder.setPositiveButton("知道了", (dialog, which) ->
                {
                    BluetoothFragment_List bluetoothFragment_list = BluetoothFragment_List.newInstance("", "");
                    getFragmentManager().beginTransaction().replace(R.id.fragment_bluetooth, bluetoothFragment_list, "bluetoothFragment_list").commitNow();
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
                m_textView.setText("");
                break;
            //开门
            case R.id.button1:
            {
                String hexStr = "680000000000006810000100E116";
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                break;
            }
            //读卡
            case R.id.btn2:
            {
                String hexStr = "680000000000006810000101E216";
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                break;
            }
            //测量电池电压
            case R.id.btn3:
            {
                String hexStr = "680000000000006810000102E316";
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                break;
            }
            //测量磁场强度
            case R.id.btn4:
            {
                String hexStr = "680000000000006810000103E416";
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                break;
            }
            //测量门状态
            case R.id.btn5:
            {
                String hexStr = "680000000000006810000104E516";
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                break;
            }
            //综合测试A
            case R.id.btn6:
            {
                String hexStr = "680000000000006810000180E616";
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                break;
            }
            //开一号门锁
            case R.id.btn7:
            {
                String hexStr = "680000000000006810000181E716";
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                break;
            }
            //开二号门锁
            case R.id.btn8:
            {
                String hexStr = "680000000000006810000182E816";
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                break;
            }
            //开全部门锁
            case R.id.btn9:
            {
                String hexStr = "680000000000006810000183E916";
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                break;
            }
            //查询内部控制参数
            case R.id.btn10:
            {
                String hexStr = "680000000000006810000186EA16";
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                break;
            }
            //修改内部控制参数
            case R.id.btn11:
            {
                String hexStr = "6800000000000068100005877F000000EA16";
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
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
            m_bluetoothDevice = getArguments().getParcelable(ARG_PARAM1);
            Map<String, UUID> map = (Map<String, UUID>) getArguments().getSerializable(ARG_PARAM2);
            SERVICE_UUID = map.get("SERVICE_UUID");
            WRITE_UUID = map.get("WRITE_UUID");
            READ_UUID = map.get("READ_UUID");
            CONFIG_UUID = map.get("CONFIG_UUID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_view = inflater.inflate(R.layout.fragment_bluetooth_read_write, container, false);
        //强制获得焦点
        m_view.requestFocus();
        m_view.setFocusable(true);
        m_view.setFocusableInTouchMode(true);
        m_view.setOnKeyListener(backListener);
        m_context = getContext();
        m_btnReturn = m_view.findViewById(R.id.btn_return);
        m_btnReturn.setOnClickListener(this);
        m_textView = m_view.findViewById(R.id.textView);
        m_textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        m_button0 = m_view.findViewById(R.id.btn0);
        m_button0.setOnClickListener(this);
        m_button1 = m_view.findViewById(R.id.button1);
        m_button1.setOnClickListener(this);
        m_button2 = m_view.findViewById(R.id.btn2);
        m_button2.setOnClickListener(this);
        m_button3 = m_view.findViewById(R.id.btn3);
        m_button3.setOnClickListener(this);
        m_button4 = m_view.findViewById(R.id.btn4);
        m_button4.setOnClickListener(this);
        m_button5 = m_view.findViewById(R.id.btn5);
        m_button5.setOnClickListener(this);
        m_button6 = m_view.findViewById(R.id.btn6);
        m_button6.setOnClickListener(this);
        m_button7 = m_view.findViewById(R.id.btn7);
        m_button7.setOnClickListener(this);
        m_button8 = m_view.findViewById(R.id.btn8);
        m_button8.setOnClickListener(this);
        m_button9 = m_view.findViewById(R.id.btn9);
        m_button9.setOnClickListener(this);
        m_button10 = m_view.findViewById(R.id.btn10);
        m_button10.setOnClickListener(this);
        m_button11 = m_view.findViewById(R.id.btn11);
        m_button11.setOnClickListener(this);
        m_progressBar = m_view.findViewById(R.id.progressBar);
        m_progressBar.setVisibility(View.VISIBLE);
        if(m_bluetoothDevice != null)
        {
            Log.d(TAG, ">>>开始连接...");
            m_bluetoothGatt = m_bluetoothDevice.connectGatt(m_context, true, new BluetoothGattCallback()
            {
                /**
                 * 连接状态改变时回调
                 * @param gatt
                 * @param status
                 * @param newState
                 */
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
                {
                    if(status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED)
                    {
                        Log.d(TAG, ">>>成功建立连接!");
                        //发现服务
                        gatt.discoverServices();
                    }
                    else
                    {
                        Log.d(TAG, ">>>连接已断开!");
                        m_bluetoothGatt.close();
                        ShowWarning(1);
                    }
                }

                /**
                 * 发现设备(真正建立连接)
                 * @param gatt
                 * @param status
                 */
                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status)
                {
                    //直到这里才是真正建立了可通信的连接
                    //通过UUID找到服务
                    m_bluetoothGattService = m_bluetoothGatt.getService(SERVICE_UUID);
                    if(m_bluetoothGattService != null)
                    {
                        //写数据的服务和特征
                        m_bluetoothGattCharacteristic_write = m_bluetoothGattService.getCharacteristic(WRITE_UUID);
                        if(m_bluetoothGattCharacteristic_write != null)
                        {
                            Log.d(TAG, ">>>已找到写入数据的特征值!");
                            Message message = new Message();
                            message.what = MESSAGE_1;
                            handler.sendMessage(message);
                        }
                        else
                        {
                            Log.e(TAG, ">>>该UUID无写入数据的特征值!");
                        }
                        //读取数据的服务和特征
                        m_bluetoothGattCharacteristic_read = m_bluetoothGattService.getCharacteristic(READ_UUID);
                        if(m_bluetoothGattCharacteristic_read != null)
                        {
                            Log.d(TAG, ">>>已找到读取数据的特征值!");
                            //订阅读取通知
                            gatt.setCharacteristicNotification(m_bluetoothGattCharacteristic_read, true);
                            BluetoothGattDescriptor descriptor = m_bluetoothGattCharacteristic_read.getDescriptor(CONFIG_UUID);
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                        else
                        {
                            Log.e(TAG, ">>>该UUID无读取数据的特征值!");
                        }
                    }
                }

                /**
                 * 写入成功后回调
                 *
                 * @param gatt
                 * @param characteristic
                 * @param status
                 */
                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
                {
                    byte[] byteArray = characteristic.getValue();
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
                        case "04":
                            sendResult = "测量门状态";
                            break;
                    }
                    Log.d(TAG, ">>>" + sendResult);
                    Message message = new Message();
                    message.what = MESSAGE_2;
                    //                    message.obj = sendResult;
                    message.obj = "发送:" + ConvertUtil.StrArrayToStr(strArray);
                    handler.sendMessage(message);
                }

                /**
                 * 收到硬件返回的数据时回调,如果是Notify的方式
                 * @param gatt
                 * @param characteristic
                 */
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
                {
                    byte[] byteArray = characteristic.getValue();
                    String result = ConvertUtil.ByteArrayToHexStr(byteArray);
                    result = ConvertUtil.HexStrAddCharacter(result, " ");
                    Log.d(TAG, ">>>接收:" + result);
                    String[] strArray = result.split(" ");
                    //一个包(20个字节)
                    if(strArray[0].equals("68") && strArray[strArray.length - 1].equals("16"))
                    {
                        Resolve(result);
                        //清空缓存
                        m_stringBuffer = new StringBuffer();
                    }
                    //分包
                    else
                    {
                        if(!strArray[strArray.length - 1].equals("16"))
                        {
                            m_stringBuffer.append(result + " ");
                        }
                        //最后一个包
                        else
                        {
                            m_stringBuffer.append(result);
                            result = m_stringBuffer.toString();
                            Resolve(result);
                            //清空缓存
                            m_stringBuffer = new StringBuffer();
                        }
                    }
                }
            });
        }
        else
        {
            ShowWarning(2);
        }
        return m_view;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener)
        {
            m_listener = (OnFragmentInteractionListener) context;
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
        m_listener = null;
        if(m_bluetoothGatt != null)
            m_bluetoothGatt.close();
        if(m_refreshTimer != null)
            m_refreshTimer.cancel();
        if(m_refreshTask != null)
            m_refreshTask.cancel();
    }
}