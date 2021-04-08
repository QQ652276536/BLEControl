package com.zistone.libble;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.zistone.libble.util.MyActivityManager;

public class BaseApplication extends Application {

    private static final String TAG = "BaseApplication";

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
        registerActivityLifecycleCallbacks(_activityLifecycleCallbacks);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterActivityLifecycleCallbacks(_activityLifecycleCallbacks);
    }

}
