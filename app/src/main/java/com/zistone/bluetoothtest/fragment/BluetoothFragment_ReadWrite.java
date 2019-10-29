package com.zistone.bluetoothtest.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zistone.bluetoothtest.R;

public class BluetoothFragment_ReadWrite extends Fragment implements View.OnClickListener
{
    private static final String TAG = "BluetoothFragment_ReadWrite";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_RREQUEST_FAIL = 1;
    private static final int MESSAGE_RESPONSE_FAIL = 2;
    private static final int MESSAGE_RESPONSE_SUCCESS = 3;
    private OnFragmentInteractionListener m_listener;
    private Context m_context;
    private View m_view;
    private ImageButton m_btnReturn;
    private TextView m_textView1;
    private TextView m_textView2;
    private Button m_button1;
    private Button m_button2;

    public static BluetoothFragment_ReadWrite newInstance(String param1, String param2)
    {
        BluetoothFragment_ReadWrite fragment = new BluetoothFragment_ReadWrite();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            super.handleMessage(message);
            String result = (String) message.obj;
            switch (message.what)
            {
                case MESSAGE_RREQUEST_FAIL:
                {
                    break;
                }
                case MESSAGE_RESPONSE_SUCCESS:
                {
                    break;
                }
                case MESSAGE_RESPONSE_FAIL:
                {
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
        }
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
        if (m_listener != null)
        {
            m_listener.onFragmentInteraction(uri);
        }
    }

    public void InitView()
    {
        m_context = getContext();
        m_btnReturn = m_view.findViewById(R.id.btn_return);
        m_btnReturn.setOnClickListener(this);
        m_textView1 = m_view.findViewById(R.id.textView1);
        m_textView2 = m_view.findViewById(R.id.textView2);
        m_button1 = m_view.findViewById(R.id.btn1);
        m_button1.setOnClickListener(this);
        m_button2 = m_view.findViewById(R.id.btn2);
        m_button2.setOnClickListener(this);
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
        if (getArguments() != null)
        {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_view = inflater.inflate(R.layout.fragment_bluetooth_read_write, container, false);
        InitView();
        return m_view;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            m_listener = (OnFragmentInteractionListener) context;
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
        m_listener = null;
    }
}
