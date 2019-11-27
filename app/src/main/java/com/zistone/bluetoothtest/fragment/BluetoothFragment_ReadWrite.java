package com.zistone.bluetoothtest.fragment;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zistone.bluetoothtest.R;
import com.zistone.bluetoothtest.util.ConvertUtil;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothFragment_ReadWrite extends Fragment implements View.OnClickListener
{
    private static final String TAG = "BluetoothFragment_ReadWrite";
    //已知服务
    //private static final UUID SERVICE_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e1011");
    private static final UUID SERVICE_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    //写入特征的UUID
    //private static final UUID WRITE_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e0011");
    private static final UUID WRITE_UUID = UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb");
    //读取特征的UUID
    //private static final UUID READ_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e0012");
    private static final UUID READ_UUID = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");
    //客户端特征配置
    private static final UUID CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_1 = 1;
    private static final int MESSAGE_2 = 2;
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
    private ProgressBar m_progressBar;
    private BluetoothDevice m_bluetoothDevice;
    private BluetoothGatt m_bluetoothGatt;
    private BluetoothGattService m_bluetoothGattService;
    private BluetoothGattCharacteristic m_bluetoothGattCharacteristic_write;
    private BluetoothGattCharacteristic m_bluetoothGattCharacteristic_read;
    private StringBuffer m_stringBuffer = new StringBuffer();
    private boolean m_isComplexAFlag = false;
    private boolean m_isComplexBFlag = false;
    private Timer m_refreshTimer;
    private TimerTask m_refreshTask;

    public static BluetoothFragment_ReadWrite newInstance(BluetoothDevice bluetoothDevice, String param2)
    {
        BluetoothFragment_ReadWrite fragment = new BluetoothFragment_ReadWrite();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, bluetoothDevice);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

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
                    m_button1.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button2.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button3.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button4.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button5.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button6.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button7.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_progressBar.setVisibility(View.INVISIBLE);
                    break;
                }
                case MESSAGE_2:
                {
                    m_textView.append("\r\n" + result);
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

    public void InitView()
    {
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
        m_button7 = m_view.findViewById(R.id.btn6);
        m_button7.setOnClickListener(this);
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
                    message.obj = sendResult;
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
    }

    private void Resolve(String data)
    {
        Log.d(TAG, ">>>共接收:" + data);
        String[] strArray = data.split(" ");
        String responseResult = "";
        String responseValue = "";
        if(!m_isComplexAFlag)
        {
            String indexStr = strArray[12];
            switch(indexStr)
            {
                //开门
                case "00":
                    responseResult = "开门";
                    responseValue = ConvertUtil.HexStrToStr(strArray[13] + strArray[14]);
                    break;
                //读卡
                case "01":
                    break;
                //电池电压
                case "02":
                    break;
                //磁场强度
                case "03":
                    responseResult = "强磁场";
                    responseValue = strArray[9].equals("00") ? "OK" : "Fail";
                    responseValue += " " + ConvertUtil.HexStrToStr(strArray[14] + strArray[15] + strArray[16] + strArray[17] + strArray[18] + strArray[19] + strArray[20] + strArray[21] + strArray[22] + strArray[23] + strArray[24]);
                    break;
                //测量门状态
                case "04":
                    responseResult = "";
                    if(strArray[13].equals("01"))
                    {
                        responseValue = "门已关";
                    }
                    else
                    {
                        responseResult = "门已开";
                    }
                    break;
            }
        }
        Message message = new Message();
        message.what = MESSAGE_2;
        message.obj = "接收:" + responseResult + responseValue;
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
                getFragmentManager().beginTransaction().remove(BluetoothFragment_ReadWrite.this).commitNow();
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
                m_textView.setText("");
                String hexStr = "680000000000006810000104E516";
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                break;
            }
            //综合测试A
            case R.id.btn6:
                m_isComplexAFlag = true;
                String hexStr = "680000000000006810000180E616";
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                m_isComplexAFlag = false;
                break;
            //综合测试B
            case R.id.btn7:
                if(!m_isComplexBFlag)
                {
                    m_textView.setText("开始接收设备状态信息...");
                    m_isComplexBFlag = true;
                    m_button7.setText("停止");
                    m_button1.setEnabled(false);
                    m_button2.setEnabled(false);
                    m_button3.setEnabled(false);
                    m_button4.setEnabled(false);
                    m_button5.setEnabled(false);
                    m_button6.setEnabled(false);
                    m_button1.setBackgroundColor(Color.BLACK);
                    m_button2.setBackgroundColor(Color.BLACK);
                    m_button3.setBackgroundColor(Color.BLACK);
                    m_button4.setBackgroundColor(Color.BLACK);
                    m_button5.setBackgroundColor(Color.BLACK);
                    m_button6.setBackgroundColor(Color.BLACK);
                    //开启定时器
                    m_refreshTimer = new Timer();
                    m_refreshTask = new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            getActivity().runOnUiThread(() ->
                            {
                                try
                                {
                                    //磁强
                                    String hexStr = "680000000000006810000103E416";
                                    Log.d(TAG, ">>>发送:" + hexStr);
                                    byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                                    m_bluetoothGattCharacteristic_write.setValue(byteArray);
                                    m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                                    Thread.sleep(100);
                                    //门状态
                                    hexStr = "680000000000006810000104E516";
                                    Log.d(TAG, ">>>发送:" + hexStr);
                                    byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                                    m_bluetoothGattCharacteristic_write.setValue(byteArray);
                                    m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                                    Thread.sleep(100);
                                }
                                catch(InterruptedException e)
                                {
                                    e.printStackTrace();
                                }
                            });
                        }
                    };
                    //任务、延迟执行时间、重复调用间隔,Timer和TimerTask在调用cancel()取消后不能再执行schedule语句
                    m_refreshTimer.schedule(m_refreshTask, 0, 5 * 1000);
                }
                else
                {
                    m_isComplexBFlag = false;
                    m_button7.setText("综合测试B");
                    m_button1.setEnabled(true);
                    m_button2.setEnabled(true);
                    m_button3.setEnabled(true);
                    m_button4.setEnabled(true);
                    m_button5.setEnabled(true);
                    m_button6.setEnabled(true);
                    m_button7.setEnabled(true);
                    m_button1.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button2.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button3.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button4.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button5.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button6.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button7.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    //关闭定时器
                    if(m_refreshTimer != null)
                    {
                        m_refreshTimer.cancel();
                        m_refreshTimer = null;
                    }
                    if(m_refreshTask != null)
                    {
                        m_refreshTask.cancel();
                        m_refreshTask = null;
                    }
                }
                break;
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_view = inflater.inflate(R.layout.fragment_bluetooth_powercontrol, container, false);
        try
        {
            InitView();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Toast.makeText(m_context, "配对异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, e.getMessage());
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
        m_bluetoothGatt.close();
        if(m_refreshTimer != null)
            m_refreshTimer.cancel();
        if(m_refreshTask != null)
            m_refreshTask.cancel();
    }
}