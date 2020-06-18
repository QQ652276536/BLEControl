package com.baidu.idl.main.facesdk.utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by v_liujialu01 on 2019/3/22.
 */

public class ZipUtils {
    public static boolean isSuccess;

    private ZipUtils() {
        // empty
    }

    /**
     * 压缩文件
     *
     * @param filePath 待压缩的文件路径
     * @return 压缩后的文件
     */
    public static File zip(String filePath) {
        File target = null;
        File source = new File(filePath);
        if (source.exists()) {
            // 压缩文件名=源文件名.zip
            String zipName = source.getName() + ".zip";
            target = new File(source.getParent(), zipName);
            if (target.exists()) {
                target.delete(); // 删除旧的文件
            }
            FileOutputStream fos = null;
            ZipOutputStream zos = null;
            try {
                fos = new FileOutputStream(target);
                zos = new ZipOutputStream(new BufferedOutputStream(fos));
                // 添加对应的文件Entry
                addEntry("/", source, zos);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtil.closeQuietly(zos, fos);
            }
        }
        return target;
    }

    /**
     * 扫描添加文件Entry
     *
     * @param base   基路径
     * @param source 源文件
     * @param zos    Zip文件输出流
     * @throws IOException
     */
    private static void addEntry(String base, File source, ZipOutputStream zos)
            throws IOException {
        // 按目录分级，形如：/aaa/bbb.txt
        String entry = base + source.getName();
        if (source.isDirectory()) {
            for (File file : source.listFiles()) {
                // 递归列出目录下的所有文件，添加文件Entry
                addEntry(entry + "/", file, zos);
            }
        } else {
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                byte[] buffer = new byte[1024 * 10];
                fis = new FileInputStream(source);
                bis = new BufferedInputStream(fis, buffer.length);
                int read = 0;
                zos.putNextEntry(new ZipEntry(entry));
                while ((read = bis.read(buffer, 0, buffer.length)) != -1) {
                    zos.write(buffer, 0, read);
                }
                zos.closeEntry();
            } finally {
                IOUtil.closeQuietly(bis, fis);
            }
        }
    }

    /**
     * 解压文件
     *
     * @param filePath 压缩文件路径
     */
//    public static boolean unzip(String filePath) {
//        File source = new File(filePath);
//        boolean flag = false;
//        if (source.exists()) {
//            ZipInputStream zis = null;
//            BufferedOutputStream bos = null;
//            try {
//                zis = new ZipInputStream(new FileInputStream(source));
//                ZipEntry entry = null;
//                while ((entry = zis.getNextEntry()) != null
//                        && !entry.isDirectory()) {
//                    File target = new File(source.getParent(), entry.getName());
//                    if (!target.getParentFile().exists()) {
//                        // 创建文件父目录
//                        target.getParentFile().mkdirs();
//                    }
//                    // 写入文件
//                    bos = new BufferedOutputStream(new FileOutputStream(target));
//                    int read = 0;
//                    byte[] buffer = new byte[1024 * 10];
//                    while ((read = zis.read(buffer, 0, buffer.length)) != -1) {
//                        bos.write(buffer, 0, read);
//                    }
//                    bos.flush();
//                }
//                zis.closeEntry();
//                flag = true;
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            } finally {
//                IOUtil.closeQuietly(zis, bos);
//            }
//        }
//        return flag;
//    }

    /**
     * 解压文件
     *
     * @param filePath 压缩文件路径
     */
    public static boolean unzip(String filePath) {
        boolean flag = false;
        File srcFile = new File(filePath);
        SafeZipEntry zipEntry = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(srcFile);
            SafeZipInputStream zipInputStream = new SafeZipInputStream(
                    new BufferedInputStream(fileInputStream));
            while ((zipEntry = (SafeZipEntry) zipInputStream.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {
                    continue;
                } else {
                    String entryName = zipEntry.getName();
                    File target = new File(srcFile.getParent(), entryName);
                    if (!target.getParentFile().exists()) {
                        // 创建文件父目录
                        target.getParentFile().mkdirs();
                    }
                    // 写入文件
                    FileOutputStream fileOutputStream = new FileOutputStream(target);
                    BufferedOutputStream bos = new BufferedOutputStream(
                            new BufferedOutputStream(fileOutputStream));
                    int read = 0;
                    byte[] buffer = new byte[1024 * 10];
                    while ((read = zipInputStream.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, read);
                    }
                    bos.flush();
                }
            }
            zipInputStream.close();
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 解压zip到指定的路径
     *
     * @param zipFileString zip的名称
     * @param outPathString 要解压缩路径
     * @throws Exception
     */
    public static boolean unZipFolder(String zipFileString, String outPathString) {
        boolean flag = false;
        try {
            ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
            ZipEntry zipEntry;
            String szName = "";

            while ((zipEntry = inZip.getNextEntry()) != null) {
                szName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    //获取部件的文件夹名
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(outPathString + File.separator + szName);
                    folder.mkdirs();
                } else {
                    Log.e("ZipUtils", outPathString + File.separator + szName);
                    File file = new File(outPathString + File.separator + szName);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    // 获取文件的输出流
                    FileOutputStream out = new FileOutputStream(file);
                    int len;
                    byte[] buffer = new byte[1024];
                    // 读取（字节）字节到缓冲区
                    while ((len = inZip.read(buffer)) != -1) {
                        // 从缓冲区（0）位置写入（字节）字节
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                    out.close();
                }
            }
            inZip.close();
            flag = true;
        } catch (Exception e) {
            Log.e("ZipUtils", "e = " + e.getMessage());
            e.printStackTrace();
        }
        return flag;
    }
}
