package com.zistone.blelocation;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class DeviceInfoDebugDialog extends Dialog implements View.OnClickListener {

    public interface DeviceInfoDebugListener {
        void StartTimerTask(boolean flag);
    }

    private static final String TAG = "DeviceInfoDebugDialog";

    private TextView _txtDebug;
    private ImageButton _btnTop, _btnBottom, _btnClear;
    private Button _btnDebug;
    private DeviceInfoDebugListener _deviceInfoDebugListener;
    private Activity _activity;

    public DeviceInfoDebugDialog(Activity activity, DeviceInfoDebugListener deviceInfoDebugListener) {
        super(activity);
        _activity = activity;
        _deviceInfoDebugListener = deviceInfoDebugListener;
    }

    public void AppendTxt(String str) {
        if (null == _txtDebug) {
            return;
        }
        _activity.runOnUiThread(() -> {
            _txtDebug.append(str);
            int offset = _txtDebug.getLineCount() * _txtDebug.getLineHeight();
            if (offset > _txtDebug.getHeight()) {
                _txtDebug.scrollTo(0, offset - _txtDebug.getHeight());
            }
        });
    }

    @Override
    public void cancel() {
        super.cancel();
        _btnDebug.setText("暂停");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_top_info:
                _txtDebug.scrollTo(0, 0);
                break;
            case R.id.btn_bottom_info:
                int offset = _txtDebug.getLineCount() * _txtDebug.getLineHeight();
                if (offset > _txtDebug.getHeight()) {
                    _txtDebug.scrollTo(0, offset - _txtDebug.getHeight());
                }
                break;
            case R.id.btn_clear_info:
                _txtDebug.setText("");
                break;
            case R.id.btn_debug_info:
                if ("暂停".equals(_btnDebug.getText().toString())) {
                    _btnDebug.setText("恢复");
                    _deviceInfoDebugListener.StartTimerTask(false);
                } else {
                    _btnDebug.setText("暂停");
                    _deviceInfoDebugListener.StartTimerTask(true);
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_device_info_debug);
        _txtDebug = findViewById(R.id.txt_debug_info);
        _txtDebug.setMovementMethod(ScrollingMovementMethod.getInstance());
        _btnDebug = findViewById(R.id.btn_debug_info);
        _btnDebug.setOnClickListener(this::onClick);
        _btnTop = findViewById(R.id.btn_top_info);
        _btnTop.setOnClickListener(this::onClick);
        _btnBottom = findViewById(R.id.btn_bottom_info);
        _btnBottom.setOnClickListener(this::onClick);
        _btnClear = findViewById(R.id.btn_clear_info);
        _btnClear.setOnClickListener(this::onClick);
    }

}
