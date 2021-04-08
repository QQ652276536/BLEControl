package com.zistone.libble.util;

import android.bluetooth.le.ScanResult;

/**
 * 低功耗蓝牙的扫描状态
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public interface MyBleScanListener {

    /**
     * 扫描到的设备
     *
     * @param result
     */
    void OnScanLeResult(ScanResult result);

}
