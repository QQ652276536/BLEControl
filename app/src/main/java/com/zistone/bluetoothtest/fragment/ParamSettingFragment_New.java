package com.zistone.bluetoothtest.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zistone.bluetoothtest.R;
import com.zistone.bluetoothtest.control.BluetoothListAdapter;

public class ParamSettingFragment_New extends Fragment implements View.OnClickListener
{
    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";

    public String m_param1;
    public String m_param2;
    private Context m_context;
    private View m_view;

    public static ParamSettingFragment_New newInstance(String param1, String param2)
    {
        ParamSettingFragment_New fragment = new ParamSettingFragment_New();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v)
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            m_param1 = getArguments().getString(ARG_PARAM1);
            m_param2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_view = inflater.inflate(R.layout.fragment_paramsetting_new, container, false);
        m_context = getContext();
        return m_view;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}
