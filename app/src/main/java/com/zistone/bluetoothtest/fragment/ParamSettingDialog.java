package com.zistone.bluetoothtest.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.zistone.bluetoothtest.R;
import com.zistone.bluetoothtest.control.MyRadioGroup;
import com.zistone.bluetoothtest.util.ConvertUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * 修改内部控制参数
 * <p>
 * 要查看修改的结果需要发送内部控制参数的查询指令,因为该指令与开门的指令索引冲突导致发送以后收到的会是开门的结果
 */
public class ParamSettingDialog extends DialogFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
    private static final String TAG = "ParamSettingDialog";
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

    public static ParamSettingDialog newInstance(String[] strArray)
    {
        ParamSettingDialog fragment = new ParamSettingDialog();
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
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(m_checkBox8.isChecked() ? "1" : "0");
                stringBuffer.append(m_checkBox7.isChecked() ? "1" : "0");
                stringBuffer.append(m_checkBox6.isChecked() ? "1" : "0");
                stringBuffer.append(m_checkBox5.isChecked() ? "1" : "0");
                stringBuffer.append(m_checkBox4.isChecked() ? "1" : "0");
                stringBuffer.append(m_checkBox3.isChecked() ? "1" : "0");
                stringBuffer.append(m_checkBox2.isChecked() ? "1" : "0");
                stringBuffer.append(m_checkBox1.isChecked() ? "1" : "0");
                String bitStr = stringBuffer.toString();
                int value = Integer.parseInt(bitStr, 2);
                String hexStr = Integer.toHexString(value);
                hexStr = hexStr.length() == 1 ? "0" + hexStr : hexStr;
                String data = "680000000000006810000587" + hexStr + "000000EA16";
                Intent intent = new Intent();
                intent.putExtra("ParamSetting", data);
                getTargetFragment().onActivityResult(2, Activity.RESULT_OK, intent);
                break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //设置背景透明
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
        if(strArray[0].equalsIgnoreCase("1"))
            m_checkBox1.setChecked(true);
        else
            m_checkBox1.setChecked(false);
        if(strArray[1].equalsIgnoreCase("1"))
            m_checkBox2.setChecked(true);
        else
            m_checkBox1.setChecked(false);
        if(strArray[2].equalsIgnoreCase("1"))
            m_checkBox3.setChecked(true);
        else
            m_checkBox1.setChecked(false);
        if(strArray[3].equalsIgnoreCase("1"))
            m_checkBox4.setChecked(true);
        else
            m_checkBox1.setChecked(false);
        if(strArray[4].equalsIgnoreCase("1"))
            m_checkBox5.setChecked(true);
        else
            m_checkBox1.setChecked(false);
        if(strArray[5].equalsIgnoreCase("1"))
            m_checkBox6.setChecked(true);
        else
            m_checkBox1.setChecked(false);
        if(strArray[6].equalsIgnoreCase("1"))
            m_checkBox7.setChecked(true);
        else
            m_checkBox1.setChecked(false);
        if(strArray[7].equalsIgnoreCase("1"))
            m_checkBox8.setChecked(true);
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
        m_view = LayoutInflater.from(getActivity()).inflate(R.layout.paramsetting_dialog, null);
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
