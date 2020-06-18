package com.baidu.idl.main.facesdk.statistic;

import android.content.Context;
import android.util.Log;

import com.baidu.idl.main.facesdk.BuildConfig;
import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.vis.unified.license.AndroidLicenser;
import com.baidu.vis.unified.license.HttpStatus;
import com.baidu.vis.unified.license.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;


public class PostDeviceInfo {


    public static void uploadDeviceInfo(final Context context, final Callback callback) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                // 硬件指纹
                String fingerPrint = AndroidLicenser.getDeviceId(context.getApplicationContext());

                // 获取当前手机的系统版本
                String versionStr = DeviceInfoUtil.getSystemVersion();

                // 获取当前手机的RAM
                Long ramLong = DeviceInfoUtil.getRamInfo(context);
                float ramFloat = ramLong / 1024 / 1024;

                // 获取手机当前的频率
                int frequenceInt = DeviceInfoUtil.getDeviceBasicFrequency();
                float frequenceFloat = frequenceInt / 1000;

                // 获取当前手机的CPU 核数
                int cpuCore = DeviceInfoUtil.getNumberOfCPUCores();
                // 获取当前手机的CPU 位数
                int cpuBit = DeviceInfoUtil.getCPUBit();

                // 获取当前手机的网络状态
                Integer network = NetWorkUtil.getNetworkState(context);

                // 5、获取当前FaceSDK的版本号
                String facesdkVersion = BuildConfig.VERSION_NAME;

                String paraStr = null;

                try {

                    JSONObject itemObject = new JSONObject();
                    itemObject.put("analysisType", "offline_Sdk");
                    itemObject.put("deviceId", fingerPrint);
                    itemObject.put("cpuCore", cpuCore); // CPU 核数
                    itemObject.put("cpuBit", cpuBit); // CPU 位数
                    itemObject.put("ghz", Math.round(frequenceFloat)); // 主频
                    itemObject.put("ram", Math.round(ramFloat)); // 内存
                    itemObject.put("networkType", network);

                    String networkDesp = null;
                    if (network == 1) {
                        networkDesp = "WIFI网络";
                    } else if (network == 2) {
                        networkDesp = "2G网络";
                    } else if (network == 3) {
                        networkDesp = "3G网络";
                    } else if (network == 4) {
                        networkDesp = "4G网络";
                    } else if (network == 5) {
                        networkDesp = "有线网卡";
                    } else {
                        networkDesp = "其他网络模块";
                    }
                    itemObject.put("networkDesc", networkDesp);
                    itemObject.put("os", 1);
                    itemObject.put("osVersion", versionStr);
                    itemObject.put("sdk", 1);
                    itemObject.put("sdkVersion", facesdkVersion);

                    JSONObject object = new JSONObject();
                    object.put("mh", "offlineSdkStatistic");
                    object.put("dt", itemObject);
                    paraStr = object.toString();
                    Log.i("bdface", "参数是：" + paraStr);

                } catch (JSONException e) {
                    e.getStackTrace();
                }
                // 联调地址
                // String urlStr = "http://bjyz-ai.epc.baidu.com:8019/record/api";
                // 线上API
                String urlStr = "http://brain.baidu.com/record/api";
                HttpStatus httpStatus = HttpUtils.requestPost(urlStr, paraStr, "application/json", "zxq");
                if (httpStatus == null) {
                    callback.onResponse(-1, "请求失败");
                    return;
                }
                String response = httpStatus.responseStr;
                // 解析response
                Log.i("bdface", "response结果：" + response);

                int responseCode = 1;

                try {
                    JSONObject jsonobject = new JSONObject(response);
                    responseCode = jsonobject.optInt("code");
                    String msg = jsonobject.optString("msg");
                    callback.onResponse(responseCode, msg);
                } catch (JSONException e) {
                    e.getStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }


}
