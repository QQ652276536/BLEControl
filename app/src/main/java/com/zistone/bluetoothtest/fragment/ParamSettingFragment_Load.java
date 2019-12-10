package com.zistone.bluetoothtest.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zistone.bluetoothtest.R;

public class ParamSettingFragment_Load extends Fragment implements View.OnClickListener
{
    private Context m_context;
    private View m_view;

    @Override
    public void onClick(View v)
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_view = inflater.inflate(R.layout.fragment_bluetooth_powercontrol, container, false);
        m_context = getContext();
        return m_view;
    }
}
