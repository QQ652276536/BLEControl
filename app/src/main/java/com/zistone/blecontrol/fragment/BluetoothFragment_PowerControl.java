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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zistone.MainActivity;
import com.zistone.blecontrol.R;
import com.zistone.blecontrol.control.MyScrollView;
import com.zistone.blecontrol.dialogfragment.DialogFragment_OTA;
import com.zistone.blecontrol.dialogfragment.DialogFragment_ParamSetting;
import com.zistone.blecontrol.dialogfragment.DialogFragment_WriteValue;
import com.zistone.blecontrol.util.BTListener;
import com.zistone.blecontrol.util.BTUtil;
import com.zistone.blecontrol.util.ConvertUtil;
import com.zistone.blecontrol.util.ProgressDialogUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothFragment_PowerControl extends Fragment implements View.OnClickListener, BTListener
{
    private static final String TAG = "BluetoothFragment_PowerControl";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String SEARCH_CONTROLPARAM_COMM = "680000000000006810000186EA16";
    private static final int MESSAGE_ERROR_1 = -1;
    private static final int MESSAGE_ERROR_2 = -2;
    private static final int MESSAGE_ERROR_3 = -3;
    private static final int MESSAGE_1 = 100;
    private static final int RECEIVE_OPENDOOR = 0;
    private static final int SEND_READCAR = 1;
    private static final int RECEIVE_READCAR = 102;
    private static final int SEND_BATTERY = 2;
    private static final int RECEIVE_BATTERY = 202;
    private static final int SEND_MAGNETIC = 3;
    private static final int RECEIVE_MAGNETIC = 302;
    private static final int SEND_DOORSTATE = 4;
    private static final int RECEIVE_DOORSTATE = 402;
    private static final int SEND_TESTA = 80;
    private static final int RECEIVE_TESTA = 8002;
    private static final int SEND_OPENDOORS1 = 81;
    private static final int RECEIVE_OPENDOORS1 = 8102;
    private static final int SEND_OPENDOORS2 = 82;
    private static final int RECEIVE_OPENDOORS2 = 8202;
    private static final int SEND_OPENALLDOORS = 83;
    private static final int RECEIVE_OPENALLDOORS = 8302;
    private static final int SEND_SEARCH_CONTROLPARAM = 86;
    private static final int RECEIVE_SEARCH_CONTROLPARAM = 8602;
    private static final int SEND_SET_CONTROLPARAM = 87;
    private static final int RECEIVE_SET_CONTROLPARAM = 8702;

    private BluetoothDevice _bluetoothDevice;

    private OnFragmentInteractionListener _onFragmentInteractionListener;
    private Context _context;
    private View _view;
    private Toolbar _toolbar;
    private ImageButton _btnReturn;
    private TextView _debugView;
    private Button _btn1, _btn2, _btn3, _btn4, _btn5;
    private TextView _txt1;
    private TextView _txt2;
    private TextView _txt3;
    private TextView _txt4;
    private TextView _txt5;
    private TextView _txt6;
    private StringBuffer _stringBuffer = new StringBuffer();
    private Timer _refreshTimer;
    private TimerTask _refreshTask;
    private MyScrollView _scrollView;
    private LinearLayout _llPowerControl;
    private DialogFragment_WriteValue _writeValue;
    private DialogFragment_ParamSetting _paramSetting;
    private DialogFragment_OTA _ota;
    //是否连接成功、是否打开参数设置界面
    private boolean _connectedSuccess = false, _isOpenParamSetting = false;
    private static Listener _listener;
    private Map<String, UUID> _uuidMap;
    private ProgressDialogUtil.Listener _progressDialogUtilListener;

    @Override
    public void OnConnected()
    {
        ProgressDialogUtil.Dismiss();
        Log.d(TAG, ">>>成功建立连接!");
        //轮询
        Message message = handler.obtainMessage(MESSAGE_1, "");
        handler.sendMessage(message);
        //连接成功的回调
        _listener.ConnectSuccessListener();
    }

    @Override
    public void OnConnecting()
    {
        ProgressDialogUtil.ShowProgressDialog(_context, _progressDialogUtilListener, "正在连接...");
    }

    @Override
    public void OnDisConnected()
    {
        Log.d(TAG, ">>>连接已断开!");
        Message message = handler.obtainMessage(MESSAGE_ERROR_1, "");
        handler.sendMessage(message);
    }

    @Override
    public void OnWriteSuccess(byte[] byteArray)
    {
        String result = ConvertUtil.ByteArrayToHexStr(byteArray);
        result = ConvertUtil.HexStrAddCharacter(result, " ");
        String[] strArray = result.split(" ");
        String indexStr = strArray[11];
        switch(indexStr)
        {
            //发送开门指令
            case "00":
                break;
            //发送读卡指令
            case "01":
                break;
            //发送测量电池电压指令
            case "02":
                break;
            //发送测量磁场强度指令
            case "03":
                break;
            //发送测量门状态指令
            case "04":
                break;
            //发送综合测量指令
            case "80":
                break;
            //发送开一号门锁指令
            case "81":
                break;
            //发送开二号门锁指令
            case "82":
                break;
            //发送开全部门锁指令
            case "83":
                break;
            //发送查询内部控制参数指令
            case "86":
                break;
            //发送修改内部控制参数指令
            case "87":
                break;
        }
    }

    @Override
    public void OnReadSuccess(byte[] byteArray)
    {
        String result = ConvertUtil.ByteArrayToHexStr(byteArray);
        result = ConvertUtil.HexStrAddCharacter(result, " ");
        //Log.d(TAG, ">>>接收:" + result);
        String[] strArray = result.split(" ");
        //一个包(20个字节)
        if(strArray[0].equals("68") && strArray[strArray.length - 1].equals("16"))
        {
            Resolve(result);
            //清空缓存
            _stringBuffer = new StringBuffer();
        }
        //分包
        else
        {
            if(!strArray[strArray.length - 1].equals("16"))
            {
                _stringBuffer.append(result + " ");
            }
            //最后一个包
            else
            {
                _stringBuffer.append(result);
                result = _stringBuffer.toString();
                Resolve(result);
                //清空缓存
                _stringBuffer = new StringBuffer();
            }
        }
    }

    private void InitListener()
    {
        _progressDialogUtilListener = new ProgressDialogUtil.Listener()
        {
            @Override
            public void OnDismiss()
            {
                if(_btn1.getText().toString().equals("断开") && !_connectedSuccess)
                {
                    _btn1.setText("连接");
                    DisConnect();
                }
            }
        };
    }

    public interface Listener
    {
        void ConnectSuccessListener();
    }

    public static BluetoothFragment_PowerControl newInstance(Listener listener, BluetoothDevice bluetoothDevice, Map<String, UUID> map)
    {
        BluetoothFragment_PowerControl fragment = new BluetoothFragment_PowerControl();
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
    private View.OnKeyListener backListener = new View.OnKeyListener()
    {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
            {
                BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
                getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
                getFragmentManager().beginTransaction().remove(BluetoothFragment_PowerControl.this).commitNow();
                return true;
            }
            return false;
        }
    };

    private void DisConnect()
    {
        _connectedSuccess = false;
        _btn2.setEnabled(false);
        _btn3.setEnabled(false);
        _btn4.setEnabled(false);
        if(_refreshTask != null)
        {
            _refreshTask.cancel();
        }
        if(_refreshTimer != null)
        {
            _refreshTimer.cancel();
        }
        BTUtil.DisConnGatt();
        _txt1.setText("Null");
        _txt1.setTextColor(Color.GRAY);
        _txt2.setText("Null");
        _txt2.setTextColor(Color.GRAY);
        _txt3.setText("Null");
        _txt3.setTextColor(Color.GRAY);
        _txt4.setText("Null");
        _txt4.setTextColor(Color.GRAY);
        _txt5.setText("Null");
        _txt6.setText("Null");
    }

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
                    DisConnect();
                    ProgressDialogUtil.ShowWarning(_context, "警告", "该设备的连接已断开,如需再次连接请重试!");
                    break;
                case MESSAGE_1:
                {
                    _btn2.setEnabled(true);
                    _btn3.setEnabled(true);
                    _btn4.setEnabled(true);
                    _refreshTimer = new Timer();
                    _refreshTask = new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            getActivity().runOnUiThread(() ->
                            {
                                try
                                {
                                    //综合测试
                                    String hexStr = "680000000000006810000180E616";
                                    //Log.d(TAG, ">>>发送综合测试:" + hexStr);
                                    BTUtil.SendComm(hexStr);
                                    Thread.sleep(100);
                                }
                                catch(InterruptedException e)
                                {
                                    e.printStackTrace();
                                }
                            });
                        }
                    };
                    //任务、延迟执行时间、重复调用间隔,Timer和TimerTask在调用cancel()取消后不能再执行schedule语句
                    _refreshTimer.schedule(_refreshTask, 0, 2 * 1000);
                    _connectedSuccess = true;
                }
                break;
                case RECEIVE_OPENDOOR:
                {
                    if(result.equalsIgnoreCase("opendoor"))
                    {
                        _debugView.append("发送开门指令 ");
                    }
                    else if(result.equalsIgnoreCase("doorisopen"))
                    {
                        _debugView.append("收到:门【已打开】\n");
                        _txt1.setText("已开");
                    }
                    else
                    {
                        _debugView.append("收到:门【未打开】\n");
                        _txt1.setText("未开");
                    }
                    //定位到最后一行
                    int offset = _debugView.getLineCount() * _debugView.getLineHeight();
                    //如果文本的高度大于ScrollView的,就自动滑动
                    if(offset > _scrollView.getHeight())
                    {
                        _debugView.scrollTo(0, offset - _scrollView.getHeight());
                    }
                }
                break;
                //电池电压
                case RECEIVE_BATTERY:
                    _txt5.setText(result + "mV");
                    break;
                //磁场强度
                case RECEIVE_MAGNETIC:
                    _txt6.setText(result);
                    break;
                //门状态
                case RECEIVE_DOORSTATE:
                    _txt1.setText(result);
                    break;
                //综合测试
                case RECEIVE_TESTA:
                {
                    String strs[] = result.split(",");
                    String doorState1 = strs[0];
                    if(doorState1.equalsIgnoreCase("1"))
                    {
                        _txt1.setText("已开");
                        _txt1.setTextColor(Color.GREEN);
                    }
                    else
                    {
                        _txt1.setText("已关");
                        _txt1.setTextColor(Color.RED);
                    }
                    String lockState1 = strs[1];
                    if(lockState1.equalsIgnoreCase("1"))
                    {
                        _txt2.setText("已开");
                        _txt2.setTextColor(Color.GREEN);
                    }
                    else
                    {
                        _txt2.setText("已关");
                        _txt2.setTextColor(Color.RED);
                    }
                    String doorState2 = strs[2];
                    if(doorState2.equalsIgnoreCase("1"))
                    {
                        _txt3.setText("已开");
                        _txt3.setTextColor(Color.GREEN);
                    }
                    else
                    {
                        _txt3.setText("已关");
                        _txt3.setTextColor(Color.RED);
                    }
                    String lockState2 = strs[3];
                    if(lockState2.equalsIgnoreCase("1"))
                    {
                        _txt4.setText("已开");
                        _txt4.setTextColor(Color.GREEN);
                    }
                    else
                    {
                        _txt4.setText("已关");
                        _txt4.setTextColor(Color.RED);
                    }
                    _txt5.setText(strs[4] + "mV");
                    _txt6.setText(String.format("下端:%sGs 上端:%sGs 前端:%sGs", strs[5], strs[6], strs[7]));
                }
                break;
                //一号门锁
                case RECEIVE_OPENDOORS1:
                    break;
                //二号门锁
                case RECEIVE_OPENDOORS2:
                {
                    byte[] bytes = ConvertUtil.HexStrToByteArray(result);
                    String bitStr = ConvertUtil.ByteToBit(bytes[0]);
                    String doorState2 = String.valueOf(bitStr.charAt(7));
                    if(doorState2.equalsIgnoreCase("1"))
                        _txt3.setText("已开");
                    else
                        _txt3.setText("已关");
                    String lockState2 = String.valueOf(bitStr.charAt(6));
                    if(lockState2.equalsIgnoreCase("1"))
                        _txt4.setText("已开");
                    else
                        _txt4.setText("已关");
                }
                break;
                //全部门锁
                case RECEIVE_OPENALLDOORS:
                {
                    byte[] bytes = ConvertUtil.HexStrToByteArray(result);
                    String bitStr = ConvertUtil.ByteToBit(bytes[0]);
                    String doorState1 = String.valueOf(bitStr.charAt(7));
                    if(doorState1.equalsIgnoreCase("1"))
                        _txt1.setText("已开");
                    else
                        _txt1.setText("已关");
                    String lockState1 = String.valueOf(bitStr.charAt(6));
                    if(lockState1.equalsIgnoreCase("1"))
                        _txt2.setText("已开");
                    else
                        _txt2.setText("已关");
                    String doorState2 = String.valueOf(bitStr.charAt(5));
                    if(doorState2.equalsIgnoreCase("1"))
                        _txt3.setText("已开");
                    else
                        _txt3.setText("已关");
                    String lockState2 = String.valueOf(bitStr.charAt(4));
                    if(lockState2.equalsIgnoreCase("1"))
                        _txt4.setText("已开");
                    else
                        _txt4.setText("已关");
                }
                break;
                //解析查询到的内部控制参数
                case RECEIVE_SEARCH_CONTROLPARAM:
                {
                    byte[] bytes = ConvertUtil.HexStrToByteArray(result);
                    String bitStr = ConvertUtil.ByteToBit(bytes[0]);
                    //启用DEBUG软串口
                    String bitStr8 = String.valueOf(bitStr.charAt(7));
                    //使用低磁检测阀值
                    String bitStr7 = String.valueOf(bitStr.charAt(6));
                    //不检测强磁
                    String bitStr6 = String.valueOf(bitStr.charAt(5));
                    //启用软关机
                    String bitStr5 = String.valueOf(bitStr.charAt(4));
                    //有外电可以进入维护方式
                    String bitStr4 = String.valueOf(bitStr.charAt(3));
                    //正常开锁不告警
                    String bitStr3 = String.valueOf(bitStr.charAt(2));
                    //锁检测开关(锁上开路)
                    String bitStr2 = String.valueOf(bitStr.charAt(1));
                    //门检测开关(关门开路)
                    String bitStr1 = String.valueOf(bitStr.charAt(0));
                    Log.d(TAG, String.format(">>>收到查询到的参数(Bit):\n门检测开关(关门开路)%s\n锁检测开关(锁上开路)%s\n正常开锁不告警%s\n有外电可以进入维护方式%s\n启用软关机%s\n不检测强磁%s\n使用低磁检测阀值%s\n启用DEBUG软串口%s", bitStr1, bitStr2, bitStr3, bitStr4, bitStr5, bitStr6, bitStr7, bitStr8));
                    //打开控制参数修改页面的时候将查询结果传递过去,此时可以不输出调试信息
                    if(_isOpenParamSetting)
                    {
                        if(_paramSetting == null)
                        {
                            _paramSetting = DialogFragment_ParamSetting.newInstance(new String[]{
                                    bitStr1, bitStr2, bitStr3, bitStr4, bitStr5, bitStr6, bitStr7, bitStr8
                            });
                            _paramSetting.setTargetFragment(BluetoothFragment_PowerControl.this, 1);
                        }
                        _paramSetting.show(getFragmentManager(), "DialogFragment_ParamSetting");
                        _isOpenParamSetting = false;
                    }
                    else
                    {
                        if(bitStr8.equalsIgnoreCase("1"))
                        {
                            _debugView.append("\n收到:\n启用DEBUG软串口【启用】\n");
                        }
                        else
                        {
                            _debugView.append("\n收到:\n启用DEBUG软串口【禁用】\n");
                        }
                        if(bitStr7.equalsIgnoreCase("1"))
                        {
                            _debugView.append("使用低磁检测阀值【启用】\n");
                        }
                        else
                        {
                            _debugView.append("使用低磁检测阀值【禁用】\n");
                        }
                        if(bitStr6.equalsIgnoreCase("1"))
                        {
                            _debugView.append("不检测强磁【启用】\n");
                        }
                        else
                        {
                            _debugView.append("不检测强磁【禁用】\n");
                        }
                        if(bitStr5.equalsIgnoreCase("1"))
                        {
                            _debugView.append("启用软关机【启用】\n");
                        }
                        else
                        {
                            _debugView.append("启用软关机【禁用】\n");
                        }
                        if(bitStr4.equalsIgnoreCase("1"))
                        {
                            _debugView.append("有外电可以进入维护方式【启用】\n");
                        }
                        else
                        {
                            _debugView.append("有外电可以进入维护方式【禁用】\n");
                        }
                        if(bitStr3.equalsIgnoreCase("1"))
                        {
                            _debugView.append("正常开锁不告警【启用】\n");
                        }
                        else
                        {
                            _debugView.append("正常开锁不告警【禁用】\n");
                        }
                        if(bitStr2.equalsIgnoreCase("1"))
                        {
                            _debugView.append("锁检测开关(锁上开路)【启用】\n");
                        }
                        else
                        {
                            _debugView.append("锁检测开关(锁上开路)【禁用】\n");
                        }
                        if(bitStr1.equalsIgnoreCase("1"))
                        {
                            _debugView.append("门检测开关(关门开路)【启用】\n");
                        }
                        else
                        {
                            _debugView.append("门检测开关(关门开路)【禁用】\n");
                        }
                    }
                    //定位到最后一行
                    int offset = _debugView.getLineCount() * _debugView.getLineHeight();
                    //如果文本的高度大于ScrollView的,就自动滑动
                    if(offset > _debugView.getHeight())
                    {
                        _debugView.scrollTo(0, offset - _debugView.getHeight());
                    }
                }
                break;
                //修改内部控制参数
                case SEND_SET_CONTROLPARAM:
                {
                    Log.d(TAG, ">>>发送参数设置:" + result);
                    BTUtil.SendComm(result);
                    _debugView.append("发送参数设置指令 ");
                    int offset = _debugView.getLineCount() * _debugView.getLineHeight();
                    if(offset > _scrollView.getHeight())
                    {
                        _debugView.scrollTo(0, offset - _scrollView.getHeight());
                    }
                }
                break;
                //发送查询内部控制参数的指令
                case SEND_SEARCH_CONTROLPARAM:
                {
                    BTUtil.SendComm(SEARCH_CONTROLPARAM_COMM);
                }
                break;
            }
        }
    };

    public void onButtonPressed(Uri uri)
    {
        if(_onFragmentInteractionListener != null)
        {
            _onFragmentInteractionListener.onFragmentInteraction(uri);
        }
    }

    /**
     * Activity中加载Fragment时会要求实现onFragmentInteraction(Uri uri)方法,此方法主要作用是从fragment向activity传递数据
     */
    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    /**
     * 解析硬件返回的数据
     *
     * @param data
     */
    private void Resolve(String data)
    {
        //Log.d(TAG, ">>>共接收:" + data);
        String[] strArray = data.split(" ");
        String indexStr = strArray[12];
        Message message = new Message();
        switch(indexStr)
        {
            //开门
            case "00":
            {
                message.what = RECEIVE_OPENDOOR;
                if(strArray[14].equalsIgnoreCase("00"))
                {
                    message.obj = "doorisopen";
                }
                else if(ConvertUtil.HexStrToStr(strArray[13] + strArray[14]).equalsIgnoreCase("OK"))
                {
                    message.obj = "doorisopen";
                }
                else
                {
                    message.obj = "";
                }
            }
            break;
            //读卡
            case "01":
                break;
            //电池电压
            case "02":
                break;
            //磁场强度
            case "03":
            {
                String responseValue1 = strArray[9].equals("00") ? "OK" : "Fail";
                //                String responseValue2 = ConvertUtil.HexStrToStr(strArray[14] + strArray[15] + strArray[16] + strArray[17] + strArray[18] + strArray[19] + strArray[20] + strArray[21] + strArray[22] + strArray[23] + strArray[24]);
                String responseValue2 = ConvertUtil.HexStrToStr(strArray[14] + strArray[15] + strArray[16] + strArray[17] + strArray[18]);
                message.what = RECEIVE_MAGNETIC;
                message.obj = "收到:磁场强度【" + responseValue2 + "】 ";
            }
            break;
            //测量门状态
            case "04":
            {
                message.what = RECEIVE_DOORSTATE;
                if(strArray[13].equals("01"))
                {
                    message.obj = "已关";
                }
                else
                {
                    message.obj = "已开";
                }
            }
            break;
            //全部门锁状态
            case "80":
            {
                byte[] bytes1 = ConvertUtil.HexStrToByteArray(strArray[13]);
                String bitStr = ConvertUtil.ByteToBit(bytes1[0]);
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
                message.what = RECEIVE_TESTA;
                message.obj = doorState1 + "," + lockState1 + "," + doorState2 + "," + lockState2 + "," + battery + "," + magneticDown + "," + magneticUp + "," + magneticBefore;
            }
            break;
            //开一号门锁
            case "81":
            {
                message.what = RECEIVE_OPENDOORS1;
                message.obj = "";
            }
            break;
            //开二号门锁
            case "82":
            {
                message.what = RECEIVE_OPENDOORS2;
                message.obj = strArray[13];
            }
            break;
            //开全部门锁
            case "83":
            {
                message.what = RECEIVE_OPENALLDOORS;
                message.obj = strArray[13];
            }
            break;
            //查询内部控制参数
            case "86":
            {
                message.what = RECEIVE_SEARCH_CONTROLPARAM;
                message.obj = strArray[13];
            }
            break;
            //修改内部控制参数
            case "87":
            {
                //发送查询内部控制参数的指令
                message.what = SEND_SEARCH_CONTROLPARAM;
                message.obj = "";
            }
            break;
        }
        handler.sendMessage(message);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(_connectedSuccess)
        {
            switch(requestCode)
            {
                case MainActivity.ACTIVITYRESULT_WRITEVALUE:
                {
                    String hexStr = data.getStringExtra("WriteValue");
                }
                break;
                case MainActivity.ACTIVITYRESULT_PARAMSETTING:
                {
                    String hexStr = data.getStringExtra("ParamSetting");
                    Message message = handler.obtainMessage(SEND_SET_CONTROLPARAM, hexStr);
                    handler.sendMessage(message);
                }
                break;
                case MainActivity.ACTIVITYRESULT_OTA:
                {
                    String hexStr = data.getStringExtra("OTA");
                }
                break;
            }
        }
        else
        {
            ProgressDialogUtil.ShowWarning(_context, "警告", "该设备的连接已断开,如需再次连接请重试!");
        }
        //发送内部参数以后关闭设置窗口
        _paramSetting.dismiss();
        _paramSetting = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        if(_connectedSuccess)
        {
            switch(item.getItemId())
            {
                //内部控制参数设置
                case R.id.menu_1_power:
                {
                    //先查询内部控制参数,再打开修改参数的界面
                    BTUtil.SendComm(SEARCH_CONTROLPARAM_COMM);
                    _isOpenParamSetting = true;
                }
                break;
                //写入指令
                case R.id.menu_2_power:
                {
                    _writeValue = new DialogFragment_WriteValue();
                    _writeValue.setTargetFragment(BluetoothFragment_PowerControl.this, 2);
                    _writeValue.show(getFragmentManager(), "DialogFragment_WriteValue");
                }
                break;
                //OTA
                case R.id.menu_3_power:
                {
                    _ota = DialogFragment_OTA.newInstance(_bluetoothDevice);
                    _ota.setTargetFragment(BluetoothFragment_PowerControl.this, 3);
                    _ota.show(getFragmentManager(), "DialogFragment_OTA");
                }
                break;
            }
        }
        else
        {
            ProgressDialogUtil.ShowWarning(_context, "提示", "请连接蓝牙设备!");
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        //Activity的onCreateOptionsMenu会在之前调用,即先Clear一下,这样就只有Fragment自己设置的了
        menu.clear();
        inflater.inflate(R.menu.powercontrol_menu_setting, menu);
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.btn_return:
            {
                BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
                getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
                getFragmentManager().beginTransaction().remove(BluetoothFragment_PowerControl.this).commitNow();
            }
            break;
            //连接
            case R.id.button1:
            {
                if(_bluetoothDevice != null)
                {
                    if(_btn1.getText().toString().equals("连接"))
                    {
                        _btn1.setText("断开");
                        Log.d(TAG, ">>>开始连接...");
                        BTUtil.ConnectDevice(_bluetoothDevice, _uuidMap);
                    }
                    else
                    {
                        _btn1.setText("连接");
                        DisConnect();
                    }
                }
                else
                {
                    ProgressDialogUtil.ShowWarning(_context, "提示", "未获取到蓝牙,请重试!");
                }
            }
            break;
            //开一号门锁
            case R.id.button2:
            {
                String hexStr = "680000000000006810000181E116";
                Log.d(TAG, ">>>发送开一号门锁:" + hexStr);
                BTUtil.SendComm(hexStr);
            }
            break;
            //开二号门锁
            case R.id.button3:
            {
                String hexStr = "680000000000006810000182E716";
                Log.d(TAG, ">>>发送开二号门锁:" + hexStr);
                BTUtil.SendComm(hexStr);
            }
            break;
            //开全部门锁
            case R.id.button4:
            {
                String hexStr = "680000000000006810000183E716";
                Log.d(TAG, ">>>发送开全部门锁:" + hexStr);
                BTUtil.SendComm(hexStr);
            }
            break;
            //清屏
            case R.id.button5:
                _debugView.setText("");
                break;
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
        _view = inflater.inflate(R.layout.fragment_bluetooth_powercontrol, container, false);
        //强制获得焦点
        _view.requestFocus();
        _view.setFocusable(true);
        _view.setFocusableInTouchMode(true);
        _view.setOnKeyListener(backListener);
        _toolbar = _view.findViewById(R.id.toolbar_powercontrol);
        //加上这句,才会调用Fragment的ToolBar,否则调用的是Activity传递过来的
        setHasOptionsMenu(true);
        //去掉标题
        _toolbar.setTitle("");
        //此处强转,必须是Activity才有这个方法
        ((MainActivity) getActivity()).setSupportActionBar(_toolbar);
        _txt1 = _view.findViewById(R.id.text1);
        _txt2 = _view.findViewById(R.id.text2);
        _txt3 = _view.findViewById(R.id.text3);
        _txt4 = _view.findViewById(R.id.text4);
        _txt5 = _view.findViewById(R.id.text5);
        _txt6 = _view.findViewById(R.id.text6);
        _debugView = _view.findViewById(R.id.debug_view);
        _btnReturn = _view.findViewById(R.id.btn_return);
        _btn1 = _view.findViewById(R.id.button1);
        _btn2 = _view.findViewById(R.id.button2);
        _btn3 = _view.findViewById(R.id.button3);
        _btn4 = _view.findViewById(R.id.button4);
        _btn5 = _view.findViewById(R.id.button5);
        _scrollView = _view.findViewById(R.id.scrollView);
        _llPowerControl = _view.findViewById(R.id.fragment_bluetooth_powercontrol);
        _btnReturn.setOnClickListener(this::onClick);
        _btn1.setOnClickListener(this::onClick);
        _btn2.setOnClickListener(this::onClick);
        _btn3.setOnClickListener(this::onClick);
        _btn4.setOnClickListener(this::onClick);
        _btn5.setOnClickListener(this::onClick);
        _debugView.setMovementMethod(ScrollingMovementMethod.getInstance());
        InitListener();
        return _view;
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
        if(_refreshTimer != null)
            _refreshTimer.cancel();
        if(_refreshTask != null)
            _refreshTask.cancel();
        BTUtil.DisConnGatt();
        _bluetoothDevice = null;
    }

}