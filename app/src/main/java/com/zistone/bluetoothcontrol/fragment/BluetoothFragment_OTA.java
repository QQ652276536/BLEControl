package com.zistone.bluetoothcontrol.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
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
import java.util.UUID;

public class BluetoothFragment_OTA extends Fragment implements View.OnClickListener
{
    private static final String TAG = "BluetoothFragment_CommandTest";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int FILE_SELECTOR_CODE = 1;
    private static UUID SERVICE_UUID;
    private static UUID WRITE_UUID;
    private static UUID READ_UUID;
    private static UUID CONFIG_UUID;
    private Context m_context;
    private View m_view;
    private Button m_button1;
    private Button m_button2;
    private ImageButton m_btnReturn;
    private TextView m_textView1;
    private TextView m_textView2;
    private ProgressBar m_progressBar;
    private BluetoothDevice m_bluetoothDevice;
    private BluetoothGatt m_bluetoothGatt;
    private BluetoothGattService m_bluetoothGattService;
    private BluetoothGattCharacteristic m_bluetoothGattCharacteristic_write;
    private BluetoothGattCharacteristic m_bluetoothGattCharacteristic_read;
    private BluetoothFragment_PowerControl.OnFragmentInteractionListener m_listener;

    public static BluetoothFragment_OTA newInstance(BluetoothDevice bluetoothDevice, Map<String, UUID> map)
    {
        BluetoothFragment_OTA fragment = new BluetoothFragment_OTA();
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
            getFragmentManager().beginTransaction().remove(BluetoothFragment_OTA.this).commitNow();
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
                case 1:
                {

                    break;
                }
            }
        }
    };

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
            case R.id.btn_return_ota:
            {
                BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
                getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
                getFragmentManager().beginTransaction().remove(BluetoothFragment_OTA.this).commitNow();
                break;
            }
            case R.id.btn1_ota:
            {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //选择图片
                //intent.setType("image/*");
                //选择音频
                //intent.setType("audio/*");
                //选择视频
                //intent.setType("video/*");
                //同时选择视频和图片
                //intent.setType("video/*;image/*");
                //选择bin文件
                //intent.setType("application/octet-stream");
                //无类型限制
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, FILE_SELECTOR_CODE);
                break;
            }
            case R.id.btn2_ota:
            {
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
        m_view = inflater.inflate(R.layout.fragment_bluetooth_ota, container, false);
        //强制获得焦点
        m_view.requestFocus();
        m_view.setFocusable(true);
        m_view.setFocusableInTouchMode(true);
        m_view.setOnKeyListener(backListener);
        m_context = getContext();
        m_btnReturn = m_view.findViewById(R.id.btn_return_ota);
        m_btnReturn.setOnClickListener(this);
        m_button1 = m_view.findViewById(R.id.btn1_ota);
        m_button2 = m_view.findViewById(R.id.btn2_ota);
        m_textView1 = m_view.findViewById(R.id.text1_ota);
        m_textView2 = m_view.findViewById(R.id.text2_ota);
        m_progressBar = m_view.findViewById(R.id.progressBar_ota);
        m_button1.setOnClickListener(this::onClick);
        m_button2.setOnClickListener(this::onClick);
        m_progressBar = m_view.findViewById(R.id.progressBar_ota);
        m_progressBar.setVisibility(View.VISIBLE);
        if(m_bluetoothDevice != null)
        {
            m_textView1.setText(m_bluetoothDevice.getAddress());
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
                            message.what = 1;
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

                }
            });
        }
        else
        {
            ShowWarning(2);
        }
        return m_view;
    }

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
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof BluetoothFragment_PowerControl.OnFragmentInteractionListener)
        {
            m_listener = (BluetoothFragment_PowerControl.OnFragmentInteractionListener) context;
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
    }
}