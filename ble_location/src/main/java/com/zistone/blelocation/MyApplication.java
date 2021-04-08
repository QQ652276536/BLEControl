package com.zistone.blelocation;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

/**
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    private static Application _application;

    /**
     * 监听Activity的生命周期
     * <p>
     * 里面对应的回调方法我们只能重写，什么时候调用由Activity决定的！我们只能手动调用finish来关闭某个Activity
     */
    private ActivityLifecycleCallbacks _activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        /**
         * 创建时调用，只会调用一次
         * @param activity
         * @param savedInstanceState
         */
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.i(TAG, String.format("%s created...", activity.getLocalClassName()));
        }

        /**
         * 显示给用户，此时不可交互
         * @param activity
         */
        @Override
        public void onActivityStarted(Activity activity) {
            Log.i(TAG, String.format("%s started...", activity.getLocalClassName()));
        }

        /**
         * 回到前台，即重新获得焦点
         * @param activity
         */
        @Override
        public void onActivityResumed(Activity activity) {
            Log.i(TAG, String.format("%s resumed...", activity.getLocalClassName()));
        }

        /**
         * 之前的Activity还可见
         * @param activity
         */
        @Override
        public void onActivityPaused(Activity activity) {
            Log.i(TAG, String.format("%s paused...", activity.getLocalClassName()));
        }

        /**
         * 之前的Activity不可见
         * @param activity
         */
        @Override
        public void onActivityStopped(Activity activity) {
            Log.i(TAG, String.format("%s stopped...", activity.getLocalClassName()));
        }

        /**
         * 销毁该Activity，也就是该Activity最后一次被调用
         * @param activity
         */
        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.i(TAG, String.format("%s destroyed...", activity));
        }
    };

    public static Application GetApplication() {
        return _application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _application = this;
        //在使用SDK各组间之前初始化context信息,传入ApplicationContext
        registerActivityLifecycleCallbacks(_activityLifecycleCallbacks);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterActivityLifecycleCallbacks(_activityLifecycleCallbacks);
    }

}
