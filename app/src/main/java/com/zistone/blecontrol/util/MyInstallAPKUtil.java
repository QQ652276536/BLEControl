package com.zistone.blecontrol.util;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public final class MyInstallAPKUtil {
    private static final String TAG = "MyInstallAPKUtil";

    private MyInstallAPKUtil() {
    }

    /**
     * 根据包名启动第三方APK，如果已经启动APK，则直接将APK从后台调到前台运行（类似Home键之后再点击APK图标启动），如果未启动APK，则重新启动
     *
     * @param context
     * @param packageName
     * @return
     */
    public static Intent GetAppOpenIntentByPackageName(Context context, String packageName) {
        String mainAct = null;
        PackageManager pkgMag = context.getPackageManager();
        //ACTION_MAIN是隐藏启动的action， 你也可以自定义
        Intent intent = new Intent(Intent.ACTION_MAIN);
        //CATEGORY_LAUNCHER有了这个，你的程序就会出现在桌面上
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        //FLAG_ACTIVITY_RESET_TASK_IF_NEEDED 按需启动的关键，如果任务队列中已经存在，则重建程序
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);
        @SuppressLint("WrongConstant") List<ResolveInfo> list = pkgMag.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo.packageName.equals(packageName)) {
                mainAct = info.activityInfo.name;
                break;
            }
        }
        if (TextUtils.isEmpty(mainAct))
            return null;
        intent.setComponent(new ComponentName(packageName, mainAct));
        return intent;
    }

    /**
     * 直接从assets里安装APK
     *
     * @param context
     * @param apk
     * @param content
     */
    public static void InstallFromAssets(Context context, String apk, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提示");
        builder.setMessage(content);
        builder.setNegativeButton("好的", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            //拷贝
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + apk);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //版本在7.0以上安装
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            }
            context.startActivity(intent);
        });
        builder.setPositiveButton("不了", (dialog, which) -> {
        });
        builder.show();
    }

    /**
     * 检查是否安装
     *
     * @param context
     * @param pkgName
     * @return
     */
    public static boolean CheckInstalled(Context context, String pkgName) {
        if (pkgName == null || pkgName.isEmpty()) {
            return false;
        }
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> info = packageManager.getInstalledPackages(0);
        if (info == null || info.isEmpty())
            return false;
        for (int i = 0; i < info.size(); i++) {
            if (pkgName.equals(info.get(i).packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从assets目录中拷贝APK到指定目录再安装
     *
     * @param context
     * @param fileName
     * @param target
     */
    public static void InstallFromCopyAssets(Context context, String fileName, String target) {
        try {
            File file = CopyFromAssets(context, fileName, target);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            } else {
                uri = Uri.fromFile(file);
            }
            Install(uri, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 安装APK
     *
     * @param uri
     * @param context
     */
    public static void Install(Uri uri, Context context) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= 24) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 从assets目录中拷贝文件到指定目录
     *
     * @param context
     * @param fileName 文件名，包含后缀
     * @param target   文件所在的目录，不包含文件及后缀
     * @return 拷贝后的文件对象
     */
    public static File CopyFromAssets(Context context, String fileName, String target) throws Exception {
        File result = null;
        InputStream inputStream = context.getAssets().open(fileName);
        //仅创建路径的File对象
        File targetFile = new File(Environment.getExternalStorageDirectory().toString() + File.separator + target);
        //如果路径不存在就先创建路径
        if (!targetFile.exists()) {
            targetFile.mkdir();
        }
        //然后再创建路径和文件的File对象
        result = new File(targetFile, fileName);
        String absolutePath = result.getAbsolutePath();
        FileOutputStream fileOutputStream = new FileOutputStream(new File(absolutePath));
        byte[] buffer = new byte[1024];
        int byteCount;
        while ((byteCount = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, byteCount);
        }
        fileOutputStream.flush();
        inputStream.close();
        fileOutputStream.close();
        return result;
    }

}
