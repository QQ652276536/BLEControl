package com.zistone.blecontrol.baidutts;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

/**
 * 在新线程中初始化合成引擎,防止UI阻塞
 */
public class NonBlockSyntherizer extends MySyntherizer {
    private static final String TAG = "NonBlockSyntherizer";
    private static final int MESSAGE1 = 1;
    private static final int MESSAGE2 = 2;
    private HandlerThread _handlerThread;
    private Handler _handler;

    public NonBlockSyntherizer(Context context, InitConfig initConfig) {
        super(context);
        InitThread();
        RunInRHandlerThread(MESSAGE1, initConfig);
    }

    @Override
    public void Release() {
        RunInHandlerThread(MESSAGE2);
        if (Build.VERSION.SDK_INT >= 18) {
            _handlerThread.quitSafely();
        }
    }

    private void InitThread() {
        _handlerThread = new HandlerThread("NonBlockSyntherizer-Thread");
        _handlerThread.start();
        _handler = new Handler(_handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MESSAGE1:
                        InitConfig config = (InitConfig) msg.obj;
                        boolean isSuccess = Init(config);
                        if (isSuccess) {
                            Log.i(TAG, "NonBlockSyntherizer初始化成功");
                        } else {
                            Log.d(TAG, "NonBlockSyntherizer初始化失败");
                        }
                        break;
                    case MESSAGE2:
                        NonBlockSyntherizer.super.Release();
                        if (Build.VERSION.SDK_INT < 18) {
                            getLooper().quit();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void RunInHandlerThread(int action) {
        RunInRHandlerThread(action, null);
    }

    private void RunInRHandlerThread(int action, Object obj) {
        Message msg = Message.obtain();
        msg.what = action;
        msg.obj = obj;
        _handler.sendMessage(msg);
    }

}
