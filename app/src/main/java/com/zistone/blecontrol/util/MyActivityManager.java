package com.zistone.blecontrol.util;

import android.app.Activity;

import java.lang.ref.WeakReference;

public class MyActivityManager {

    private static MyActivityManager _myActivityManager = new MyActivityManager();
    private WeakReference<Activity> _currentActivityWeak;

    private MyActivityManager() {
    }

    public static MyActivityManager getInstance() {
        return _myActivityManager;
    }

    public Activity GetCurrentActivity() {
        Activity activity = null;
        if (_currentActivityWeak != null && _currentActivityWeak.get() != null) {
            activity = _currentActivityWeak.get();
        }
        return activity;
    }

    public void SetCurrentActivity(Activity activity) {
        this._currentActivityWeak = new WeakReference<>(activity);
    }

}
