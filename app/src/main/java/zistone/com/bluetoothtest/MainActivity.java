package zistone.com.bluetoothtest;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

import zistone.com.onecheckactivity.R;

public class MainActivity extends AppCompatActivity implements OnClickListener, OnItemClickListener, OnCheckedChangeListener
{
    private static final String TAG = "BluetoothActivity";
    //已知服务
    private static final String SERVICE_UUID = "0000ff01-0000-1000-8000-00805f9b34fb";
    //已知特征,发送数据用
    private static final String CHARACTERISTIC_UUID = "0000ff03-0000-1000-8000-00805f9b34fb";
    private static final String CHARACTERISTIC_UUID_2 = "0000ff02-0000-1000-8000-00805f9b34fb";
    private CheckBox m_checkBox;
    private TextView m_textView1;
    private ListView m_listView;
    //蓝牙适配器
    private BluetoothAdapter m_bluetoothAdapter;
    //蓝牙列表
    private ArrayList<BluetoothDevice> m_deviceList = new ArrayList<>();
    private BluetoothReceiver m_blueReceiver;
    private BluetoothDevice m_bluetoothDevice;
    private BluetoothGatt m_bluetoothGatt;
    private BluetoothGattService m_bluetoothGattService;
    private BluetoothGattCharacteristic m_bluetoothGattCharacteristic;

