package com.zistone.blecontrol;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.zistone.blecontrol.util.MyBleConnectListener;
import com.zistone.blecontrol.util.MyBleUtil;
import com.zistone.blecontrol.util.MyInstallAPKUtil;
import com.zistone.blecontrol.util.MyProgressDialogUtil;

import java.util.Map;
import java.util.UUID;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MenuActivity";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ImageButton _btnReturn;
    private Button _btn1, _btn2, _btn5, _btn7, _btn8;
    private TextView _txt1;
    private BluetoothDevice _bluetoothDevice;
    private Map<String, UUID> _uuidMap;
    private FragmentManager _fragmentManager;
    private Toolbar _toolbar;

    private void SetConnectSuccess(boolean flag) {
        UpdateBtn(_btn1, flag);
        UpdateBtn(_btn2, flag);
        UpdateBtn(_btn5, flag);
        UpdateBtn(_btn7, flag);
        UpdateBtn(_btn8, flag);
        if (flag) {
            UpdateText(_txt1, null, Color.parseColor("#3CB371"));
        } else {
            UpdateText(_txt1, null, Color.RED);
        }
    }

    private void UpdateBtn(Button btn, boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btn.setEnabled(enable);
            }
        });
    }

    private void UpdateText(TextView txt, String str, int color) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != str && !"".equals(str.trim()))
                    txt.setText(str);
                if (color != 0)
                    txt.setTextColor(color);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.xxx, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            MyProgressDialogUtil.DismissAlertDialog();
            MyBleUtil.DisConnGatt();
            finish();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.btn_return_menu:
                MyBleUtil.DisConnGatt();
                MyProgressDialogUtil.DismissAlertDialog();
                this.finish();
                break;
            case R.id.btn_power_menu:
                intent = new Intent(this, PowerActivity.class);
                intent.putExtra(ARG_PARAM1, _bluetoothDevice);
                break;
            case R.id.btn_cmd_menu:
                intent = new Intent(this, CmdActivity.class);
                intent.putExtra(ARG_PARAM1, _bluetoothDevice);
                break;
            case R.id.btn_location_menu:
                intent = new Intent(this, LocationActivity.class);
                intent.putExtra(ARG_PARAM1, _bluetoothDevice);
                break;
            case R.id.btn_ota_menu:
                //检查是否安装第三方的OTA升级工具
                if (MyInstallAPKUtil.CheckInstalled(this, "com.ambiqmicro.android.amota")) {
                    MyProgressDialogUtil.ShowWarning(this, "知道了", "警告", "使用OTA升级功能会关闭当前与设备的连接", false, () -> {
                        //启动第三方的OTA升级工具
                        Intent otaIntent = MyInstallAPKUtil.GetAppOpenIntentByPackageName(MenuActivity.this, "com.ambiqmicro.android.amota");
                        startActivity(otaIntent);

                    });
                } else {
                    MyProgressDialogUtil.ShowConfirm(this, "好的", "不了", "提示", "未安装OTA_ZM301，无法使用该功能！是否安装？", true, new MyProgressDialogUtil.ConfirmListener() {
                        @Override
                        public void OnConfirm() {
                            MyInstallAPKUtil.InstallFromCopyAssets(MenuActivity.this, "ambiq_ota.apk", "sdcard");
                        }

                        @Override
                        public void OnCancel() {
                        }
                    });
                }
                break;
            case R.id.btn_xyz_menu:
                intent = new Intent(this, XYZActivity.class);
                intent.putExtra(ARG_PARAM1, _bluetoothDevice);
                break;
        }
        if (null != intent)
            startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyBleUtil.DisConnGatt();
        _bluetoothDevice = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        _fragmentManager = getSupportFragmentManager();
        Intent intent = getIntent();
        _bluetoothDevice = intent.getParcelableExtra(ARG_PARAM1);
        _uuidMap = (Map<String, UUID>) intent.getSerializableExtra(ARG_PARAM2);
        _btnReturn = findViewById(R.id.btn_return_menu);
        _btnReturn.setOnClickListener(this::onClick);
        //Toolbar
        _toolbar = findViewById(R.id.toolbar_menu);
        _toolbar.setTitle("");
        setSupportActionBar(_toolbar);
        _txt1 = findViewById(R.id.txt1_menu);
        _txt1.setText(_bluetoothDevice.getName() + "\r\n" + _bluetoothDevice.getAddress());
        _btn1 = findViewById(R.id.btn_power_menu);
        _btn2 = findViewById(R.id.btn_cmd_menu);
        _btn5 = findViewById(R.id.btn_location_menu);
        _btn7 = findViewById(R.id.btn_ota_menu);
        _btn8 = findViewById(R.id.btn_xyz_menu);
        _btn1.setOnClickListener(this::onClick);
        _btn2.setOnClickListener(this::onClick);
        _btn5.setOnClickListener(this::onClick);
        _btn7.setOnClickListener(this::onClick);
        _btn8.setOnClickListener(this::onClick);
        MyBleUtil.SetConnectListener(new MyBleConnectListener() {
            @Override
            public void OnConnected() {
                Log.i(TAG, "成功建立连接");
                MyProgressDialogUtil.DismissAlertDialog();
                SetConnectSuccess(true);
                //返回时告知该设备已成功连接
                setResult(2, new Intent());
            }

            @Override
            public void OnConnecting() {
                Log.i(TAG, "正在建立连接...");
                MyProgressDialogUtil.ShowProgressDialog(MenuActivity.this, true, null, "正在连接...\n如长时间未连\n接请返回重试");
            }

            @Override
            public void OnDisConnected() {
                Log.e(TAG, "连接已断开");
                SetConnectSuccess(false);
                runOnUiThread(() -> {
                    MyProgressDialogUtil.DismissAlertDialog();
                    MyProgressDialogUtil.ShowWarning(MenuActivity.this, "知道了", "警告", "连接已断开，请检查设备然后重新连接！", false, () -> finish());
                });
            }
        });
        //连接
        Log.i(TAG, "开始连接蓝牙...");
        MyBleUtil.ConnectDevice(_bluetoothDevice, _uuidMap);
    }

}
