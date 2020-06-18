package com.baidu.idl.main.facesdk;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.statistic.PostDeviceInfo;
import com.baidu.idl.main.facesdk.utils.FileUitls;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.baidu.idl.main.facesdk.utils.ZipUtils;
import com.baidu.vis.unified.license.AndroidLicenser;
import com.baidu.vis.unified.license.BDLicenseLocalInfo;
import com.baidu.vis.unified.license.HttpStatus;
import com.baidu.vis.unified.license.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FaceAuth {
    private static final String TAG = "FaceSDK";
    private static final String LICENSE_FILE_NAME = "idl-license.face-android";
    private static int ALGORITHM_ID = 3;

    static {
        try {
            System.loadLibrary("bdface_sdk");
            System.loadLibrary("bd_unifylicense");
            System.loadLibrary("aikl_calc_arm");
            System.loadLibrary("aikl_cluster_arm");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setActiveLog(BDFaceSDKCommon.BDFaceLogInfo logInfo) {
        nativeSetActiveLog(logInfo.ordinal());
    }

    public void setCoreConfigure(BDFaceSDKCommon.BDFaceCoreRunMode runMode, int coreNum) {
        nativeSetCoreConfigure(runMode.ordinal(), coreNum);
    }

    public String getDeviceId(Context context) {
        return AndroidLicenser.getDeviceId(context.getApplicationContext());
    }

    /**
     * 初始化鉴权,鉴权方式:通过本地文件鉴权
     *
     * @param context
     * @param licenseID
     * @param licenseFileName
     * @param isRemote
     * @param callback
     */
    public void initLicense(final Context context, final String licenseID, final String licenseFileName,
                            final boolean isRemote, final Callback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (context == null) {
                    callback.onResponse(1, "没有初始化上下文");
                    return;
                }

                // 打点统计
                PreferencesUtil.initPrefs(context);
                String statics = PreferencesUtil.getString("statics", "");
                if (TextUtils.isEmpty(statics)) {
                    PostDeviceInfo.uploadDeviceInfo(context, new Callback() {
                        @Override
                        public void onResponse(int code, String response) {
                            if (code == 0) {
                                PreferencesUtil.putString("statics", "ok");
                            }
                        }
                    });
                }

                if (TextUtils.isEmpty(licenseID) || TextUtils.isEmpty(licenseFileName)) {
                    callback.onResponse(2, "license 关键字为空");
                    return;
                }

                AndroidLicenser licenser = AndroidLicenser.getInstance();
                AndroidLicenser.ErrorCode errorCode = licenser.authFromFile(context, licenseID,
                        licenseFileName, isRemote, ALGORITHM_ID);
                if (errorCode != AndroidLicenser.ErrorCode.SUCCESS) {
                    BDLicenseLocalInfo info = licenser.authGetLocalInfo(context, ALGORITHM_ID);
                    if (info != null) {
                        Log.i(TAG, info.toString());
                    }
                } else {
                    int status = nativeCreatInstance();
                    Log.v(TAG, "bdface_create_instance status " + status);
                }
                String errMsg = licenser.getErrorMsg(ALGORITHM_ID);
                callback.onResponse(errorCode.ordinal(), errMsg);
            }
        };
        FaceQueue.getInstance().execute(runnable);
    }

    /**
     * 初始化鉴权,鉴权方式:通过AIPE序列码在线激活鉴权
     *
     * @param context
     * @param licenseID
     * @param callback
     */
    public void initLicenseOnLine(final Context context, final String licenseID, final Callback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (context == null) {
                    callback.onResponse(1, "没有初始化上下文");
                    return;
                }

                // 打点统计
                PreferencesUtil.initPrefs(context);
                String statics = PreferencesUtil.getString("statics", "");
                if (TextUtils.isEmpty(statics)) {
                    PostDeviceInfo.uploadDeviceInfo(context, new Callback() {
                        @Override
                        public void onResponse(int code, String response) {
                            if (code == 0) {
                                PreferencesUtil.putString("statics", "ok");
                            }
                        }
                    });
                }

                if (TextUtils.isEmpty(licenseID) || TextUtils.isEmpty(LICENSE_FILE_NAME)) {
                    callback.onResponse(2, "license 关键字为空");
                    return;
                }

                AndroidLicenser licenser = AndroidLicenser.getInstance();
                AndroidLicenser.ErrorCode errorCode = licenser.authFromFile(context, licenseID,
                        LICENSE_FILE_NAME, false, ALGORITHM_ID);
                Log.e(TAG, "errCode = " + errorCode);
                if (errorCode == AndroidLicenser.ErrorCode.SUCCESS) {
                    int status = nativeCreatInstance();
                    Log.v(TAG, "bdface_create_instance status " + status);

                    String errMsg = licenser.getErrorMsg(ALGORITHM_ID);
                    callback.onResponse(errorCode.ordinal(), errMsg);
                    return;
                }

                String device = AndroidLicenser.getDeviceId(context.getApplicationContext());
                String url = "https://ai.baidu.com/activation/key/activate";
                String paramStr = null;
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("deviceId", device);
                    jsonObject.put("key", licenseID);
                    jsonObject.put("platformType", 2);
                    jsonObject.put("version", BuildConfig.VERSION_CODE);
                    paramStr = jsonObject.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                HttpStatus httpStatus = HttpUtils.requestPost(url, paramStr, "application/json", TAG);
                if (httpStatus == null) {
                    callback.onResponse(-1, "在线激活失败");
                    return;
                }

                String response = httpStatus.responseStr;
                try {
                    JSONObject json = new JSONObject(response);
                    int jsonErrorCode = json.optInt("error_code");
                    if (jsonErrorCode != 0) {
                        String errorMsg = json.optString("error_msg");
                        Log.i(TAG, "error_msg->" + errorMsg);
                        callback.onResponse(-1, errorMsg);
                    } else {
                        JSONObject result = json.optJSONObject("result");
                        if (result != null) {
                            String license = result.optString("license");
                            if (!TextUtils.isEmpty(license)) {
                                String[] licenses = license.split(",");
                                if (licenses != null && licenses.length == 2) {
                                    PreferencesUtil.putString("activate_online_key", licenseID);
                                    errorCode = licenser.authFromMemory(context, licenseID, licenses
                                            , LICENSE_FILE_NAME, ALGORITHM_ID);
                                    if (errorCode != AndroidLicenser.ErrorCode.SUCCESS) {
                                        BDLicenseLocalInfo info = licenser.authGetLocalInfo(context, ALGORITHM_ID);
                                        if (info != null) {
                                            Log.i(TAG, info.toString());
                                        }
                                    } else {
                                        int status = nativeCreatInstance();
                                        Log.v(TAG, "bdface_create_instance status " + status);
                                    }
                                    String errMsg = licenser.getErrorMsg(ALGORITHM_ID);
                                    callback.onResponse(errorCode.ordinal(), errMsg);
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "netRequest->" + response);
            }
        };
        FaceQueue.getInstance().execute(runnable);
    }

    public void initLicenseOffLine(final Context context, final Callback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (context == null) {
                    callback.onResponse(1, "没有初始化上下文");
                    return;
                }

                // 打点统计
                PreferencesUtil.initPrefs(context);
                String statics = PreferencesUtil.getString("statics", "");
                if (TextUtils.isEmpty(statics)) {
                    PostDeviceInfo.uploadDeviceInfo(context, new Callback() {
                        @Override
                        public void onResponse(int code, String response) {
                            if (code == 0) {
                                PreferencesUtil.putString("statics", "ok");
                            }
                        }
                    });
                }

                String path = FileUitls.getSDPath();
                String sdCardDir = path + "/" + "License.zip";
                if (FileUitls.fileIsExists(sdCardDir)) {
                    boolean isZipSuccess = ZipUtils.unzip(sdCardDir);
                    if (isZipSuccess) {
                        String keyPath = path + "/" + "license.key";
                        String offLicenseKey = FileUitls.readFile(keyPath);
                        PreferencesUtil.putString("activate_offline_key", offLicenseKey);
                        AndroidLicenser licenser = AndroidLicenser.getInstance();
                        AndroidLicenser.ErrorCode errorCode = licenser.authFromFile(context, offLicenseKey,
                                LICENSE_FILE_NAME, false, ALGORITHM_ID);

                        if (errorCode == AndroidLicenser.ErrorCode.SUCCESS) {

                            int status = nativeCreatInstance();
                            Log.v(TAG, "bdface_create_instance status " + status);

                            String errMsg = licenser.getErrorMsg(ALGORITHM_ID);
                            callback.onResponse(errorCode.ordinal(), errMsg);
                            return;
                        }

                        String licensePath = path + "/" + "license.ini";
                        ArrayList<String> licenseList = FileUitls.readLicense(licensePath);
                        String[] licenses = licenseList.toArray(new String[licenseList.size()]);
                        if (licenses != null && licenses.length == 2) {
                            errorCode = licenser.authFromMemory(context, offLicenseKey, licenses
                                    , LICENSE_FILE_NAME, ALGORITHM_ID);
                            if (errorCode != AndroidLicenser.ErrorCode.SUCCESS) {
                                BDLicenseLocalInfo info = licenser.authGetLocalInfo(context, ALGORITHM_ID);
                                if (info != null) {
                                    Log.i(TAG, info.toString());
                                }
                            } else {
                                int status = nativeCreatInstance();
                                Log.v(TAG, "bdface_create_instance status " + status);
                            }
                            String errMsg = licenser.getErrorMsg(ALGORITHM_ID);
                            callback.onResponse(errorCode.ordinal(), errMsg);
                        }
                    } else {
                        callback.onResponse(-1, "license 文件解压失败");
                        Log.i(TAG, "file_state->" + "license zip failed");
                    }
                } else {
                    callback.onResponse(-1, "license 文件不存在!");
                    Log.i(TAG, "file_state->" + "file not found");
                }
            }
        };
        FaceQueue.getInstance().execute(runnable);
    }

    /**
     * 初始化鉴权：鉴权方式：在线批量激活鉴权
     *
     * @param context
     * @param licenseKey
     * @param callback
     */
    public void initLicenseBatchLine(final Context context, final String licenseKey, final Callback callback) {
        Runnable runnable = new Runnable() {
            public void run() {
                if (context == null) {
                    callback.onResponse(1, "没有初始化上下文");
                } else {
                    PreferencesUtil.initPrefs(context);
                    String statics = PreferencesUtil.getString("statics", "");
                    if (TextUtils.isEmpty(statics)) {
                        PostDeviceInfo.uploadDeviceInfo(context, new Callback() {
                            public void onResponse(int code, String response) {
                                if (code == 0) {
                                    PreferencesUtil.putString("statics", "ok");
                                }

                            }
                        });
                    }

                    if (!TextUtils.isEmpty(licenseKey) && !TextUtils.isEmpty(LICENSE_FILE_NAME)) {
                        AndroidLicenser licenser = AndroidLicenser.getInstance();
                        AndroidLicenser.ErrorCode errorCode = licenser.authFromFile(context,
                                licenseKey, LICENSE_FILE_NAME, true, ALGORITHM_ID);
                        if (errorCode != AndroidLicenser.ErrorCode.SUCCESS) {
                            BDLicenseLocalInfo info = licenser.authGetLocalInfo(context, ALGORITHM_ID);
                            if (info != null) {
                                Log.i(TAG, info.toString());
                            }
                        } else {
                            int status = nativeCreatInstance();
                            Log.v(TAG, "bdface_create_instance status " + status);
                        }
                        String errMsg = licenser.getErrorMsg(ALGORITHM_ID);
                        callback.onResponse(errorCode.ordinal(), errMsg);

                    } else {
                        callback.onResponse(2, "license 关键字为空");
                    }
                }
            }
        };
        FaceQueue.getInstance().execute(runnable);
    }

    private native int nativeCreatInstance();

    private native void nativeSetActiveLog(int isLog);

    private native void nativeSetCoreConfigure(int runMode, int coreNum);
}
