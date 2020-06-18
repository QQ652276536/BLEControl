package com.baidu.idl.main.facesdk.utils;


import java.util.zip.ZipEntry;

/**
 * Created by v_shishuaifeng on 2020/1/13.
 * 为了保证ZIPFile不存在目录穿越漏洞，即ZipFile中不包含../
 */

public class SafeZipEntry extends ZipEntry {
    public SafeZipEntry(String name) {
        super(name);
    }

    public SafeZipEntry(ZipEntry ze) {
        super(ze);
    }

    public String getName() {
        String name = super.getName();
        if (name.contains("../")) {
            throw new RuntimeException("invalid name contains ../");
        }
        return name;
    }
}