    /**
     * 动态权限
     */
    private void BluetoothPermissions()
    {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);
        }
        //动态注册注册广播接收器,接收蓝牙发现讯息
        IntentFilter btFilter = new IntentFilter();
        btFilter.setPriority(1000);
        btFilter.addAction(BluetoothDevice.ACTION_FOUND);
        btFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BluetoothPermissions();
        //获取蓝牙适配器
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        m_checkBox = findViewById(R.id.ck_bluetooth);
        m_textView1 = findViewById(R.id.tv_discovery);
        m_listView = findViewById(R.id.lv_bluetooth);
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
        m_textView1.setOnClickListener(this);
        if(m_bluetoothAdapter == null)
        {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            finish();
        }
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
            m_textView1.setOnClickListener(this);
            m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(m_bluetoothAdapter == null)
            {
                Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        else
        {
            Toast.makeText(this, "用户拒绝了权限", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if(buttonView.getId() == R.id.ck_bluetooth)
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
                BlueListAdapter adapter = new BlueListAdapter(this, m_deviceList);
                m_listView.setAdapter(adapter);
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.tv_discovery)
        {
            BeginDiscovery();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == 1)
        {
            switch(resultCode)
            {
                case RESULT_OK:
                    Toast.makeText(this, "允许本地蓝牙被附近的其它蓝牙设备发现", Toast.LENGTH_SHORT).show();
                    break;
                case RESULT_CANCELED:
                    Toast.makeText(this, "不允许蓝牙被附近的其它蓝牙设备发现", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        else if(requestCode == 0)
        {
            switch(resultCode)
            {
                case RESULT_OK:
                    Toast.makeText(this, "蓝牙打开成功", Toast.LENGTH_SHORT).show();
                    break;
                case RESULT_CANCELED:
                    Toast.makeText(this, "蓝牙打开失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    /**
     * 异步搜索蓝牙设备
     */
    private Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            BeginDiscovery();
            handler.postDelayed(this, 1000);
        }
    };

    /**
     * 开始搜索蓝牙
     */
    private void BeginDiscovery()
    {
        if(m_bluetoothAdapter.isDiscovering() != true)
        {
            m_deviceList.clear();
            BlueListAdapter adapter = new BlueListAdapter(MainActivity.this, m_deviceList);
            m_listView.setAdapter(adapter);
            m_textView1.setText("正在搜索蓝牙设备");
            //startDiscovery虽然兼容经典蓝牙和低功耗蓝牙,但有些设备无法检测到低功耗蓝牙
            m_bluetoothAdapter.startDiscovery();
        }
    }

    /**
     * 取消搜索蓝牙
     */
    private void CancelDiscovery()
    {
        handler.removeCallbacks(runnable);
        m_textView1.setText("取消搜索蓝牙设备");
        if(m_bluetoothAdapter.isDiscovering() == true)
        {
            m_bluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        handler.postDelayed(runnable, 50);
        m_blueReceiver = new BluetoothReceiver();
        //需要过滤多个动作，则调用IntentFilter对象的addAction添加新动作
        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        foundFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        foundFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(m_blueReceiver, foundFilter);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        CancelDiscovery();
        unregisterReceiver(m_blueReceiver);
    }

    private class BluetoothReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            // 获得已经搜索到的蓝牙设备
            if(action.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                m_deviceList.add(device);
                BlueListAdapter adapter = new BlueListAdapter(MainActivity.this, m_deviceList);
                m_listView.setAdapter(adapter);
                m_listView.setOnItemClickListener(MainActivity.this);
            }
            else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
            {
                handler.removeCallbacks(runnable);
                m_textView1.setText("蓝牙设备搜索完成");
            }
            else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() == BluetoothDevice.BOND_BONDING)
                {
                    m_textView1.setText("正在配对" + device.getName());
                }
                else if(device.getBondState() == BluetoothDevice.BOND_BONDED)
                {
                    m_textView1.setText("完成配对" + device.getName());
                    handler.postDelayed(runnable, 50);
                }
                else if(device.getBondState() == BluetoothDevice.BOND_NONE)
                {
                    m_textView1.setText("取消配对" + device.getName());
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        CancelDiscovery();
        m_bluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_deviceList.get(position).getAddress());
        try
        {
            if(m_bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE)
            {
                m_textView1.setText("开始连接");
                m_bluetoothGatt = m_bluetoothDevice.connectGatt(this, false, new BluetoothGattCallback()
                {
                    /**
                     * 连接状态改变时回调
                     * @param gatt
                     * @param status
                     * @param newState
                     */
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
                    {
                        super.onConnectionStateChange(gatt, status, newState);
                        if(newState == BluetoothProfile.STATE_CONNECTED)
                        {
                            Log.i(TAG, ">>>成功建立连接!");
                            Toast.makeText(MainActivity.this, "成功建立连接!", Toast.LENGTH_SHORT).show();

                        }
                        gatt.discoverServices();
                        if(newState == BluetoothGatt.STATE_DISCONNECTED)
                        {
                            Log.i(TAG, ">>>连接断开!");
                            Toast.makeText(MainActivity.this, "连接已断开!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    /**
                     *
                     * @param gatt
                     * @param status
                     */
                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status)
                    {
                        super.onServicesDiscovered(gatt, status);
                        //通过UUID找到服务
                        m_bluetoothGattService = gatt.getService(UUID.fromString(SERVICE_UUID));
                        //找到服务后在通过UUID找到特征
                        m_bluetoothGattCharacteristic = m_bluetoothGattService.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID));
                        if(m_bluetoothGattCharacteristic != null)
                        {
                            //启用onCharacteristicChanged(),用于接收数据
                            gatt.setCharacteristicNotification(m_bluetoothGattCharacteristic, true);
                            Log.i(TAG, ">>>查找服务成功!");
                            Toast.makeText(MainActivity.this, "查找服务成功", Toast.LENGTH_SHORT).show();
                            byte[] senddatas = new byte[]{8, 8, 8, 8, 8, 8, 8, 8};
                            m_bluetoothGattCharacteristic.setValue(senddatas);
                            m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic);
                        }
                        else
                        {
                            Log.i(TAG, ">>>查找服务失败!");
                            Toast.makeText(MainActivity.this, "查找服务失败", Toast.LENGTH_LONG).show();
                        }
                    }

                    /**
                     * 读取成功后回调
                     * @param gatt
                     * @param characteristic
                     * @param status
                     */
                    @Override
                    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
                    {
                        super.onCharacteristicRead(gatt, characteristic, status);
                        Log.i(TAG, ">>>数据读取成功!");

                        byte[] bytesreceive = characteristic.getValue();
                        Log.i(TAG, ">>>接收数据:" + bytesreceive[0] + "" + bytesreceive[1] + "" + bytesreceive[2] + "" + bytesreceive[4]);
                    }

                    /**
                     * 写入成功后回调
                     * @param gatt
                     * @param characteristic
                     * @param status
                     */
                    @Override
                    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
                    {
                        super.onCharacteristicWrite(gatt, characteristic, status);
                        Log.i(TAG, ">>>数据发送成功!");
                    }

                    /**
                     * 接收数据
                     * @param gatt
                     * @param characteristic
                     */
                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
                    {
                        super.onCharacteristicChanged(gatt, characteristic);

                        byte[] bytesreceive = characteristic.getValue();
                        Log.i(TAG, ">>>接收数据:" + bytesreceive[0] + "" + bytesreceive[1] + "" + bytesreceive[2] + "" + bytesreceive[4]);
                    }
                });
            }
            else if(m_bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED && m_bluetoothDevice.getBondState() == BlueListAdapter.CONNECTED)
            {
                m_textView1.setText("正在发送消息");
                //TODO:发送信息
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            m_textView1.setText("配对异常：" + e.getMessage());
        }
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
                Log.i(TAG, "handleMessage readMessage=" + readMessage);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("我收到消息啦").setMessage(readMessage).setPositiveButton("确定", null);
                builder.create().show();
            }
        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

}
