package com.zistone.blecontrol.baiduface.listener;

public interface SdkInitListener {
    public void initStart();

    public void initLicenseSuccess();

    public void initLicenseFail(int errorCode, String msg);

    public void initModelSuccess();

    public void initModelFail(int errorCode, String msg);
}
