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
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.zistone.bluetoothtest.R;
import com.zistone.bluetoothtest.activity.MainActivity;
import com.zistone.bluetoothtest.util.ConvertUtil;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothFragment_PowerControl extends Fragment implements View.OnClickListener
{
    private static final String TAG = "BluetoothFragment_PowerControl";
    //已知服务
    private static final UUID SERVICE_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e1011");
    //private static final UUID SERVICE_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    //写入特征的UUID
    private static final UUID WRITE_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e0011");
    //private static final UUID WRITE_UUID = UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb");
    //读取特征的UUID
    private static final UUID READ_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e0012");
    //private static final UUID READ_UUID = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");
    //客户端特征配置
    private static final UUID CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_1 = 1;
    private static final int MESSAGE_2 = 2;
    private static final int MESSAGE_3 = 3;
    private static final int MESSAGE_4 = 4;
    private static final int MESSAGE_5 = 5;
    private OnFragmentInteractionListener m_listener;
    private Context m_context;
    private View m_view;
    private ImageButton m_btnReturn;
    private TextView m_debugView;
    private Button m_button1;
    private Button m_button2;
    private Button m_button3;
    private Button m_button4;
    private Button m_button5;
    private TextView m_textView1;
    private TextView m_textView2;
    private TextView m_textView3;
    private TextView m_textView4;
    private TextView m_textView5;
    private TextView m_textView6;
    private ProgressBar m_progressBar;
    private BluetoothDevice m_bluetoothDevice;
    private BluetoothGatt m_bluetoothGatt;
    private BluetoothGattService m_bluetoothGattService;
    private BluetoothGattCharacteristic m_bluetoothGattCharacteristic_write;
    private BluetoothGattCharacteristic m_bluetoothGattCharacteristic_read;
    private StringBuffer m_stringBuffer = new StringBuffer();
    private Timer m_refreshTimer;
    private TimerTask m_refreshTask;
    private Toolbar m_toolbar;
    private ScrollView m_scrollView;

    public static BluetoothFragment_PowerControl newInstance(BluetoothDevice bluetoothDevice, String param2)
    {
        BluetoothFragment_PowerControl fragment = new BluetoothFragment_PowerControl();
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
                    m_button2.setEnabled(true);
                    m_button3.setEnabled(true);
                    m_button4.setEnabled(true);
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
                                    //门状态
                                    String hexStr = "680000000000006810000104E516";
                                    Log.d(TAG, ">>>发送:" + hexStr);
                                    byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                                    m_bluetoothGattCharacteristic_write.setValue(byteArray);
                                    m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                                    Thread.sleep(100);

                                    //电池电压
                                    hexStr = "680000000000006810000102E316";
                                    Log.d(TAG, ">>>发送:" + hexStr);
                                    byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                                    m_bluetoothGattCharacteristic_write.setValue(byteArray);
                                    m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                                    Thread.sleep(100);

                                    //磁强
                                    hexStr = "680000000000006810000103E416";
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
                    m_refreshTimer.schedule(m_refreshTask, 0, 3 * 1000);
                }
                break;
                case MESSAGE_2:
                {
                    m_debugView.append(result);
                    //定位到最后一行
                    int offset = m_debugView.getLineCount() * m_debugView.getLineHeight();
                    //如果文本的高度大于ScrollView的,就自动滑动
                    if(offset > m_scrollView.getHeight())
                    {
                        m_debugView.scrollTo(0, offset - m_scrollView.getHeight());
                    }
                }
                break;
                //电池电压
                case MESSAGE_3:
                    m_textView5.setText(result);
                    break;
                //磁场强度
                case MESSAGE_4:
                    m_textView6.setText(result);
                    break;
                //门状态
                case MESSAGE_5:
                    m_textView1.setText(result);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        //Activity的onCreateOptionsMenu会在之前调用,即先Clear一下,这样就只有Fragment自己设置的了
        menu.clear();
        inflater.inflate(R.menu.menu_setting, menu);
    }

    public void InitView()
    {
        m_context = getContext();
        m_toolbar = m_view.findViewById(R.id.toolbar);
        //加上这句,才会调用Fragment的ToolBar,否则调用的是Activity传递过来的
        setHasOptionsMenu(true);
        //去掉标题
        m_toolbar.setTitle("");
        //此处强转,必须是Activity才有这个方法
        ((MainActivity) getActivity()).setSupportActionBar(m_toolbar);
        m_btnReturn = m_view.findViewById(R.id.btn_return);
        m_btnReturn.setOnClickListener(this);
        m_debugView = m_view.findViewById(R.id.debug_view);
        m_button1 = m_view.findViewById(R.id.button1);
        m_button1.setOnClickListener(this);
        m_button2 = m_view.findViewById(R.id.button2);
        m_button2.setOnClickListener(this);
        m_button3 = m_view.findViewById(R.id.button3);
        m_button3.setOnClickListener(this);
        m_button4 = m_view.findViewById(R.id.button4);
        m_button4.setOnClickListener(this);
        m_button5 = m_view.findViewById(R.id.button5);
        m_button5.setOnClickListener(this);
        m_textView1 = m_view.findViewById(R.id.text1);
        m_textView2 = m_view.findViewById(R.id.text2);
        m_textView3 = m_view.findViewById(R.id.text3);
        m_textView4 = m_view.findViewById(R.id.text4);
        m_textView5 = m_view.findViewById(R.id.text5);
        m_textView6 = m_view.findViewById(R.id.text6);
        m_scrollView = m_view.findViewById(R.id.scrollView);
    }

    private void Resolve(String data)
    {
        Log.d(TAG, ">>>共接收:" + data);
        String[] strArray = data.split(" ");
        String indexStr = strArray[12];
        switch(indexStr)
        {
            //开门
            case "00":
            {
                String responseValue = ConvertUtil.HexStrToStr(strArray[13] + strArray[14]);
                Message message = new Message();
                message.what = MESSAGE_2;
                message.obj = "收到:开门【" + responseValue + "】 ";
                handler.sendMessage(message);
            }
            break;
            //读卡
            case "01":
            {
                Message message = new Message();
                message.what = MESSAGE_3;
                message.obj = "收到:读卡【 】 ";
                handler.sendMessage(message);
            }
            break;
            //电池电压
            case "02":
            {
                Message message = new Message();
                message.what = MESSAGE_3;
                message.obj = "收到:电池电压【 】 ";
                handler.sendMessage(message);
            }
            break;
            //磁场强度
            case "03":
            {
                String responseValue1 = strArray[9].equals("00") ? "OK" : "Fail";
                //                String responseValue2 = ConvertUtil.HexStrToStr(strArray[14] + strArray[15] + strArray[16] + strArray[17] + strArray[18] + strArray[19] + strArray[20] + strArray[21] + strArray[22] + strArray[23] + strArray[24]);
                String responseValue2 = ConvertUtil.HexStrToStr(strArray[14] + strArray[15] + strArray[16] + strArray[17] + strArray[18]);
                Message message = new Message();
                message.what = MESSAGE_4;
                message.obj = "收到:磁场强度【" + responseValue2 + "】 ";
                handler.sendMessage(message);
            }
            break;
            //测量门状态
            case "04":
            {
                Message message = new Message();
                message.what = MESSAGE_5;
                if(strArray[13].equals("01"))
                {
                    message.obj = "收到:门状态【已关】 ";
                }
                else
                {
                    message.obj = "收到:门状态【已开】 ";
                }
                handler.sendMessage(message);
            }
            break;
        }
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
                getFragmentManager().beginTransaction().remove(BluetoothFragment_PowerControl.this).commitNow();
                break;
            }
            //连接
            case R.id.button1:
            {
                if(m_bluetoothDevice != null)
                {
                    if(m_button1.getText().toString().equalsIgnoreCase("连接"))
                    {
                        m_button1.setText("断开");
                    }
                    else
                    {
                        m_button1.setText("连接");
                        m_button2.setEnabled(false);
                        m_button3.setEnabled(false);
                        m_button4.setEnabled(false);
                        if(m_bluetoothGatt != null)
                        {
                            m_bluetoothGatt.disconnect();
                        }
                        if(m_refreshTask != null)
                        {
                            m_refreshTask.cancel();
                        }
                        if(m_refreshTimer != null)
                        {
                            m_refreshTimer.cancel();
                        }
                        return;
                    }
                    Log.d(TAG, ">>>开始连接...");
                    m_bluetoothGatt = m_bluetoothDevice.connectGatt(m_context, false, new BluetoothGattCallback()
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
                                    sendResult = "发送开门 ";
                                    break;
                                case "01":
                                    sendResult = "发送读卡 ";
                                    break;
                                case "02":
                                    //sendResult = "发送测量电池电压 ";
                                    break;
                                case "03":
                                    //sendResult = "发送测量磁场强度 ";
                                    break;
                                case "04":
                                    //sendResult = "发送测量门状态 ";
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
                break;
            }
            //开一号门锁
            case R.id.button2:
                String hexStr = "680000000000006810000100E116";
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                break;
            case R.id.button3:
                break;
            case R.id.button4:
                break;
            //清屏
            case R.id.button5:
                m_debugView.setText("");
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
        if(m_bluetoothGatt != null)
            m_bluetoothGatt.close();
        if(m_refreshTimer != null)
            m_refreshTimer.cancel();
        if(m_refreshTask != null)
            m_refreshTask.cancel();
    }
}