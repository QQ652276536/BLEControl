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
import android.text.InputType;
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

public class ParamSettingDialog extends DialogFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
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
                String data = "";

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
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        m_view = LayoutInflater.from(getActivity()).inflate(R.layout.paramsetting_dialog, null);
        m_context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        m_checkBox1.setOnCheckedChangeListener(this::onCheckedChanged);
        m_checkBox2.setOnCheckedChangeListener(this::onCheckedChanged);
        m_checkBox3.setOnCheckedChangeListener(this::onCheckedChanged);
        m_checkBox4.setOnCheckedChangeListener(this::onCheckedChanged);
        m_checkBox5.setOnCheckedChangeListener(this::onCheckedChanged);
        m_checkBox6.setOnCheckedChangeListener(this::onCheckedChanged);
        m_checkBox7.setOnCheckedChangeListener(this::onCheckedChanged);
        m_checkBox8.setOnCheckedChangeListener(this::onCheckedChanged);
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
