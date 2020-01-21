package com.zistone.blecontrol.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zistone.blecontrol.R;
import com.zistone.blecontrol.util.BTListener;
import com.zistone.blecontrol.util.BTUtil;
import com.zistone.blecontrol.util.ConvertUtil;
import com.zistone.blecontrol.util.ProgressDialogUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

public class BluetoothFragment_DB extends Fragment implements View.OnClickListener, BTListener
{
    private static final String TAG = "BluetoothFragment_DB";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_1 = 1;
    private static final int MESSAGE_ERROR_1 = -1;
    private OnFragmentInteractionListener _onFragmentInteractionListener;
    private Context _context;
    private View _view;
    private Button _btn1;
    private Button _btn2;
    private ImageButton _btnReturn;
    private TextView _txt1;
    private TextView _txt2;
    private TextView _txt3;
    private TextView _txt4;
    private BluetoothDevice _bluetoothDevice;
    private Map<String, UUID> _uuidMap;

    public static BluetoothFragment_DB newInstance(BluetoothDevice bluetoothDevice, Map<String, UUID> map)
    {
        BluetoothFragment_DB fragment = new BluetoothFragment_DB();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, bluetoothDevice);
        args.putSerializable(ARG_PARAM2, (Serializable) map);
        fragment.setArguments(args);
        return fragment;
    }

    private View.OnKeyListener backListener = new View.OnKeyListener()
    {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
            {
                BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
                getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
                getFragmentManager().beginTransaction().remove(BluetoothFragment_DB.this).commitNow();
                return true;
            }
            return false;
        }
    };

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            super.handleMessage(message);
            String result = (String) message.obj;
            switch(message.what)
            {
                case MESSAGE_ERROR_1:
                    ProgressDialogUtil.Dismiss();
                    ShowWarning(1);
                    break;
                case MESSAGE_1:
                {
                    _btn1.setEnabled(true);
                    _btn1.setBackgroundColor(Color.argb(255, 0, 133, 119));
                    ProgressDialogUtil.Dismiss();
                    break;
                }
                default:
                    break;
            }
        }
    };

    private void ShowWarning(int param)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setTitle("警告");
        switch(param)
        {
            case 1:
                builder.setMessage("该设备的连接已断开!,请重试!");
                builder.setPositiveButton("知道了", (dialog, which) ->
                {
                    BluetoothFragment_List bluetoothFragment_list = BluetoothFragment_List.newInstance("", "");
                    getFragmentManager().beginTransaction().replace(R.id.fragment_bluetooth, bluetoothFragment_list, "bluetoothFragment_list").commitNow();
                });
                break;
            case 2:
                builder.setMessage("未获取到蓝牙,请重试!");
                builder.setPositiveButton("知道了", (dialog, which) ->
                {
                    BluetoothFragment_List bluetoothFragment_list = BluetoothFragment_List.newInstance("", "");
                    getFragmentManager().beginTransaction().replace(R.id.fragment_bluetooth, bluetoothFragment_list, "bluetoothFragment_list").commitNow();
                });
                break;
        }
        builder.show();
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.btn_return_db:
            case R.id.btn2_db:
            {
                ProgressDialogUtil.Dismiss();
                BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
                getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
                getFragmentManager().beginTransaction().remove(BluetoothFragment_DB.this).commitNow();
                break;
            }
            case R.id.btn1_db:
            {
                break;
            }
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
            _bluetoothDevice = getArguments().getParcelable(ARG_PARAM1);
            _uuidMap = (Map<String, UUID>) getArguments().getSerializable(ARG_PARAM2);
        }
        _context = getContext();
        BTUtil.Init(_context, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        _view = inflater.inflate(R.layout.fragment_bluetooth_db, container, false);
        //强制获得焦点
        _view.requestFocus();
        _view.setFocusable(true);
        _view.setFocusableInTouchMode(true);
        _view.setOnKeyListener(backListener);
        _context = getContext();
        _btnReturn = _view.findViewById(R.id.btn_return_db);
        _btnReturn.setOnClickListener(this);
        _btn1 = _view.findViewById(R.id.btn1_db);
        _btn2 = _view.findViewById(R.id.btn2_db);
        _txt1 = _view.findViewById(R.id.text1_db);
        _txt2 = _view.findViewById(R.id.text2_db);
        _txt3 = _view.findViewById(R.id.text3_db);
        _txt4 = _view.findViewById(R.id.text4_db);
        _btn1.setOnClickListener(this::onClick);
        _btn2.setOnClickListener(this::onClick);
        if(_bluetoothDevice != null)
        {
            _txt1.setText(_bluetoothDevice.getAddress());
            _txt2.setText(_bluetoothDevice.getName());
            _txt3.setText("");
            _txt4.setText("");
            Log.d(TAG, ">>>开始连接...");
            BTUtil.ConnectDevice(_bluetoothDevice, _uuidMap);
        }
        else
        {
            ShowWarning(2);
        }
        return _view;
    }

    @Override
    public void OnConnected()
    {
        Log.d(TAG, ">>>成功建立连接!");
        Message message = new Message();
        message.what = MESSAGE_1;
        handler.sendMessage(message);
    }

    @Override
    public void OnConnecting()
    {
        ProgressDialogUtil.ShowProgressDialog(_context, "正在连接...");
    }

    @Override
    public void OnDisConnected()
    {
        Log.d(TAG, ">>>连接已断开!");
        Message message = new Message();
        message.what = MESSAGE_ERROR_1;
        handler.sendMessage(message);
    }

    @Override
    public void OnWriteSuccess(byte[] byteArray)
    {
        String result = ConvertUtil.ByteArrayToHexStr(byteArray);
        result = ConvertUtil.HexStrAddCharacter(result, " ");
        Log.d(TAG, ">>>发送:" + result);
    }

    @Override
    public void OnReadSuccess(byte[] byteArray)
    {
        String result = ConvertUtil.ByteArrayToHexStr(byteArray);
        result = ConvertUtil.HexStrAddCharacter(result, " ");
        Log.d(TAG, ">>>接收:" + result);
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
        if(_onFragmentInteractionListener != null)
        {
            _onFragmentInteractionListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener)
        {
            _onFragmentInteractionListener = (OnFragmentInteractionListener) context;
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
        _onFragmentInteractionListener = null;
        BTUtil.DisConnGatt();
        _bluetoothDevice = null;
    }
}