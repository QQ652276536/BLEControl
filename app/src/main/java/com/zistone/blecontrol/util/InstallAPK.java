package com.zistone.blecontrol.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;

import com.zistone.blecontrol.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class InstallAPK {

    public static boolean copyApkFromAssets(Context context, String fileName, String path) {
        boolean copyIsFinish = false;
        try {
            InputStream is = context.getAssets().open(fileName);
            File file = new File(path);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();
            copyIsFinish = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return copyIsFinish;
    }

    public static String getAssetsCacheFile(Context context, String fileName) {
        File cacheFile = new File(context.getCacheDir(), fileName);
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            try {
                FileOutputStream outputStream = new FileOutputStream(cacheFile);
                try {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cacheFile.getAbsolutePath();
    }


    public static void Install(Activity activity) {
        if (copyApkFromAssets(activity, "ambiq_ota.apk", Environment.getExternalStorageDirectory().getAbsolutePath() + "/ambiq_ota.apk")) {

            AlertDialog.Builder m = new AlertDialog.Builder(activity).setIcon(R.drawable.ic_launcher).setMessage("是否安装？").setIcon(R.drawable.ic_launcher).setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    //                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //                    intent.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/ambiq_ota.apk"), "application/vnd.android.package-archive");
                    //                    activity.startActivity(intent);


                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    File apkFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ambiq_ota.apk");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", apkFile);
                        intent.setDataAndType(uri, "application/vnd.android.package-archive");
                    } else {
                        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                    }
                    activity.startActivity(intent);
                }

            });
            m.show();
        }
    }

}
