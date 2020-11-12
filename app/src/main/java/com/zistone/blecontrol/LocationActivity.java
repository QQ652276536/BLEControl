package com.zistone.blecontrol;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.animation.Animation;
import com.baidu.mapapi.animation.Transformation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.zistone.blecontrol.util.MyBleConnectListener;
import com.zistone.blecontrol.util.MyBleMessageListener;
import com.zistone.blecontrol.util.MyBleUtil;
import com.zistone.blecontrol.util.MyConvertUtil;
import com.zistone.blecontrol.util.MyProgressDialogUtil;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class LocationActivity extends AppCompatActivity implements View.OnClickListener, BaiduMap.OnMapClickListener,
        OnGetGeoCoderResultListener, Serializable, BaiduMap.OnMarkerClickListener, BaiduMap.OnMapLoadedCallback {

    private static final String TAG = "LocationActivity";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final SimpleDateFormat SIMPLEDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //查询内部控制参数
    private static final String SEARCH_CONTROLPARAM_COMM = "680000000000006810000186EA16";
    //读取基本信息：版本，电池电压，内部温度
    private static final String BASEINFO_COMM = "6800000000000068210100EC16";
    //读取GPS位置信息
    private static final String LOCATION_COMM = "6800000000000068220100EC16";
    //综合测试：循环发送检测门的状态
    private static final String TESTA = "680000000000006810000180E616";
    //开一号门锁
    private static final String OPENDOOR1_COMM = "680000000000006810000181E116";
    //开二号门锁
    private static final String OPENDOOR2_COMM = "680000000000006810000182E716";
    //开全部门锁
    private static final String OPENDOORS_COMM = "680000000000006810000183E716";
    private static final int MESSAGE_1 = 100;
    private static final int RECEIVE_BASEINFO = 21;
    private static final int RECEIVE_LOCATION = 22;
    private static final int RECEIVE_TESTA = 8002;
    private static final int RECEIVE_OPENDOORS1 = 8102;
    private static final int RECEIVE_OPENDOORS2 = 8202;
    private static final int RECEIVE_OPENALLDOORS = 8302;
    private static final int SEND_SEARCH_CONTROLPARAM = 86;
    private static final int RECEIVE_SEARCH_CONTROLPARAM = 8602;

    private BluetoothDevice _bluetoothDevice;
    private Toolbar _toolbar;
    private ImageButton _btnReturn, _btnHideBle;
    private Button _btn2, _btn3, _btn4;
    private TextView _txt2, _txt5, _txt6, _txt7, _txt8, _txtVersion;
    private StringBuffer _stringBuffer = new StringBuffer();
    private Map<String, UUID> _uuidMap;
    private MyHandler _myHandler;
    private MapView _baiduMapView;
    private BaiduMap _baiduMap;
    private boolean _isPermissionRequested;
    private SDKReceiver _sdkReceiver;
    //设备标记
    private Marker _marker;
    //经纬度对应的地址信息
    private String _latLngStr = "";
    //设备的经纬度
    private LatLng _latLng;
    //地理编码搜索
    private GeoCoder _geoCoder;
    private Timer _refreshTimer = new Timer();
    //定时发送综合测试指令
    private TimerTask _refreshTask = new TimerTask() {
        @Override
        public void run() {
            try {
                MyBleUtil.SendComm(BASEINFO_COMM);
                Log.i(TAG, "发送'读取设备基本信息'指令：" + BASEINFO_COMM);
                Thread.sleep(100);
                MyBleUtil.SendComm(LOCATION_COMM);
                Log.i(TAG, "发送'GPS位置'指令：" + LOCATION_COMM);
                Thread.sleep(100);
                MyBleUtil.SendComm(TESTA);
                Log.i(TAG, "发送'综合测试A'指令：" + LOCATION_COMM);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
    private MyBleConnectListener _connectListener;
    private MyBleMessageListener _messageListener;

    static class MyHandler extends Handler {
        WeakReference<LocationActivity> _weakReference;
        LocationActivity _locationActivity;

        public MyHandler(LocationActivity activity) {
            _weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            if (_weakReference.get() == null)
                return;
            _locationActivity = _weakReference.get();
            String result = (String) message.obj;
            switch (message.what) {
                //设备基本信息
                case RECEIVE_BASEINFO: {
                    String[] strArray = result.split(" ");
                    String versionStr = MyConvertUtil.HexStrToStr((strArray[10] + strArray[11] + strArray[12] + strArray[13]).trim());
                    versionStr = MyConvertUtil.StrAddCharacter(versionStr, 2, ".");
                    String voltageStr1 = String.valueOf(Integer.valueOf(strArray[14], 16));
                    //不足两位补齐，比如0->0、1->01
                    if (voltageStr1.length() == 1)
                        voltageStr1 = "0" + voltageStr1;
                    String voltageStr2 = String.valueOf(Integer.valueOf(strArray[15], 16));
                    if (voltageStr2.length() == 1)
                        voltageStr2 = "0" + voltageStr2;
                    double voltage = Double.parseDouble(voltageStr1 + voltageStr2) / 1000;
                    double temperature = 23.0;
                    try {
                        temperature = 23 + Double.parseDouble(strArray[16]) / 2;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    _locationActivity._txtVersion.setText(versionStr);
                    _locationActivity._txt5.setText(voltage + "V");
                    _locationActivity._txt6.setText(temperature + "℃");
                }
                break;
                //设备位置信息
                case RECEIVE_LOCATION: {
                    String[] strArray = result.split(" ");
                    int state = Integer.parseInt(strArray[10], 16);
                    if (state != 1) {
                        _locationActivity._txt7.setText("定位失败");
                        return;
                    }
                    //                String latStr = strArray[11] + strArray[12] + strArray[13] + strArray[14];
                    String latStr = strArray[14] + strArray[13] + strArray[12] + strArray[11];
                    double latNum = Double.valueOf(Integer.valueOf(latStr, 16)) / 1000000;
                    int len = Integer.parseInt(strArray[1], 16);
                    //                String lotStr = strArray[15] + strArray[16] + strArray[17] + strArray[2];
                    String lotStr = strArray[2] + strArray[17] + strArray[16] + strArray[15];
                    double lotNum = Double.valueOf(Integer.valueOf(lotStr, 16)) / 1000000;
                    //                    String heightStr = strArray[3] + strArray[4];
                    String heightStr1 = strArray[4];
                    int height = Integer.parseInt(heightStr1, 16);
                    String heightStr2 = strArray[3];
                    height += Integer.parseInt(heightStr2, 16);
                    _locationActivity._txt7.setText(latNum + "，" + lotNum + "，" + height);
                    _locationActivity._latLng = new LatLng(lotNum, latNum);
                    //经纬度->地址
                    _locationActivity._geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(_locationActivity._latLng).newVersion(1).radius(500));
                    MapStatus mapStatus = new MapStatus.Builder().target(_locationActivity._latLng).zoom(16).build();
                    MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
                    _locationActivity._baiduMap.setMapStatus(mapStatusUpdate);
                    MarkerOptions markerOptions = new MarkerOptions().position(_locationActivity._latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark2));
                    //标记添加至地图中
                    _locationActivity._baiduMap.clear();
                    _locationActivity._baiduMap.addOverlay(markerOptions);
                }
                break;
                //综合测试
                case RECEIVE_TESTA: {
                    String[] strArray = result.split(" ");
                    byte[] bytes1 = MyConvertUtil.HexStrToByteArray(strArray[13]);
                    String bitStr = MyConvertUtil.ByteToBit(bytes1[0]);
                    String doorState1 = String.valueOf(bitStr.charAt(7));
                    String lockState1 = String.valueOf(bitStr.charAt(6));
                    String doorState2 = String.valueOf(bitStr.charAt(5));
                    String lockState2 = String.valueOf(bitStr.charAt(4));
                    //强磁开关状态
                    String magneticState = String.valueOf(bitStr.charAt(3));
                    //外接电源状态
                    String outsideState = String.valueOf(bitStr.charAt(2));
                    //内部电池充电状态
                    String insideState = String.valueOf(bitStr.charAt(1));
                    //电池电量
                    int battery = Integer.parseInt(strArray[14] + strArray[15], 16);
                    //下端磁强
                    int magneticDown = Integer.parseInt(strArray[16] + strArray[17], 16);
                    //上端磁强
                    int magneticUp = Integer.parseInt(strArray[2] + strArray[3], 16);
                    //前端磁强
                    int magneticBefore = Integer.parseInt(strArray[4] + strArray[5], 16);
                    if (doorState1.equals("1")) {
                        _locationActivity._txt2.setText("已开");
                        _locationActivity._txt2.setTextColor(Color.GREEN);
                    } else {
                        _locationActivity._txt2.setText("已关");
                        _locationActivity._txt2.setTextColor(Color.RED);
                    }
                    if (lockState1.equals("1")) {
                        _locationActivity._txt8.setText("已开");
                        _locationActivity._txt8.setTextColor(Color.GREEN);
                    } else {
                        _locationActivity._txt8.setText("已关");
                        _locationActivity._txt8.setTextColor(Color.RED);
                    }
                }
                break;
                //一号门锁
                case RECEIVE_OPENDOORS1: {
                    String[] strArray = result.split(" ");
                    byte[] bytes = MyConvertUtil.HexStrToByteArray(strArray[13]);
                    String bitStr = MyConvertUtil.ByteToBit(bytes[0]);
                    String doorState2 = String.valueOf(bitStr.charAt(7));
                }
                break;
                //二号门锁
                case RECEIVE_OPENDOORS2: {
                    String[] strArray = result.split(" ");
                    byte[] bytes = MyConvertUtil.HexStrToByteArray(strArray[13]);
                    String bitStr = MyConvertUtil.ByteToBit(bytes[0]);
                    String doorState2 = String.valueOf(bitStr.charAt(7));
                }
                break;
                //全部门锁
                case RECEIVE_OPENALLDOORS: {
                    String[] strArray = result.split(" ");
                    byte[] bytes = MyConvertUtil.HexStrToByteArray(strArray[13]);
                    String bitStr = MyConvertUtil.ByteToBit(bytes[0]);
                    String doorState1 = String.valueOf(bitStr.charAt(7));
                }
                break;
            }
        }
    }

    /**
     * 构造广播监听类,监听SDK的Key验证以及网络异常广播
     */
    public class SDKReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            //鉴权错误信息描述
            if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                Log.e(TAG, "Key验证出错!错误码:" + intent.getIntExtra(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE, 0) + ";错误信息:" + intent.getStringExtra(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_MESSAGE));
            } else if (action.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                Log.e(TAG, "网络出错");
            } else if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                Log.e(TAG, "Key验证成功！功能可以正常使用");
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
            String[] permissions = {Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_SETTINGS,
                                    Manifest.permission.ACCESS_WIFI_STATE,};
            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != this.checkSelfPermission(perm)) {
                    //进入到这里代表没有权限
                    permissionsList.add(perm);
                }
            }
            if (!permissionsList.isEmpty()) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 0);
            }
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
                runOnUiThread(() -> {
                    MyProgressDialogUtil.ShowWarning(LocationActivity.this, "知道了", "警告", "连接已断开，请检查设备然后重新连接！", false, () -> {
                        Intent intent = new Intent(LocationActivity.this, ListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    });
                });
            }
        };
        _messageListener = new MyBleMessageListener() {
            @Override
            public void OnWriteSuccess(byte[] byteArray) {
                String result = MyConvertUtil.ByteArrayToHexStr(byteArray);
                result = MyConvertUtil.StrAddCharacter(result, 2, " ");
                String[] strArray = result.split(" ");
                String indexStr = strArray[11];
                switch (indexStr) {
                    case "80":
                        Log.i(TAG, "发送'综合测试'指令：" + TESTA);
                        break;
                    case "81":
                        Log.i(TAG, "发送开一号门锁：" + OPENDOOR1_COMM);
                        break;
                    case "82":
                        Log.i(TAG, "发送开二号门锁：" + OPENDOOR2_COMM);
                        break;
                    case "83":
                        Log.i(TAG, "发送开全部门锁：" + OPENDOORS_COMM);
                        break;
                }
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
        };
    }

    /**
     * 解析硬件返回的数据
     *
     * @param data 带空格的16进制字符串
     */
    private void Resolve(String data) {
        String[] strArray = data.split(" ");
        int what = 0;
        /*
         * 设备基本信息的通信协议和之前的开关门协议不一样，需要留意
         *
         * */
        if (strArray[8].equals("A1")) {
            what = RECEIVE_BASEINFO;
        }
        /*
         * GPS位置信息的通信协议和之前的开关门协议不一样，需要留意
         *
         * */
        else if (strArray[8].equals("A2")) {
            what = RECEIVE_LOCATION;
        }
        /*
         * 开关门协议
         *
         * */
        else if (strArray[8].equals("10")) {
            String indexStr = strArray[12];
            switch (indexStr) {
                //全部门锁状态
                case "80":
                    what = RECEIVE_TESTA;
                    break;
                //开一号门锁
                case "81":
                    what = RECEIVE_OPENDOORS1;
                    break;
                //开二号门锁
                case "82":
                    what = RECEIVE_OPENDOORS2;
                    break;
                //开全部门锁
                case "83":
                    what = RECEIVE_OPENALLDOORS;
                    break;
                //查询内部控制参数
                case "86":
                    what = RECEIVE_SEARCH_CONTROLPARAM;
                    break;
                //修改内部控制参数
                case "87":
                    //先查询再修改
                    what = SEND_SEARCH_CONTROLPARAM;
                    break;
            }
        }
        _myHandler.obtainMessage(what, data).sendToTarget();
    }

    /**
     * 创建平移动画
     */
    private Animation Transformation() {
        Point point = _baiduMap.getProjection().toScreenLocation(_latLng);
        LatLng latLng = _baiduMap.getProjection().fromScreenLocation(new Point(point.x, point.y - 30));
        Transformation mTransforma = new Transformation(_latLng, latLng, _latLng);
        mTransforma.setDuration(500);
        //动画重复模式
        mTransforma.setRepeatMode(Animation.RepeatMode.RESTART);
        //动画重复次数
        mTransforma.setRepeatCount(2);
        mTransforma.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart() {
            }

            @Override
            public void onAnimationEnd() {
            }

            @Override
            public void onAnimationCancel() {
            }

            @Override
            public void onAnimationRepeat() {
            }
        });
        return mTransforma;
    }

    @Override
    public void onMapClick(LatLng latLng) {
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    /**
     * 根据地理位置查找经纬度
     *
     * @param geoCodeResult
     */
    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
        if (null == geoCodeResult || SearchResult.ERRORNO.NO_ERROR != geoCodeResult.error) {
            Toast.makeText(this, "经纬度查询异常", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 根据经纬度查找地理位置
     * <p>
     * 创建围栏时需要带入地址信息,所以创建围栏的逻辑放在这里面
     *
     * @param reverseGeoCodeResult
     */
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (null == reverseGeoCodeResult || SearchResult.ERRORNO.NO_ERROR != reverseGeoCodeResult.error) {
            Toast.makeText(this, "地理位置查询异常", Toast.LENGTH_SHORT).show();
        } else {
            _latLngStr = reverseGeoCodeResult.getAddress();
        }
    }

    /**
     * 地图加载成功后
     */
    @Override
    public void onMapLoaded() {
        if (null == _latLng) {
            return;
        }
        //设置标记的位置和图标
        MarkerOptions markerOptions = new MarkerOptions().position(_latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark2));
        //标记添加至地图中
        _marker = (Marker) (_baiduMap.addOverlay(markerOptions));
        //定义地图缩放级别3~16,值越大地图越精细
        MapStatus mapStatus = new MapStatus.Builder().target(_latLng).zoom(16).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        //改变地图状态
        _baiduMap.setMapStatus(mapStatusUpdate);
        //添加平移动画
        _marker.setAnimation(Transformation());
        _marker.startAnimation();
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
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //返回
            case R.id.btn_return_location: {
                MyProgressDialogUtil.DismissAlertDialog();
                this.finish();
            }
            break;
            //隐藏蓝牙信息
            case R.id.btn_hide_ble: {
                if (_baiduMapView.getVisibility() == View.VISIBLE) {
                    _baiduMapView.setVisibility(View.GONE);
                    _btnHideBle.setImageResource(R.drawable.down);
                } else {
                    _baiduMapView.setVisibility(View.VISIBLE);
                    _btnHideBle.setImageResource(R.drawable.up);
                }
            }
            break;
            //开一号门锁
            case R.id.button2_location: {
                Log.i(TAG, "发送开一号门锁：" + OPENDOOR1_COMM);
                MyBleUtil.SendComm(OPENDOOR1_COMM);
            }
            break;
            //开二号门锁
            case R.id.button3_location: {
                Log.i(TAG, "发送开二号门锁：" + OPENDOOR2_COMM);
                MyBleUtil.SendComm(OPENDOOR2_COMM);
            }
            break;
            //开全部门锁
            case R.id.button4_location: {
                Log.i(TAG, "发送开全部门锁：" + OPENDOORS_COMM);
                MyBleUtil.SendComm(OPENDOORS_COMM);
            }
            break;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(_sdkReceiver);
        if (null != _baiduMap) {
            _baiduMap.clear();
            _baiduMap = null;
        }
        //MapView的生命周期与Fragment同步,当Fragment销毁时需调用MapView.destroy()
        if (null != _baiduMapView) {
            _baiduMapView.onDestroy();
            _baiduMapView = null;
        }
        MyProgressDialogUtil.DismissAlertDialog();
        if (_refreshTimer != null)
            _refreshTimer.cancel();
        if (_refreshTask != null)
            _refreshTask.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _myHandler = new MyHandler(this);
        setContentView(R.layout.activity_location);
        //任务、延迟执行时间、重复调用间隔，Timer和TimerTask在调用cancel方法取消后不能再执行schedule语句
        _refreshTimer.schedule(_refreshTask, 0, 1 * 1000);
        Intent intent = getIntent();
        _bluetoothDevice = intent.getParcelableExtra(ARG_PARAM1);
        _uuidMap = (Map<String, UUID>) intent.getSerializableExtra(ARG_PARAM2);
        //Toolbar
        _toolbar = findViewById(R.id.toolbar_location);
        _toolbar.setTitle("");
        setSupportActionBar(_toolbar);
        _txt2 = findViewById(R.id.txt2_location);
        _txt5 = findViewById(R.id.txt5_location);
        _txt6 = findViewById(R.id.txt6_location);
        _txt7 = findViewById(R.id.txt7_location);
        _txt8 = findViewById(R.id.txt8_location);
        _txtVersion = findViewById(R.id.txtVersion_location);
        _btnReturn = findViewById(R.id.btn_return_location);
        _btnHideBle = findViewById(R.id.btn_hide_ble);
        _btn2 = findViewById(R.id.button2_location);
        _btn3 = findViewById(R.id.button3_location);
        _btn4 = findViewById(R.id.button4_location);
        _btnReturn.setOnClickListener(this::onClick);
        _btnHideBle.setOnClickListener(this::onClick);
        _btn2.setOnClickListener(this::onClick);
        _btn3.setOnClickListener(this::onClick);
        _btn4.setOnClickListener(this::onClick);
        //动态获取权限
        RequestPermission();
        _baiduMapView = findViewById(R.id.mapView_location);
        //地理编码
        _geoCoder = GeoCoder.newInstance();
        _geoCoder.setOnGetGeoCodeResultListener(this);
        //注册SDK广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        _sdkReceiver = new SDKReceiver();
        registerReceiver(_sdkReceiver, iFilter);
        //地图初始化
        _baiduMap = _baiduMapView.getMap();
        _baiduMap.setOnMapClickListener(this);
        _baiduMap.setOnMarkerClickListener(this::onMarkerClick);
        //地图加载完毕回调
        _baiduMap.setOnMapLoadedCallback(this::onMapLoaded);
        InitListener();
        MyBleUtil.SetConnectListener(_connectListener);
        MyBleUtil.SetMessageListener(_messageListener);
    }

}
