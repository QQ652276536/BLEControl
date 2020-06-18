package com.baidu.idl.main.facesdk.statistic;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetWorkUtil {

    /**
     * 获取当前的网络状态 ：其他网络模块-0：WIFI网络1  2G网络-2  3G网络-3  4G网络-4： 有线连接：5
     * 自定义
     *
     * @param context
     * @return
     */
    public static Integer getNetworkState(Context context) {


        if (context == null) {
            return 0;
        } else {
            // 结果返回值
            int netType = 0;
            // 获取手机所有连接管理对象
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            // 获取NetworkInfo对象
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            // NetworkInfo对象为空 则代表没有网络
            if (networkInfo == null || !networkInfo.isConnected()) {
                return 0;
            }
            // 否则 NetworkInfo对象不为空 则获取该networkInfo的类型
            int nType = networkInfo.getType();
            if (nType == ConnectivityManager.TYPE_WIFI) {
                // WIFI
                netType = 1;
            } else if (nType == ConnectivityManager.TYPE_MOBILE) {
                int nSubType = networkInfo.getSubtype();
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(
                        Context.TELEPHONY_SERVICE);
                if (nSubType == TelephonyManager.NETWORK_TYPE_LTE
                        && !telephonyManager.isNetworkRoaming()) {
                    netType = 4;
                    // 3G   联通的3G为UMTS或HSDPA 电信的3G为EVDO
                } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
                        || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0
                        || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_A
                        || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA
                        || nSubType == TelephonyManager.NETWORK_TYPE_HSUPA
                        || nSubType == TelephonyManager.NETWORK_TYPE_HSPA
                        || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_B
                        || nSubType == TelephonyManager.NETWORK_TYPE_EHRPD
                        || nSubType == TelephonyManager.NETWORK_TYPE_HSPAP
                        && !telephonyManager.isNetworkRoaming()) {
                    netType = 3;
                    // 2G 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA
                } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS
                        || nSubType == TelephonyManager.NETWORK_TYPE_EDGE
                        || nSubType == TelephonyManager.NETWORK_TYPE_CDMA
                        || nSubType == TelephonyManager.NETWORK_TYPE_1xRTT
                        || nSubType == TelephonyManager.NETWORK_TYPE_IDEN
                        && !telephonyManager.isNetworkRoaming()) {
                    netType = 2;
                } else {
                    netType = 0;
                }
            } else if (nType == ConnectivityManager.TYPE_ETHERNET) {
                // 有线网络
                netType = 5;
            }
            return netType;
        }
    }


}
