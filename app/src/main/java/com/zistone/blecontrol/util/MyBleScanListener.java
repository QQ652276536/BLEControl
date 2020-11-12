package com.zistone.blecontrol.util;

import android.bluetooth.le.ScanResult;

public interface MyBleScanListener {

    /**
     * 扫描到的设备
     *
     * @param result
     */
    void OnScanLeResult(ScanResult result);

}
