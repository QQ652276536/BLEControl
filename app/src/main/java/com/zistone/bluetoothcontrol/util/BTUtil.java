package com.zistone.bluetoothcontrol.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.util.Map;
import java.util.UUID;

public class BTUtil
{
    private static UUID SERVICE_UUID, WRITE_UUID, READ_UUID, CONFIG_UUID;
    private static Context _context;
    private static BTListener _listener;
    private static BluetoothAdapter _bluetoothAdapter;
    private static BluetoothGatt _bluetoothGatt;
    private static BluetoothGattService _bluetoothGattService;
    private static BluetoothGattCharacteristic _bluetoothGattCharacteristic_write, _bluetoothGattCharacteristic_read;
    private static BluetoothDevice _bluetoothDevice;

    /**
     * @param context
     * @param listener         接口回调
     * @param bluetoothAdapter 蓝牙适配器
     * @param bluetoothDevice  BLE设备
     */
    public static void Init(Context context, BTListener listener, BluetoothAdapter bluetoothAdapter, BluetoothDevice bluetoothDevice)
    {
        _context = context;
        _listener = listener;
        _bluetoothAdapter = bluetoothAdapter;
        _bluetoothDevice = bluetoothDevice;
    }

    /**
     * 连接设备
     *
     * @param map    UUID
     * @param device
     */
    public static void ConnectDevice(BluetoothDevice device, Map<String, UUID> map)
    {
        SERVICE_UUID = map.get("SERVICE_UUID");
        WRITE_UUID = map.get("WRITE_UUID");
        READ_UUID = map.get("READ_UUID");
        CONFIG_UUID = map.get("CONFIG_UUID");
        if(_bluetoothGatt == null)
        {
            _bluetoothGatt = device.connectGatt(_context, true, _bluetoothGattCallback);
            //设备正在连接中,如果连接成功会执行回调函数discoverServices()
            _listener.OnConnecting();
        }
        else
        {
            //设备没有连接过是调用connectGatt()来连接,已经连接过后因意外断开则调用connect()来连接.
            _bluetoothGatt.connect();
            //启用发现服务
            _bluetoothGatt.discoverServices();
        }
    }

    /**
     * 断开连接
     */
    public static void DisConnGatt()
    {
        if(_bluetoothGatt != null)
        {
            _bluetoothGatt.disconnect();
            _bluetoothGatt.close();
            _bluetoothGatt = null;
        }
    }

    /**
     * 连接结果的回调
     */
    public static BluetoothGattCallback _bluetoothGattCallback = new BluetoothGattCallback()
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
                //启用发现服务
                gatt.discoverServices();
            }
            else
            {
                gatt.close();
                _listener.OnDisConnected();
            }
        }

        /**
         * 发现设备(真正建立连接)后回调
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            //通过UUID找到服务,直到这里才是真正建立了可通信的连接
            _bluetoothGattService = gatt.getService(SERVICE_UUID);
            if(_bluetoothGattService != null)
            {
                //读写数据的服务和特征
                _bluetoothGattCharacteristic_write = _bluetoothGattService.getCharacteristic(WRITE_UUID);
                _bluetoothGattCharacteristic_read = _bluetoothGattService.getCharacteristic(READ_UUID);
                if(_bluetoothGattCharacteristic_read != null)
                {
                    //订阅读取通知
                    _bluetoothGatt.setCharacteristicNotification(_bluetoothGattCharacteristic_read, true);
                    BluetoothGattDescriptor descriptor = _bluetoothGattCharacteristic_read.getDescriptor(CONFIG_UUID);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    _bluetoothGatt.writeDescriptor(descriptor);
                    _listener.OnConnected();
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
            _listener.OnWriteSuccess(result);
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
            _listener.OnReadSuccess(result);
        }
    };

    /**
     * 发送数据
     *
     * @param data
     * @return
     */
    public static int SendComm(String data)
    {
        if(_bluetoothGatt != null && _bluetoothGattCharacteristic_write != null && data != null && !data.equals(""))
        {
            byte[] byteArray = ConvertUtil.HexStrToByteArray(data);
            _bluetoothGattCharacteristic_write.setValue(byteArray);
            _bluetoothGatt.writeCharacteristic(_bluetoothGattCharacteristic_write);
            return 1;
        }
        else
        {
            return -1;
        }
    }

}