package com.zistone.blecontrol.util;

public interface DialogFragmentListener {
    void OnDismiss(String tag);

    void OnComfirm(String tag, String str);
}
