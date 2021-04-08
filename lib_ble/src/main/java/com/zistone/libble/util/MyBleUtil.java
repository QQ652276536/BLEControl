package com.zistone.libble.util;

import android.app.Activity;
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
import android.content.Intent;
import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 低功耗蓝牙的工具类封装
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public final class MyBleUtil {
    private static Context _context;
    private static BluetoothAdapter _bluetoothAdapter;
    private static BluetoothLeScanner _bluetoothLeScanner;
    //    //筛选条件,可以设置名称、地址、UUID
    //    private static List<ScanFilter> _scanFilterList = new ArrayList<ScanFilter>()
    //    {{
    //        ScanFilter.Builder filter = new ScanFilter.Builder();
    //        ParcelUuid parcelUuidMask = ParcelUuid.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF");
    //        ParcelUuid parcelUuid = ParcelUuid.fromString("00002760-08c2-11e1-9073-0e8ac72e1001");
    //        filter.setServiceUuid(parcelUuid, parcelUuidMask);
    //        this.add(filter.build());
    //    }};
    //    //扫描设置,可以设置扫描模式、时间、类型、结果等
    //    //SCAN_MODE_LOW_LATENCY:扫描优先
    //    //SCAN_MODE_LOW_POWER:省电优先
    //    //SCAN_MODE_BALANCED:平衡模式
    //    //SCAN_MODE_OPPORTUNISTIC:这是一个特殊的扫描模式（投机取巧的）,就是说程序本身不会使用BLE扫描功能,而是借助其他的扫描结果.比如:程序A用了这个模式,其实程序A没有使用到蓝牙功能,但是程序B在扫描的话,程序B的扫描结果会共享给程序A
    //    //时间:扫描到设置时间后执行onBatchScanResults的回调
    //    private static ScanSettings _scanSettings = new ScanSettings.Builder().setReportDelay(15 * 1000).setScanMode(ScanSettings
    //    .SCAN_MODE_LOW_LATENCY).setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build();
    private static UUID SERVICE_UUID, WRITE_UUID, READ_UUID, CONFIG_UUID;
    private static MyBleScanListener _scanListener;
    private static MyBleConnectListener _connectListener;
    private static MyBleMessageListener _messageListener;
    private static BluetoothGatt _bluetoothGatt;
    private static BluetoothGattService _bluetoothGattService;
    private static BluetoothGattCharacteristic _bluetoothGattCharacteristic_write, _bluetoothGattCharacteristic_read;

    /**
     * 扫描到BLE设备的回调
     */
    private static final ScanCallback _leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            _scanListener.OnScanLeResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
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
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    /**
     * 连接状态以及读写状态的回调
     */
    public static BluetoothGattCallback _bluetoothGattCallback = new BluetoothGattCallback() {
        /**
         * 连接状态改变时回调
         * @param gatt
         * @param status Gatt的状态
         * @param newState 连接状态
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
                //启用发现服务
                _bluetoothGatt.discoverServices();
            }
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                if (_bluetoothGatt != null)
                    _bluetoothGatt.close();
                _connectListener.OnDisConnected();
            }
        }

        /**
         * 发现设备(真正建立连接)后回调
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //通过UUID找到服务,直到这里才是真正建立了可通信的连接
            _bluetoothGattService = _bluetoothGatt.getService(SERVICE_UUID);
            if (_bluetoothGattService != null) {
                //读写数据的服务和特征
                _bluetoothGattCharacteristic_write = _bluetoothGattService.getCharacteristic(WRITE_UUID);
                _bluetoothGattCharacteristic_read = _bluetoothGattService.getCharacteristic(READ_UUID);
                if (_bluetoothGattCharacteristic_read != null) {
                    //订阅读取通知
                    _bluetoothGatt.setCharacteristicNotification(_bluetoothGattCharacteristic_read, true);
                    BluetoothGattDescriptor descriptor = _bluetoothGattCharacteristic_read.getDescriptor(CONFIG_UUID);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    _bluetoothGatt.writeDescriptor(descriptor);
                    _connectListener.OnConnected();
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
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            byte[] byteArray = characteristic.getValue();
            _messageListener.OnWriteSuccess(byteArray);
        }

        /**
         * 收到硬件返回的数据时回调,如果是Notify的方式
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] byteArray = characteristic.getValue();
            _messageListener.OnReadSuccess(byteArray);
        }
    };

    /**
     * 私有构造方法，禁止外部实例化
     */
    private MyBleUtil() {
    }

    /**
     * 设置扫描监听
     *
     * @param listener
     */
    public static void SetScanListener(MyBleScanListener listener) {
        _scanListener = listener;
    }

    /**
     * 设置连接监听
     *
     * @param listener
     */
    public static void SetConnectListener(MyBleConnectListener listener) {
        _connectListener = listener;
    }

    /**
     * 设置消息监听
     *
     * @param listener
     */
    public static void SetMessageListener(MyBleMessageListener listener) {
        _messageListener = listener;
    }

    /**
     * @param context
     * @param bluetoothAdapter   蓝牙适配器
     * @param bluetoothLeScanner BLE设备扫描器
     * @return
     */
    public static int Init(Context context, BluetoothAdapter bluetoothAdapter, BluetoothLeScanner bluetoothLeScanner) {
        _context = context;
        _bluetoothAdapter = bluetoothAdapter;
        _bluetoothLeScanner = bluetoothLeScanner;
        int result;
        //打开手机蓝牙
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (_bluetoothAdapter != null) {
            if (!_bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((Activity) _context).startActivityForResult(intent, 1);
                result = 1;
            } else {
                switch (_bluetoothAdapter.getState()) {
                    //蓝牙已开启
                    case BluetoothAdapter.STATE_ON:
                    case BluetoothAdapter.STATE_TURNING_ON:
                        result = 1;
                        break;
                    //蓝牙未开启
                    case BluetoothAdapter.STATE_OFF:
                    case BluetoothAdapter.STATE_TURNING_OFF:
                    default:
                        result = -1;
                }
            }
        } else {
            result = -2;
        }
        return result;
    }

    /**
     * 检查蓝牙适配器是否打开
     * 在开始扫描和取消扫描的都时候都需要判断适配器的状态以及是否获取到扫描器,否则将抛出异常IllegalStateException: BT Adapter is not turned
     * ON.
     *
     * @return
     */
    private static boolean IsBluetoothAvailable() {
        return (_bluetoothLeScanner != null && _bluetoothAdapter != null && _bluetoothAdapter.isEnabled() && _bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON);
    }

    /**
     * 开始扫描BLE设备
     *
     * @return 蓝牙适配器是否打开、扫描器是否已获取到
     */
    public static int StartScanLe() {
        if (IsBluetoothAvailable()) {
            _bluetoothLeScanner.stopScan(_leScanCallback);
            //            _bluetoothLeScanner.startScan(_scanFilterList, _scanSettings, _leScanCallback);
            _bluetoothLeScanner.startScan(_leScanCallback);
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * 停止扫描BLE设备
     *
     * @return 蓝牙适配器是否打开、扫描器是否已获取到
     */
    public static int StopScanLe() {
        if (IsBluetoothAvailable()) {
            _bluetoothLeScanner.stopScan(_leScanCallback);
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * 连接设备
     *
     * @param map
     * @param device
     */
    public static void ConnectDevice(BluetoothDevice device, Map<String, UUID> map) {
        SERVICE_UUID = map.get("SERVICE_UUID");
        WRITE_UUID = map.get("WRITE_UUID");
        READ_UUID = map.get("READ_UUID");
        CONFIG_UUID = map.get("CONFIG_UUID");
        _bluetoothGatt = device.connectGatt(_context, false, _bluetoothGattCallback);
        //设备正在连接中,如果连接成功会执行回调函数discoverServices()
        _connectListener.OnConnecting();
    }

    /**
     * 断开连接
     * <p>
     * 如果手动disconnect不要立即close,不然onConnectionStateChange里会抛空指针异常,因为手动断开时会回调onConnectionStateChange
     * 方法,在这个方法中close释放资源
     */
    public static void DisConnGatt() {
        if (_bluetoothGatt != null)
            _bluetoothGatt.disconnect();
    }

    /**
     * 发送数据
     *
     * @param data
     * @return
     */
    public static String SendComm(String data) {
        String checkCdoe = "";
        if (_bluetoothGatt != null && _bluetoothGattCharacteristic_write != null && data != null && !data.equals("")) {
            try {
                //除了校验码本身和结尾的16，都参与校验
                data = data.substring(0, data.length() - 4);
                //添加空格
                data = MyConvertUtil.StrAddCharacter(data, 2, " ");
                //计算校验码
                checkCdoe = MyConvertUtil.CRC_Zistone_BLE(data);
                //拼接校验码和结尾
                data += " " + checkCdoe + " 16";
                //去掉空格
                data = data.replaceAll(" ", "");
                //转为16进制
                byte[] byteArray = MyConvertUtil.HexStrToByteArray(data);
                //发送
                _bluetoothGattCharacteristic_write.setValue(byteArray);
                _bluetoothGatt.writeCharacteristic(_bluetoothGattCharacteristic_write);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return checkCdoe;
    }

}