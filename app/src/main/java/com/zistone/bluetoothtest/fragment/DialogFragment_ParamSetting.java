package com.zistone.bluetoothtest.fragment;

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

import com.zistone.bluetoothtest.MainActivity;
import com.zistone.bluetoothtest.R;

/**
 * 修改内部控制参数
 * <p>
 * 要查看修改的结果需要发送内部控制参数的查询指令,因为该指令与开门的指令索引冲突导致发送以后收到的会是开门的结果
 */
public class DialogFragment_ParamSetting extends DialogFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
    private static final String TAG = "DialogFragment_ParamSetting";
    private static final String ARG_PARAM1 = "param1";
    private Context m_context;
    private View m_view;
    private Button m_button1;
    private Button m_button2;
    private CheckBox m_checkBox1;
    private CheckBox m_checkBox2;
    private CheckBox m_checkBox3;
    private CheckBox m_checkBox4;
    private CheckBox m_checkBox5;
    private CheckBox m_checkBox6;
    private CheckBox m_checkBox7;
    private CheckBox m_checkBox8;

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
                String bitStr8 = m_checkBox8.isChecked() ? "1" : "0";
                String bitStr7 = m_checkBox7.isChecked() ? "1" : "0";
                String bitStr6 = m_checkBox6.isChecked() ? "1" : "0";
                String bitStr5 = m_checkBox5.isChecked() ? "1" : "0";
                String bitStr4 = m_checkBox4.isChecked() ? "1" : "0";
                String bitStr3 = m_checkBox3.isChecked() ? "1" : "0";
                String bitStr2 = m_checkBox2.isChecked() ? "1" : "0";
                String bitStr1 = m_checkBox1.isChecked() ? "1" : "0";
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
                break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_button1 = m_view.findViewById(R.id.btn1_paramsetting);
        m_button2 = m_view.findViewById(R.id.btn2_paramsetting);
        m_checkBox1 = m_view.findViewById(R.id.cbx1_paramsetting);
        m_checkBox2 = m_view.findViewById(R.id.cbx2_paramsetting);
        m_checkBox3 = m_view.findViewById(R.id.cbx3_paramsetting);
        m_checkBox4 = m_view.findViewById(R.id.cbx4_paramsetting);
        m_checkBox5 = m_view.findViewById(R.id.cbx5_paramsetting);
        m_checkBox6 = m_view.findViewById(R.id.cbx6_paramsetting);
        m_checkBox7 = m_view.findViewById(R.id.cbx7_paramsetting);
        m_checkBox8 = m_view.findViewById(R.id.cbx8_paramsetting);
        m_button1.setOnClickListener(this::onClick);
        m_button2.setOnClickListener(this::onClick);
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
            m_checkBox8.setChecked(true);
        else
            m_checkBox8.setChecked(false);
        if(bitStr7.equals("1"))
            m_checkBox7.setChecked(true);
        else
            m_checkBox7.setChecked(false);
        if(bitStr6.equals("1"))
            m_checkBox6.setChecked(true);
        else
            m_checkBox6.setChecked(false);
        if(bitStr5.equals("1"))
            m_checkBox5.setChecked(true);
        else
            m_checkBox5.setChecked(false);
        if(bitStr4.equals("1"))
            m_checkBox4.setChecked(true);
        else
            m_checkBox4.setChecked(false);
        if(bitStr3.equals("1"))
            m_checkBox3.setChecked(true);
        else
            m_checkBox3.setChecked(false);
        if(bitStr2.equals("1"))
            m_checkBox2.setChecked(true);
        else
            m_checkBox2.setChecked(false);
        if(bitStr1.equals("1"))
            m_checkBox1.setChecked(true);
        else
            m_checkBox1.setChecked(false);
        m_checkBox1.setOnCheckedChangeListener(this::onCheckedChanged);
        m_checkBox2.setOnCheckedChangeListener(this::onCheckedChanged);
        m_checkBox3.setOnCheckedChangeListener(this::onCheckedChanged);
        m_checkBox4.setOnCheckedChangeListener(this::onCheckedChanged);
        m_checkBox5.setOnCheckedChangeListener(this::onCheckedChanged);
        m_checkBox6.setOnCheckedChangeListener(this::onCheckedChanged);
        m_checkBox7.setOnCheckedChangeListener(this::onCheckedChanged);
        m_checkBox8.setOnCheckedChangeListener(this::onCheckedChanged);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        m_view = LayoutInflater.from(getActivity()).inflate(R.layout.dialogfragment_paramsetting, null);
        m_context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(m_view);
        return builder.create();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        switch(buttonView.getId())
        {
            case R.id.cbx1_paramsetting:
                break;
            case R.id.cbx2_paramsetting:
                break;
            case R.id.cbx3_paramsetting:
                break;
            case R.id.cbx4_paramsetting:
                break;
            case R.id.cbx5_paramsetting:
                break;
            case R.id.cbx6_paramsetting:
                break;
            case R.id.cbx7_paramsetting:
                break;
            case R.id.cbx8_paramsetting:
                break;
        }
    }
}
