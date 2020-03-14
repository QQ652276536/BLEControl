package com.zistone.blecontrol.fragment;

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
import android.widget.TextView;

import com.zistone.blecontrol.R;
import com.zistone.blecontrol.util.ConvertUtil;
import com.zistone.blecontrol.util.ProgressDialogUtil;

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
    private Context _context;
    private View _view;
    private Button _btn1;
    private Button _btn2;
    private ImageButton _btnReturn;
    private TextView _txt1;
    private TextView _txt2;
    private BluetoothDevice _bluetoothDevice;
    private BluetoothGatt _bluetoothGatt;
    private BluetoothGattService _bluetoothGattService;
    private BluetoothGattCharacteristic _bluetoothGattCharacteristic_write;
    private BluetoothGattCharacteristic _bluetoothGattCharacteristic_read;
    private OnFragmentInteractionListener _onFragmentInteractionListener;

    public static BluetoothFragment_OTA newInstance(BluetoothDevice bluetoothDevice, Map<String, UUID> map)
    {
        BluetoothFragment_OTA fragment = new BluetoothFragment_OTA();
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
                getFragmentManager().beginTransaction().remove(BluetoothFragment_OTA.this).commitNow();
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
                case 1:
                    break;
            }
        }
    };

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
            }
            break;
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
                //intent.setType("application/octxt-stream");
                //无类型限制
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, FILE_SELECTOR_CODE);
            }
            break;
            case R.id.btn2_ota:
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
            _bluetoothDevice = getArguments().getParcelable(ARG_PARAM1);
            Map<String, UUID> map = (Map<String, UUID>) getArguments().getSerializable(ARG_PARAM2);
            SERVICE_UUID = map.get("SERVICE_UUID");
            WRITE_UUID = map.get("WRITE_UUID");
            READ_UUID = map.get("READ_UUID");
            CONFIG_UUID = map.get("CONFIG_UUID");
        }
        _context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        _view = inflater.inflate(R.layout.fragment_bluetooth_ota, container, false);
        //强制获得焦点
        _view.requestFocus();
        _view.setFocusable(true);
        _view.setFocusableInTouchMode(true);
        _view.setOnKeyListener(backListener);
        _btnReturn = _view.findViewById(R.id.btn_return_ota);
        _btnReturn.setOnClickListener(this);
        _btn1 = _view.findViewById(R.id.btn1_ota);
        _btn2 = _view.findViewById(R.id.btn2_ota);
        _txt1 = _view.findViewById(R.id.txt1_ota);
        _txt2 = _view.findViewById(R.id.txt2_ota);
        _btn1.setOnClickListener(this::onClick);
        _btn2.setOnClickListener(this::onClick);
        if(_bluetoothDevice != null)
        {
            _txt1.setText(_bluetoothDevice.getAddress());
            Log.d(TAG, ">>>开始连接...");
            _bluetoothGatt = _bluetoothDevice.connectGatt(_context, false, new BluetoothGattCallback()
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
                        _bluetoothGatt.disconnect();
                        ProgressDialogUtil.ShowWarning(_context, "警告", "该设备的连接已断开,如需再次连接请重试!");
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
                    _bluetoothGattService = _bluetoothGatt.getService(SERVICE_UUID);
                    if(_bluetoothGattService != null)
                    {
                        //写数据的服务和特征
                        _bluetoothGattCharacteristic_write = _bluetoothGattService.getCharacteristic(WRITE_UUID);
                        if(_bluetoothGattCharacteristic_write != null)
                        {
                            Log.d(TAG, ">>>已找到写入数据的特征值!");
                            Message message = handler.obtainMessage(1, "");
                            handler.sendMessage(message);
                        }
                        else
                        {
                            Log.e(TAG, ">>>该UUID无写入数据的特征值!");
                        }
                        //读取数据的服务和特征
                        _bluetoothGattCharacteristic_read = _bluetoothGattService.getCharacteristic(READ_UUID);
                        if(_bluetoothGattCharacteristic_read != null)
                        {
                            Log.d(TAG, ">>>已找到读取数据的特征值!");
                            //订阅读取通知
                            gatt.setCharacteristicNotification(_bluetoothGattCharacteristic_read, true);
                            BluetoothGattDescriptor descriptor = _bluetoothGattCharacteristic_read.getDescriptor(CONFIG_UUID);
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
            ProgressDialogUtil.ShowWarning(_context, "警告", "未获取到蓝牙,请重试!");
        }
        return _view;
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
        if(_onFragmentInteractionListener != null)
        {
            _onFragmentInteractionListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof BluetoothFragment_PowerControl.OnFragmentInteractionListener)
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
        if(_bluetoothGatt != null)
            _bluetoothGatt.disconnect();
    }
}