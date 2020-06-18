/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.idl.main.facesdk.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FileUitls {
    /**
     * Checks if is sd card available.
     *
     * @return true, if is sd card available
     */
    public static boolean isSdCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Gets the SD root file.
     *
     * @return the SD root file
     */
    public static File getSDRootFile() {
        if (isSdCardAvailable()) {
            return Environment.getExternalStorageDirectory();
        } else {
            return null;
        }
    }

    public static File getFaceDirectory() {
        File sdRootFile = getSDRootFile();
        File file = null;
        if (sdRootFile != null && sdRootFile.exists()) {
            file = new File(sdRootFile, "faces");
            if (!file.exists()) {
                boolean success = file.mkdirs();
            }
        }

        return file;
    }

    public static File getBatchFaceDirectory(String batchDir) {
        File sdRootFile = getSDRootFile();
        File file = null;
        if (sdRootFile != null && sdRootFile.exists()) {
            file = new File(sdRootFile, batchDir);
            if (!file.exists()) {
                boolean success = file.mkdirs();
            }
        }

        return file;
    }

    public static boolean saveFile(File file, Bitmap bitmap) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public static void deleteLicense(Context context, String licenseName) {
        String filePath = context.getFilesDir().getParent() + "/" + licenseName;
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }

        File var4 = context.getDir(licenseName, 0);
        if (var4 != null && var4.exists()) {
            var4.delete();
        }
    }


    // 判断文件是否存在
    public static boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                Log.i("wtf", "file_state->" + "file not exits");
                return false;
            }
        } catch (Exception e) {
            Log.i("wtf", "file_state->" + e.getMessage());
            return false;
        }
        return true;
    }

    // 读取文本文件中的内容
    public static String readFile(String strFilePath) {
        String path = strFilePath;
        String content = ""; // 文件内容字符串
        // 打开文件
        File file = new File(path);
        // 如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Log.d("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    // 分行读取
                    while ((line = buffreader.readLine()) != null) {
                        content = line;
                    }
                    instream.close();
                }
            } catch (java.io.FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                Log.d("TestFile", e.getMessage());
            }
        }
        return content;
    }


    // 读取文本文件中的内容
    public static ArrayList<String> readLicense(String strFilePath) {
        ArrayList<String> licenseList = new ArrayList<>();
        String path = strFilePath;
        String content = ""; // 文件内容字符串
        // 打开文件
        File file = new File(path);
        // 如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Log.d("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    // 分行读取
                    while ((line = buffreader.readLine()) != null) {
                        content = line;
                        licenseList.add(content);
                    }
                    instream.close();
                }
            } catch (java.io.FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                Log.d("TestFile", e.getMessage());
            }
        }
        return licenseList;
    }

    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory(); // 获取跟目录
        }
        if (sdDir != null) {
            return sdDir.toString();
        }
        return null;
    }

    public static boolean assetOpen(Context context, String modelPath) {
        boolean exist = false;
        if (context == null || TextUtils.isEmpty(modelPath)) {
            return false;
        } else {
            try {
                context.getAssets().open(modelPath);
                exist = true;
            } catch (Exception e) {
            }
        }
        return exist;
    }

    /**
     * 模型加载路径包含SD卡和asset
     * @param context 上下文
     * @param modelName 模型路径
     * @return 模型内容字节数组
     */
    public static byte[] getModelContent(Context context, String modelName) {

        //
        InputStream is = null;
        byte[] bytes = new byte[0];

        // 先从SD卡中读取
        File file = new File(modelName);
        if (file.exists()) {
            try {
                is = new FileInputStream(file);
                bytes = new byte[is.available()];
                is.read(bytes);
            } catch (FileNotFoundException e) {
                // e.printStackTrace();
            } catch (IOException e) {
                // e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
        }

        // 如果SD 卡中模型不为空，返回读取字节
        if (bytes.length > 0) {
            return bytes;
        }

        // 从asset 目录下读取
        try {
            is = context.getResources().getAssets().open(modelName);
            bytes = new byte[is.available()];
            is.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // e.printStackTrace();
            }
        }
        return bytes;
    }

    /**
     * @param context   上下文
     * @param imageName 图片名称
     * @return bitmap对象
     */
    public static Bitmap getBitmap(Context context, String imageName) {
        InputStream is = null;
        try {
            AssetManager assetManager = context.getResources().getAssets();
            is = assetManager.open(imageName);
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static byte[] getByteArrayFromAssets(Context context, String imageName) {
        byte[] bytes = null;
        try {
            InputStream is = context.getAssets().open(imageName);
            bytes = new byte[is.available()];
            is.read(bytes);
            is.close();
        } catch (IOException e) {
            Log.e("zq", "e-->" + e.getMessage());
            e.printStackTrace();
        }
        return bytes;
    }
}
