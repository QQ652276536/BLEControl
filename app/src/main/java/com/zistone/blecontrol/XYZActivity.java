package com.zistone.blecontrol;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
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

import com.zistone.blecontrol.util.BleListener;
import com.zistone.blecontrol.util.MyBleUtil;
import com.zistone.blecontrol.util.MyConvertUtil;
import com.zistone.blecontrol.util.MyProgressDialogUtil;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class XYZActivity extends AppCompatActivity implements View.OnClickListener, BleListener {

    private static final String TAG = "XYZActivity";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //查询内部控制参数
    private static final String SEARCH_CONTROLPARAM_COMM = "680000000000006810000186EA16";
    //读取基本信息：版本，电池电压，内部温度
    private static final String BASEINFO_COMM = "6800000000000068210100EC16";
    //读取GPS位置信息
    private static final String LOCATION_COMM = "6800000000000068220100EC16";
    //综合测试：循环发送检测门的状态
    private static final String TESTA = "680000000000006810000180E616";
    private static final int SEND_SET_CONTROLPARAM = 87;

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
                Log.i(TAG, "发送'读取设备基本信息'指令：6800000000000068000000EC16");
                Thread.sleep(100);
                //                MyBleUtil.SendComm(LOCATION_COMM);
                //                Log.i(TAG, "发送'GPS位置'指令：" + LOCATION_COMM);
                //                Thread.sleep(100);
                //                MyBleUtil.SendComm(TESTA);
                //                Log.i(TAG, "发送'综合测试'指令：" + TESTA);
                //                Thread.sleep(100);
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
        }
    };
    private Timer _refreshTimer2222 = new Timer();

    static class MyHandler extends Handler {
        WeakReference<XYZActivity> _weakReference;
        XYZActivity Activity;

        public MyHandler(XYZActivity activity) {
            _weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            if (_weakReference.get() == null)
                return;
            Activity = _weakReference.get();
            String result = (String) message.obj;
            Activity._debugView.append(result+"\n");
            //定位到最后一行
            int offset = Activity._debugView.getLineCount() * Activity._debugView.getLineHeight();
            //如果文本的高度大于ScrollView的，就自动滑动
            if (offset > Activity._debugView.getHeight())
                Activity._debugView.scrollTo(0, offset - Activity._debugView.getHeight());
        }
    }

    /**
     * 解析硬件返回的数据
     *
     * @param data 带空格的16进制字符串
     */
    private void Resolve(String data) {
        String[] strArray = data.split(" ");
        _myHandler.obtainMessage(1, Arrays.toString(strArray)).sendToTarget();
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
                _debugView.setText("");
                break;
        }
    }

    @Override
    public void OnScanLeResult(ScanResult result) {
    }

    @Override
    public void OnConnected() {
    }

    @Override
    public void OnConnecting() {
    }

    @Override
    public void OnDisConnected() {
    }

    @Override
    public void OnWriteSuccess(byte[] byteArray) {
        String result = MyConvertUtil.ByteArrayToHexStr(byteArray);
        result = MyConvertUtil.StrAddCharacter(result, 2, " ");
        String[] strArray = result.split(" ");
        String indexStr = strArray[11];
        Log.i(TAG, "发送'" + indexStr + "'指令：" + TESTA);
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
        //分包
        else {
            if (!strArray[strArray.length - 1].equals("16")) {
                _stringBuffer.append(result + " ");
            }
            //最后一个包
            else {
                _stringBuffer.append(result);
                result = _stringBuffer.toString();
                Resolve(result);
                //清空缓存
                _stringBuffer = new StringBuffer();
            }
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
        //初始化蓝牙
        MyBleUtil.SetListener(this);
    }

    @Override
    public void onDestroy() {
        _refreshTimer.cancel();
        _refreshTask.cancel();
        _bluetoothDevice = null;
        super.onDestroy();
    }

}
