package com.zistone.bluetoothcontrol.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.zistone.bluetoothcontrol.MainActivity;
import com.zistone.bluetoothcontrol.R;
import com.zistone.bluetoothcontrol.control.BluetoothListAdapter;
import com.zistone.bluetoothcontrol.dialogfragment.DialogFragment_DeviceFilter;
import com.zistone.bluetoothcontrol.util.DeviceFilterShared;
import com.zistone.material_refresh_layout.MaterialRefreshLayout;
import com.zistone.material_refresh_layout.MaterialRefreshListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BluetoothFragment_List extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener
{
    private static final String TAG = "BluetoothFragment_List";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //已知服务
    private static UUID SERVICE_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    //写入特征的UUID
    private static UUID WRITE_UUID = UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb");
    //读取特征的UUID
    private static UUID READ_UUID = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");
    //客户端特征配置
    private static UUID CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private BluetoothListAdapter m_bluetoothListAdapter;
    private Context m_context;
    private View m_view;
    private Toolbar m_toolbar;
    private OnFragmentInteractionListener m_onFragmentInteractionListener;
    private Button m_btn1;
    private ListView m_listView;
    private BluetoothAdapter m_bluetoothAdapter;
    //蓝牙设备的集合
    private List<BluetoothDevice> m_deviceList = new ArrayList<>();
    private BluetoothDevice m_bluetoothDevice;
    private BluetoothFragment_CommandTest m_bluetoothFragment_commandTest;
    private BluetoothFragment_CommandTest.Listener m_commandTestListener;
    private BluetoothFragment_PowerControl m_bluetoothFragment_powerControl;
    private BluetoothFragment_PowerControl.Listener m_powerControlListener;
    private BluetoothFragment_OTA m_bluetoothFragment_ota;
    //下拉刷新控件
    private MaterialRefreshLayout m_materialRefreshLayout;
    private RadioGroup m_radioGroup1, m_radioGroup2;
    private RadioButton m_radioButton1, m_radioButton2, m_radioButton3, m_radioButton4, m_radioButton5;
    private long m_exitTime = 0;
    private DialogFragment_DeviceFilter m_dialogFragment_deviceFilter;
    private DialogFragment_DeviceFilter.Listener m_deviceFilterListener;
    private String m_param1, m_param2;
    //根据名称筛选设备、根据地址过滤设备
    private List<String> m_filterNameList = new ArrayList<>(), m_filterAddressList = new ArrayList<>();
    private boolean m_isHideConnectedDevice = false;
    private Map<String, Integer> m_rssiMap = new HashMap<>();
    //BLE的扫描,与不一样startDiscovery()是有区别的
    private BluetoothLeScanner m_bluetoothLeScanner;
    //筛选条件,可以设置名称、地址、UUID
    private List<ScanFilter> m_scanFilterList = new ArrayList<ScanFilter>()
    {{
        ScanFilter.Builder filter = new ScanFilter.Builder();
        ParcelUuid parcelUuid = ParcelUuid.fromString("00002760-08c2-11e1-9073-0e8ac72e0002");
        filter.setServiceUuid(parcelUuid);
        this.add(filter.build());

    }};
    //扫描设置,可以设置扫描模式、时间、类型、结果等
    //模式:
    //  SCAN_MODE_LOW_LATENCY:扫描优先
    //  SCAN_MODE_LOW_POWER:省电优先
    //  SCAN_MODE_BALANCED:平衡模式
    //  SCAN_MODE_OPPORTUNISTIC:这是一个特殊的扫描模式（投机取巧的）,就是说程序本身不会使用BLE扫描功能,而是借助其他的扫描结果.比如:程序A用了这个模式,其实程序A没有使用到蓝牙功能,但是程序B在扫描的话,程序B的扫描结果会共享给程序A
    //时间:
    //  扫描到设置时间后执行onBatchScanResults的回调
    private ScanSettings m_scanSettings = new ScanSettings.Builder().setReportDelay(15 * 1000).setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

    /**
     * Activity中加载Fragment时会要求实现onFragmentInteraction(Uri uri)方法,此方法主要作用是从fragment向activity传递数据
     */
    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    public static BluetoothFragment_List newInstance(String param1, String param2)
    {
        BluetoothFragment_List fragment = new BluetoothFragment_List();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private final ScanCallback scanCallback = new ScanCallback()
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            String address = device.getAddress();
            int rssi = result.getRssi();
            if(!m_deviceList.contains(device))
            {
                m_deviceList.add(device);
                m_bluetoothListAdapter.SetM_list(FilterDeviceByCondition(m_deviceList));
            }
            Log.i(TAG, String.format("设备%s的信号强度%d", address, rssi));
            m_rssiMap.put(address, rssi);
            m_bluetoothListAdapter.SetM_rssiMap(m_rssiMap);
            //使用notifyDataSetChanged()会保存当前的状态信息,然后更新适配器里的内容
            m_bluetoothListAdapter.notifyDataSetChanged();
            m_listView.setOnItemClickListener(BluetoothFragment_List.this);
            //根据设备地址找设备在m_deviceList中的下标
            //            for(BluetoothDevice tempDevice : m_deviceList)
            //            {
            //                if(address.equals(tempDevice.getAddress()))
            //                {
            //                    //使用局部刷新
            //                    int itemIndex = m_deviceList.indexOf(tempDevice);
            //                    UpdateListView(itemIndex);
            //                    break;
            //                }
            //            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode)
        {
            super.onScanFailed(errorCode);
            //扫描失败返回的参数有4个
            //errorCode=1:已启动具有相同设置的BLE扫描
            //errorCode=2:应用未注册
            //errorCode=3:内部错误
            //errorCode=4:设备不支持低功耗蓝牙
        }
    };

    private final View.OnKeyListener backListener = (v, keyCode, event) ->
    {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            if((System.currentTimeMillis() - m_exitTime) > 2000)
            {
                Toast.makeText(getActivity(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                m_exitTime = System.currentTimeMillis();
            }
            else
            {
                getActivity().finish();
                System.exit(0);
            }
            return true;
        }
        return false;
    };

    /**
     * 局部刷新ListView
     *
     * @param itemIndex
     */
    private void UpdateListView(int itemIndex)
    {
        //ListView没有加载完时,getFirstVisiblePosition和getLastVisiblePosition获取的始终为0和-1
        m_listView.post(new Runnable()
        {
            @Override
            public void run()
            {
                //第一个可显示控件的位置
                int firstVisiblePosition = m_listView.getFirstVisiblePosition();
                int lastVisiblePosition = m_listView.getLastVisiblePosition();
                //当要更新View在可见的位置时才更新
                if(itemIndex > firstVisiblePosition && itemIndex <= lastVisiblePosition)
                {
                    View view = m_listView.getChildAt(itemIndex - firstVisiblePosition);
                    m_bluetoothListAdapter.UpdateView(view, itemIndex);
                }
            }
        });
    }

    /**
     * 检查蓝牙适配器是否打开
     * 在开始扫描和取消扫描的都时候都需要判断适配器的状态以及是否获取到扫描器,否则将抛出异常IllegalStateException: BT Adapter is not turned
     * ON.
     *
     * @return
     */
    private boolean IsBluetoothAvailable()
    {
        return (m_bluetoothLeScanner != null && m_bluetoothAdapter != null && m_bluetoothAdapter.isEnabled() && m_bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON);
    }

    /**
     * 统一初始化监听
     */
    private void InitListener()
    {
        //命令测试的回调
        m_commandTestListener = new BluetoothFragment_CommandTest.Listener()
        {
            @Override
            public void ConnectSuccessListener()
            {
                //将连接成功的设备地址存起来,用来过滤.
                m_filterAddressList.add(m_bluetoothDevice.getAddress());
            }
        };

        //设备筛选的回调
        m_deviceFilterListener = new DialogFragment_DeviceFilter.Listener()
        {
            @Override
            public void OnlyShowSetDeviceListener(List<String> list)
            {
                m_filterNameList = list;
                Toast.makeText(m_context, "保存成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void HideConnectedDeviceListener(boolean flag)
            {
                m_isHideConnectedDevice = flag;
                if(!m_isHideConnectedDevice)
                {
                    m_filterAddressList.clear();
                }
            }
        };

        //电力控制的回调
        m_powerControlListener = new BluetoothFragment_PowerControl.Listener()
        {
            @Override
            public void ConnectSuccessListener()
            {
                //将连接成功的设备地址存起来,用来过滤.
                m_filterAddressList.add(m_bluetoothDevice.getAddress());
            }
        };

    }

    /**
     * 根据设置选项过滤扫描到蓝牙设备
     *
     * @param list
     * @return
     */
    private List<BluetoothDevice> FilterDeviceByCondition(List<BluetoothDevice> list)
    {
        //设置的设备名称相同的蓝牙设备
        if(m_filterNameList != null && m_filterNameList.size() > 0)
        {
            list = OnlyShowSetNameSameDevice(list);
        }
        //隐藏了连接成功的蓝牙设备
        if(m_isHideConnectedDevice)
        {
            list = HideConnectedDevice(list);
        }
        return list;
    }

    /**
     * 隐藏已经连接成功的蓝牙设备
     * <p>
     * 该方法在接口回调里也有调用,在过滤设备（连接成功后该设备不再显示）后,不要调用会导致BluetoothDevice对象被删除的方法,因为该对象可能正在被使用!
     *
     * @param list 扫描到的蓝牙设备
     * @return
     */
    private List<BluetoothDevice> HideConnectedDevice(List<BluetoothDevice> list)
    {
        List<BluetoothDevice> resultList = new ArrayList<>();
        for(BluetoothDevice tempDevice : list)
        {
            boolean flag = false;
            String address = tempDevice.getAddress();
            for(String tempAddress : m_filterAddressList)
            {
                //地址相同的过滤掉(不是销毁对象)
                if(address.equals(tempAddress))
                {
                    flag = true;
                    break;
                }
            }
            if(!flag)
            {
                resultList.add(tempDevice);
            }
        }
        return resultList;
    }

    /**
     * 只显示和设置的设备名称相同的蓝牙设备
     *
     * @param list 扫描到的蓝牙设备
     * @return
     */
    private List<BluetoothDevice> OnlyShowSetNameSameDevice(List<BluetoothDevice> list)
    {
        Iterator<BluetoothDevice> iterator = list.iterator();
        while(iterator.hasNext())
        {
            String name = iterator.next().getName();
            //设备名相同的保留
            boolean flag = false;
            for(String tempName : m_filterNameList)
            {
                //存在没有名称的设备
                if(name != null && name.equals(tempName))
                {
                    flag = true;
                    break;
                }
            }
            if(!flag)
            {
                iterator.remove();
            }
        }
        return list;
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what == 0)
            {
                byte[] readBuf = (byte[]) msg.obj;
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, "handleMessage readMessage=" + readMessage);
                AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
                builder.setTitle("我收到消息啦").setMessage(readMessage).setPositiveButton("确定", null);
                builder.create().show();
            }
        }
    };

    /**
     * 根据包名启动APK
     *
     * @param context
     * @param packageName
     * @return
     */
    private Intent GetAppOpenIntentByPackageName(Context context, String packageName)
    {
        String mainAct = null;
        PackageManager pkgMag = context.getPackageManager();
        //ACTION_MAIN是隐藏启动的action, 你也可以自定义
        Intent intent = new Intent(Intent.ACTION_MAIN);
        //CATEGORY_LAUNCHER有了这个,你的程序就会出现在桌面上
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        //FLAG_ACTIVITY_RESET_TASK_IF_NEEDED 按需启动的关键,如果任务队列中已经存在,则重建程序
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

        @SuppressLint("WrongConstant") List<ResolveInfo> list = pkgMag.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
        for(int i = 0; i < list.size(); i++)
        {
            ResolveInfo info = list.get(i);
            if(info.activityInfo.packageName.equals(packageName))
            {
                mainAct = info.activityInfo.name;
                break;
            }
        }
        if(TextUtils.isEmpty(mainAct))
        {
            return null;
        }
        intent.setComponent(new ComponentName(packageName, mainAct));
        return intent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        //Activity的onCreateOptionsMenu会在之前调用,即先Clear一下,这样就只有Fragment自己设置的了
        menu.clear();
        inflater.inflate(R.menu.menu_setting, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        switch(item.getItemId())
        {
            //OTA
            case R.id.menu_1_setting:
                //                    m_bluetoothFragment_ota = BluetoothFragment_OTA.newInstance(m_bluetoothDevice, map);
                //                    //不要使用replace,不然前面的Fragment被释放了会连蓝牙也关掉
                //                    getFragmentManager().beginTransaction().add(R.id.fragment_bluetooth, m_bluetoothFragment_ota, "bluetoothFragment_ota").commitNow();

                //启动第三方apk
                getFragmentManager().beginTransaction().hide(BluetoothFragment_List.this).commitNow();
                Intent intent = GetAppOpenIntentByPackageName(m_context, "com.ambiqmicro.android.amota");
                m_context.startActivity(intent);
                break;
            //设备过滤
            case R.id.menu_2_setting:
                m_dialogFragment_deviceFilter = DialogFragment_DeviceFilter.newInstance(m_deviceFilterListener, "");
                m_dialogFragment_deviceFilter.show(getFragmentManager(), "DialogFragment_OTA");
                break;
            //...
            case R.id.menu_3_setting:
                break;
        }
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        switch(buttonView.getId())
        {
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.btn1_bluetoothlist:
                if(m_btn1.getText().toString().equals("开始扫描"))
                {
                    BeginDiscovery();
                }
                else
                {
                    CancelDiscovery();
                }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        //连接设备前先关闭扫描蓝牙,否则连接成功后再次扫描会发生阻塞,导致扫描不到设备
        CancelDiscovery();
        String address = m_deviceList.get(position).getAddress();
        m_bluetoothDevice = m_bluetoothAdapter.getRemoteDevice(address);
        //BlueNRG
        if(m_radioButton1.isChecked())
        {
            SERVICE_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
            WRITE_UUID = UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb");
            READ_UUID = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");
        }
        //Amdtp
        else if(m_radioButton2.isChecked())
        {
            SERVICE_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e1011");
            WRITE_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e0011");
            READ_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e0012");
        }
        //OTA
        else if(m_radioButton5.isChecked())
        {
            SERVICE_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e1001");
            WRITE_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e0001");
            READ_UUID = UUID.fromString("00002760-08c2-11e1-9073-0e8ac72e0002");
        }
        Map<String, UUID> map = new HashMap<>();
        map.put("SERVICE_UUID", SERVICE_UUID);
        map.put("READ_UUID", READ_UUID);
        map.put("WRITE_UUID", WRITE_UUID);
        map.put("CONFIG_UUID", CONFIG_UUID);
        if(m_bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE)
        {
            //电力控制
            if(m_radioButton3.isChecked())
            {
                m_bluetoothFragment_powerControl = BluetoothFragment_PowerControl.newInstance(m_powerControlListener, m_bluetoothDevice, map);
                //不要使用replace,不然前面的Fragment被释放了会连蓝牙也关掉
                getFragmentManager().beginTransaction().add(R.id.fragment_bluetooth, m_bluetoothFragment_powerControl, "bluetoothFragment_powerControl").commitNow();
                getFragmentManager().beginTransaction().hide(BluetoothFragment_List.this).commitNow();
            }
            //命令测试
            else if(m_radioButton4.isChecked())
            {
                m_bluetoothFragment_commandTest = BluetoothFragment_CommandTest.newInstance(m_commandTestListener, m_bluetoothDevice, map);
                //不要使用replace,不然前面的Fragment被释放了会连蓝牙也关掉
                getFragmentManager().beginTransaction().add(R.id.fragment_bluetooth, m_bluetoothFragment_commandTest, "bluetoothFragment_commandTest").commitNow();
                getFragmentManager().beginTransaction().hide(BluetoothFragment_List.this).commitNow();
            }
        }
        else
        {
            Toast.makeText(m_context, "请检查该设备是否被占用", Toast.LENGTH_SHORT);
        }
    }

    private void onButtonPressed(Uri uri)
    {
        if(m_onFragmentInteractionListener != null)
        {
            m_onFragmentInteractionListener.onFragmentInteraction(uri);
        }
    }

    /**
     * 开始搜索蓝牙
     * 开始扫描时候要确保蓝牙适配器处于开启状态
     */
    private void BeginDiscovery()
    {
        if(IsBluetoothAvailable())
        {
            m_btn1.setText("停止扫描");
            m_bluetoothLeScanner.stopScan(scanCallback);
            m_bluetoothLeScanner.startScan(scanCallback);
        }
        else
        {
            Toast.makeText(m_context, "请确认系统蓝牙是否开启", Toast.LENGTH_SHORT);
        }
    }

    /**
     * 取消搜索蓝牙
     * 取消扫描的时候要确保蓝牙适配器处于开启状态
     */
    private void CancelDiscovery()
    {
        if(IsBluetoothAvailable())
        {
            m_btn1.setText("开始扫描");
            m_bluetoothLeScanner.stopScan(scanCallback);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            m_param1 = getArguments().getString(ARG_PARAM1);
            m_param2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_view = inflater.inflate(R.layout.fragment_bluetooth_list, container, false);
        m_context = getContext();
        //强制获得焦点
        m_view.requestFocus();
        m_view.setFocusable(true);
        m_view.setFocusableInTouchMode(true);
        m_view.setOnKeyListener(backListener);
        m_toolbar = m_view.findViewById(R.id.toolbar_bluetoothlist);
        //加上这句,才会调用Fragment的ToolBar,否则调用的是Activity传递过来的
        setHasOptionsMenu(true);
        //去掉标题
        m_toolbar.setTitle("");
        //此处强转,必须是Activity才有这个方法
        ((MainActivity) getActivity()).setSupportActionBar(m_toolbar);
        //动态注册注册广播接收器,接收蓝牙发现讯息
        IntentFilter btFilter = new IntentFilter();
        btFilter.setPriority(10);
        btFilter.addAction(BluetoothDevice.ACTION_FOUND);
        btFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        m_onFragmentInteractionListener.onFragmentInteraction(Uri.parse("content://com.zistone.bluetoothcontrol/list"));
        //下拉刷新控件
        m_materialRefreshLayout = m_view.findViewById(R.id.refresh_bluetoothlist);
        //启用加载更多
        m_materialRefreshLayout.setLoadMore(false);
        m_materialRefreshLayout.setMaterialRefreshListener(new MaterialRefreshListener()
        {
            /**
             * 下拉刷新
             * 下拉刷新的时候需要清空ListView然后重新绑定
             * @param materialRefreshLayout
             */
            @Override
            public void onRefresh(final MaterialRefreshLayout materialRefreshLayout)
            {
                materialRefreshLayout.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        m_deviceList.clear();
                        m_rssiMap.clear();
                        m_bluetoothListAdapter.SetM_list(m_deviceList);
                        m_bluetoothListAdapter.SetM_rssiMap(m_rssiMap);
                        //使用notifyDataSetChanged()会保存当前的状态信息,然后更新适配器里的内容
                        m_bluetoothListAdapter.notifyDataSetChanged();
                        m_listView.setAdapter(m_bluetoothListAdapter);
                        BeginDiscovery();
                        //结束下拉刷新
                        materialRefreshLayout.finishRefresh();
                    }
                }, 500);
            }

            /**
             * 加载完毕
             */
            @Override
            public void onfinish()
            {
                //Toast.makeText(m_context, "完成", Toast.LENGTH_LONG).show();
            }

            /**
             * 加载更多
             * @param materialRefreshLayout
             */
            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout)
            {
                Toast.makeText(m_context, "别滑了,到底了", Toast.LENGTH_SHORT).show();
            }
        });
        //自动刷新
        m_materialRefreshLayout.autoRefresh();
        //使用线性布局
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(m_context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //强制获得焦点
        m_view.requestFocus();
        m_view.setFocusable(true);
        m_view.setFocusableInTouchMode(true);
        m_view.setOnKeyListener(backListener);
        m_radioGroup1 = m_view.findViewById(R.id.radioGroup1_bluetoothlist);
        m_radioGroup2 = m_view.findViewById(R.id.radioGroup2_bluetoothlist);
        m_btn1 = m_view.findViewById(R.id.btn1_bluetoothlist);
        m_btn1.setOnClickListener(this::onClick);
        m_radioButton1 = m_view.findViewById(R.id.radioButton1_bluetoothlist);
        m_radioButton2 = m_view.findViewById(R.id.radioButton2_bluetoothlist);
        m_radioButton3 = m_view.findViewById(R.id.radioButton3_bluetoothlist);
        m_radioButton4 = m_view.findViewById(R.id.radioButton4_bluetoothlist);
        m_radioButton5 = m_view.findViewById(R.id.radioButton5_bluetoothlist);
        m_listView = m_view.findViewById(R.id.lv_bluetoothlist);
        //获取本地蓝牙适配器
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(m_bluetoothAdapter != null)
        {
            //未打开蓝牙,才需要打开蓝牙
            //会以Dialog样式显示一个Activity,我们可以在onActivityResult()方法去处理返回值
            if(!m_bluetoothAdapter.isEnabled())
            {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, 1);
            }
            else
            {
                switch(m_bluetoothAdapter.getState())
                {
                    //蓝牙已开启则直接开始扫描设备
                    case BluetoothAdapter.STATE_ON:
                    case BluetoothAdapter.STATE_TURNING_ON:
                        m_btn1.setText("停止扫描");
                        m_bluetoothLeScanner = m_bluetoothAdapter.getBluetoothLeScanner();
                        BeginDiscovery();
                        break;
                    //蓝牙未开启
                    case BluetoothAdapter.STATE_OFF:
                    case BluetoothAdapter.STATE_TURNING_OFF:
                    default:
                        m_btn1.setText("开始扫描");
                        break;
                }
            }
        }
        else
        {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
            builder.setPositiveButton("知道了", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    onDestroy();
                    System.exit(0);
                }
            });
            builder.setMessage("设备不支持蓝牙");
            builder.show();
        }
        m_bluetoothListAdapter = new BluetoothListAdapter(m_context);
        m_filterNameList = DeviceFilterShared.GetFilterName(m_context);
        m_isHideConnectedDevice = DeviceFilterShared.GetFilterDevice(m_context);
        InitListener();
        return m_view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case 1:
                //用户授权开启蓝牙
                if(requestCode != 0)
                {
                    m_bluetoothLeScanner = m_bluetoothAdapter.getBluetoothLeScanner();
                    BeginDiscovery();
                }
                //用户拒绝开启蓝牙
                else
                {
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener)
        {
            m_onFragmentInteractionListener = (OnFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        m_onFragmentInteractionListener = null;
        m_deviceList.clear();
        m_rssiMap.clear();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        CancelDiscovery();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        CancelDiscovery();
        m_bluetoothAdapter.disable();
        m_deviceList.clear();
        m_rssiMap.clear();
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);
        //不在最前端显示
        if(hidden)
        {
            int a = 1;
        }
        //在最前端显示
        else
        {
            int b = 2;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);
        //界面可见
        if(isVisibleToUser)
        {
        }
        //界面不可见
        else
        {
        }
    }

}
