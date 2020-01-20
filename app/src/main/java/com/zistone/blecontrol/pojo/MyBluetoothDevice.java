package com.zistone.blecontrol.pojo;

import android.bluetooth.BluetoothDevice;

public class MyBluetoothDevice
{
    private String _name;
    private String _address;
    private int _rssi;
    private int _boundState;
    private BluetoothDevice _bluetoothDevice;

    public String get_name()
    {
        return _name;
    }

    public void set_name(String _name)
    {
        this._name = _name;
    }

    public String get_address()
    {
        return _address;
    }

    public void set_address(String _address)
    {
        this._address = _address;
    }

    public int get_rssi()
    {
        return _rssi;
    }

    public void set_rssi(int _rssi)
    {
        this._rssi = _rssi;
    }

    public int get_boundState()
    {
        return _boundState;
    }

    public void set_boundState(int _boundState)
    {
        this._boundState = _boundState;
    }

    public BluetoothDevice get_bluetoothDevice()
    {
        return _bluetoothDevice;
    }

    public void set_bluetoothDevice(BluetoothDevice _bluetoothDevice)
    {
        this._bluetoothDevice = _bluetoothDevice;
    }
}
