package com.zistone.bluetoothtest.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

public class BLEUtil
{
    private static final String TAG = "BleUtil";
    //已知服务
    private static UUID SERVICE_UUID;
    //写入特征的UUID
    private static UUID WRITE_UUID;
    //读取特征的UUID
    private static UUID READ_UUID;
    //客户端特征配置
    private static UUID CONFIG_UUID;
    private static Context m_context;
    private static BLEUtil m_bleUtil;
    private BLEUtilCallback m_callback;
    public static BluetoothGatt m_bluetoothGatt;
    public static BluetoothGattService m_bluetoothGattService;
    public static BluetoothGattCharacteristic m_bluetoothGattCharacteristic_write;
    public static BluetoothGattCharacteristic m_bluetoothGattCharacteristic_read;
    private UUID[] m_uuidArray;
    //设备连接状态
    private boolean m_connectionState = false;
    //设备返回的数据
    private String m_deviceReturnData;

    public static synchronized BLEUtil GetInstance()
    {
        if(m_bleUtil == null)
        {
            m_bleUtil = new BLEUtil();
        }
        return m_bleUtil;
    }

    public void Init(Context context, BLEUtilCallback callback, UUID[] uuidArray)
    {
        m_context = context;
        m_callback = callback;
        m_uuidArray = uuidArray;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
        {
            ShowWarning(-3);
            ((Activity) m_context).finish();
        }
    }

    //开始扫描
    public void StartScan()
    {
        m_callback.OnLeScanStart();
    }

    //停止扫描
    public void StopScan()
    {
        m_callback.OnLeScanStop();
    }

    /**
     *
     */
    public BluetoothGattCallback m_bluetoothGattCallback = new BluetoothGattCallback()
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
                //启用发现服务
                gatt.discoverServices();
            }
            else
            {
                Log.d(TAG, ">>>连接已断开!");
                gatt.close();
                m_callback.OnDisConnected();
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
            m_bluetoothGattService = gatt.getService(SERVICE_UUID);
            if(m_bluetoothGattService != null)
            {
                //写数据的服务和特征
                m_bluetoothGattCharacteristic_write = m_bluetoothGattService.getCharacteristic(WRITE_UUID);
                if(m_bluetoothGattCharacteristic_write != null)
                {
                    Log.d(TAG, ">>>已找到写入数据的特征值!");
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
                    m_bluetoothGatt.setCharacteristicNotification(m_bluetoothGattCharacteristic_read, true);
                    BluetoothGattDescriptor descriptor = m_bluetoothGattCharacteristic_read.getDescriptor(CONFIG_UUID);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    m_bluetoothGatt.writeDescriptor(descriptor);
                    //设备已连接
                    m_callback.OnConnected();
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
            String indexStr = strArray[11];
            switch(indexStr)
            {
                //发送开门指令
                case "00":
                {
                    break;
                }
                //发送读卡指令
                case "01":
                    break;
                //发送测量电池电压指令
                case "02":
                    break;
                //发送测量磁场强度指令
                case "03":
                    break;
                //发送测量门状态指令
                case "04":
                    break;
                //发送综合测量指令
                case "80":
                    break;
                //发送开一号门锁指令
                case "81":
                    break;
                //发送开二号门锁指令
                case "82":
                    break;
                //发送开全部门锁指令
                case "83":
                    break;
                //发送查询内部控制参数指令
                case "86":
                {
                    break;
                }
                //发送修改内部控制参数指令
                case "87":
                {
                    break;
                }
            }
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
            StringBuffer stringBuffer = new StringBuffer();
            //一个包(20个字节)
            if(strArray[0].equals("68") && strArray[strArray.length - 1].equals("16"))
            {
                //清空缓存
                m_deviceReturnData = result;
            }
            //分包
            else
            {
                if(!strArray[strArray.length - 1].equals("16"))
                {
                    stringBuffer.append(result + " ");
                }
                //最后一个包
                else
                {
                    stringBuffer.append(result);
                    m_deviceReturnData = stringBuffer.toString();
                }
            }
        }
    };

    /**
     * 连接设备
     *
     * @param device
     */
    public void ConnectDevice(BluetoothDevice device)
    {
        if(m_bluetoothGatt == null)
        {
            m_bluetoothGatt = device.connectGatt(m_context, true, m_bluetoothGattCallback);
            //设备正在连接中,如果连接成功会执行回调函数OnConnected()
            m_callback.OnConnecting();
        }
        else
        {
            //设备没有连接过是调用connectGatt()来连接,已经连接过后因意外断开则调用connect()来连接.
            m_bluetoothGatt.connect();
            //启用发现服务
            m_bluetoothGatt.discoverServices();
        }
    }

    /**
     * 断开连接
     */
    public static void DisConnGatt()
    {
        if(m_bluetoothGatt != null)
        {
            m_bluetoothGatt.disconnect();
            m_bluetoothGatt.close();
            m_bluetoothGatt = null;
        }
    }

    /**
     * 发送指令
     */
    public static void SendComm(String data)
    {
        if(m_bluetoothGatt != null && m_bluetoothGattCharacteristic_write != null && data != null && !data.equals(""))
        {
            byte[] byteArray = ConvertUtil.HexStrToByteArray(data);
            m_bluetoothGattCharacteristic_write.setValue(byteArray);
            m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
        }
    }

    private static void ShowWarning(int param)
    {
        switch(param)
        {
            case -1:
                Toast.makeText(m_context, "蓝牙已断开", Toast.LENGTH_SHORT).show();
                break;
            case -2:
                Toast.makeText(m_context, "未连接蓝牙", Toast.LENGTH_SHORT).show();
                break;
            case -3:
            {
                Toast.makeText(m_context, "该设备不支持BLE", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    /**
     * 回调接口
     */
    public interface BLEUtilCallback
    {
        //扫描开始
        void OnLeScanStart();

        //扫描停止
        void OnLeScanStop();

        //设备已连接
        void OnConnected();

        //设备已断开连接
        void OnDisConnected();

        //设备连接中
        void OnConnecting();
    }
}