package com.zistone.blecontrol.util;

public interface MyProgressDialogListener {
    void OnDismiss(String tag);

    void OnComfirm(String tag, String str);

    void OnComfirm(String tag, Object[] objectArray);
}
