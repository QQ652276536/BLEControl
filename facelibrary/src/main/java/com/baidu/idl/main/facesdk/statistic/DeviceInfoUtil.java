package com.baidu.idl.main.facesdk.statistic;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DeviceInfoUtil {


    /*-------------------- 获取系统信息---------------------- */

    /**
     * 获取当前手机安卓版本号
     *
     * @return 安卓系统版本号
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取当前手机安卓系统SDK
     *
     * @return 系统SDK
     */
    public static int getDeviceSDK() {
        return Build.VERSION.SDK_INT;
    }



    /*-------------------- 获取硬件信息---------------------- */

    /**
     * 获取手机硬件序列号
     *
     * @return 硬件序列号
     */
    public static String getDeviceSerial() {
        return Build.SERIAL;
    }

    /**
     * 获取手机品牌
     *
     * @return 手机品牌
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }


    /**
     * 获取产品名
     *
     * @return 产品名
     */
    public static String getDeviceProduct() {
        return Build.PRODUCT;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }


    /**
     * 获取手机主板名
     *
     * @return 手机主板名
     */
    public static String getDeviceBoard() {
        return Build.BOARD;
    }


    /**
     * 获取设备名
     *
     * @return 设备名
     */
    public static String getDeviceDevice() {
        return Build.DEVICE;
    }


    /**
     * 获取平台架构
     *
     * @return 平台架构
     */

//    public static String getPlatFormArchitecture() {
//        String abi = null;
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            abi = Build.CPU_ABI;
//        } else {
//            abi = Build.SUPPORTED_ABIS[0];
//        }
//        return abi;
//    }

    /**
     * 获取设备处理器
     *
     * @return 处理器
     */

    public static String getDeviceProcessor() {
        String processor = null;
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取设备CPU核数
     *
     * @return 核数
     */
    public static int getNumberOfCPUCores() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            // Gingerbread doesn't support giving a single application access to both cores, but a
            // handful of devices (Atrix 4G and Droid X2 for example) were released with a dual-core
            // chipset and Gingerbread; that can let an app in the background run without impacting
            // the foreground application. But for our purposes, it makes them single core.
            return 1;  // 上面的意思就是2.3以前不支持多核,有些特殊的设备有双核...不考虑,就当单核!!
        }
        int cores;
        try {
            cores = new File("/sys/devices/system/cpu/").listFiles(CPU_FILTER).length;
        } catch (SecurityException e) {
            cores = 0;   // 这个常量得自己约定
        } catch (NullPointerException e) {
            cores = 0;
        }
        return cores;
    }

    private static final FileFilter CPU_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            String path = pathname.getName();
            // regex is slow, so checking char by char.
            if (path.startsWith("cpu")) {
                for (int i = 3; i < path.length(); i++) {
                    if (path.charAt(i) < '0' || path.charAt(i) > '9') {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    };


    /**
     * 获取设备位数
     *
     * @return cpu位数
     */
    public static Integer getCPUBit() {
        Integer result = 0;
        String mProcessor = null;
        try {
            mProcessor = getFieldFromCpuinfo("Processor");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mProcessor != null) {
            if (mProcessor.contains("aarch64")) {
                result = 64;
            } else {
                result = 32;
            }
        }

        return result;
    }


    public static String getFieldFromCpuinfo(String field) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"));
        Pattern p = Pattern.compile(field + "\\s*:\\s*(.*)");

        try {
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    return m.group(1);
                }
            }
        } finally {
            br.close();
        }
        return null;
    }


    /**
     * 获取设备主频  KHZ
     *
     * @return 主频
     */


    private static final String CurPath = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";

    // 获取当前CPU频率
    public static int getDeviceBasicFrequency() {
        int result = 0;
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(CurPath);
            br = new BufferedReader(fr);
            String text = br.readLine();
            result = Integer.parseInt(text.trim());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }


    /**
     * 获取设备RAM   单位KB
     *
     * @return 获取RAM
     */
    public static long getRamInfo(Context context) {
        long totalSize = 0;
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        manager.getMemoryInfo(memoryInfo);
        totalSize = memoryInfo.totalMem;
        return totalSize;
    }

    /**
     * 获取设备存储器大小
     * type 用于区分内置/外置存储卡
     * 内置存储卡  INTERNAL_STORAGE = 0
     * 外置存储卡  EXTERNAL_STORAGE = 1
     *
     * @return 返回存储器大小
     */
    public static String getStorageInfo(Context context, int type) {
        String path = getStoragePath(context, type);
        String storageInfo;

        if (isSDCardMount() == false || path == null || path.toString().equals("")) {
            storageInfo = "无外置SD卡";
        } else {
            File file = new File(path);
            StatFs statFs = new StatFs(file.getPath());
            long blockCount = statFs.getBlockCountLong();
            long blockSize = statFs.getBlockSizeLong();
            long totalSpace = blockCount * blockSize;

            long aviableBlocks = statFs.getAvailableBlocksLong();
            long aviableSpace = aviableBlocks * blockSize;

            storageInfo = "可用/总共：" + Long.toString(aviableSpace) + "/" + Long.toString(totalSpace);
        }

        return storageInfo;
    }

    public static boolean isSDCardMount() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static String getStoragePath(Context context, int type) {
        StorageManager storageManager = (StorageManager) context.getSystemService(context.STORAGE_SERVICE);

        try {
            Method getPathMethod = storageManager.getClass().getMethod("getVolumePaths");
            String[] path = (String[]) getPathMethod.invoke(storageManager);
            switch (type) {
                case 0:
                    return path[type];
                case 1:
                    if (path.length > 1) {
                        return path[type];
                    } else {
                        return null;
                    }
                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


}
