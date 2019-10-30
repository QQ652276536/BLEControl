package com.zistone.bluetoothtest.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.zistone.bluetoothtest.R;
import com.zistone.bluetoothtest.control.BluetoothListAdapter;
import com.zistone.bluetoothtest.util.ConvertUtil;

import java.util.UUID;

public class BluetoothFragment_ReadWrite extends Fragment implements View.OnClickListener
{
    private static final String TAG = "BluetoothFragment_ReadWrite";
    //已知服务
    private static final String SERVICE_UUID = "0000ff01-0000-1000-8000-00805f9b34fb";
    //已知特征
    private static final String CHARACTERISTIC_UUID = "0000ff02-0000-1000-8000-00805f9b34fb";
    //已知特征,写数据用
    private static final String CHARACTERISTIC_UUID_NOTIFY = "0000ff03-0000-1000-8000-00805f9b34fb";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_1 = 1;
    private static final int MESSAGE_2 = 2;
    private OnFragmentInteractionListener m_listener;
    private Context m_context;
    private View m_view;
    private ImageButton m_btnReturn;
    private TextView m_textView;
    private Button m_button1;
    private Button m_button2;
    private Button m_button3;
    private Button m_button4;
    private Button m_button5;
    private BluetoothDevice m_bluetoothDevice;
    private BluetoothGatt m_bluetoothGatt;
    private BluetoothGattService m_bluetoothGattService;
    private BluetoothGattCharacteristic m_bluetoothGattCharacteristic;
    private BluetoothGattCharacteristic m_bluetoothGattCharacteristic_notify;

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
                    m_button1.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button2.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button3.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button4.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    m_button5.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    break;
                }
                case MESSAGE_2:
                {
                    String str = m_textView.getText().toString();
                    str += "\r\n" + result;
                    m_textView.setText(str);
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
        m_button1 = m_view.findViewById(R.id.btn1);
        m_button1.setOnClickListener(this);
        m_button2 = m_view.findViewById(R.id.btn2);
        m_button2.setOnClickListener(this);
        m_button3 = m_view.findViewById(R.id.btn3);
        m_button3.setOnClickListener(this);
        m_button4 = m_view.findViewById(R.id.btn4);
        m_button4.setOnClickListener(this);
        m_button5 = m_view.findViewById(R.id.btn5);
        m_button5.setOnClickListener(this);
        if(m_bluetoothDevice != null)
        {
            Log.i(TAG, ">>>开始连接...");
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
                    super.onConnectionStateChange(gatt, status, newState);
                    if(status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED)
                    {
                        Log.i(TAG, ">>>成功建立连接!");
                        //发现服务
                        gatt.discoverServices();
                    }
                    else
                    {
                        Log.i(TAG, ">>>连接已断开!");
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
                    super.onServicesDiscovered(gatt, status);
                    //直到这里才是真正建立了可通信的连接
                    //通过UUID找到服务
                    m_bluetoothGattService = gatt.getService(UUID.fromString(SERVICE_UUID));
                    if(m_bluetoothGattService != null)
                    {
                        //写数据的服务和特征
                        m_bluetoothGattCharacteristic = m_bluetoothGattService.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID_NOTIFY));
                        if(m_bluetoothGattCharacteristic != null)
                        {
                            //订阅写入通知
                            m_bluetoothGatt.setCharacteristicNotification(m_bluetoothGattCharacteristic, true);
                            Log.i(TAG, ">>>已找到写入数据的特征值!");
                            Message message = new Message();
                            message.what = MESSAGE_1;
                            handler.sendMessage(message);
                        }
                        else
                        {
                            Log.e(TAG, ">>>该UUID无写入数据的特征值!");
                        }
                        //读取数据的服务和特征
                        m_bluetoothGattCharacteristic_notify = m_bluetoothGattService.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID));
                        if(m_bluetoothGattCharacteristic_notify != null)
                        {
                            //订阅读取通知
                            //m_bluetoothGatt.setCharacteristicNotification
                            // (m_bluetoothGattCharacteristic_notify, true);
                            Log.i(TAG, ">>>已找到读取数据的特征值!");
                        }
                        else
                        {
                            Log.e(TAG, ">>>该UUID无读取数据的特征值!");
                        }
                    }
                }

                /**
                 * 读取成功后回调,用于读取Read通道返回的数据
                 * 比如:在onCharacteristicWrite里面调用gatt.readCharacteristic(readCharact)后会回调该方法
                 * @param gatt
                 * @param characteristic
                 * @param status
                 */
                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
                {
                    super.onCharacteristicRead(gatt, characteristic, status);
                    byte[] byteArray = characteristic.getValue();
                    String result = "";
                    for(int i = 0; i < byteArray.length; i++)
                    {
                        if(i != byteArray.length - 1)
                        {
                            result += byteArray[i] + "";
                        }
                    }
                    Log.i(TAG, ">>>收到设备的数据:" + result);
                    Message message = new Message();
                    message.what = MESSAGE_2;
                    message.obj = "收到设备的数据:" + result;
                    handler.sendMessage(message);
                }

                /**
                 * 写入成功后回调,主动去蓝牙获取数据,自己主动去READ通道获取蓝牙数据
                 * 如果用的是Notify的话不用理会该方法,写出到蓝牙之后等待Notify的监听,即onCharacteristicChanged方法回调
                 *
                 * @param gatt
                 * @param characteristic
                 * @param status
                 */
                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
                {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    Log.i(TAG, ">>>数据发送成功!");
                    Message message = new Message();
                    message.what = MESSAGE_2;
                    message.obj = "数据发送成功!";
                    handler.sendMessage(message);
                }

                /**
                 * 收到硬件返回的数据时回调
                 * @param gatt
                 * @param characteristic
                 */
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
                {
                    super.onCharacteristicChanged(gatt, characteristic);
                    byte[] byteArray = characteristic.getValue();
                    String result = "";
                    for(int i = 0; i < byteArray.length; i++)
                    {
                        if(i != byteArray.length - 1)
                        {
                            result += byteArray[i] + " ";
                        }
                    }
                    Log.i(TAG, ">>>收到设备的数据2:" + result);
                    Message message = new Message();
                    message.what = MESSAGE_2;
                    message.obj = "收到设备的数据:" + result;
                    handler.sendMessage(message);
                }
            });
        }
        else
        {
            ShowWarning(2);
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
                BluetoothFragment_List bluetoothFragment_list = BluetoothFragment_List.newInstance("", "");
                getFragmentManager().beginTransaction().replace(R.id.fragment_bluetooth, bluetoothFragment_list, "bluetoothFragment_list").commitNow();
                break;
            }
            //开门
            case R.id.btn1:
            {
                String hexStr = "680000000000006810000100E116";
                String str = m_textView.getText().toString();
                str += "\r\n发送数据:" + hexStr;
                m_textView.setText(str);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic);
                break;
            }
            //读卡
            case R.id.btn2:
            {
                String hexStr = "680000000000006810000101E216";
                String str = m_textView.getText().toString();
                str += "\r\n发送数据:" + hexStr;
                m_textView.setText(str);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic);
                break;
            }
            //测量电池电压
            case R.id.btn3:
            {
                String hexStr = "680000000000006810000102E316";
                String str = m_textView.getText().toString();
                str += "\r\n发送数据:" + hexStr;
                m_textView.setText(str);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic);
                break;
            }
            //测量磁场强度
            case R.id.btn4:
            {
                String hexStr = "680000000000006810000103E416";
                String str = m_textView.getText().toString();
                str += "\r\n发送数据:" + hexStr;
                m_textView.setText(str);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic);
                break;
            }
            //测量门状态
            case R.id.btn5:
            {
                String hexStr = "680000000000006810000104E516";
                String str = m_textView.getText().toString();
                str += "\r\n发送数据:" + hexStr;
                m_textView.setText(str);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic);
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_view = inflater.inflate(R.layout.fragment_bluetooth_read_write, container, false);
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
    }
}
