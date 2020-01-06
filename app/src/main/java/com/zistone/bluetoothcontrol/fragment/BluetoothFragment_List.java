package com.zistone.bluetoothcontrol.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.CheckBox;
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
    public static final String TAG = "BluetoothFragment_List";
    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    //已知服务
    private static UUID SERVICE_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    //写入特征的UUID
    private static UUID WRITE_UUID = UUID.fromString("0000ff03-0000-1000-8000-00805f9b34fb");
    //读取特征的UUID
    private static UUID READ_UUID = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");
    //客户端特征配置
    private static UUID CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private BluetoothListAdapter m_bluetoothListAdapter;
    public Context m_context;
    public View m_view;
    private Toolbar m_toolbar;
    public OnFragmentInteractionListener m_listener;
    public CheckBox m_checkBox;
    public ListView m_listView;
    public BluetoothAdapter m_bluetoothAdapter;
    //蓝牙设备的集合
    public List<BluetoothDevice> m_deviceList = new ArrayList<>();
    public BluetoothReceiver m_bluetoothReceiver;
    public BluetoothDevice m_bluetoothDevice;
    public BluetoothFragment_CommandTest m_bluetoothFragment_commandTest;
    private BluetoothFragment_CommandTest.Callback m_commandTestCallback;
    public BluetoothFragment_PowerControl m_bluetoothFragment_powerControl;
    private BluetoothFragment_PowerControl.Callback m_powerControlCallback;
    public BluetoothFragment_OTA m_bluetoothFragment_ota;
    //下拉刷新控件
    private MaterialRefreshLayout m_materialRefreshLayout;
    private RadioGroup m_radioGroup1, m_radioGroup2;
    private RadioButton m_radioButton1, m_radioButton2, m_radioButton3, m_radioButton4, m_radioButton5;
    private long m_exitTime = 0;
    private DialogFragment_DeviceFilter m_dialogFragment_deviceFilter;
    private DialogFragment_DeviceFilter.Callback m_deviceFilterCallback;
    private String m_param1, m_param2;
    //根据名称筛选设备、根据地址过滤设备
    private List<String> m_filterNameList = new ArrayList<>(), m_filterAddressList = new ArrayList<>();
    private boolean m_isHideConnectedDevice = false;

    private View.OnKeyListener backListener = (v, keyCode, event) ->
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

    public static BluetoothFragment_List newInstance(String param1, String param2)
    {
        BluetoothFragment_List fragment = new BluetoothFragment_List();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void InitListener()
    {
        //命令测试的回调
        m_commandTestCallback = new BluetoothFragment_CommandTest.Callback()
        {
            @Override
            public void IsConnectSuccess()
            {
                //将连接成功的设备地址存起来,用来过滤.
                m_filterAddressList.add(m_bluetoothDevice.getAddress());
                //过滤设备后重新绑定适配器
                m_bluetoothListAdapter.SetM_list(FilterDeviceByCondition(m_deviceList));
                m_listView.setAdapter(m_bluetoothListAdapter);
                //适配器的数据改变后更新ListView
                m_bluetoothListAdapter.notifyDataSetChanged();
            }
        };

        //设备筛选的回调
        m_deviceFilterCallback = new DialogFragment_DeviceFilter.Callback()
        {
            @Override
            public void OnlyShowSetDeviceCallback(List<String> list)
            {
                m_filterNameList = list;
                Toast.makeText(m_context, "保存成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void HideConnectedDevice(boolean flag)
            {
                m_isHideConnectedDevice = flag;
                if(!m_isHideConnectedDevice)
                {
                    m_filterAddressList.clear();
                }
            }
        };

        //电力控制的回调
        m_powerControlCallback = new BluetoothFragment_PowerControl.Callback()
        {
            @Override
            public void IsConnectSuccess()
            {
                //将连接成功的设备地址存起来,用来过滤.
                m_filterAddressList.add(m_bluetoothDevice.getAddress());
                //过滤设备后重新绑定适配器
                m_bluetoothListAdapter.SetM_list(FilterDeviceByCondition(m_deviceList));
                m_listView.setAdapter(m_bluetoothListAdapter);
                //适配器的数据改变后更新ListView
                m_bluetoothListAdapter.notifyDataSetChanged();
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

    public Handler handler = new Handler()
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
    public Intent GetAppOpenIntentByPackageName(Context context, String packageName)
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
        if(!m_checkBox.isChecked())
        {
            Toast.makeText(m_context, "请先开启蓝牙", Toast.LENGTH_SHORT).show();
            return false;
        }
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
                m_dialogFragment_deviceFilter = DialogFragment_DeviceFilter.newInstance(m_deviceFilterCallback, "");
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
            case R.id.ck_bluetooth_bluetoothlist:
            {
                if(isChecked == true)
                {
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent, 1);
                    BeginDiscovery();
                }
                else
                {
                    CancelDiscovery();
                    m_bluetoothAdapter.disable();
                    m_deviceList.clear();
                    m_bluetoothListAdapter.SetM_list(m_deviceList);
                    m_listView.setAdapter(m_bluetoothListAdapter);
                }
                break;
            }
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        handler.postDelayed(runnable, 0);
        m_bluetoothReceiver = new BluetoothReceiver();
        //注册广播
        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        foundFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        foundFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        m_context.registerReceiver(m_bluetoothReceiver, foundFilter);
    }

    /**
     * 重写onRequestPermissionsResult方法
     * 获取动态权限请求的结果,再开启蓝牙
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch(requestCode)
        {
            case 1:
                switch(grantResults[0])
                {
                    case PackageManager.PERMISSION_GRANTED:
                        switch(m_bluetoothAdapter.getState())
                        {
                            case BluetoothAdapter.STATE_ON:
                            case BluetoothAdapter.STATE_TURNING_ON:
                                m_checkBox.setChecked(true);
                                break;
                            case BluetoothAdapter.STATE_OFF:
                            case BluetoothAdapter.STATE_TURNING_OFF:
                            default:
                                m_checkBox.setChecked(false);
                                break;
                        }
                        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if(m_bluetoothAdapter == null)
                        {
                            m_checkBox.setChecked(false);
                            Toast.makeText(m_context, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                break;
            case 2:
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onClick(View v)
    {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        //连接设备前先关闭扫描蓝牙,否则连接成功后再次扫描会发生阻塞,导致扫描不到设备
        CancelDiscovery();
        String address = m_deviceList.get(position).getAddress();
        m_bluetoothDevice = m_bluetoothAdapter.getRemoteDevice(address);
        m_bluetoothListAdapter.SetM_clickItemAddress(address);
        //如果没有开启设备连接成功后隐藏的选项则,给选中的蓝牙设备加上选中效果,因为该选项开启后选中的设备会被隐藏,效果就会显示在下一个设备上.
        if(!m_isHideConnectedDevice)
        {
            //m_bluetoothListAdapter.SetM_isClick(true);
        }
        m_bluetoothListAdapter.notifyDataSetChanged();
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
                m_bluetoothFragment_powerControl = BluetoothFragment_PowerControl.newInstance(m_powerControlCallback, m_bluetoothDevice, map);
                //不要使用replace,不然前面的Fragment被释放了会连蓝牙也关掉
                getFragmentManager().beginTransaction().add(R.id.fragment_bluetooth, m_bluetoothFragment_powerControl, "bluetoothFragment_powerControl").commitNow();
                getFragmentManager().beginTransaction().hide(BluetoothFragment_List.this).commitNow();
            }
            //命令测试
            else if(m_radioButton4.isChecked())
            {
                m_bluetoothFragment_commandTest = BluetoothFragment_CommandTest.newInstance(m_commandTestCallback, m_bluetoothDevice, map);
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

    /**
     * Activity中加载Fragment时会要求实现onFragmentInteraction(Uri uri)方法,此方法主要作用是从fragment向activity传递数据
     */
    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    public void onButtonPressed(Uri uri)
    {
        if(m_listener != null)
        {
            m_listener.onFragmentInteraction(uri);
        }
    }

    /**
     * 异步搜索蓝牙设备
     */
    public Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            BeginDiscovery();
            handler.postDelayed(this, 500);
        }
    };

    /**
     * 开始搜索蓝牙
     */
    public void BeginDiscovery()
    {
        //先清空适配器
        m_deviceList.clear();
        m_bluetoothListAdapter.SetM_list(m_deviceList);
        m_listView.setAdapter(m_bluetoothListAdapter);
        if(m_bluetoothAdapter.isDiscovering())
        {
            m_bluetoothAdapter.cancelDiscovery();
        }
        //startDiscovery虽然兼容经典蓝牙和低功耗蓝牙,但有些设备无法检测到低功耗蓝牙
        m_bluetoothAdapter.startDiscovery();
    }

    /**
     * 取消搜索蓝牙
     */
    public void CancelDiscovery()
    {
        if(m_bluetoothAdapter.isDiscovering())
        {
            m_bluetoothAdapter.cancelDiscovery();
        }
        handler.removeCallbacks(runnable);
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
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
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
        if(ContextCompat.checkSelfPermission(m_context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);
        }
        //动态注册注册广播接收器,接收蓝牙发现讯息
        IntentFilter btFilter = new IntentFilter();
        btFilter.setPriority(10);
        btFilter.addAction(BluetoothDevice.ACTION_FOUND);
        btFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        //获取蓝牙适配器
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(m_bluetoothAdapter == null)
        {
            Toast.makeText(m_context, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
        }
        m_listener.onFragmentInteraction(Uri.parse("content://com.zistone.bluetoothcontrol/list"));
        //下拉刷新控件
        m_materialRefreshLayout = m_view.findViewById(R.id.refresh_bluetoothlist);
        //启用加载更多
        m_materialRefreshLayout.setLoadMore(false);
        m_materialRefreshLayout.setMaterialRefreshListener(new MaterialRefreshListener()
        {
            /**
             * 下拉刷新
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
        m_checkBox = m_view.findViewById(R.id.ck_bluetooth_bluetoothlist);
        m_radioButton1 = m_view.findViewById(R.id.radioButton1_bluetoothlist);
        m_radioButton2 = m_view.findViewById(R.id.radioButton2_bluetoothlist);
        m_radioButton3 = m_view.findViewById(R.id.radioButton3_bluetoothlist);
        m_radioButton4 = m_view.findViewById(R.id.radioButton4_bluetoothlist);
        m_radioButton5 = m_view.findViewById(R.id.radioButton5_bluetoothlist);
        m_listView = m_view.findViewById(R.id.lv_bluetoothlist);
        switch(m_bluetoothAdapter.getState())
        {
            case BluetoothAdapter.STATE_ON:
            case BluetoothAdapter.STATE_TURNING_ON:
                m_checkBox.setChecked(true);
                break;
            case BluetoothAdapter.STATE_OFF:
            case BluetoothAdapter.STATE_TURNING_OFF:
            default:
                m_checkBox.setChecked(false);
                break;
        }
        m_checkBox.setOnCheckedChangeListener(this);
        m_bluetoothListAdapter = new BluetoothListAdapter(m_context);
        m_filterNameList = DeviceFilterShared.GetFilterName(m_context);
        m_isHideConnectedDevice = DeviceFilterShared.GetFilterDevice(m_context);
        InitListener();
        return m_view;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener)
        {
            m_listener = (OnFragmentInteractionListener) context;
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
        m_listener = null;
        m_deviceList.clear();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        CancelDiscovery();
        m_context.unregisterReceiver(m_bluetoothReceiver);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        CancelDiscovery();
        m_bluetoothAdapter.disable();
        m_deviceList.clear();
        m_bluetoothListAdapter.SetM_list(m_deviceList);
        m_listView.setAdapter(m_bluetoothListAdapter);
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

    public class BluetoothReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            //获得已经搜索到的蓝牙设备
            if(action.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(!m_deviceList.contains(device.getAddress()))
                {
                    m_deviceList.add(device);
                }
                m_bluetoothListAdapter.SetM_list(FilterDeviceByCondition(m_deviceList));
                m_listView.setAdapter(m_bluetoothListAdapter);
                m_listView.setOnItemClickListener(BluetoothFragment_List.this);
            }
            //蓝牙设备搜索完成
            else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
            {
                handler.removeCallbacks(runnable);
            }
            else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //正在配对
                if(device.getBondState() == BluetoothDevice.BOND_BONDING)
                {
                }
                //完成配对
                else if(device.getBondState() == BluetoothDevice.BOND_BONDED)
                {
                }
                //取消配对
                else if(device.getBondState() == BluetoothDevice.BOND_NONE)
                {
                }
            }
        }
    }

}
