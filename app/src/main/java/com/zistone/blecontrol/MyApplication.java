package com.zistone.blecontrol;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.zistone.blecontrol.activity.BleDeviceList;
import com.zistone.blecontrol.activity.CommandTest;
import com.zistone.blecontrol.activity.MainActivity;
import com.zistone.blecontrol.activity.MaterialsInDB;
import com.zistone.blecontrol.activity.PowerControl;
import com.zistone.blecontrol.activity.TemperatureMeasure;
import com.zistone.blecontrol.util.MyActivityManager;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    /**
     * 监听Activity的生命周期
     */
    private ActivityLifecycleCallbacks _activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            if (activity.getClass() == MainActivity.class) {
                Log.d(TAG, ">>>MainActivity created...");
            } else if (activity.getClass() == BleDeviceList.class) {
                Log.d(TAG, ">>>BleDeviceList created...");
            } else if (activity.getClass() == PowerControl.class) {
                Log.d(TAG, ">>>PowerControl created...");
            } else if (activity.getClass() == CommandTest.class) {
                Log.d(TAG, ">>>CommandTest created...");
            } else if (activity.getClass() == MaterialsInDB.class) {
                Log.d(TAG, ">>>MaterialsInDB created...");
            } else if (activity.getClass() == TemperatureMeasure.class) {
                Log.d(TAG, ">>>TemperatureMeasure created...");
            }
            MyActivityManager.getInstance().SetCurrentActivity(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
            Log.d(TAG, String.format(">>>%s started...", activity.getLocalClassName()));
        }

        @Override
        public void onActivityResumed(Activity activity) {
            Log.d(TAG, String.format(">>>%s resumed...", activity.getLocalClassName()));
            MyActivityManager.getInstance().SetCurrentActivity(activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Log.d(TAG, String.format(">>>%s paused...", activity.getLocalClassName()));
        }

        @Override
        public void onActivityStopped(Activity activity) {
            Log.d(TAG, String.format(">>>%s stopped...", activity.getLocalClassName()));
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.d(TAG, String.format(">>>%s destroyed...", activity.getLocalClassName()));
        }
    };

    @Override
    public void onCreate() {
        registerActivityLifecycleCallbacks(_activityLifecycleCallbacks);
        super.onCreate();
    }
}
