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

    public static List<String> Get(Context context)
    {
        String jsonStr = Share(context).getString("deviceFilter", "");
        List<String> list = JSON.parseArray(jsonStr, String.class);
        return list;
    }

    public static boolean Set(Context context, List<String> list)
    {
        SharedPreferences.Editor editor = Share(context).edit();
        String jsonStr = JSON.toJSONString(list);
        editor.putString("deviceFilter", jsonStr);
        return editor.commit();
    }

}
