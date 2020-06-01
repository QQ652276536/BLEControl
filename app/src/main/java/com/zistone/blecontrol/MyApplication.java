package com.zistone.blecontrol;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.zistone.blecontrol.util.MyActivityManager;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    /**
     * 监听Activity的生命周期
     */
    private ActivityLifecycleCallbacks _activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.i(TAG, String.format("%s created...", activity.getLocalClassName()));
            MyActivityManager.getInstance().AddActivity(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
            Log.i(TAG, String.format("%s started...", activity.getLocalClassName()));
        }

        @Override
        public void onActivityResumed(Activity activity) {
            Log.i(TAG, String.format("%s resumed...", activity.getLocalClassName()));
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Log.i(TAG, String.format("%s paused...", activity.getLocalClassName()));
        }

        @Override
        public void onActivityStopped(Activity activity) {
            Log.i(TAG, String.format("%s stopped...", activity.getLocalClassName()));
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.i(TAG, String.format("%s destroyed...", activity));
            MyActivityManager.getInstance().FinishActivity(activity);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(_activityLifecycleCallbacks);//在使用SDK各组间之前初始化context信息,传入ApplicationContext
        //默认本地个性化地图初始化方法
        SDKInitializer.initialize(this);
        //自4.3.0起,百度地图SDK所有接口均支持百度坐标和国测局坐标,用此方法设置您使用的坐标类型
        //包括BD09LL和GCJ02两种坐标,默认是BD09LL坐标
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterActivityLifecycleCallbacks(_activityLifecycleCallbacks);
    }
}
