package com.zistone.bluetoothcontrol.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;

import java.util.List;

public class DeviceFilterShared
{
    public static SharedPreferences Share(Context context)
    {
        return context.getSharedPreferences("DEVICEFILTER", Context.MODE_PRIVATE);
    }

    public static List<String> GetFilterName(Context context)
    {
        String jsonStr = Share(context).getString("filterDeviceByName", "");
        List<String> list = JSON.parseArray(jsonStr, String.class);
        return list;
    }

    public static boolean SetFilterName(Context context, List<String> list)
    {
        SharedPreferences.Editor editor = Share(context).edit();
        String jsonStr = JSON.toJSONString(list);
        editor.putString("filterDeviceByName", jsonStr);
        return editor.commit();
    }

    public static boolean GetFilterDevice(Context context)
    {
        return Share(context).getBoolean("filterDevice", false);
    }

    public static boolean SetFilterDevie(Context context, boolean flag)
    {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putBoolean("filterDevice", flag);
        return editor.commit();
    }

}
