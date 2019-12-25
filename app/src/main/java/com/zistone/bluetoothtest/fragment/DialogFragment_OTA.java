package com.zistone.bluetoothtest.fragment;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zistone.bluetoothtest.R;

/**
 * 设备空中升级
 */
public class DialogFragment_OTA extends DialogFragment implements View.OnClickListener
{
    private static final String TAG = "DialogFragment_OTA";
    private static final String ARG_PARAM1 = "param1";
    private Context m_context;
    private View m_view;
    private Button m_button1;
    private Button m_button2;
    private TextView m_textView1;
    private TextView m_textView2;
    private TextView m_textView3;
    private TextView m_textView4;
    private BluetoothDevice m_bluetoothDevice;

    public static DialogFragment_OTA newInstance(BluetoothDevice bluetoothDevice)
    {
        DialogFragment_OTA fragment = new DialogFragment_OTA();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, bluetoothDevice);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.btn1_ota:
            {
                break;
            }
            case R.id.btn2_ota:
            {
                break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //设置背景透明
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_button1 = m_view.findViewById(R.id.btn1_ota);
        m_button2 = m_view.findViewById(R.id.btn2_ota);
        m_textView1 = m_view.findViewById(R.id.text1_ota);
        m_textView2 = m_view.findViewById(R.id.text2_ota);
        m_textView3 = m_view.findViewById(R.id.text3_ota);
        m_textView4 = m_view.findViewById(R.id.text4_ota);
        m_button1.setOnClickListener(this::onClick);
        m_button2.setOnClickListener(this::onClick);
        m_bluetoothDevice = getArguments().getParcelable(ARG_PARAM1);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        m_view = LayoutInflater.from(getActivity()).inflate(R.layout.dialogfragment_ota, null);
        m_context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(m_view);
        return builder.create();
    }

}
