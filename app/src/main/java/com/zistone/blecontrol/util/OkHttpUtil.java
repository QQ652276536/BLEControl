package com.zistone.blecontrol.util;

import android.nfc.Tag;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtil implements Callback
{
    private static final String TAG = "OkHttpUtil";
    private static final int MESSAGE_1 = 1;
    private static final int MESSAGE_ERROR_1 = -1;
    private static final int MESSAGE_ERROR_2 = -2;

    private static OkHttpListener _okHttpListener;

    public static String _url = "";
    public static String _jsonData = "";
    public static String _mediaType = "application/json; charset=utf-8";
    public static long _readTimeout = 10;
    public static long _writeTimeout = 10;
    public static long _connectTimeout = 10;

    public interface OkHttpListener
    {
        void AsyOkHttpResult(int result, String content);
    }

    public void Init(OkHttpListener okHttpListener) throws Exception
    {
        if (_url.equals("") && _jsonData.equals("") && _mediaType.equals("") && _connectTimeout <= 0 && _readTimeout <= 0 && _writeTimeout <= 0)
        {
            throw new Exception("实例化OkHttp失败,参数异常!");
        }
        _okHttpListener = okHttpListener;
        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(_connectTimeout, TimeUnit.SECONDS).readTimeout(_readTimeout, TimeUnit.SECONDS).writeTimeout(_writeTimeout, TimeUnit.SECONDS).build();
        RequestBody requestBody = FormBody.create(_jsonData, MediaType.parse(_mediaType));
        Request request = new Request.Builder().post(requestBody).url(_url).build();
        Call call = okHttpClient.newCall(request);
        //异步请求
        call.enqueue(this);
    }

    /**
     * 网络请求失败
     *
     * @param call
     * @param e
     */
    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e)
    {
        String content = e.toString();
        Log.e(TAG, "网络请求失败:" + content);
        _okHttpListener.AsyOkHttpResult(MESSAGE_ERROR_1, content);
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
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
    {
        String result = response.body().string();
        if (response.isSuccessful())
        {
            Log.i(TAG, "响应成功:" + result);
            _okHttpListener.AsyOkHttpResult(MESSAGE_1, result);
        }
        else
        {
            Log.e(TAG, "响应失败:" + result);
            _okHttpListener.AsyOkHttpResult(MESSAGE_ERROR_2, result);
        }
    }
}
