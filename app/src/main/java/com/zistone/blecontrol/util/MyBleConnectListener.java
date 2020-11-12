package com.zistone.blecontrol.util;

import android.bluetooth.le.ScanResult;

public interface MyBleConnectListener {

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

}
