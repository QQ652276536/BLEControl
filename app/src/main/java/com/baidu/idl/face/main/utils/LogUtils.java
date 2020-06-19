package com.baidu.idl.face.main.utils;

import android.util.Log;

import com.baidu.idl.face.main.model.SingleBaseConfig;

public class LogUtils {

    private static boolean isDebug = SingleBaseConfig.getBaseConfig().isDebug();

    public static void setIsDebug(boolean isDebug) {
        LogUtils.isDebug = isDebug;
    }

    public static int v(String tag, String msg) {
        if (isDebug) {
            return Log.v(tag, msg);
        } else {
            return -1;
        }
    }

    public static int d(String tag, String msg) {
        if (isDebug) {
            return Log.d(tag, msg);
        } else {
            return -1;
        }
    }

    public static int i(String tag, String msg) {
        if (isDebug) {
            return Log.i(tag, msg);
        } else {
            return -1;
        }
    }

    public static int w(String tag, String msg) {
        if (isDebug) {
            return Log.w(tag, msg);
        } else {
            return -1;
        }
    }

    public static int e(String tag, String msg) {
        if (isDebug) {
            return Log.e(tag, msg);
        } else {
            return -1;
        }
    }
}
