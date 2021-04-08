package com.zistone.blecontrol;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zistone.libble.util.MyBleConnectListener;
import com.zistone.libble.util.MyBleMessageListener;
import com.zistone.libble.util.MyBleUtil;
import com.zistone.libble.util.MyConvertUtil;
import com.zistone.libble.util.MyProgressDialogUtil;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class XYZActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "XYZActivity";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private BluetoothDevice _bluetoothDevice;
    private ImageButton _btnReturn, _btnTop, _btnBottom, _btnClear;
    private TextView _debugView;
    private StringBuffer _stringBuffer = new StringBuffer();
    private MyHandler _myHandler;
    private Timer _refreshTimer = new Timer();
    //定时发送综合测试指令
    private TimerTask _refreshTask = new TimerTask() {
        @Override
        public void run() {
            try {
                MyBleUtil.SendComm("6800000000000068000000EC16");
                Log.i(TAG, "发送指令：6800000000000068000000EC16");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private TimerTask _refreshTask222 = new TimerTask() {
        @Override
        public void run() {
            if (_refreshTimer != null)
                _refreshTimer.cancel();
            if (_refreshTask != null)
                _refreshTask.cancel();
            Log.i(TAG, "定时请求终止！");
            _refreshTimer2222.cancel();
            _refreshTask222.cancel();
        }
    };
    private Timer _refreshTimer2222 = new Timer();
    private MyBleConnectListener _connectListener;
    private MyBleMessageListener _messageListener;

    static class MyHandler extends Handler {
        WeakReference<XYZActivity> _weakReference;
        XYZActivity xyzActivity;

        public MyHandler(XYZActivity activity) {
            _weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            if (_weakReference.get() == null)
                return;
            xyzActivity = _weakReference.get();
            String[] strArray = (String[]) message.obj;
            switch (message.what) {
                case 89:
                    xyzActivity._debugView.append("收到：" + Arrays.toString(strArray) + "\n");
                    //有符号16进制转10进制
                    double x = Integer.valueOf(strArray[10] + strArray[11], 16).shortValue() / 100.0;
                    double y = Integer.valueOf(strArray[12] + strArray[13], 16).shortValue() / 100.0;
                    double z = Integer.valueOf(strArray[14] + strArray[15], 16).shortValue() / 100.0;
                    int type = Integer.valueOf(strArray[16]);
                    String cmdStr = Arrays.toString(strArray).replaceAll("[\\s|\\[|\\]|,]", "");
                    cmdStr = MyConvertUtil.StrAddCharacter(cmdStr, 2, " ");
                    String logStr = "X轴：" + x + "，Y轴：" + y + "，Z轴：" + z + "，事件类型：" + (type == 0 ? "振动" : "位移") + "\n";
                    Log.i(TAG, logStr);
                    xyzActivity._debugView.append(logStr);
                    break;
            }
            //定位到最后一行
            int offset = xyzActivity._debugView.getLineCount() * xyzActivity._debugView.getLineHeight();
            //如果文本的高度大于ScrollView的，就自动滑动
            if (offset > xyzActivity._debugView.getHeight())
                xyzActivity._debugView.scrollTo(0, offset - xyzActivity._debugView.getHeight());
        }
    }

    private void InitListener() {
        _connectListener = new MyBleConnectListener() {
            @Override
            public void OnConnected() {
            }

            @Override
            public void OnConnecting() {
            }

            @Override
            public void OnDisConnected() {
                Log.e(TAG, "连接已断开");
                runOnUiThread(() -> MyProgressDialogUtil.ShowWarning(XYZActivity.this, "知道了", "警告", "连接已断开，请检查设备然后重新连接！", false, () -> {
                    Intent intent = new Intent(XYZActivity.this, ListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }));
            }
        };
        _messageListener = new MyBleMessageListener() {
            @Override
            public void OnWriteSuccess(byte[] byteArray) {
                String result = MyConvertUtil.ByteArrayToHexStr(byteArray);
                result = MyConvertUtil.StrAddCharacter(result, 2, " ");
                String[] strArray = result.split(" ");
                String indexStr = strArray[11];
            }

            @Override
            public void OnReadSuccess(byte[] byteArray) {
                String result = MyConvertUtil.ByteArrayToHexStr(byteArray);
                result = MyConvertUtil.StrAddCharacter(result, 2, " ");
                Log.i(TAG, "收到：" + result);
                String[] strArray = result.split(" ");
                //一个包(20个字节)
                if (strArray[0].equals("68") && strArray[strArray.length - 1].equals("16")) {
                    Resolve(result);
                    //清空缓存
                    _stringBuffer = new StringBuffer();
                }
            }
        };
    }

    /**
     * 解析硬件返回的数据
     *
     * @param data 带空格的16进制字符串
     */
    private void Resolve(String data) {
        String[] strArray = data.split(" ");
        //目前最短的指令为14位
        if (strArray.length < 14) {
            Log.e(TAG, "指令长度" + strArray.length + "错误，不予解析！");
            return;
        }
        String type = strArray[8].toUpperCase();
        if ("89".equals(type))
            _myHandler.obtainMessage(89, strArray).sendToTarget();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
            this.finish();
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.power_setting, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_return:
                MyProgressDialogUtil.DismissAlertDialog();
                this.finish();
                //回到顶部
            case R.id.btnTop:
                _debugView.scrollTo(0, 0);
                break;
            //回到底部
            case R.id.btnBottom:
                int offset = _debugView.getLineCount() * _debugView.getLineHeight();
                if (offset > _debugView.getHeight())
                    _debugView.scrollTo(0, offset - _debugView.getHeight());
                break;
            //清屏
            case R.id.btnClear:
                _debugView.setText(".");
                _debugView.scrollTo(0, 0);
                break;
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _myHandler = new MyHandler(this);
        setContentView(R.layout.activity_xyz);
        //任务、延迟执行时间、重复调用间隔，Timer和TimerTask在调用cancel方法取消后不能再执行schedule语句
        _refreshTimer.schedule(_refreshTask, 0, 1 * 1000);
        _refreshTimer2222.schedule(_refreshTask222, 5 * 1000, 1 * 1000);
        Intent intent = getIntent();
        _bluetoothDevice = intent.getParcelableExtra(ARG_PARAM1);
        _debugView = findViewById(R.id.txt);
        _debugView.setMovementMethod(ScrollingMovementMethod.getInstance());
        _btnReturn = findViewById(R.id.btn_return);
        _btnTop = findViewById(R.id.btnTop);
        _btnBottom = findViewById(R.id.btnBottom);
        _btnClear = findViewById(R.id.btnClear);
        _btnReturn.setOnClickListener(this::onClick);
        _btnTop.setOnClickListener(this::onClick);
        _btnBottom.setOnClickListener(this::onClick);
        _btnClear.setOnClickListener(this::onClick);
        _debugView.setMovementMethod(ScrollingMovementMethod.getInstance());
        InitListener();
        MyBleUtil.SetConnectListener(_connectListener);
        MyBleUtil.SetMessageListener(_messageListener);
    }

    @Override
    public void onDestroy() {
        _refreshTimer.cancel();
        _refreshTask.cancel();
        _bluetoothDevice = null;
        super.onDestroy();
    }

}
