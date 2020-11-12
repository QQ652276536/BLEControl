package com.zistone.blecontrol.util;

import android.bluetooth.le.ScanResult;

public interface MyBleMessageListener {

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
