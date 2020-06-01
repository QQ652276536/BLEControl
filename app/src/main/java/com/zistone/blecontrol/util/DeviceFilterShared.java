package com.zistone.blecontrol.util;

import android.content.Context;
import android.content.SharedPreferences;

public class DeviceFilterShared {
    public static SharedPreferences Share(Context context) {
        return context.getSharedPreferences("DEVICEFILTER", Context.MODE_PRIVATE);
    }

    public static int GetFilterRssi(Context context) {
        return Share(context).getInt("filterDeviceByRssi", 100);
    }

    public static boolean SetFilterRssi(Context context, int rssi) {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putInt("filterDeviceByRssi", rssi);
        return editor.commit();
    }

    public static String GetFilterAddress(Context context) {
        return Share(context).getString("filterDeviceByAddress", "");
    }

    public static boolean SetFilterAddress(Context context, String address) {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putString("filterDeviceByAddress", address);
        return editor.commit();
    }

    public static String GetFilterName(Context context) {
        return Share(context).getString("filterDeviceByName", "");
    }

    public static boolean SetFilterName(Context context, String name) {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putString("filterDeviceByName", name);
        return editor.commit();
    }

    public static boolean GetFilterDevice(Context context) {
        return Share(context).getBoolean("filterConnectSuccessDevice", false);
    }

    public static boolean SetFilterDevie(Context context, boolean flag) {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putBoolean("filterConnectSuccessDevice", flag);
        return editor.commit();
    }

    public static String GetTemperatureParam(Context context) {
        return Share(context).getString("setTemperatureParam", "0.0");
    }

    public static boolean SetTemperatureParam(Context context, String value) {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putString("setTemperatureParam", value);
        return editor.commit();
    }

}
