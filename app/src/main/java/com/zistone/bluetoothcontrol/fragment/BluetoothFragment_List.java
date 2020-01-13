package com.zistone.bluetoothcontrol.fragment;

import android.annotation.SuppressLint;
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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zistone.bluetoothcontrol.MainActivity;
import com.zistone.bluetoothcontrol.R;
import com.zistone.bluetoothcontrol.control.BluetoothListAdapter;
import com.zistone.bluetoothcontrol.pojo.MyBluetoothDevice;
import com.zistone.bluetoothcontrol.util.ConvertUtil;
import com.zistone.bluetoothcontrol.util.DeviceFilterShared;
import com.zistone.material_refresh_layout.MaterialRefreshLayout;
import com.zistone.material_refresh_layout.MaterialRefreshListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BluetoothFragment_List extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, CompoundButton.OnCheckedChangeListener
{
    private static final String TAG = "BluetoothFragment_List";
    private static final String ARG_PARAM1 = "param1", ARG_PARAM2 = "param2";
    //已知服务、写入特征的UUID、读取特征的UUID、客户端特征配置
    private static UUID SERVICE_UUID, WRITE_UUID, READ_UUID, CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private BluetoothListAdapter m_bluetoothListAdapter;
    private Context m_context;
    private View m_view;
    private Toolbar m_toolbar;
    private ListView m_listView;
    private BluetoothAdapter m_bluetoothAdapter;
    private BluetoothFragment_CommandTest m_bluetoothFragment_commandTest;
    private BluetoothFragment_PowerControl m_bluetoothFragment_powerControl;
    private BluetoothFragment_OTA m_bluetoothFragment_ota;
    private MaterialRefreshLayout m_materialRefreshLayout;
    private RadioButton m_radioButton1, m_radioButton2, m_radioButton3, m_radioButton4, m_radioButton5;
    private MaterialRefreshListener m_materialRefreshListener;
    private OnFragmentInteractionListener m_onFragmentInteractionListener;
    private BluetoothFragment_CommandTest.Listener m_commandTestListener;
    private BluetoothFragment_PowerControl.Listener m_powerControlListener;
    private SeekBar.OnSeekBarChangeListener m_onSeekBarChangeListener;
    private PopupWindow.OnDismissListener m_onDismissListener;
    private TextWatcher m_textWatcher;
    private Button m_btn1, m_btnFilterContent;
    private Drawable m_drawableUp;
    private Drawable m_drawableDown;
    private ImageButton m_btnClearContentFilter;
    private ImageButton m_btnClearNameFilter;
    private ImageButton m_btnClearAddressFilter;
    private SeekBar m_seekBar;
    private EditText m_editName;
    private EditText m_editAddress;
    private TextView m_textRssi;
    private LinearLayout m_linearLayout1;
    private CheckBox m_chkHideDevice;
    //BLE的扫描器
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
    //记录点击返回键的时间
    private long m_exitTime = 0;
    //当前连接的设备
    private MyBluetoothDevice m_myBluetoothDevice;
    //扫描到的设备,由Map转换,为保证和map数据的同步,不允许对该集合操作
    private List<MyBluetoothDevice> m_deviceList = new ArrayList<>();
    //扫描到的设备、连接成功的设备
    private Map<String, MyBluetoothDevice> m_deviceMap = new HashMap<>(), m_connectSuccessMap = new HashMap<>();
    //传递进来的参数1、传递进来的参数2、根据名称筛选设备、根据地址过滤设备
    private String m_param1, m_param2, m_filterName, m_filterAddress, m_filterContent;
    //根据信号强度筛选设备
    private int m_filterRssi = 100;
    private boolean m_isHideConnectSuccessDevice = false, m_isBtnUpDownFlag = false;
    private PopupWindow m_filterPopWindow;

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
            BluetoothDevice bluetoothDevice = result.getDevice();
            String name = bluetoothDevice.getName();
            String address = bluetoothDevice.getAddress();
            int rssi = result.getRssi();
            int state = bluetoothDevice.getBondState();
            //设备去重
            if(m_deviceMap.containsKey(address))
            {
                MyBluetoothDevice device = m_deviceMap.get(address);
                device.set_name(name);
                device.set_rssi(rssi);
                device.set_boundState(state);
                m_deviceMap.put(address, device);
            }
            else
            {
                MyBluetoothDevice device = new MyBluetoothDevice();
                device.set_name(name);
                device.set_address(address);
                device.set_rssi(rssi);
                device.set_boundState(state);
                device.set_bluetoothDevice(bluetoothDevice);
                m_deviceMap.put(address, device);
            }
            //根据条件筛选设备
            Map<String, MyBluetoothDevice> map = FilterDeviceByCondition(m_deviceMap);
            m_deviceList = new ArrayList<>(map.values());
            //按照信号强度降序排序
            Collections.sort(m_deviceList, new Comparator<MyBluetoothDevice>()
            {
                @Override
                public int compare(MyBluetoothDevice o1, MyBluetoothDevice o2)
                {
                    if(o1.get_rssi() > o2.get_rssi())
                        return -1;
                    if(o1.get_rssi() < o2.get_rssi())
                        return 1;
                    return 0;
                }
            });
            m_bluetoothListAdapter.setM_deviceList(m_deviceList);
            Log.i(TAG, String.format("设备%s的信号强度%d", address, rssi));
            //使用notifyDataSetChanged()会保存当前的状态信息,然后更新适配器里的内容
            m_bluetoothListAdapter.notifyDataSetChanged();
            m_listView.setOnItemClickListener(BluetoothFragment_List.this);
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
     * 将筛选条件的内容显示在Button上
     */
    private void ShowSetFilterContent()
    {
        m_filterContent = "";
        if(!m_filterName.trim().equals(""))
            m_filterContent += m_filterName + ",";
        if(!m_filterAddress.trim().equals(""))
            m_filterContent += m_filterAddress + ",";
        if(m_filterRssi != 100)
            m_filterContent += m_filterRssi * -1 + "dBm,";
        if(m_isHideConnectSuccessDevice)
            m_filterContent += "Yes,";
        m_filterContent = ConvertUtil.ReplaceLast(m_filterContent, ",", "");
        if(m_filterContent.equals(""))
        {
            m_filterContent = "No filter";
        }
        m_btnFilterContent.setText(m_filterContent);
    }

    /**
     * 筛选条件窗体
     *
     * @param view 弹出位置
     */
    private void ShowFilterPop(View view)
    {
        View contentView = LayoutInflater.from(m_context).inflate(R.layout.popwindow_filter, null);
        m_btnClearNameFilter = contentView.findViewById(R.id.btnClearName_filterPop);
        m_btnClearNameFilter.setOnClickListener(this::onClick);
        m_btnClearAddressFilter = contentView.findViewById(R.id.btnClearAddress_filterPop);
        m_btnClearAddressFilter.setOnClickListener(this::onClick);
        m_editName = contentView.findViewById(R.id.editName_filterPop);
        if(!m_filterName.trim().equals(""))
            m_editName.setText(m_filterName);
        m_editName.addTextChangedListener(m_textWatcher);
        m_editAddress = contentView.findViewById(R.id.editAddress_filterPop);
        if(!m_filterAddress.trim().equals(""))
            m_editAddress.setText(m_filterAddress);
        m_editAddress.addTextChangedListener(m_textWatcher);
        m_seekBar = contentView.findViewById(R.id.seekBar_filterPop);
        //进度条的最大数值为60,信号强度 = (数值+40) * -1
        m_seekBar.setMax(60);
        //进度条的数值 = Math.abs(信号强度) - 40
        m_seekBar.setProgress(Math.abs(m_filterRssi) - 40);
        m_seekBar.setOnSeekBarChangeListener(m_onSeekBarChangeListener);
        m_textRssi = contentView.findViewById(R.id.tvRssi_filterPop);
        m_textRssi.setText(m_filterRssi * -1 + "dBm");
        m_chkHideDevice = contentView.findViewById(R.id.chk_filterPop);
        m_chkHideDevice.setChecked(m_isHideConnectSuccessDevice);
        m_chkHideDevice.setOnCheckedChangeListener(this::onCheckedChanged);
        m_filterPopWindow = new PopupWindow(contentView, view.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //popWindow可点击,有些厂商的手机必须设置这个才行
        m_filterPopWindow.setTouchable(true);
        //popWindow外部点击消失
        m_filterPopWindow.setOutsideTouchable(true);
        m_filterPopWindow.setOnDismissListener(m_onDismissListener);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        m_filterPopWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0], location[1] + view.getHeight());
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
        //EditText内容改变
        m_textWatcher = new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if(s == m_editName.getEditableText())
                    m_filterName = m_editName.getText().toString();
                else if(s == m_editAddress.getEditableText())
                    m_filterAddress = m_editAddress.getText().toString();
                ShowSetFilterContent();
            }
        };

        //Pop隐藏
        m_onDismissListener = new PopupWindow.OnDismissListener()
        {
            @Override
            public void onDismiss()
            {
                //隐藏键盘
                InputMethodManager imm = (InputMethodManager) m_context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(m_btnFilterContent.getApplicationWindowToken(), 0);
                m_filterName = m_editName.getText().toString();
                m_filterAddress = m_editAddress.getText().toString();
                DeviceFilterShared.SetFilterName(m_context, m_filterName);
                DeviceFilterShared.SetFilterAddress(m_context, m_filterAddress);
                DeviceFilterShared.SetFilterRssi(m_context, m_filterRssi);
                DeviceFilterShared.SetFilterDevie(m_context, m_isHideConnectSuccessDevice);
                ShowSetFilterContent();
                m_btnFilterContent.setCompoundDrawables(null, null, m_drawableDown, null);
                m_isBtnUpDownFlag = false;
            }
        };

        //进度条拖动
        m_onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener()
        {
            /**
             * 进度条发生改变
             * @param seekBar
             * @param progress
             * @param fromUser
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                m_filterRssi = progress + 40;
                m_textRssi.setText(m_filterRssi * -1 + "dBm");
            }

            /**
             * 按住进度条时
             * @param seekBar
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            /**
             * 放开进度条时
             * @param seekBar
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                ShowSetFilterContent();
            }
        };

        //下拉刷新
        m_materialRefreshListener = new MaterialRefreshListener()
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
                        m_deviceMap.clear();
                        m_deviceList.clear();
                        m_bluetoothListAdapter.setM_deviceList(m_deviceList);
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
        };

        //命令测试
        m_commandTestListener = new BluetoothFragment_CommandTest.Listener()
        {
            @Override
            public void ConnectSuccessListener()
            {
                String address = m_myBluetoothDevice.get_address();
                Log.i(TAG, String.format("设备%s连接成功", address));
                //选择设备后会停止扫描,连接成功后再调用一次筛选
                if(m_isHideConnectSuccessDevice)
                {
                    //根据条件筛选设备
                    m_connectSuccessMap.put(address, m_myBluetoothDevice);
                    Map<String, MyBluetoothDevice> map = HideConnectSuccessDevice(m_deviceMap);
                    m_deviceList = new ArrayList<>(map.values());
                    m_bluetoothListAdapter.setM_deviceList(m_deviceList);
                    //使用notifyDataSetChanged()会保存当前的状态信息,然后更新适配器里的内容
                    m_bluetoothListAdapter.notifyDataSetChanged();
                }
            }
        };

        //电力控制
        m_powerControlListener = new BluetoothFragment_PowerControl.Listener()
        {
            @Override
            public void ConnectSuccessListener()
            {
                String address = m_myBluetoothDevice.get_address();
                Log.i(TAG, String.format("设备%s连接成功", address));
                //选择设备后会停止扫描,连接成功后再调用一次筛选
                if(m_isHideConnectSuccessDevice)
                {
                    //根据条件筛选设备
                    m_connectSuccessMap.put(address, m_myBluetoothDevice);
                    Map<String, MyBluetoothDevice> map = HideConnectSuccessDevice(m_deviceMap);
                    m_deviceList = new ArrayList<>(map.values());
                    m_bluetoothListAdapter.setM_deviceList(m_deviceList);
                    //使用notifyDataSetChanged()会保存当前的状态信息,然后更新适配器里的内容
                    m_bluetoothListAdapter.notifyDataSetChanged();
                }
            }
        };

    }

    /**
     * 根据设置选项过滤扫描到蓝牙设备
     *
     * @param map
     * @return
     */
    private Map<String, MyBluetoothDevice> FilterDeviceByCondition(Map<String, MyBluetoothDevice> map)
    {
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
    private Map<String, MyBluetoothDevice> HideConnectSuccessDevice(Map<String, MyBluetoothDevice> map)
    {
        if(m_isHideConnectSuccessDevice)
        {
            Map<String, MyBluetoothDevice> resultMap = new HashMap<>();
            Iterator<Map.Entry<String, MyBluetoothDevice>> iterator1 = map.entrySet().iterator();
            while(iterator1.hasNext())
            {
                MyBluetoothDevice tempDevice1 = iterator1.next().getValue();
                String tempAddress1 = tempDevice1.get_address();
                boolean flag = false;
                Iterator<Map.Entry<String, MyBluetoothDevice>> iterator2 = m_connectSuccessMap.entrySet().iterator();
                while(iterator2.hasNext())
                {
                    MyBluetoothDevice tempDevice2 = iterator2.next().getValue();
                    String tempAddress2 = tempDevice2.get_address();
                    if(tempAddress1.equals(tempAddress2))
                    {
                        flag = true;
                        break;
                    }
                }
                if(!flag)
                    resultMap.put(tempAddress1, tempDevice1);
            }
            return resultMap;
        }
        else
        {
            return map;
        }
    }

    /**
     * 只显示和筛选条件的强度相似的蓝牙设备
     *
     * @param map 扫描到的蓝牙设备
     * @return
     */
    private Map<String, MyBluetoothDevice> OnlyShowSetRssiSameDevice(Map<String, MyBluetoothDevice> map)
    {
        if(m_filterRssi != 100)
        {
            Iterator<Map.Entry<String, MyBluetoothDevice>> iterator = map.entrySet().iterator();
            while(iterator.hasNext())
            {
                MyBluetoothDevice device = iterator.next().getValue();
                if(device.get_rssi() < m_filterRssi * (-1))
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
    private Map<String, MyBluetoothDevice> OnlyShowSetAddressSameDevice(Map<String, MyBluetoothDevice> map)
    {
        if(!m_filterAddress.trim().equals(""))
        {
            Iterator<Map.Entry<String, MyBluetoothDevice>> iterator = map.entrySet().iterator();
            while(iterator.hasNext())
            {
                String address = iterator.next().getValue().get_address();
                if(address == null || !address.trim().contains(m_filterAddress))
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
    private Map<String, MyBluetoothDevice> OnlyShowSetNameSameDevice(Map<String, MyBluetoothDevice> map)
    {
        if(!m_filterName.trim().equals(""))
        {
            Iterator<Map.Entry<String, MyBluetoothDevice>> iterator = map.entrySet().iterator();
            while(iterator.hasNext())
            {
                String name = iterator.next().getValue().get_name();
                if(name == null || !name.trim().contains(m_filterName))
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
            return null;
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
                //getFragmentManager().beginTransaction().hide(BluetoothFragment_List.this)
                // .commitNow();
                Intent intent = GetAppOpenIntentByPackageName(m_context, "com.ambiqmicro.android.amota");
                if(intent != null)
                {
                    m_context.startActivity(intent);
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setPositiveButton("知道了", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            onDestroy();
                            System.exit(0);
                        }
                    });
                    builder.setMessage("未安装OTA_ZM301,无法使用该功能!");
                    builder.show();
                }
                break;
            //...
            case R.id.menu_2_setting:
                break;
        }
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        switch(buttonView.getId())
        {
            case R.id.chk_filterPop:
                m_isHideConnectSuccessDevice = isChecked;
                break;
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
            case R.id.btn1_bluetoothList:
                if(m_btn1.getText().toString().equals("SCAN"))
                    BeginDiscovery();
                else
                    CancelDiscovery();
                break;
            case R.id.btnClearFilterContent_bluetoothList:
                m_filterName = "";
                m_filterAddress = "";
                m_filterRssi = 100;
                m_isHideConnectSuccessDevice = false;
                m_filterContent = "No filter";
                //清空连接成功的设备
                m_connectSuccessMap.clear();
                DeviceFilterShared.SetFilterName(m_context, m_filterName);
                DeviceFilterShared.SetFilterAddress(m_context, m_filterAddress);
                DeviceFilterShared.SetFilterRssi(m_context, m_filterRssi);
                DeviceFilterShared.SetFilterDevie(m_context, m_isHideConnectSuccessDevice);
                m_btnFilterContent.setText(m_filterContent);
                break;
            case R.id.btnFilterContent_bluetoothList:
                if(!m_isBtnUpDownFlag)
                {
                    m_isBtnUpDownFlag = true;
                    m_btnFilterContent.setCompoundDrawables(null, null, m_drawableUp, null);
                    m_btnFilterContent.setText(m_filterContent);
                    ShowFilterPop(m_linearLayout1);
                }
                //因为PopWindow的setOutsideTouchable事件,这里将不再会执行,里面的逻辑写在onDismiss里代替
                else
                {
                    m_isBtnUpDownFlag = false;
                    //隐藏键盘
                    InputMethodManager imm = (InputMethodManager) m_context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(m_btnFilterContent.getApplicationWindowToken(), 0);
                    m_btnFilterContent.setCompoundDrawables(null, null, m_drawableDown, null);
                    m_filterPopWindow.dismiss();
                }
                break;
            case R.id.btnClearName_filterPop:
                m_editName.setText("");
                break;
            case R.id.btnClearAddress_filterPop:
                m_editAddress.setText("");
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        //连接设备前先关闭扫描蓝牙,否则连接成功后再次扫描会发生阻塞,导致扫描不到设备
        CancelDiscovery();
        String address = m_deviceList.get(position).get_address();
        BluetoothDevice bluetoothDevice = m_bluetoothAdapter.getRemoteDevice(address);
        m_myBluetoothDevice = m_deviceMap.get(address);
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
        if(bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE)
        {
            //电力控制
            if(m_radioButton3.isChecked())
            {
                m_bluetoothFragment_powerControl = BluetoothFragment_PowerControl.newInstance(m_powerControlListener, bluetoothDevice, map);
                //不要使用replace,不然前面的Fragment被释放了会连蓝牙也关掉
                getFragmentManager().beginTransaction().add(R.id.fragment_bluetooth, m_bluetoothFragment_powerControl, "bluetoothFragment_powerControl").commitNow();
                getFragmentManager().beginTransaction().hide(BluetoothFragment_List.this).commitNow();
            }
            //命令测试
            else if(m_radioButton4.isChecked())
            {
                m_bluetoothFragment_commandTest = BluetoothFragment_CommandTest.newInstance(m_commandTestListener, bluetoothDevice, map);
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
     * 开启本地蓝牙
     */
    private void StartBluetoothAdapter()
    {
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
                        m_btn1.setText("STOP");
                        m_bluetoothLeScanner = m_bluetoothAdapter.getBluetoothLeScanner();
                        break;
                    //蓝牙未开启
                    case BluetoothAdapter.STATE_OFF:
                    case BluetoothAdapter.STATE_TURNING_OFF:
                    default:
                        m_btn1.setText("SCAN");
                        break;
                }
            }
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
    }

    private void onButtonPressed(Uri uri)
    {
        if(m_onFragmentInteractionListener != null)
            m_onFragmentInteractionListener.onFragmentInteraction(uri);
    }

    /**
     * 开始搜索蓝牙
     * 开始扫描时候要确保蓝牙适配器处于开启状态
     */
    private void BeginDiscovery()
    {
        if(IsBluetoothAvailable())
        {
            m_btn1.setText("STOP");
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
            m_btn1.setText("SCAN");
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
        m_context = getContext();
        m_filterContent = "No filter";
        m_filterName = DeviceFilterShared.GetFilterName(m_context);
        m_filterAddress = DeviceFilterShared.GetFilterAddress(m_context);
        m_filterRssi = DeviceFilterShared.GetFilterRssi(m_context);
        m_isHideConnectSuccessDevice = DeviceFilterShared.GetFilterDevice(m_context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_view = inflater.inflate(R.layout.fragment_bluetooth_list, container, false);
        m_bluetoothListAdapter = new BluetoothListAdapter(m_context);
        m_onFragmentInteractionListener.onFragmentInteraction(Uri.parse("content://com.zistone.bluetoothcontrol/list"));
        //强制获得焦点
        m_view.requestFocus();
        m_view.setFocusable(true);
        m_view.setFocusableInTouchMode(true);
        m_view.setOnKeyListener(backListener);
        m_toolbar = m_view.findViewById(R.id.toolbar_bluetoothList);
        //加上这句,才会调用Fragment的ToolBar,否则调用的是Activity传递过来的
        setHasOptionsMenu(true);
        //去掉标题
        m_toolbar.setTitle("");
        //此处强转,必须是Activity才有这个方法
        ((MainActivity) getActivity()).setSupportActionBar(m_toolbar);
        //下拉刷新控件
        m_materialRefreshLayout = m_view.findViewById(R.id.refresh_bluetoothList);
        //启用加载更多
        m_materialRefreshLayout.setLoadMore(false);
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
        m_btn1 = m_view.findViewById(R.id.btn1_bluetoothList);
        m_btn1.setOnClickListener(this::onClick);
        m_radioButton1 = m_view.findViewById(R.id.radioButton1_bluetoothList);
        m_radioButton2 = m_view.findViewById(R.id.radioButton2_bluetoothList);
        m_radioButton3 = m_view.findViewById(R.id.radioButton3_bluetoothList);
        m_radioButton4 = m_view.findViewById(R.id.radioButton4_bluetoothList);
        m_radioButton5 = m_view.findViewById(R.id.radioButton5_bluetoothList);
        m_listView = m_view.findViewById(R.id.lv_bluetoothList);
        m_btnFilterContent = m_view.findViewById(R.id.btnFilterContent_bluetoothList);
        m_btnFilterContent.setOnClickListener(this::onClick);
        m_drawableUp = getResources().getDrawable(R.drawable.up1, null);
        m_drawableUp.setBounds(0, 0, m_drawableUp.getMinimumWidth(), m_drawableUp.getMinimumHeight());
        m_drawableDown = getResources().getDrawable(R.drawable.down1, null);
        m_drawableDown.setBounds(0, 0, m_drawableDown.getMinimumWidth(), m_drawableDown.getMinimumHeight());
        m_btnClearContentFilter = m_view.findViewById(R.id.btnClearFilterContent_bluetoothList);
        m_btnClearContentFilter.setOnClickListener(this::onClick);
        m_linearLayout1 = m_view.findViewById(R.id.ll1_bluetoothList);
        ShowSetFilterContent();
        //所有的控件、对象都实例化后再初始化回调方法
        InitListener();
        //设置监听在后
        m_materialRefreshLayout.setMaterialRefreshListener(m_materialRefreshListener);
        //控件、对象、事件监听都加载完毕后才开始扫描蓝牙设备
        StartBluetoothAdapter();
        BeginDiscovery();
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
            m_onFragmentInteractionListener = (OnFragmentInteractionListener) context;
        else
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        m_onFragmentInteractionListener = null;
        m_deviceList.clear();
        m_deviceMap.clear();
        m_connectSuccessMap.clear();
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
        m_deviceMap.clear();
        m_connectSuccessMap.clear();
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

    /**
     * 当fragment结合viewpager使用时,这个方法会调用
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);
        //界面可见
        if(isVisibleToUser)
        {
            int a = 1;
        }
        //界面不可见
        else
        {
            int b = 2;
        }
    }

}
