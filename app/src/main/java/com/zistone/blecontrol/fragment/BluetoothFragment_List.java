package com.zistone.blecontrol.fragment;

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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.zistone.MainActivity;
import com.zistone.blecontrol.R;
import com.zistone.blecontrol.control.BluetoothListAdapter;
import com.zistone.blecontrol.pojo.MyBluetoothDevice;
import com.zistone.blecontrol.util.BLEListener;
import com.zistone.blecontrol.util.BLEUtil;
import com.zistone.blecontrol.util.ConvertUtil;
import com.zistone.blecontrol.util.DeviceFilterShared;
import com.zistone.blecontrol.util.ProgressDialogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import pl.droidsonroids.gif.GifImageView;

public class BluetoothFragment_List extends Fragment
        implements View.OnClickListener, AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener, TextWatcher,
        SeekBar.OnSeekBarChangeListener, BLEListener {
    private static final String TAG = "BluetoothFragment_List";
    private static final String ARG_PARAM1 = "param1", ARG_PARAM2 = "param2";
    //已知服务、写入特征的UUID、读取特征的UUID、客户端特征配置
    private static UUID SERVICE_UUID, WRITE_UUID, READ_UUID, CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private BluetoothListAdapter _bluetoothListAdapter;
    private Context _context;
    private View _view;
    private Toolbar _toolbar;
    private ListView _listView;
    private BluetoothAdapter _bluetoothAdapter;
    private BluetoothFragment_CommandTest _bluetoothFragment_commandTest;
    private BluetoothFragment_PowerControl _bluetoothFragment_powerControl;
    private BluetoothFragment_OTA _bluetoothFragment_ota;
    private BluetoothFragment_DB _bluetoothFragment_db;
    private BluetoothFragment_Temperature _bluetoothFragment_temperature;
    private RadioButton _rdoUUID1, _rdoUUID2, _rdoUUID3, _rdoUUID4, _rdoFunc1, _rdoFunc2, _rdoFunc3, _rdoFunc4;
    private OnFragmentInteractionListener _onFragmentInteractionListener;
    private MaterialRefreshLayout _materialRefreshLayout;
    private MaterialRefreshListener _materialRefreshListener;
    private BluetoothFragment_PowerControl.Listener _powerControlListener;
    private BluetoothFragment_Temperature.Listener _temperatureListener;
    private Button _btnFilterContent;
    private Drawable _drawableUp, _drawableDown;
    private ImageButton _btnClearContentFilter, _btnClearNameFilter, _btnClearAddressFilter;
    private SeekBar _seekBar;
    private EditText _editName, _editAddress;
    private TextView _txtRssi;
    private TableRow _row2, _row3, _row4, _row5;
    private CheckBox _chkHideDevice;
    private GifImageView _gifImageView;
    //BLE的扫描器
    private BluetoothLeScanner _bluetoothLeScanner;
    //记录点击返回键的时间
    private long _exitTime = 0;
    //当前连接的设备
    private MyBluetoothDevice _myBluetoothDevice;
    //扫描到的设备,由Map转换,为保证和map数据的同步,不允许对该集合操作
    private List<MyBluetoothDevice> _deviceList = new ArrayList<>();
    //扫描到的设备、连接成功的设备
    private Map<String, MyBluetoothDevice> _deviceMap = new HashMap<>(), _connectSuccessMap = new HashMap<>();
    //传递进来的参数1、传递进来的参数2、根据名称筛选设备、根据地址过滤设备
    private String _param1, _param2, _filterName, _filterAddress, _filterContent;
    //根据信号强度筛选设备
    private int _filterRssi = 100;
    private boolean _isHideConnectSuccessDevice = false, _isBtnUpDownFlag = false, _isStartOrStopScan = false;

    /**
     * 成功连接BLE设备
     */
    public void OnConnected() {
        String address = _myBluetoothDevice.getAddress();
        Log.i(TAG, String.format("设备%s连接成功", address));
        //选择设备后会停止扫描,隐藏连接成功的设备后再调用一次筛选
        if (_isHideConnectSuccessDevice) {
            //根据条件筛选设备
            _connectSuccessMap.put(address, _myBluetoothDevice);
            Map<String, MyBluetoothDevice> map = HideConnectSuccessDevice(_deviceMap);
            _deviceList = new ArrayList<>(map.values());
            _bluetoothListAdapter.setM_deviceList(_deviceList);
            //使用notifyDataSetChanged()会保存当前的状态信息,然后更新适配器里的内容
            _bluetoothListAdapter.notifyDataSetChanged();
        }
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
        //设备去重
        MyBluetoothDevice device = new MyBluetoothDevice();
        device.setName(name);
        device.setAddress(address);
        device.setRssi(rssi);
        device.setBoundState(state);
        device.setBluetoothDevice(bluetoothDevice);
        _deviceMap.put(address, device);
        //根据条件筛选设备
        Map<String, MyBluetoothDevice> map = FilterDeviceByCondition(_deviceMap);
        _deviceList = new ArrayList<>(map.values());
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
        _bluetoothListAdapter.setM_deviceList(_deviceList);
        Log.i(TAG, String.format("设备%s的信号强度%d", address, rssi));
        //使用notifyDataSetChanged()会保存当前的状态信息,然后更新适配器里的内容
        _bluetoothListAdapter.notifyDataSetChanged();
        _listView.setOnItemClickListener(BluetoothFragment_List.this);
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
     * Activity中加载Fragment时会要求实现onFragmentInteraction(Uri uri)方法,此方法主要作用是从fragment向activity传递数据
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public static BluetoothFragment_List newInstance(String param1, String param2) {
        BluetoothFragment_List fragment = new BluetoothFragment_List();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private View.OnKeyListener backListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                if ((System.currentTimeMillis() - _exitTime) > 2000) {
                    Toast.makeText(getActivity(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    _exitTime = System.currentTimeMillis();
                } else {
                    getActivity().finish();
                    System.exit(0);
                }
                return true;
            }
            return false;
        }
    };

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
        if (_isHideConnectSuccessDevice)
            _filterContent += "Yes,";
        _filterContent = ConvertUtil.ReplaceLast(_filterContent, ",", "");
        if (_filterContent.equals(""))
            _filterContent = " No filter";
        _btnFilterContent.setText(_filterContent);
    }

    /**
     * 筛选设备的窗体
     */
    private void ShowHideFilter() {
        if (!_isBtnUpDownFlag) {
            _row2.setVisibility(View.VISIBLE);
            _row3.setVisibility(View.VISIBLE);
            _row4.setVisibility(View.VISIBLE);
            _row5.setVisibility(View.VISIBLE);
        } else {
            _row2.setVisibility(View.GONE);
            _row3.setVisibility(View.GONE);
            _row4.setVisibility(View.GONE);
            _row5.setVisibility(View.GONE);
        }
    }

    /**
     * 统一初始化监听
     */
    private void InitListener() {
        //成功连接设备
        _powerControlListener = () -> OnConnected();
        _temperatureListener = () -> OnConnected();
        //下拉刷新
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
                        _deviceMap.clear();
                        _deviceList.clear();
                        _bluetoothListAdapter.setM_deviceList(_deviceList);
                        //使用notifyDataSetChanged()会保存当前的状态信息,然后更新适配器里的内容
                        _bluetoothListAdapter.notifyDataSetChanged();
                        _listView.setAdapter(_bluetoothListAdapter);
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
                //Toast.makeText(_context, "完成", Toast.LENGTH_LONG).show();
            }

            /**
             * 加载更多
             *
             * @param materialRefreshLayout
             */
            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {
                Toast.makeText(_context, "别滑了,到底了", Toast.LENGTH_SHORT).show();
            }
        };
    }

    /**
     * 根据设置选项过滤扫描到蓝牙设备
     *
     * @param map
     * @return
     */
    private Map<String, MyBluetoothDevice> FilterDeviceByCondition(Map<String, MyBluetoothDevice> map) {
        map = OnlyShowSetNameSameDevice(map);
        map = OnlyShowSetAddressSameDevice(map);
        map = OnlyShowSetRssiSameDevice(map);
        map = HideConnectSuccessDevice(map);
        return map;
    }

    /**
     * 隐藏已经连接成功的蓝牙设备
     * <p>
     * 该方法在接口回调里也有调用,在过滤设备（连接成功后该设备不再显示）后,不要调用会导致BluetoothDevice对象被删除的方法,因为该对象可能正在被使用!
     * <p>
     * 在连接成功的设备与扫描出来的设备比较时用设备地址比较,不要用对象,因为下拉刷新扫描出来的对象与上次的地址值不一样!
     *
     * @param map 扫描到的蓝牙设备
     * @return
     */
    private Map<String, MyBluetoothDevice> HideConnectSuccessDevice(Map<String, MyBluetoothDevice> map) {
        if (_isHideConnectSuccessDevice) {
            Map<String, MyBluetoothDevice> resultMap = new HashMap<>();
            Iterator<Map.Entry<String, MyBluetoothDevice>> iterator1 = map.entrySet().iterator();
            while (iterator1.hasNext()) {
                MyBluetoothDevice tempDevice1 = iterator1.next().getValue();
                String tempAddress1 = tempDevice1.getAddress();
                boolean flag = false;
                Iterator<Map.Entry<String, MyBluetoothDevice>> iterator2 = _connectSuccessMap.entrySet().iterator();
                while (iterator2.hasNext()) {
                    MyBluetoothDevice tempDevice2 = iterator2.next().getValue();
                    String tempAddress2 = tempDevice2.getAddress();
                    if (tempAddress1.equals(tempAddress2)) {
                        flag = true;
                        break;
                    }
                }
                if (!flag)
                    resultMap.put(tempAddress1, tempDevice1);
            }
            return resultMap;
        } else {
            return map;
        }
    }

    /**
     * 只显示和筛选条件的强度相似的蓝牙设备
     *
     * @param map 扫描到的蓝牙设备
     * @return
     */
    private Map<String, MyBluetoothDevice> OnlyShowSetRssiSameDevice(Map<String, MyBluetoothDevice> map) {
        if (_filterRssi != 100) {
            Iterator<Map.Entry<String, MyBluetoothDevice>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                MyBluetoothDevice device = iterator.next().getValue();
                if (device.getRssi() < _filterRssi * (-1))
                    iterator.remove();
            }
        }
        return map;
    }

    /**
     * 只显示和筛选条件的地址相似的蓝牙设备
     *
     * @param map 扫描到的蓝牙设备
     * @return
     */
    private Map<String, MyBluetoothDevice> OnlyShowSetAddressSameDevice(Map<String, MyBluetoothDevice> map) {
        if (!_filterAddress.trim().equals("")) {
            Iterator<Map.Entry<String, MyBluetoothDevice>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                String address = iterator.next().getValue().getAddress();
                if (address == null || !address.trim().contains(_filterAddress))
                    iterator.remove();
            }
        }
        return map;
    }

    /**
     * 只显示和筛选条件的名称相似的蓝牙设备
     *
     * @param map 扫描到的蓝牙设备
     * @return
     */
    private Map<String, MyBluetoothDevice> OnlyShowSetNameSameDevice(Map<String, MyBluetoothDevice> map) {
        if (!_filterName.trim().equals("")) {
            Iterator<Map.Entry<String, MyBluetoothDevice>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                String name = iterator.next().getValue().getName();
                if (name == null || !name.trim().contains(_filterName))
                    iterator.remove();
            }
        }
        return map;
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Activity的onCreateOptionsMenu会在之前调用,即先Clear一下,这样就只有Fragment自己设置的了
        menu.clear();
        inflater.inflate(R.menu.menu_setting, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            //OTA
            case R.id.menu_1_setting:
                //                    _bluetoothFragment_ota = BluetoothFragment_OTA.newInstance(_bluetoothDevice, map);
                //                    //不要使用replace,不然前面的Fragment被释放了会连蓝牙也关掉
                //                    getFragmentManager().beginTransaction().add(R.id.fragment_bluetooth, _bluetoothFragment_ota, "bluetoothFragment_ota").commitNow();

                //启动第三方apk
                //getFragmentManager().beginTransaction().hide(BluetoothFragment_List.this)
                // .commitNow();
                Intent intent = GetAppOpenIntentByPackageName(_context, "com.ambiqmicro.android.amota");
                if (intent != null) {
                    BLEUtil.StopScanLe();
                    _context.startActivity(intent);
                } else {
                    ProgressDialogUtil.ShowWarning(_context, "警告", "未安装OTA_ZM301,无法使用该功能!");
                }
                break;
            //...
            case R.id.menu_2_setting:
                break;
        }
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.chk_filter:
                _isHideConnectSuccessDevice = isChecked;
                ShowSetFilterContent();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClearFilterContent_filter:
                _filterName = "";
                _filterAddress = "";
                _filterRssi = 100;
                _isHideConnectSuccessDevice = false;
                _filterContent = " No filter";
                _editName.setText("");
                _editAddress.setText("");
                //进度条的数值 = Math.abs(信号强度) - 40
                _seekBar.setProgress(Math.abs(_filterRssi) - 40);
                _chkHideDevice.setChecked(false);
                //清空连接成功的设备
                _connectSuccessMap.clear();
                DeviceFilterShared.SetFilterName(_context, _filterName);
                DeviceFilterShared.SetFilterAddress(_context, _filterAddress);
                DeviceFilterShared.SetFilterRssi(_context, _filterRssi);
                DeviceFilterShared.SetFilterDevie(_context, _isHideConnectSuccessDevice);
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
                    InputMethodManager imm = (InputMethodManager) _context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(_btnFilterContent.getApplicationWindowToken(), 0);
                    _btnFilterContent.setCompoundDrawables(null, null, _drawableDown, null);
                    _filterName = _editName.getText().toString();
                    _filterAddress = _editAddress.getText().toString();
                    DeviceFilterShared.SetFilterName(_context, _filterName);
                    DeviceFilterShared.SetFilterAddress(_context, _filterAddress);
                    DeviceFilterShared.SetFilterRssi(_context, _filterRssi);
                    DeviceFilterShared.SetFilterDevie(_context, _isHideConnectSuccessDevice);
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
        _myBluetoothDevice = _deviceMap.get(address);
        //BlueNRG
        if (_rdoUUID1.isChecked()) {
            SERVICE_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
            WRITE_UUID = UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb");
            READ_UUID = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");
        }
        //Amdtp
        else if (_rdoUUID2.isChecked()) {
            SERVICE_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e1011");
            WRITE_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e0011");
            READ_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e0012");
        }
        //OTA
        else if (_rdoUUID3.isChecked()) {
            SERVICE_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e1001");
            WRITE_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e0001");
            READ_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e0002");
        }
        //Tag
        else if (_rdoUUID4.isChecked()) {
            SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
            READ_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
            WRITE_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
        }
        Map<String, UUID> map = new HashMap<>();
        map.put("SERVICE_UUID", SERVICE_UUID);
        map.put("READ_UUID", READ_UUID);
        map.put("WRITE_UUID", WRITE_UUID);
        map.put("CONFIG_UUID", CONFIG_UUID);
        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
            //电力控制
            if (_rdoFunc1.isChecked()) {
                _bluetoothFragment_powerControl = BluetoothFragment_PowerControl.newInstance(_powerControlListener, bluetoothDevice, map);
                //不要使用replace,不然前面的Fragment被释放了会连蓝牙也关掉
                getFragmentManager().beginTransaction().add(R.id.fragment_bluetooth, _bluetoothFragment_powerControl, "bluetoothFragment_powerControl").commitNow();
                getFragmentManager().beginTransaction().hide(BluetoothFragment_List.this).commitNow();
            }
            //命令测试
            else if (_rdoFunc2.isChecked()) {
                _bluetoothFragment_commandTest = BluetoothFragment_CommandTest.newInstance(bluetoothDevice, map);
                //不要使用replace,不然前面的Fragment被释放了会连蓝牙也关掉
                getFragmentManager().beginTransaction().add(R.id.fragment_bluetooth, _bluetoothFragment_commandTest, "bluetoothFragment_commandTest").commitNow();
                getFragmentManager().beginTransaction().hide(BluetoothFragment_List.this).commitNow();
            }
            //设备绑定入库
            else if (_rdoFunc3.isChecked()) {
                if (_rdoUUID4.isChecked()) {
                    _bluetoothFragment_db = BluetoothFragment_DB.newInstance(bluetoothDevice, map, _deviceMap.get(address).getRssi());
                    //不要使用replace,不然前面的Fragment被释放了会连蓝牙也关掉
                    getFragmentManager().beginTransaction().add(R.id.fragment_bluetooth, _bluetoothFragment_db, "bluetoothFragment_db").commitNow();
                    getFragmentManager().beginTransaction().hide(BluetoothFragment_List.this).commitNow();
                } else {
                    ProgressDialogUtil.ShowWarning(_context, "错误", "【物料入库】的功能仅支持【Tag】模块");
                }
            }
            //测量体温
            else if (_rdoFunc4.isChecked()) {
                _bluetoothFragment_temperature = BluetoothFragment_Temperature.newInstance(_temperatureListener, bluetoothDevice, map);
                //不要使用replace,不然前面的Fragment被释放了会连蓝牙也关掉
                getFragmentManager().beginTransaction().add(R.id.fragment_bluetooth, _bluetoothFragment_temperature, "bluetoothFragment_temperature").commitNow();
                getFragmentManager().beginTransaction().hide(BluetoothFragment_List.this).commitNow();
            }
        } else {
            ProgressDialogUtil.ShowWarning(_context, "错误", "请检查该设备是否被占用");
        }
    }

    /**
     * 开启本地蓝牙
     */
    private void OpenBluetoothAdapter() {
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (_bluetoothAdapter != null) {
            //未打开蓝牙,才需要打开蓝牙
            //会以Dialog样式显示一个Activity,我们可以在onActivityResult()方法去处理返回值
            if (!_bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, 1);
            } else {
                switch (_bluetoothAdapter.getState()) {
                    //蓝牙已开启则直接开始扫描设备
                    case BluetoothAdapter.STATE_ON:
                    case BluetoothAdapter.STATE_TURNING_ON:
                        _toolbar.setNavigationIcon(R.drawable.stop1);
                        _bluetoothLeScanner = _bluetoothAdapter.getBluetoothLeScanner();
                        BLEUtil.Init(_context, this, _bluetoothAdapter, _bluetoothLeScanner);
                        break;
                    //蓝牙未开启
                    case BluetoothAdapter.STATE_OFF:
                    case BluetoothAdapter.STATE_TURNING_OFF:
                    default:
                        _toolbar.setNavigationIcon(R.drawable.start1);
                        break;
                }
            }
        } else {
            ProgressDialogUtil.ShowWarning(_context, "错误", "该设备不支持BLE");
        }
    }

    private void onButtonPressed(Uri uri) {
        if (_onFragmentInteractionListener != null)
            _onFragmentInteractionListener.onFragmentInteraction(uri);
    }

    private void BeginScan() {
        if (BLEUtil.StartScanLe() == 1) {
            _isStartOrStopScan = true;
            _gifImageView.setVisibility(View.VISIBLE);
            _toolbar.setNavigationIcon(R.drawable.stop1);
        } else {
            ProgressDialogUtil.ShowWarning(_context, "提示", "请确认系统蓝牙是否开启");
        }
    }

    private void StopScan() {
        BLEUtil.StopScanLe();
        _isStartOrStopScan = false;
        _gifImageView.setVisibility(View.INVISIBLE);
        _toolbar.setNavigationIcon(R.drawable.start1);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            _param1 = getArguments().getString(ARG_PARAM1);
            _param2 = getArguments().getString(ARG_PARAM2);
        }
        _context = getContext();
        _filterContent = " No filter";
        _filterName = DeviceFilterShared.GetFilterName(_context);
        _filterAddress = DeviceFilterShared.GetFilterAddress(_context);
        _filterRssi = DeviceFilterShared.GetFilterRssi(_context);
        _isHideConnectSuccessDevice = DeviceFilterShared.GetFilterDevice(_context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _view = inflater.inflate(R.layout.fragment_bluetooth_list, container, false);
        _bluetoothListAdapter = new BluetoothListAdapter(_context);
        _onFragmentInteractionListener.onFragmentInteraction(Uri.parse("content://com.zistone.blecontrol/list"));
        //强制获得焦点
        _view.requestFocus();
        _view.setFocusable(true);
        _view.setFocusableInTouchMode(true);
        _view.setOnKeyListener(backListener);
        _toolbar = _view.findViewById(R.id.toolbar_bluetoothList);
        //加上这句,才会调用Fragment的ToolBar,否则调用的是Activity传递过来的
        setHasOptionsMenu(true);
        //去掉标题
        _toolbar.setTitle("");
        //此处强转,必须是Activity才有这个方法
        ((MainActivity) getActivity()).setSupportActionBar(_toolbar);
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
        _materialRefreshLayout = _view.findViewById(R.id.refresh_bluetoothList);
        //启用加载更多
        _materialRefreshLayout.setLoadMore(false);
        //自动刷新
        _materialRefreshLayout.autoRefresh();
        //使用线性布局
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(_context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //强制获得焦点
        _view.requestFocus();
        _view.setFocusable(true);
        _view.setFocusableInTouchMode(true);
        _view.setOnKeyListener(backListener);
        _rdoUUID1 = _view.findViewById(R.id.rdo_uuid1_bluetoothList);
        _rdoUUID2 = _view.findViewById(R.id.rdo_uuid2_bluetoothList);
        _rdoUUID3 = _view.findViewById(R.id.rdo_uuid3_bluetoothList);
        _rdoUUID4 = _view.findViewById(R.id.rdo_uuid4_bluetoothList);
        _rdoFunc1 = _view.findViewById(R.id.rdo_func1_bluetoothList);
        _rdoFunc2 = _view.findViewById(R.id.rdo_func2_bluetoothList);
        _rdoFunc3 = _view.findViewById(R.id.rdo_func3_bluetoothList);
        _rdoFunc4 = _view.findViewById(R.id.rdo_func4_bluetoothList);
        _listView = _view.findViewById(R.id.lv_bluetoothList);
        _btnFilterContent = _view.findViewById(R.id.btnFilterContent_filter);
        _btnFilterContent.setOnClickListener(this::onClick);
        _drawableUp = getResources().getDrawable(R.drawable.up1, null);
        _drawableUp.setBounds(0, 0, _drawableUp.getMinimumWidth(), _drawableUp.getMinimumHeight());
        _drawableDown = getResources().getDrawable(R.drawable.down1, null);
        _drawableDown.setBounds(0, 0, _drawableDown.getMinimumWidth(), _drawableDown.getMinimumHeight());
        _btnClearContentFilter = _view.findViewById(R.id.btnClearFilterContent_filter);
        _btnClearContentFilter.setOnClickListener(this::onClick);
        _row2 = _view.findViewById(R.id.row2_filter);
        _row3 = _view.findViewById(R.id.row3_filter);
        _row4 = _view.findViewById(R.id.row4_filter);
        _row5 = _view.findViewById(R.id.row5_filter);
        _btnClearNameFilter = _view.findViewById(R.id.btnClearName_filter);
        _btnClearNameFilter.setOnClickListener(this::onClick);
        _btnClearAddressFilter = _view.findViewById(R.id.btnClearAddress_filter);
        _btnClearAddressFilter.setOnClickListener(this::onClick);
        _editName = _view.findViewById(R.id.editName_filter);
        if (!_filterName.trim().equals(""))
            _editName.setText(_filterName);
        _editName.addTextChangedListener(this);
        _editAddress = _view.findViewById(R.id.editAddress_filter);
        if (!_filterAddress.trim().equals(""))
            _editAddress.setText(_filterAddress);
        _editAddress.addTextChangedListener(this);
        _seekBar = _view.findViewById(R.id.seekBar_filter);
        //进度条的最大数值为60,信号强度 = (数值+40) * -1
        _seekBar.setMax(60);
        //进度条的数值 = Math.abs(信号强度) - 40
        _seekBar.setProgress(Math.abs(_filterRssi) - 40);
        _seekBar.setOnSeekBarChangeListener(this);
        _txtRssi = _view.findViewById(R.id.tvRssi_filter);
        _txtRssi.setText(_filterRssi * -1 + "dBm");
        _chkHideDevice = _view.findViewById(R.id.chk_filter);
        _chkHideDevice.setChecked(_isHideConnectSuccessDevice);
        _chkHideDevice.setOnCheckedChangeListener(this::onCheckedChanged);
        ShowSetFilterContent();
        //所有的控件、对象都实例化后再初始化回调方法
        InitListener();
        //设置监听在后
        _materialRefreshLayout.setMaterialRefreshListener(_materialRefreshListener);
        //控件、对象、事件监听都加载完毕后才开始扫描蓝牙设备
        OpenBluetoothAdapter();
        return _view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                //用户授权开启蓝牙
                if (requestCode != 0) {
                    _bluetoothLeScanner = _bluetoothAdapter.getBluetoothLeScanner();
                    BLEUtil.Init(_context, this, _bluetoothAdapter, _bluetoothLeScanner);
                    BeginScan();
                }
                //用户拒绝开启蓝牙
                else {
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
            _onFragmentInteractionListener = (OnFragmentInteractionListener) context;
        else
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }

    @Override
    public void onStop() {
        super.onStop();
        StopScan();
    }

    @Override
    public void onDestroy() {
        StopScan();
        _bluetoothAdapter.disable();
        if (_deviceList != null)
            _deviceList.clear();
        if (_deviceMap != null)
            _deviceMap.clear();
        if (_connectSuccessMap != null)
            _connectSuccessMap.clear();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        _onFragmentInteractionListener = null;
        super.onDetach();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //不在最前端显示
        if (hidden) {
            int a = 1;
        }
        //在最前端显示
        else {
            int b = 2;
        }
    }

    /**
     * 当fragment结合viewpager使用时,这个方法会调用
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        //界面可见
        if (isVisibleToUser) {
            int a = 1;
        }
        //界面不可见
        else {
            int b = 2;
        }
    }

}
