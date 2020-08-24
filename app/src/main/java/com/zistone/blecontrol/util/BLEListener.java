package com.zistone.blecontrol.util;

import android.bluetooth.le.ScanResult;

public interface BleListener {

    /**
     * 扫描到的设备
     *
     * @param result
     */
    void OnScanLeResult(ScanResult result);

    /**
     * 连接成功
     */
    void OnConnected();

    /**
     * 正在连接
     */
    void OnConnecting();

    /**
     * 断开连接
     */
    void OnDisConnected();

    /**
     * 数据发送成功
     *
     * @param byteArray
     */
    void OnWriteSuccess(byte[] byteArray);

    /**
     * 成功收到数据
     *
     * @param byteArray
     */
    void OnReadSuccess(byte[] byteArray);

}
