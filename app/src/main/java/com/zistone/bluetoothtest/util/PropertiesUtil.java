package com.zistone.bluetoothtest.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil
{
    public static Properties GetValueProperties(Context context)
    {
        Properties properties = new Properties();
        InputStream inputStream = context.getClassLoader().getResourceAsStream("assets/config.properties");
        try
        {
            properties.load(inputStream);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return properties;
    }
}
