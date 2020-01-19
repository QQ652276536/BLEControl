package com.zistone.bluetoothcontrol.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;

public class BLEUtil
{
    private static Context _context;
    private static BLEListener _listener;
    private static BluetoothAdapter _bluetoothAdapter;
    private static BluetoothLeScanner _bluetoothLeScanner;
    //筛选条件,可以设置名称、地址、UUID
    private static List<ScanFilter> _scanFilterList = new ArrayList<ScanFilter>()
    {{
        ScanFilter.Builder filter = new ScanFilter.Builder();
        ParcelUuid parcelUuidMask = ParcelUuid.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF");
        ParcelUuid parcelUuid = ParcelUuid.fromString("00002760-08c2-11e1-9073-0e8ac72e1001");
        filter.setServiceUuid(parcelUuid, parcelUuidMask);
        this.add(filter.build());
    }};
    //扫描设置,可以设置扫描模式、时间、类型、结果等
    //SCAN_MODE_LOW_LATENCY:扫描优先
    //SCAN_MODE_LOW_POWER:省电优先
    //SCAN_MODE_BALANCED:平衡模式
    //SCAN_MODE_OPPORTUNISTIC:这是一个特殊的扫描模式（投机取巧的）,就是说程序本身不会使用BLE扫描功能,而是借助其他的扫描结果.比如:程序A用了这个模式,其实程序A没有使用到蓝牙功能,但是程序B在扫描的话,程序B的扫描结果会共享给程序A
    //时间:扫描到设置时间后执行onBatchScanResults的回调
    private static ScanSettings _scanSettings = new ScanSettings.Builder().setReportDelay(15 * 1000).setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build();

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
            //            _bluetoothLeScanner.startScan(_scanFilterList, _scanSettings, _leScanCallback);
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