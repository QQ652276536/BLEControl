package com.zistone.libble.util;

import android.util.Log;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * OkHttP3请求的相关操作
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public final class MyOkHttpUtil implements Callback {
    private static final String TAG = "MyOkHttpUtil";
    private static final int MESSAGE_1 = 1;
    private static final int MESSAGE_ERROR_1 = -1;
    private static final int MESSAGE_ERROR_2 = -2;
    private static MyOkHttpUtil _myOkHttpUtil;
    private static MyOkHttpListener _myOkHttpListener;
    private static String _url = "";
    private static String _jsonData = "";

    public static String _mediaType = "application/json; charset=utf-8";
    public static long _readTimeout = 10;
    public static long _writeTimeout = 10;
    public static long _connectTimeout = 10;

    public interface MyOkHttpListener {
        void AsyOkHttpResult(int result, String content);
    }

    private MyOkHttpUtil() {
    }

    public static MyOkHttpUtil Init() {
        if (_myOkHttpUtil == null)
            _myOkHttpUtil = new MyOkHttpUtil();
        return _myOkHttpUtil;
    }

    /**
     * Post请求异步发送键值对数据
     *
     * @param url
     * @param map
     * @param listener
     */
    public static void AsySendMap(String url, Map<String, String> map, MyOkHttpListener listener) {
        _url = url;
        _myOkHttpListener = listener;
        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(_connectTimeout, TimeUnit.SECONDS).readTimeout(_readTimeout,
                TimeUnit.SECONDS).writeTimeout(_writeTimeout, TimeUnit.SECONDS).build();
        FormBody.Builder builder = new FormBody.Builder();
        for (String key : map.keySet()) {
            builder.add(key, map.get(key));
        }
        Request request = new Request.Builder().post(builder.build()).url(_url).build();
        Call call = okHttpClient.newCall(request);
        //异步请求
        call.enqueue(_myOkHttpUtil);
    }

    /**
     * Post请求异步发送Json数据
     *
     * @param url
     * @param data
     * @param listener
     */
    public static void AsySendBody(String url, String data, MyOkHttpListener listener) {
        _url = url;
        _jsonData = data;
        _myOkHttpListener = listener;
        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(_connectTimeout, TimeUnit.SECONDS).readTimeout(_readTimeout,
                TimeUnit.SECONDS).writeTimeout(_writeTimeout, TimeUnit.SECONDS).build();
        RequestBody requestBody = FormBody.create(_jsonData, MediaType.parse(_mediaType));
        Request request = new Request.Builder().post(requestBody).url(_url).build();
        Call call = okHttpClient.newCall(request);
        //异步请求
        call.enqueue(_myOkHttpUtil);
    }

    /**
     * 网络请求失败
     *
     * @param call
     * @param e
     */
    @Override
    public void onFailure(Call call, IOException e) {
        String content = e.toString();
        Log.e(TAG, "网络连接失败:" + content);
        _myOkHttpListener.AsyOkHttpResult(MESSAGE_ERROR_2, content);
    }

    /**
     * 响应的结果
     * <p>
     * 获取字符串:response.body().string(),这个方法只能被调用一次!
     * 获取字节数组:response.body().bytes()
     * 获取字节流:response.body().byteStream()
     *
     * @param call
     * @param response
     * @throws IOException
     */
    @Override
    public void onResponse(Call call, Response response) throws IOException {
        String result = response.body().string();
        if (response.isSuccessful()) {
            Log.i(TAG, "响应成功:" + result);
            _myOkHttpListener.AsyOkHttpResult(MESSAGE_1, result);
        } else {
            Log.e(TAG, "响应失败:" + result);
            _myOkHttpListener.AsyOkHttpResult(MESSAGE_ERROR_1, result);
        }
    }
}
