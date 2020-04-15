package com.zistone.blecontrol.util;

import android.app.Activity;
import android.os.Process;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Stack;

public class MyActivityManager {

    private static MyActivityManager _myActivityManager = new MyActivityManager();
    private Stack<WeakReference<Activity>> _stack;

    private MyActivityManager() {
    }

    public static MyActivityManager getInstance() {
        return _myActivityManager;
    }

    public void AddActivity(Activity activity) {
        if (_stack == null)
            _stack = new Stack<>();
        _stack.add(new WeakReference<>(activity));
    }

    public void CheckWeakReference() {
        if (_stack != null) {
            Iterator<WeakReference<Activity>> iterator = _stack.iterator();
            while (iterator.hasNext()) {
                WeakReference<Activity> tempReference = iterator.next();
                Activity tempActivity = tempReference.get();
                if (tempActivity == null) {
                    iterator.remove();
                }
            }
        }
    }

    public Activity GetCurrentActivity() {
        CheckWeakReference();
        if (_stack != null && !_stack.isEmpty())
            return _stack.lastElement().get();
        else
            return null;
    }

    public void FinishActivity(Activity activity) {
        if (activity != null && _stack != null) {
            Iterator<WeakReference<Activity>> iterator = _stack.iterator();
            while (iterator.hasNext()) {
                WeakReference<Activity> tempReference = iterator.next();
                Activity tempActivity = tempReference.get();
                if (tempActivity == null) {
                    iterator.remove();
                    continue;
                }
                if (tempActivity.equals(activity)) {
                    iterator.remove();
                }
            }
            activity.finish();
        }
    }

    public void FinishActivity(Class<?> activity) {
        if (activity != null && _stack != null) {
            Iterator<WeakReference<Activity>> iterator = _stack.iterator();
            while (iterator.hasNext()) {
                WeakReference<Activity> tempReference = iterator.next();
                Activity tempActivity = tempReference.get();
                if (tempActivity == null) {
                    iterator.remove();
                    continue;
                }
                if (tempActivity.getClass().equals(activity)) {
                    iterator.remove();
                    tempActivity.finish();
                }
            }
        }
    }

    public void FinishAllActivity() {
        if (_stack != null) {
            for (WeakReference<Activity> tempReference : _stack) {
                Activity activity = tempReference.get();
                if (activity != null)
                    activity.finish();
            }
            _stack.clear();
        }
    }

    public void ExitAPP() {
        try {
            FinishAllActivity();
            System.exit(0);
            Process.killProcess(Process.myPid());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
