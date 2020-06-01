package com.zistone.blecontrol.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.zistone.blecontrol.R;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class MyAnimation extends AppCompatActivity {
    private static final int MESSAGE_1 = 1;

    private Timer _timer;
    private TimerTask _timerTask;
    private int _count = 3;
    private MyHandler _myHandler;

    static class MyHandler extends Handler {
        WeakReference<MyAnimation> _weakReference;
        MyAnimation _myAnimation;

        public MyHandler(MyAnimation activity) {
            _myAnimation = activity;
        }

        @Override
        public void handleMessage(Message message) {
            if (_weakReference.get() == null)
                return;
            _myAnimation = _weakReference.get();
            String result = (String) message.obj;
            switch (message.what) {
                case MESSAGE_1: {
                    _myAnimation._timerTask.cancel();
                    _myAnimation._timer.cancel();
                    _myAnimation.finish();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _myHandler = new MyHandler(this);
        setContentView(R.layout.activity_animation);
        _timer = new Timer();
        _timerTask = new TimerTask() {
            @Override
            public void run() {
                if (_count > 0)
                    _count--;
                else
                    _myHandler.obtainMessage(MESSAGE_1, "").sendToTarget();
            }
        };
        //_timer.schedule(_timerTask, 0, 800);
    }

}
