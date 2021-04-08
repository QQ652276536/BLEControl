package com.zistone.libble.util;

/**
 * 低功耗蓝牙的数据状态
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
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
