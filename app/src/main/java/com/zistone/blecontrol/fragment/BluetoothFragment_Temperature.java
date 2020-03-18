package com.zistone.blecontrol.fragment;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zistone.blecontrol.activity.MainActivity;
import com.zistone.blecontrol.R;
import com.zistone.blecontrol.util.BluetoothListener;
import com.zistone.blecontrol.util.BluetoothUtil;
import com.zistone.blecontrol.util.ConvertUtil;
import com.zistone.blecontrol.util.ProgressDialogUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothFragment_Temperature extends Fragment implements View.OnClickListener, BluetoothListener {
    private static final String TAG = "BluetoothFragment_Temperature";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_ERROR_1 = -1;
    private static final int MESSAGE_ERROR_2 = -2;
    private static final int MESSAGE_ERROR_3 = -3;
    private static final int MESSAGE_1 = 100;
    private static final int RECEIVE_TESTA = 8002;
    private static final int SEND_SET_CONTROLPARAM = 87;
    private static Listener _listener;

    private BluetoothDevice _bluetoothDevice;
    private OnFragmentInteractionListener _onFragmentInteractionListener;
    private Context _context;
    private View _view;
    private Toolbar _toolbar;
    private ImageButton _btnReturn;
    private Button _btn1, _btn2;
    private TextView _txt1, _txt2;
    private CheckBox _chk1, _chk2;
    private StringBuffer _stringBuffer = new StringBuffer();
    private Timer _refreshTimer;
    private TimerTask _refreshTask;
    private Map<String, UUID> _uuidMap;
    private ProgressDialogUtil.Listener _progressDialogUtilListener;
    //是否连接成功
    private boolean _connectedSuccess = false;

    @Override
    public void OnConnected() {
        ProgressDialogUtil.Dismiss();
        Log.d(TAG, ">>>成功建立连接!");
        //轮询
        Message message = handler.obtainMessage(MESSAGE_1, "");
        handler.sendMessage(message);
        //连接成功的回调
        _listener.ConnectSuccessListener();
    }

    @Override
    public void OnConnecting() {
        ProgressDialogUtil.ShowProgressDialog(_context, _progressDialogUtilListener, "正在连接...");
    }

    @Override
    public void OnDisConnected() {
        Log.d(TAG, ">>>连接已断开!");
        Message message = handler.obtainMessage(MESSAGE_ERROR_1, "");
        handler.sendMessage(message);
    }

    @Override
    public void OnWriteSuccess(byte[] byteArray) {
        String result = ConvertUtil.ByteArrayToHexStr(byteArray);
        result = ConvertUtil.HexStrAddCharacter(result, " ");
        String[] strArray = result.split(" ");
        String indexStr = strArray[11];
        switch (indexStr) {
        }
    }

    @Override
    public void OnReadSuccess(byte[] byteArray) {
        String result = ConvertUtil.ByteArrayToHexStr(byteArray);
        result = ConvertUtil.HexStrAddCharacter(result, " ");
        //Log.d(TAG, ">>>接收:" + result);
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

    private void InitListener() {
        _progressDialogUtilListener = new ProgressDialogUtil.Listener() {
            @Override
            public void OnDismiss() {
                DisConnect();
                Log.d(TAG, ">>>连接已断开!");
            }
        };
    }

    public interface Listener {
        void ConnectSuccessListener();
    }

    public static BluetoothFragment_Temperature newInstance(Listener listener, BluetoothDevice bluetoothDevice, Map<String, UUID> map) {
        BluetoothFragment_Temperature fragment = new BluetoothFragment_Temperature();
        _listener = listener;
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, bluetoothDevice);
        args.putSerializable(ARG_PARAM2, (Serializable) map);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 返回键的监听
     */
    private View.OnKeyListener backListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
                getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
                getFragmentManager().beginTransaction().remove(BluetoothFragment_Temperature.this).commitNow();
                return true;
            }
            return false;
        }
    };

    private void DisConnect() {
        _connectedSuccess = false;
        _btn1.setEnabled(false);
        if (_refreshTask != null) {
            _refreshTask.cancel();
        }
        if (_refreshTimer != null) {
            _refreshTimer.cancel();
        }
        BluetoothUtil.DisConnGatt();
        _txt1.setText("Null");
        _txt1.setTextColor(Color.GRAY);
        _txt2.setText("Null");
        _txt2.setTextColor(Color.GRAY);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            String result = (String) message.obj;
            switch (message.what) {
                case MESSAGE_ERROR_1:
                    DisConnect();
                    ProgressDialogUtil.ShowWarning(_context, "警告", "该设备的连接已断开,如需再次连接请重试!");
                    break;
                case MESSAGE_1: {
                    _btn1.setEnabled(true);
                    _refreshTimer = new Timer();
                    _refreshTask = new TimerTask() {
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(() ->
                            {
                                try {
                                    String hexStr = "680000000000006810000181E116";
                                    Log.d(TAG, ">>>发送测量体温:" + hexStr);
                                    BluetoothUtil.SendComm(hexStr);
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    };
                    //任务、延迟执行时间、重复调用间隔,Timer和TimerTask在调用cancel()取消后不能再执行schedule语句
                    _refreshTimer.schedule(_refreshTask, 0, 1 * 1000);
                    _connectedSuccess = true;
                }
                break;
                //
                case RECEIVE_TESTA: {
                    String strs[] = result.split(",");
                    Log.d(TAG, String.format("环境温度:%s℃ 人体温度:%s℃", strs[0], strs[1]));
                    _txt1.setText(strs[0] + "℃");
                    _txt1.setTextColor(Color.BLUE);
                    _txt2.setText(strs[1] + "℃");
                    _txt2.setTextColor(Color.GREEN);
                }
                break;
            }
        }
    };

    public void onButtonPressed(Uri uri) {
        if (_onFragmentInteractionListener != null) {
            _onFragmentInteractionListener.onFragmentInteraction(uri);
        }
    }

    /**
     * Activity中加载Fragment时会要求实现onFragmentInteraction(Uri uri)方法,此方法主要作用是从fragment向activity传递数据
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    /**
     * 解析硬件返回的数据
     *
     * @param data
     */
    private void Resolve(String data) {
        //Log.d(TAG, ">>>共接收:" + data);
        String[] strArray = data.split(" ");
        String indexStr = strArray[12];
        Message message = new Message();
        switch (indexStr) {
            case "80": {
                byte[] bytes1 = ConvertUtil.HexStrToByteArray(strArray[13]);
                String bitStr = ConvertUtil.ByteToBit(bytes1[0]);
                String str1 = String.valueOf(bitStr.charAt(7));
                String str2 = String.valueOf(bitStr.charAt(6));
                String str3 = String.valueOf(bitStr.charAt(5));
                String str4 = String.valueOf(bitStr.charAt(4));
                String str5 = String.valueOf(bitStr.charAt(3));
                String str6 = String.valueOf(bitStr.charAt(2));
                String str7 = String.valueOf(bitStr.charAt(1));
                //电池容量（环境温度）
                int value1 = Integer.parseInt(strArray[14] + strArray[15], 16);
                //下端磁强
                int value2 = Integer.parseInt(strArray[16] + strArray[17], 16);
                //上端磁强(人体温度)
                int value3 = Integer.parseInt(strArray[2] + strArray[3], 16);
                //前端磁强
                int value4 = Integer.parseInt(strArray[4] + strArray[5], 16);
                message.what = RECEIVE_TESTA;
                message.obj = value1 + "," + value3;
            }
            break;
        }
        handler.sendMessage(message);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (_connectedSuccess) {
            switch (requestCode) {
                case MainActivity.ACTIVITYRESULT_WRITEVALUE: {
                    String hexStr = data.getStringExtra("WriteValue");
                }
                break;
                case MainActivity.ACTIVITYRESULT_PARAMSETTING: {
                    String hexStr = data.getStringExtra("ParamSetting");
                    Message message = handler.obtainMessage(SEND_SET_CONTROLPARAM, hexStr);
                    handler.sendMessage(message);
                }
                break;
                case MainActivity.ACTIVITYRESULT_OTA: {
                    String hexStr = data.getStringExtra("OTA");
                }
                break;
            }
        } else {
            ProgressDialogUtil.ShowWarning(_context, "警告", "该设备的连接已断开,如需再次连接请重试!");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (_connectedSuccess) {
        } else {
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Activity的onCreateOptionsMenu会在之前调用,即先Clear一下,这样就只有Fragment自己设置的了
        menu.clear();
        inflater.inflate(R.menu.powercontrol_menu_setting, menu);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn2_temperature:
            case R.id.btn_return_temperature: {
                BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
                getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
                getFragmentManager().beginTransaction().remove(BluetoothFragment_Temperature.this).commitNow();
            }
            break;
            //开启OpenCV
            case R.id.btn1_temperature: {
                if (_bluetoothDevice != null) {

                } else {
                    ProgressDialogUtil.ShowWarning(_context, "提示", "未获取到蓝牙,请重试!");
                }
            }
            break;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            _bluetoothDevice = getArguments().getParcelable(ARG_PARAM1);
            _uuidMap = (Map<String, UUID>) getArguments().getSerializable(ARG_PARAM2);
        }
        _context = getContext();
        BluetoothUtil.Init(_context, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _view = inflater.inflate(R.layout.fragment_bluetooth_temperature, container, false);
        //强制获得焦点
        _view.requestFocus();
        _view.setFocusable(true);
        _view.setFocusableInTouchMode(true);
        _view.setOnKeyListener(backListener);
        _toolbar = _view.findViewById(R.id.toolbar_temperature);
        //加上这句,才会调用Fragment的ToolBar,否则调用的是Activity传递过来的
        setHasOptionsMenu(true);
        //去掉标题
        _toolbar.setTitle("");
        //此处强转,必须是Activity才有这个方法
        ((MainActivity) getActivity()).setSupportActionBar(_toolbar);
        _txt1 = _view.findViewById(R.id.txt1_temperature);
        _txt2 = _view.findViewById(R.id.txt2_temperature);
        _btnReturn = _view.findViewById(R.id.btn_return_temperature);
        _btn1 = _view.findViewById(R.id.btn1_temperature);
        _btn2 = _view.findViewById(R.id.btn2_temperature);
        _btnReturn.setOnClickListener(this::onClick);
        _btn1.setOnClickListener(this::onClick);
        _btn2.setOnClickListener(this::onClick);
        InitListener();
        return _view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            _onFragmentInteractionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDestroy() {
        if (_refreshTimer != null)
            _refreshTimer.cancel();
        if (_refreshTask != null)
            _refreshTask.cancel();
        BluetoothUtil.DisConnGatt();
        _bluetoothDevice = null;
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        _onFragmentInteractionListener = null;
        super.onDetach();
    }

}