package com.zistone.blecontrol.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.zistone.blecontrol.R;

import java.util.Timer;
import java.util.TimerTask;

public class MyAnimation extends AppCompatActivity {

    private static final int MESSAGE_1 = 1;

    private Timer _timer;
    private TimerTask _timerTask;
    private int _count = 3;

    private Handler _handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            String result = (String) message.obj;
            switch (message.what) {
                case MESSAGE_1: {
                    _timerTask.cancel();
                    _timer.cancel();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation);
        _timer = new Timer();
        _timerTask = new TimerTask() {
            @Override
            public void run() {
                if (_count > 0)
                    _count--;
                else
                    _handler.sendMessage(_handler.obtainMessage(MESSAGE_1, ""));
            }
        };
        //_timer.schedule(_timerTask, 0, 800);
    }

}
