package com.zistone.libble.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 读取本地配置文件
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public final class MyPropertiesUtil {
    private MyPropertiesUtil() {
    }

    public static Properties GetValueProperties(Context context) {
        Properties properties = new Properties();
        InputStream inputStream = context.getClassLoader().getResourceAsStream("assets/config.properties");
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
