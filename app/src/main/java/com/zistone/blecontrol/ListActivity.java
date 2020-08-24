package com.zistone.blecontrol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.zistone.blecontrol.controls.BluetoothListAdapter;
import com.zistone.blecontrol.pojo.MyBluetoothDevice;
import com.zistone.blecontrol.util.BleListener;
import com.zistone.blecontrol.util.MyBleUtil;
import com.zistone.blecontrol.util.MyDeviceFilterShared;
import com.zistone.blecontrol.util.MyConvertUtil;
import com.zistone.blecontrol.util.MyProgressDialogUtil;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import pl.droidsonroids.gif.GifImageView;

public class ListActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener,
        TextWatcher, SeekBar.OnSeekBarChangeListener, BleListener {

    private static final String TAG = "ListActivity";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final int MESSAGE_1 = 1;
    //客户端特征配置、已知服务、写入特征的UUID、读取特征的UUID
    private static UUID CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    //OTA
    private static UUID SERVICE_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e1001");
    private static UUID WRITE_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e0001");
    private static UUID READ_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e0002");
    //    //BlueNRG
    //    private static UUID SERVICE_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    //    private static UUID WRITE_UUID = UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb");
    //    private static UUID READ_UUID = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");
    //    //Amdtp
    //    private static UUID SERVICE_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e1011");
    //    private static UUID WRITE_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e0011");
    //    private static UUID READ_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e0012");
    //    //Tag
    //    private static UUID SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    //    private static UUID READ_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    //    private static UUID WRITE_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    private BluetoothListAdapter _bleListAdapter;
    private Context _context;
    private Toolbar _toolbar;
    private ListView _listView;
    private BluetoothAdapter _bluetoothAdapter;
    private MaterialRefreshLayout _materialRefreshLayout;
    private MaterialRefreshListener _materialRefreshListener;
    private Button _btnFilterContent;
    private Drawable _drawableUp, _drawableDown;
    private ImageButton _btnClearContentFilter, _btnClearNameFilter, _btnClearAddressFilter;
    private SeekBar _seekBar;
    private EditText _editName, _editAddress;
    private TextView _txtRssi;
    private TableRow _row1, _row2, _row3;
    private GifImageView _gifImageView;
    //BLE的扫描器
    private BluetoothLeScanner _bluetoothLeScanner;
    //记录点击返回键的时间
    private long _exitTime = 0;
    //扫描到的设备
    private List<MyBluetoothDevice> _deviceList = new ArrayList<>();
    //根据名称筛选设备、根据地址筛选设备、筛选条件显示
    private String _filterName, _filterAddress, _filterContent;
    //根据信号强度筛选设备
    private int _filterRssi = 100;
    private boolean _isBtnUpDownFlag = false, _isStartOrStopScan = false, _isPermissionRequested = false;
    private MyHandler _myHandler;
    private ShowHideSettingDialogFragment _deviceListSetting;
    private FragmentManager _fragmentManager;

    static class MyHandler extends Handler {
        private WeakReference<ListActivity> _weakReference;
        private ListActivity _listActivity;

        public MyHandler(ListActivity activity) {
            _weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            if (_weakReference.get() == null)
                return;
            _listActivity = _weakReference.get();
            switch (message.what) {
            }
        }
    }

    /**
     * Android6.0之后需要动态申请权限
     */
    private void RequestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !_isPermissionRequested) {
            _isPermissionRequested = true;
            ArrayList<String> permissionsList = new ArrayList<>();
            String[] permissions = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE,
                                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_WIFI_STATE,
                                    Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_SETTINGS};
            for (String perm : permissions) {
                //进入到这里代表没有权限
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm))
                    permissionsList.add(perm);
            }
            if (!permissionsList.isEmpty())
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 1);
        }
    }

    /**
     * 统一初始化监听
     */
    private void InitListener() {
        _materialRefreshListener = new MaterialRefreshListener() {
            /**
             * 下拉刷新
             * 下拉刷新的时候需要清空ListView然后重新绑定
             *
             * @param materialRefreshLayout
             */
            @Override
            public void onRefresh(final MaterialRefreshLayout materialRefreshLayout) {
                materialRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        _deviceList.clear();
                        _bleListAdapter.setM_deviceList(_deviceList);
                        //使用notifyDataSetChanged()会保存当前的状态信息,然后更新适配器里的内容
                        //                        _bleListAdapter.notifyDataSetChanged();
                        //使用setAdapter()不会保存当前的状态信息，会使页面回到顶部，不会停留在之前的位置
                        _listView.setAdapter(_bleListAdapter);
                        BeginScan();
                        //结束下拉刷新
                        materialRefreshLayout.finishRefresh();
                    }
                }, 500);
            }

            /**
             * 加载完毕
             */
            @Override
            public void onfinish() {
                //Toast.makeText(ListActivity.this, "完成", Toast.LENGTH_LONG).show();
            }

            /**
             * 加载更多
             *
             * @param materialRefreshLayout
             */
            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {
                Toast.makeText(ListActivity.this, "别滑了,到底了", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void BeginScan() {
        MyBleUtil.DisConnGatt();
        if (MyBleUtil.StartScanLe() == 1) {
            _isStartOrStopScan = true;
            _gifImageView.setVisibility(View.VISIBLE);
            _toolbar.setNavigationIcon(R.drawable.stop);
        } else {
            MyProgressDialogUtil.ShowWarning(ListActivity.this, "知道了", "提示", "请确认系统蓝牙是否开启", false, null);
        }
    }

    private void StopScan() {
        MyBleUtil.StopScanLe();
        _isStartOrStopScan = false;
        _gifImageView.setVisibility(View.INVISIBLE);
        _toolbar.setNavigationIcon(R.drawable.start);
    }

    /**
     * 开启本地蓝牙
     */
    private void OpenBluetoothAdapter() {
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (_bluetoothAdapter != null) {
            //未打开蓝牙,才需要打开蓝牙
            //会以Dialog样式显示一个Activity,在onActivityResult()方法去处理返回值
            if (!_bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, 1);
            } else {
                switch (_bluetoothAdapter.getState()) {
                    //蓝牙已开启则直接开始扫描设备
                    case BluetoothAdapter.STATE_ON:
                    case BluetoothAdapter.STATE_TURNING_ON:
                        _toolbar.setNavigationIcon(R.drawable.stop);
                        _bluetoothLeScanner = _bluetoothAdapter.getBluetoothLeScanner();
                        MyBleUtil.Init(this, _bluetoothAdapter, _bluetoothLeScanner);
                        MyBleUtil.SetListener(this);
                        break;
                    //蓝牙未开启
                    case BluetoothAdapter.STATE_OFF:
                    case BluetoothAdapter.STATE_TURNING_OFF:
                    default:
                        _toolbar.setNavigationIcon(R.drawable.start);
                        break;
                }
            }
        } else {
            MyProgressDialogUtil.ShowWarning(ListActivity.this, "错误", "错误", "该设备不支持BLE", false, null);
        }
    }

    /**
     * 只显示在筛选强度区间的
     */
    private void OnlyShowSetRssiSameDevice() {
        if (_filterRssi != 100) {
            Iterator<MyBluetoothDevice> iterator = _deviceList.iterator();
            while (iterator.hasNext()) {
                MyBluetoothDevice device = iterator.next();
                if (device.getRssi() < _filterRssi * (-1))
                    iterator.remove();
            }
        }
    }

    /**
     * 只显示和筛选地址相似的
     */
    private void OnlyShowSetAddressSameDevice() {
        if (!_filterAddress.trim().equals("")) {
            Iterator<MyBluetoothDevice> iterator = _deviceList.iterator();
            while (iterator.hasNext()) {
                String address = iterator.next().getAddress();
                if (null == address || !address.trim().contains(_filterAddress))
                    iterator.remove();
            }
        }
    }

    /**
     * 只显示和筛选名称相似的
     */
    private void OnlyShowSetNameSameDevice() {
        if (!_filterName.trim().equals("")) {
            Iterator<MyBluetoothDevice> iterator = _deviceList.iterator();
            while (iterator.hasNext()) {
                String name = iterator.next().getName();
                if (null == name || !name.trim().contains(_filterName))
                    iterator.remove();
            }
        }
    }

    /**
     * 根据包名启动APK
     *
     * @param context
     * @param packageName
     * @return
     */
    private Intent GetAppOpenIntentByPackageName(Context context, String packageName) {
        String mainAct = null;
        PackageManager pkgMag = context.getPackageManager();
        //ACTION_MAIN是隐藏启动的action, 你也可以自定义
        Intent intent = new Intent(Intent.ACTION_MAIN);
        //CATEGORY_LAUNCHER有了这个,你的程序就会出现在桌面上
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        //FLAG_ACTIVITY_RESET_TASK_IF_NEEDED 按需启动的关键,如果任务队列中已经存在,则重建程序
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

        @SuppressLint("WrongConstant") List<ResolveInfo> list = pkgMag.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo.packageName.equals(packageName)) {
                mainAct = info.activityInfo.name;
                break;
            }
        }
        if (TextUtils.isEmpty(mainAct))
            return null;
        intent.setComponent(new ComponentName(packageName, mainAct));
        return intent;
    }

    /**
     * 筛选设备的窗体
     */
    private void ShowHideFilter() {
        if (!_isBtnUpDownFlag) {
            _row1.setVisibility(View.VISIBLE);
            _row2.setVisibility(View.VISIBLE);
            _row3.setVisibility(View.VISIBLE);
        } else {
            _row1.setVisibility(View.GONE);
            _row2.setVisibility(View.GONE);
            _row3.setVisibility(View.GONE);
        }
    }

    /**
     * 将筛选条件的内容显示在Button上
     */
    private void ShowSetFilterContent() {
        _filterContent = "";
        if (!_filterName.trim().equals(""))
            _filterContent += _filterName + ",";
        if (!_filterAddress.trim().equals(""))
            _filterContent += _filterAddress + ",";
        if (_filterRssi != 100)
            _filterContent += _filterRssi * -1 + "dBm,";
        _filterContent = MyConvertUtil.ReplaceLast(_filterContent, ",", "");
        if (_filterContent.equals(""))
            _filterContent = " No filter";
        _btnFilterContent.setText(_filterContent);
    }

    @Override
    public void onStop() {
        super.onStop();
        StopScan();
    }

    /**
     * 获取跳转之后界面传递回来的状态
     *
     * @param requestCode 根据不同的请求码,设置不同的传递内容
     * @param resultCode  目标界面返回的内容标识
     * @param data        数据内容
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                //用户授权开启蓝牙
                if (requestCode != 0) {
                    _bluetoothLeScanner = _bluetoothAdapter.getBluetoothLeScanner();
                    MyBleUtil.Init(ListActivity.this, _bluetoothAdapter, _bluetoothLeScanner);
                    MyBleUtil.SetListener(this);
                    BeginScan();
                }
                //用户拒绝开启蓝牙
                else {
                }
                break;
            //连接成功的蓝牙设备
            case 2:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - _exitTime) > 2000) {
                Toast.makeText(ListActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                _exitTime = System.currentTimeMillis();
            } else {
                this.finish();
                System.exit(0);
            }
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s == _editName.getEditableText())
            _filterName = _editName.getText().toString();
        else if (s == _editAddress.getEditableText())
            _filterAddress = _editAddress.getText().toString();
        ShowSetFilterContent();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClearFilterContent_filter:
                _filterName = "";
                _filterAddress = "";
                _filterRssi = 100;
                _filterContent = " No filter";
                _editName.setText("");
                _editAddress.setText("");
                //进度条的数值 = Math.abs(信号强度) - 40
                _seekBar.setProgress(Math.abs(_filterRssi) - 40);
                MyDeviceFilterShared.SetFilterName(_context, _filterName);
                MyDeviceFilterShared.SetFilterAddress(_context, _filterAddress);
                MyDeviceFilterShared.SetFilterRssi(_context, _filterRssi);
                _btnFilterContent.setText(_filterContent);
                break;
            case R.id.btnFilterContent_filter:
                if (!_isBtnUpDownFlag) {
                    ShowHideFilter();
                    _isBtnUpDownFlag = true;
                    _btnFilterContent.setCompoundDrawables(null, null, _drawableUp, null);
                    _btnFilterContent.setText(_filterContent);
                } else {
                    ShowHideFilter();
                    _isBtnUpDownFlag = false;
                    //隐藏键盘
                    InputMethodManager imm = (InputMethodManager) ListActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(_btnFilterContent.getApplicationWindowToken(), 0);
                    _btnFilterContent.setCompoundDrawables(null, null, _drawableDown, null);
                    _filterName = _editName.getText().toString();
                    _filterAddress = _editAddress.getText().toString();
                    MyDeviceFilterShared.SetFilterName(_context, _filterName);
                    MyDeviceFilterShared.SetFilterAddress(_context, _filterAddress);
                    MyDeviceFilterShared.SetFilterRssi(_context, _filterRssi);
                    ShowSetFilterContent();
                    _btnFilterContent.setCompoundDrawables(null, null, _drawableDown, null);
                    _isBtnUpDownFlag = false;
                }
                break;
            case R.id.btnClearName_filter:
                _editName.setText("");
                break;
            case R.id.btnClearAddress_filter:
                _editAddress.setText("");
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        StopScan();
        String address = _deviceList.get(position).getAddress();
        BluetoothDevice bluetoothDevice = _bluetoothAdapter.getRemoteDevice(address);
        Map<String, UUID> map = new HashMap<>();
        map.put("SERVICE_UUID", SERVICE_UUID);
        map.put("READ_UUID", READ_UUID);
        map.put("WRITE_UUID", WRITE_UUID);
        map.put("CONFIG_UUID", CONFIG_UUID);
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra(ARG_PARAM1, bluetoothDevice);
        intent.putExtra(ARG_PARAM2, (Serializable) map);
        startActivityForResult(intent, 2);
    }

    /**
     * 进度条发生改变
     *
     * @param seekBar
     * @param progress
     * @param fromUser
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        _filterRssi = progress + 40;
        _txtRssi.setText(_filterRssi * -1 + "dBm");
    }

    /**
     * 按住进度条
     *
     * @param seekBar
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    /**
     * 放开进度条
     *
     * @param seekBar
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        ShowSetFilterContent();
    }

    /**
     * 扫描到BLE设备
     *
     * @param result
     */
    @Override
    public void OnScanLeResult(ScanResult result) {
        BluetoothDevice bluetoothDevice = result.getDevice();
        String name = bluetoothDevice.getName();
        String address = bluetoothDevice.getAddress();
        int rssi = result.getRssi();
        int state = bluetoothDevice.getBondState();
        MyBluetoothDevice device = new MyBluetoothDevice();
        device.setName(name);
        device.setAddress(address);
        device.setRssi(rssi);
        device.setBoundState(state);
        device.setBluetoothDevice(bluetoothDevice);
        //设备去重
        if (!_deviceList.contains(device)) {
            _deviceList.add(device);
        }
        //根据条件筛选设备
        OnlyShowSetNameSameDevice();
        OnlyShowSetAddressSameDevice();
        OnlyShowSetRssiSameDevice();
        //按照信号强度降序排序
        Collections.sort(_deviceList, new Comparator<MyBluetoothDevice>() {
            @Override
            public int compare(MyBluetoothDevice o1, MyBluetoothDevice o2) {
                if (o1.getRssi() > o2.getRssi())
                    return -1;
                if (o1.getRssi() < o2.getRssi())
                    return 1;
                return 0;
            }
        });
        if (null != _deviceList) {
            _bleListAdapter.setM_deviceList(_deviceList);
            Log.i(TAG, String.format("扫描到设备%s的信号强度%d", address, rssi));
            //使用notifyDataSetChanged()会保存当前的状态信息,然后更新适配器里的内容
            _bleListAdapter.notifyDataSetChanged();
            _listView.setOnItemClickListener(this);
        }
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
    }

    @Override
    public void OnReadSuccess(byte[] byteArray) {
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    @Override
    public void onDestroy() {
        MyProgressDialogUtil.DismissAlertDialog();
        StopScan();
        _deviceList.clear();
        super.onDestroy();
    }

    /**
     * 动态授权的回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        String content = "动态授权的回调:";
        for (int i = 0; i < permissions.length; i++) {
            content += "\r\n权限" + permissions[i] + "【" + (grantResults[i] != -1 ? "允许" : "拒绝") + "】";
        }
        Log.i(TAG, content);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _myHandler = new MyHandler(this);
        setContentView(R.layout.activity_list);
        RequestPermission();
        _fragmentManager = getSupportFragmentManager();
        _context = getApplicationContext();
        //扫描的过滤条件
        _filterContent = " No filter";
        _filterName = MyDeviceFilterShared.GetFilterName(_context);
        _filterAddress = MyDeviceFilterShared.GetFilterAddress(_context);
        _filterRssi = MyDeviceFilterShared.GetFilterRssi(_context);
        //BLE设备列表的适配器
        _bleListAdapter = new BluetoothListAdapter(_context);
        //Toolbar
        _toolbar = findViewById(R.id.toolbar_bleList);
        _toolbar.setTitle("");
        setSupportActionBar(_toolbar);
        //Toolbar的icon点击事件要设置在setSupportActionBar()的后面
        _toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_isStartOrStopScan) {
                    BeginScan();
                } else {
                    StopScan();
                }
            }
        });
        _gifImageView = _toolbar.findViewById(R.id.toolbar_gifView);
        //下拉刷新控件
        _materialRefreshLayout = findViewById(R.id.refresh_bleList);
        _listView = findViewById(R.id.lv_bleList);
        _btnFilterContent = findViewById(R.id.btnFilterContent_filter);
        _btnFilterContent.setOnClickListener(this::onClick);
        _drawableUp = getResources().getDrawable(R.drawable.up, null);
        _drawableUp.setBounds(0, 0, _drawableUp.getMinimumWidth(), _drawableUp.getMinimumHeight());
        _drawableDown = getResources().getDrawable(R.drawable.down, null);
        _drawableDown.setBounds(0, 0, _drawableDown.getMinimumWidth(), _drawableDown.getMinimumHeight());
        _btnClearContentFilter = findViewById(R.id.btnClearFilterContent_filter);
        _btnClearContentFilter.setOnClickListener(this::onClick);
        _row1 = findViewById(R.id.row2_filter);
        _row1.setVisibility(View.GONE);
        _row2 = findViewById(R.id.row3_filter);
        _row2.setVisibility(View.GONE);
        _row3 = findViewById(R.id.row4_filter);
        _row3.setVisibility(View.GONE);
        _btnClearNameFilter = findViewById(R.id.btnClearName_filter);
        _btnClearNameFilter.setOnClickListener(this::onClick);
        _btnClearAddressFilter = findViewById(R.id.btnClearAddress_filter);
        _btnClearAddressFilter.setOnClickListener(this::onClick);
        _editName = findViewById(R.id.editName_filter);
        if (!_filterName.trim().equals(""))
            _editName.setText(_filterName);
        _editName.addTextChangedListener(this);
        _editAddress = findViewById(R.id.editAddress_filter);
        if (!_filterAddress.trim().equals(""))
            _editAddress.setText(_filterAddress);
        _editAddress.addTextChangedListener(this);
        _seekBar = findViewById(R.id.seekBar_filter);
        //进度条的最大数值为60,信号强度 = (数值+40) * -1
        _seekBar.setMax(60);
        //进度条的数值 = Math.abs(信号强度) - 40
        _seekBar.setProgress(Math.abs(_filterRssi) - 40);
        _seekBar.setOnSeekBarChangeListener(this);
        _txtRssi = findViewById(R.id.tvRssi_filter);
        _txtRssi.setText(_filterRssi * -1 + "dBm");
        ShowSetFilterContent();
        //所有的控件、对象都实例化后再初始化回调方法
        InitListener();
        //下拉刷新
        _materialRefreshLayout.setMaterialRefreshListener(_materialRefreshListener);
        _materialRefreshLayout.autoRefresh();
        //控件、对象、事件监听都加载完毕后才开始扫描蓝牙设备
        OpenBluetoothAdapter();
    }

}
