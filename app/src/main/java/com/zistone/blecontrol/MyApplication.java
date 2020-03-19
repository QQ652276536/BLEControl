package com.zistone.blecontrol;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.zistone.blecontrol.util.MyActivityManager;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    /**
     * 监听Activity的生命周期
     */
    private ActivityLifecycleCallbacks _activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.d(TAG, String.format(">>>%s created...", activity.getLocalClassName()));
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

    @Override
    public void onTerminate() {
        unregisterActivityLifecycleCallbacks(_activityLifecycleCallbacks);
        super.onTerminate();
    }
}
