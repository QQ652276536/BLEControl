package com.zistone.bluetoothcontrol.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import java.util.List;

public class BLEUtil
{
    private static Context _context;
    private static BLEListener _listener;
    private static BluetoothAdapter _bluetoothAdapter;
    private static BluetoothLeScanner _bluetoothLeScanner;

    /**
     * @param context
     * @param listener           接口回调
     * @param bluetoothAdapter   蓝牙适配器
     * @param bluetoothLeScanner BLE设备扫描器
     */
    public static void Init(Context context, BLEListener listener, BluetoothAdapter bluetoothAdapter, BluetoothLeScanner bluetoothLeScanner)
    {
        _context = context;
        _listener = listener;
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
            _listener.OnScanLeResult(result);
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

}