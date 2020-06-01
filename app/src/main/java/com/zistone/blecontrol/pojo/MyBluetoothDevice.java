package com.zistone.blecontrol.pojo;

import android.bluetooth.BluetoothDevice;

public class MyBluetoothDevice {
    private int id;
    private String name;
    private String address;
    private int rssi;
    private int boundState;
    private BluetoothDevice bluetoothDevice;
    private int materialId;

    @Override
    public String toString() {
        return "MyBluetoothDevice{" + "id=" + id + ", name='" + name + '\'' + ", address='" + address + '\'' + ", rssi=" + rssi + ", boundState=" + boundState + ", bluetoothDevice=" + bluetoothDevice + ", materialId=" + materialId + '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getBoundState() {
        return boundState;
    }

    public void setBoundState(int boundState) {
        this.boundState = boundState;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }
}
