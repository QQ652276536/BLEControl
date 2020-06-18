package com.baidu.idl.main.facesdk.utils;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by v_shishuaifeng on 2020/1/13.
 *
 */

public class SafeZipInputStream extends ZipInputStream {
    /**
     * Constructs a new {@code ZipInputStream} to read zip entries from the given input stream.
     * <p>
     * <p>UTF-8 is used to decode all strings in the file.
     *
     * @param stream
     */
    public SafeZipInputStream(InputStream stream) {
        super(stream);
    }

    protected ZipEntry createZipEntry(String name) {
        return new SafeZipEntry(name);
    }
}
