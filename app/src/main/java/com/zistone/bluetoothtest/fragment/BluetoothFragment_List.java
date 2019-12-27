package com.zistone.bluetoothtest.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.zistone.bluetoothtest.R;
import com.zistone.bluetoothtest.control.BluetoothListAdapter;
import com.zistone.material_refresh_layout.MaterialRefreshLayout;
import com.zistone.material_refresh_layout.MaterialRefreshListener;

import java.util.ArrayList;
import java.util.HashMap;
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
    public String m_param1;
    public String m_param2;
    public Context m_context;
    public View m_view;
    public OnFragmentInteractionListener m_listener;
    public CheckBox m_checkBox;
    public ListView m_listView;
    public BluetoothAdapter m_bluetoothAdapter;
    public ArrayList<BluetoothDevice> m_deviceList = new ArrayList<>();
    public BluetoothReceiver m_bluetoothReceiver;
    public BluetoothDevice m_bluetoothDevice;
    public BluetoothFragment_CommandTest m_bluetoothFragment_commandTest;
    public BluetoothFragment_PowerControl m_bluetoothFragment_powerControl;
    public BluetoothFragment_OTA m_bluetoothFragment_ota;
    //下拉刷新控件
    private MaterialRefreshLayout m_materialRefreshLayout;
    private RadioGroup m_radioGroup1;
    private RadioGroup m_radioGroup2;
    private RadioButton m_radioButton1;
    private RadioButton m_radioButton2;
    private RadioButton m_radioButton3;
    private RadioButton m_radioButton4;
    private RadioButton m_radioButton5;
    private RadioButton m_radioButton6;
    private long m_exitTime = 0;

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

    public class BluetoothReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            // 获得已经搜索到的蓝牙设备
            if(action.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(!m_deviceList.contains(device.getAddress()))
                {
                    m_deviceList.add(device);
                }
                BluetoothListAdapter adapter = new BluetoothListAdapter(m_context, m_deviceList);
                m_listView.setAdapter(adapter);
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
                    handler.postDelayed(runnable, 100);
                }
                //取消配对
                else if(device.getBondState() == BluetoothDevice.BOND_NONE)
                {
                }
            }
        }
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        switch(buttonView.getId())
        {
            case R.id.ck_bluetooth_bluetoothlist:
            {
                if(isChecked == true)
                {
                    BeginDiscovery();
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent, 1);
                }
                else
                {
                    CancelDiscovery();
                    m_bluetoothAdapter.disable();
                    m_deviceList.clear();
                    BluetoothListAdapter adapter = new BluetoothListAdapter(m_context, m_deviceList);
                    m_listView.setAdapter(adapter);
                }
                break;
            }
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        handler.postDelayed(runnable, 100);
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
        if(requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
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
            m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(m_bluetoothAdapter == null)
            {
                Toast.makeText(m_context, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(m_context, "用户拒绝了权限", Toast.LENGTH_SHORT).show();
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
        m_bluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_deviceList.get(position).getAddress());
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
            //停止搜索蓝牙
            CancelDiscovery();
            //电力控制
            if(m_radioButton3.isChecked())
            {
                m_bluetoothFragment_powerControl = BluetoothFragment_PowerControl.newInstance(m_bluetoothDevice, map);
                //不要使用replace,不然前面的Fragment被释放了会连蓝牙也关掉
                getFragmentManager().beginTransaction().add(R.id.fragment_bluetooth, m_bluetoothFragment_powerControl, "bluetoothFragment_powerControl").commitNow();
                getFragmentManager().beginTransaction().hide(BluetoothFragment_List.this).commitNow();
            }
            //命令测试
            else if(m_radioButton4.isChecked())
            {
                m_bluetoothFragment_commandTest = BluetoothFragment_CommandTest.newInstance(m_bluetoothDevice, map);
                //不要使用replace,不然前面的Fragment被释放了会连蓝牙也关掉
                getFragmentManager().beginTransaction().add(R.id.fragment_bluetooth, m_bluetoothFragment_commandTest, "bluetoothFragment_commandTest").commitNow();
                getFragmentManager().beginTransaction().hide(BluetoothFragment_List.this).commitNow();
            }
            //设备升级
            else if(m_radioButton6.isChecked())
            {
                m_bluetoothFragment_ota = BluetoothFragment_OTA.newInstance(m_bluetoothDevice, map);
                //不要使用replace,不然前面的Fragment被释放了会连蓝牙也关掉
                getFragmentManager().beginTransaction().add(R.id.fragment_bluetooth, m_bluetoothFragment_ota, "bluetoothFragment_ota").commitNow();
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
            handler.postDelayed(this, 100);
        }
    };

    /**
     * 开始搜索蓝牙
     */
    public void BeginDiscovery()
    {
        if(m_bluetoothAdapter.isDiscovering() != true)
        {
            m_deviceList.clear();
            BluetoothListAdapter adapter = new BluetoothListAdapter(m_context, m_deviceList);
            m_listView.setAdapter(adapter);
            //startDiscovery虽然兼容经典蓝牙和低功耗蓝牙,但有些设备无法检测到低功耗蓝牙
            m_bluetoothAdapter.startDiscovery();
        }
    }

    /**
     * 取消搜索蓝牙
     */
    public void CancelDiscovery()
    {
        handler.removeCallbacks(runnable);
        if(m_bluetoothAdapter.isDiscovering() == true)
        {
            m_bluetoothAdapter.cancelDiscovery();
        }
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
        m_listener.onFragmentInteraction(Uri.parse("content://com.zistone.bluetoothtest/list"));
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
                        m_deviceList.clear();
                        BluetoothListAdapter adapter = new BluetoothListAdapter(m_context, m_deviceList);
                        m_listView.setAdapter(adapter);
                        //startDiscovery虽然兼容经典蓝牙和低功耗蓝牙,但有些设备无法检测到低功耗蓝牙
                        m_bluetoothAdapter.startDiscovery();
                        //结束下拉刷新
                        materialRefreshLayout.finishRefresh();
                    }
                }, 1 * 1000);
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
        m_radioButton6 = m_view.findViewById(R.id.radioButton6_bluetoothlist);
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
        BluetoothListAdapter adapter = new BluetoothListAdapter(m_context, m_deviceList);
        m_listView.setAdapter(adapter);
    }
}
