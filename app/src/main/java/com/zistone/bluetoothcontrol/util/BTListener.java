package com.zistone.bluetoothcontrol.util;

public interface BTListener
{
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
     * @param data
     */
    void OnWriteSuccess(String data);

    /**
     * 收到数据
     *
     * @param data
     */
    void OnReadSuccess(String data);
}
