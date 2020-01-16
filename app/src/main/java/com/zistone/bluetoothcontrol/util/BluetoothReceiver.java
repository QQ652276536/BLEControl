package com.zistone.bluetoothcontrol.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 蓝牙广播
 */
public class BluetoothReceiver extends BroadcastReceiver
{
    private static final String TAG = "BluetoothReceiver";
    private Listener _listener;

    public interface Listener
    {
        /**
         * 正在扫描
         */
        void StartedScannListener();

        /**
         * 扫描到一台设备
         *
         * @param device 扫描到的设备
         * @param rssi   信号强度
         */
        void FoundDeviceListener(BluetoothDevice device, int rssi);

        /**
         * 设备状态改变
         *
         * @param param
         */
        void StateChangedListener(int param);

        /**
         * 扫描完成
         */
        void ScannOverListener();
    }

    public BluetoothReceiver(Listener listener)
    {
        this._listener = listener;
    }

    /**
     * 扫描到设备
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        //正在扫描设备
        if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
        {
            _listener.StartedScannListener();
        }
        //扫描到一台设备
        else if(action.equals(BluetoothDevice.ACTION_FOUND))
        {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String name = device.getName();
            String address = device.getAddress();
            Log.d(TAG, String.format("扫描到设备:%s,地址:%s", name, address));
            int rssi = 0;
            //获取未配对设备的信号强度
            if(device.getBondState() != BluetoothDevice.BOND_BONDED)
            {
                rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
            }
            _listener.FoundDeviceListener(device, rssi);
        }
        //设备状态改变
        else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
        {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //正在配对
            if(device.getBondState() == BluetoothDevice.BOND_BONDING)
            {
            }
            //完成配对
            else if(device.getBondState() == BluetoothDevice.BOND_BONDED)
            {
            }
            //取消配对
            else if(device.getBondState() == BluetoothDevice.BOND_NONE)
            {
            }
        }
        //扫描完成
        else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
        {
            _listener.ScannOverListener();
            Log.d(TAG, "本次扫描完毕,停止扫描.");
        }
    }
}
