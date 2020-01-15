package com.zistone.bluetoothcontrol.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 传统蓝牙
 */
public class BluetoothUtil
{
    private static final String TAG = "BluetoothUtil";
    //服务,写入,读取,配置
    private static UUID SERVICE_UUID, WRITE_UUID, READ_UUID, CONFIG_UUID;
    private static Context _context;
    private static Callback _callback;
    private static BluetoothUtil _bluetoothUtil;
    private static BluetoothAdapter _bluetoothAdapter;
    private static BluetoothLeScanner _bluetoothLeScanner;
    private static BluetoothGatt _bluetoothGatt;
    private static BluetoothGattService _bluetoothGattService;
    private static BluetoothGattCharacteristic _bluetoothGattCharacteristic_write, _bluetoothGattCharacteristic_read;

    public static synchronized void GetInstance()
    {
        if(_bluetoothUtil == null)
        {
            _bluetoothUtil = new BluetoothUtil();
        }
    }

    /**
     * @param context
     * @param callback           接口回调
     * @param bluetoothAdapter   蓝牙适配器
     * @param bluetoothLeScanner BLE设备扫描器
     * @param map                UUID
     */
    public static void Init(Context context, Callback callback, BluetoothAdapter bluetoothAdapter, BluetoothLeScanner bluetoothLeScanner, Map<String, UUID> map)
    {
        _context = context;
        _callback = callback;
        SERVICE_UUID = map.get("SERVICE_UUID");
        WRITE_UUID = map.get("WRITE_UUID");
        READ_UUID = map.get("READ_UUID");
        CONFIG_UUID = map.get("CONFIG_UUID");
        _bluetoothAdapter = bluetoothAdapter;
        _bluetoothLeScanner = bluetoothLeScanner;
    }

    /**
     * 检查蓝牙适配器是否打开
     * 在开始扫描和取消扫描的都时候都需要判断适配器的状态以及是否获取到扫描器,否则将抛出异常IllegalStateException: BT Adapter is not turned
     * ON.
     *
     * @return
     */
    private static boolean IsBluetoothAvailable()
    {
        return (_bluetoothLeScanner != null && _bluetoothAdapter != null && _bluetoothAdapter.isEnabled() && _bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON);
    }

    /**
     * 开始扫描BLE设备
     *
     * @return 蓝牙适配器是否打开、扫描器是否已获取到
     */
    public static int StartScanLe()
    {
        if(IsBluetoothAvailable())
        {
            _bluetoothLeScanner.stopScan(_leScanCallback);
            _bluetoothLeScanner.startScan(_leScanCallback);
            return 1;
        }
        else
        {
            return -1;
        }
    }

    /**
     * 停止扫描BLE设备
     *
     * @return 蓝牙适配器是否打开、扫描器是否已获取到
     */
    public static int StopScanLe()
    {
        if(IsBluetoothAvailable())
        {
            _bluetoothLeScanner.stopScan(_leScanCallback);
            return 1;
        }
        else
        {
            return -1;
        }
    }

    /**
     * 扫描到的BLE设备的回调
     */
    private static final ScanCallback _leScanCallback = new ScanCallback()
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            super.onScanResult(callbackType, result);
            BluetoothDevice bluetoothDevice = result.getDevice();
            String address = bluetoothDevice.getAddress();
            int rssi = result.getRssi();
            Log.i(TAG, String.format("设备%s的信号强度%d", address, rssi));
            _callback.OnScanLeResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            super.onBatchScanResults(results);
        }

        /**
         * 扫描失败
         *
         * errorCode=1:已启动具有相同设置的BLE扫描
         * errorCode=2:应用未注册
         * errorCode=3:内部错误
         * errorCode=4:设备不支持低功耗蓝牙
         *
         * @param errorCode
         */
        @Override
        public void onScanFailed(int errorCode)
        {
            super.onScanFailed(errorCode);
        }
    };

    /**
     * 连接设备
     *
     * @param device
     */
    public static void ConnectDevice(BluetoothDevice device)
    {
        if(_bluetoothGatt == null)
        {
            _bluetoothGatt = device.connectGatt(_context, true, _bluetoothGattCallback);
            //设备正在连接中,如果连接成功会执行回调函数OnConnected()
            _callback.OnConnecting();
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
                Log.d(TAG, ">>>成功建立连接!");
                //启用发现服务
                gatt.discoverServices();
            }
            else
            {
                Log.d(TAG, ">>>连接已断开!");
                gatt.close();
                _callback.OnDisConnected();
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
            //直到这里才是真正建立了可通信的连接
            //通过UUID找到服务
            _bluetoothGattService = gatt.getService(SERVICE_UUID);
            if(_bluetoothGattService != null)
            {
                //写数据的服务和特征
                _bluetoothGattCharacteristic_write = _bluetoothGattService.getCharacteristic(WRITE_UUID);
                if(_bluetoothGattCharacteristic_write != null)
                {
                    Log.d(TAG, ">>>已找到写入数据的特征值!");
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
                    _bluetoothGatt.setCharacteristicNotification(_bluetoothGattCharacteristic_read, true);
                    BluetoothGattDescriptor descriptor = _bluetoothGattCharacteristic_read.getDescriptor(CONFIG_UUID);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    _bluetoothGatt.writeDescriptor(descriptor);
                    //设备已连接
                    _callback.OnConnected();
                }
                else
                {
                    Log.e(TAG, ">>>该UUID无读取数据的特征值!");
                }
            }
            else
            {
                Log.e(TAG, ">>>该UUID无蓝牙服务!");
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
            _callback.OnWriteSuccess(result);
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
            _callback.OnReadSuccess(result);
        }
    };

    /**
     * 发送指令
     */
    public static void SendComm(String data)
    {
        if(_bluetoothGatt != null && _bluetoothGattCharacteristic_write != null && data != null && !data.equals(""))
        {
            byte[] byteArray = ConvertUtil.HexStrToByteArray(data);
            _bluetoothGattCharacteristic_write.setValue(byteArray);
            _bluetoothGatt.writeCharacteristic(_bluetoothGattCharacteristic_write);
        }
    }

    /**
     * 回调接口
     */
    public interface Callback
    {
        /**
         * 扫描开始
         */
        void OnLeScanStart();

        /**
         * 扫描停止
         */
        void OnLeScanStop();

        /**
         * 已连接设备
         */
        void OnConnected();

        /**
         * 已断开连接
         */
        void OnDisConnected();

        /**
         * 正在连接设备
         */
        void OnConnecting();

        /**
         * 扫描到的BLE设备
         */
        void OnScanLeResult(ScanResult result);

        /**
         * 成功写入数据
         */
        void OnWriteSuccess(String str);

        /**
         * 成功读取数据
         */
        void OnReadSuccess(String str);
    }
}