package com.zistone.bluetoothtest.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
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
    //已知特征,发送数据用
    private static final String CHARACTERISTIC_UUID_SEND = "0000ff02-0000-1000-8000-00805f9b34fb";
    //已知特征,接收数据用
    private static final String CHARACTERISTIC_UUID_NOFITY = "0000ff03-0000-1000-8000-00805f9b34fb";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_1 = 1;
    private static final int MESSAGE_RESPONSE_FAIL = 2;
    private static final int MESSAGE_RESPONSE_SUCCESS = 3;
    private OnFragmentInteractionListener m_listener;
    private Context m_context;
    private View m_view;
    private ImageButton m_btnReturn;
    private EditText m_editText;
    private TextView m_textView;
    private Button m_button1;
    private Button m_button2;
    private BluetoothFragment_List m_bluetoothFragment_list;
    private BluetoothDevice m_bluetoothDevice;
    private BluetoothGatt m_bluetoothGatt;
    private BluetoothGattService m_bluetoothGattService;
    private BluetoothGattCharacteristic m_bluetoothGattCharacteristic;
    private BluetoothGattCharacteristic m_bluetoothGattCharacteristic_notify;

    public static BluetoothFragment_ReadWrite newInstance(String param1, String param2)
    {
        BluetoothFragment_ReadWrite fragment = new BluetoothFragment_ReadWrite();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
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
                    break;
                }
                case MESSAGE_RESPONSE_SUCCESS:
                {
                    break;
                }
                case MESSAGE_RESPONSE_FAIL:
                {
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
        m_editText = m_view.findViewById(R.id.editText);
        m_textView = m_view.findViewById(R.id.textView);
        m_button1 = m_view.findViewById(R.id.btn1);
        m_button1.setOnClickListener(this);
        m_button2 = m_view.findViewById(R.id.btn2);
        m_button2.setOnClickListener(this);
        m_bluetoothFragment_list = (BluetoothFragment_List) getActivity().getSupportFragmentManager().findFragmentByTag("bluetoothFragment_list");
        if(m_bluetoothFragment_list != null)
        {
            m_bluetoothDevice = m_bluetoothFragment_list.m_bluetoothDevice;
            Log.i(TAG, ">>>开始连接...");
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
                        Message message = new Message();
                        message.what = MESSAGE_1;
                        handler.sendMessage(message);
                        //写数据的服务和特征
                        m_bluetoothGattCharacteristic = m_bluetoothGattService.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID_SEND));
                        if(m_bluetoothGattCharacteristic != null)
                        {
                            //订阅写入通知
                            m_bluetoothGatt.setCharacteristicNotification(m_bluetoothGattCharacteristic, true);
                            Log.i(TAG, ">>>已找到写入数据的特征值!");
                        }
                        else
                        {
                            Log.e(TAG, ">>>该UUID无写入数据的特征值!");
                        }
                        //读取数据的服务和特征
                        m_bluetoothGattCharacteristic_notify = m_bluetoothGattService.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID_NOFITY));
                        if(m_bluetoothGattCharacteristic_notify != null)
                        {
                            //订阅读取通知
                            m_bluetoothGatt.setCharacteristicNotification(m_bluetoothGattCharacteristic_notify, true);
                            Log.i(TAG, ">>>已找到读取数据的特征值!");
                        }
                        else
                        {
                            Log.e(TAG, ">>>该UUID无读取数据的特征值!");
                        }
                    }
                }

                /**
                 * 读取成功后回调
                 * @param gatt
                 * @param characteristic
                 * @param status
                 */
                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
                {
                    super.onCharacteristicRead(gatt, characteristic, status);
                    byte[] byteArray = characteristic.getValue();
                    String str = "";
                    for(int i = 0; i < byteArray.length; i++)
                    {
                        if(i != byteArray.length - 1)
                        {
                            str += byteArray[i] + "";
                        }
                    }
                    Log.i(TAG, ">>>接收到硬件的数据:" + str);
                    String finalStr = str;
                    getActivity().runOnUiThread(() -> m_textView.setText(finalStr));
                }

                /**
                 * 写入成功后回调
                 * @param gatt
                 * @param characteristic
                 * @param status
                 */
                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
                {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    Log.i(TAG, ">>>数据发送成功!");
                    getActivity().runOnUiThread(() -> Toast.makeText(m_context, "数据发送成功!", Toast.LENGTH_SHORT));
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

                    byte[] bytesreceive = characteristic.getValue();
                    Log.i(TAG, ">>>接收数据:" + bytesreceive[0] + "" + bytesreceive[1] + "" + bytesreceive[2] + "" + bytesreceive[4]);
                }
            });
        }
        else
        {
            ShowWarning(1);
        }
    }

    private void ShowWarning(int param)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
        builder.setTitle("警告");
        switch(param)
        {
            case 1:
                builder.setMessage("该设备未连接成功,请重试!");
                builder.setPositiveButton("知道了", (dialog, which) ->
                {
                    BluetoothFragment_List bluetoothFragment_list = BluetoothFragment_List.newInstance("", "");
                    getFragmentManager().beginTransaction().replace(R.id.fragment_current_device, bluetoothFragment_list, "bluetoothFragment_list").commitNow();
                });
                break;
            case 2:
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
                BluetoothFragment_List bluetoothFragment_list = BluetoothFragment_List.newInstance("", "");
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_device, bluetoothFragment_list, "bluetoothFragment_list").commitNow();
                break;
            case R.id.btn1:
                String str = m_editText.getText().toString();
                byte[] byteArray = ConvertUtil.HexStrToByteArray(str);
                m_bluetoothGattCharacteristic.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic);
                break;
            case R.id.btn2:
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
