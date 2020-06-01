package com.zistone.blecontrol.util;

public interface BluetoothListener {
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
     * 收到数据
     *
     * @param byteArray
     */
    void OnReadSuccess(byte[] byteArray);
}
