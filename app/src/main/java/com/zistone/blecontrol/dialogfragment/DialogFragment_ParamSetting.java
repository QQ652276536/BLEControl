package com.zistone.blecontrol.dialogfragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.zistone.blecontrol.MainActivity;
import com.zistone.blecontrol.R;

/**
 * 修改内部控制参数
 * <p>
 * 要查看修改的结果需要发送内部控制参数的查询指令,因为该指令与开门的指令索引冲突导致发送以后收到的会是开门的结果
 */
public class DialogFragment_ParamSetting extends DialogFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
    private static final String TAG = "DialogFragment_ParamSetting";
    private static final String ARG_PARAM1 = "param1";
    private Context _context;
    private View _view;
    private Button _btn1;
    private Button _btn2;
    private CheckBox _chk1;
    private CheckBox _chk2;
    private CheckBox _chk3;
    private CheckBox _chk4;
    private CheckBox _chk5;
    private CheckBox _chk6;
    private CheckBox _chk7;
    private CheckBox _chk8;

    public static DialogFragment_ParamSetting newInstance(String[] strArray)
    {
        DialogFragment_ParamSetting fragment = new DialogFragment_ParamSetting();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, strArray);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.btn1_paramsetting:
                dismiss();
                break;
            case R.id.btn2_paramsetting:
            {
                String bitStr8 = _chk8.isChecked() ? "1" : "0";
                String bitStr7 = _chk7.isChecked() ? "1" : "0";
                String bitStr6 = _chk6.isChecked() ? "1" : "0";
                String bitStr5 = _chk5.isChecked() ? "1" : "0";
                String bitStr4 = _chk4.isChecked() ? "1" : "0";
                String bitStr3 = _chk3.isChecked() ? "1" : "0";
                String bitStr2 = _chk2.isChecked() ? "1" : "0";
                String bitStr1 = _chk1.isChecked() ? "1" : "0";
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(bitStr1);
                stringBuffer.append(bitStr2);
                stringBuffer.append(bitStr3);
                stringBuffer.append(bitStr4);
                stringBuffer.append(bitStr5);
                stringBuffer.append(bitStr6);
                stringBuffer.append(bitStr7);
                stringBuffer.append(bitStr8);
                String bitStr = stringBuffer.toString();
                Log.d(TAG, String.format(">>>发送参数设置(Bit):\n门检测开关用采用常开型(关门开路)%s\n门锁检测开定于关用采用常开型(锁上开路)%s\n正常开锁不告警%s\n有外电可以进入维护方式%s\n启用软关机%s\n不检测强磁%s\n使用低磁检测阀值%s\n启用DEBUG软串口%s", bitStr1, bitStr2, bitStr3, bitStr4, bitStr5, bitStr6, bitStr7, bitStr8));
                int value = Integer.parseInt(bitStr, 2);
                String hexStr = Integer.toHexString(value);
                hexStr = hexStr.length() == 1 ? "0" + hexStr : hexStr;
                String data = "680000000000006810000587" + hexStr.toUpperCase() + "000000EA16";
                Intent intent = new Intent();
                intent.putExtra("ParamSetting", data);
                getTargetFragment().onActivityResult(MainActivity.ACTIVITYRESULT_PARAMSETTING, Activity.RESULT_OK, intent);
            }
            break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        _btn1 = _view.findViewById(R.id.btn1_paramsetting);
        _btn2 = _view.findViewById(R.id.btn2_paramsetting);
        _chk1 = _view.findViewById(R.id.chk1_paramsetting);
        _chk2 = _view.findViewById(R.id.chk2_paramsetting);
        _chk3 = _view.findViewById(R.id.chk3_paramsetting);
        _chk4 = _view.findViewById(R.id.chk4_paramsetting);
        _chk5 = _view.findViewById(R.id.chk5_paramsetting);
        _chk6 = _view.findViewById(R.id.chk6_paramsetting);
        _chk7 = _view.findViewById(R.id.chk7_paramsetting);
        _chk8 = _view.findViewById(R.id.chk8_paramsetting);
        _btn1.setOnClickListener(this::onClick);
        _btn2.setOnClickListener(this::onClick);
        String[] strArray = (String[]) getArguments().getSerializable(ARG_PARAM1);
        //启用DEBUG软串口
        String bitStr8 = strArray[7];
        //使用低磁检测阀值
        String bitStr7 = strArray[6];
        //不检测强磁
        String bitStr6 = strArray[5];
        //启用软关机
        String bitStr5 = strArray[4];
        //有外电可以进入维护方式
        String bitStr4 = strArray[3];
        //正常开锁不告警
        String bitStr3 = strArray[2];
        //锁检测开关(锁上开路)
        String bitStr2 = strArray[1];
        //门检测开关(关门开路)
        String bitStr1 = strArray[0];
        Log.d(TAG, String.format(">>>收到查询到的参数(Bit):\n门检测开关(关门开路)%s\n锁检测开关(锁上开路)%s\n正常开锁不告警%s\n有外电可以进入维护方式%s\n启用软关机%s\n不检测强磁%s\n使用低磁检测阀值%s\n启用DEBUG软串口%s", bitStr1, bitStr2, bitStr3, bitStr4, bitStr5, bitStr6, bitStr7, bitStr8));
        if(bitStr8.equals("1"))
            _chk8.setChecked(true);
        else
            _chk8.setChecked(false);
        if(bitStr7.equals("1"))
            _chk7.setChecked(true);
        else
            _chk7.setChecked(false);
        if(bitStr6.equals("1"))
            _chk6.setChecked(true);
        else
            _chk6.setChecked(false);
        if(bitStr5.equals("1"))
            _chk5.setChecked(true);
        else
            _chk5.setChecked(false);
        if(bitStr4.equals("1"))
            _chk4.setChecked(true);
        else
            _chk4.setChecked(false);
        if(bitStr3.equals("1"))
            _chk3.setChecked(true);
        else
            _chk3.setChecked(false);
        if(bitStr2.equals("1"))
            _chk2.setChecked(true);
        else
            _chk2.setChecked(false);
        if(bitStr1.equals("1"))
            _chk1.setChecked(true);
        else
            _chk1.setChecked(false);
        _chk1.setOnCheckedChangeListener(this::onCheckedChanged);
        _chk2.setOnCheckedChangeListener(this::onCheckedChanged);
        _chk3.setOnCheckedChangeListener(this::onCheckedChanged);
        _chk4.setOnCheckedChangeListener(this::onCheckedChanged);
        _chk5.setOnCheckedChangeListener(this::onCheckedChanged);
        _chk6.setOnCheckedChangeListener(this::onCheckedChanged);
        _chk7.setOnCheckedChangeListener(this::onCheckedChanged);
        _chk8.setOnCheckedChangeListener(this::onCheckedChanged);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        _view = LayoutInflater.from(getActivity()).inflate(R.layout.dialogfragment_paramsetting, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setView(_view);
        return builder.create();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        switch(buttonView.getId())
        {
            case R.id.chk1_paramsetting:
                break;
            case R.id.chk2_paramsetting:
                break;
            case R.id.chk3_paramsetting:
                break;
            case R.id.chk4_paramsetting:
                break;
            case R.id.chk5_paramsetting:
                break;
            case R.id.chk6_paramsetting:
                break;
            case R.id.chk7_paramsetting:
                break;
            case R.id.chk8_paramsetting:
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
        }
        _context = getContext();
    }
}
