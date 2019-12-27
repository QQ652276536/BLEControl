package com.zistone.bluetoothtest.dialogfragment;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zistone.bluetoothtest.R;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.UUID;

/**
 * 设备空中升级
 */
public class DialogFragment_OTA extends DialogFragment implements View.OnClickListener
{
    private static final String TAG = "DialogFragment_OTA";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int FILE_SELECTOR_CODE = 1;
    private static UUID SERVICE_UUID;
    private static UUID WRITE_UUID;
    private static UUID READ_UUID;
    private static UUID CONFIG_UUID;
    private Context m_context;
    private View m_view;
    private Button m_button1;
    private Button m_button2;
    private TextView m_textView1;
    private TextView m_textView2;
    private ProgressBar m_progressBar;
    private BluetoothDevice m_bluetoothDevice;
    private byte[] m_byteArray;

    public static DialogFragment_OTA newInstance(BluetoothDevice bluetoothDevice)
    {
        DialogFragment_OTA fragment = new DialogFragment_OTA();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, bluetoothDevice);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case FILE_SELECTOR_CODE:
            {
                String path = "";
                Uri uri = data.getData();
                if("content".equalsIgnoreCase(uri.getScheme()))
                {
                    String[] projection = {"_data"};
                    try
                    {
                        Cursor cursor = m_context.getContentResolver().query(uri, projection, null, null, null);
                        int column_index = cursor.getColumnIndexOrThrow("_data");
                        if(cursor.moveToFirst())
                        {
                            path = cursor.getString(column_index);
                        }
                    }
                    catch(Exception e)
                    {
                        Log.e(TAG, e.getMessage());
                    }
                }
                else if("file".equalsIgnoreCase(uri.getScheme()))
                {
                    path = uri.getPath();
                }
                m_textView2.setText(path);
                File file = new File(path);
                FileInputStream fileInputStream;
                BufferedInputStream bufferedInputStream;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024 * 150);
                try
                {
                    fileInputStream = new FileInputStream(file);
                    bufferedInputStream = new BufferedInputStream(fileInputStream);
                    byte[] bytes = new byte[1024];
                    while(bufferedInputStream.available() > 0)
                    {
                        bufferedInputStream.read(bytes);
                        byteArrayOutputStream.write(bytes);
                    }
                    m_byteArray = byteArrayOutputStream.toByteArray();
                    fileInputStream.close();
                    byteArrayOutputStream.close();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(m_context, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.btn1_ota:
            {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //选择图片
                //intent.setType("image/*");
                //选择音频
                //intent.setType("audio/*");
                //选择视频
                //intent.setType("video/*");
                //同时选择视频和图片
                //intent.setType("video/*;image/*");
                //选择bin文件
                //intent.setType("application/octet-stream");
                //无类型限制
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, FILE_SELECTOR_CODE);
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
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        m_button1 = m_view.findViewById(R.id.btn1_ota);
        m_button2 = m_view.findViewById(R.id.btn2_ota);
        m_textView1 = m_view.findViewById(R.id.text1_ota);
        m_textView2 = m_view.findViewById(R.id.text2_ota);
        m_progressBar = m_view.findViewById(R.id.progressBar_ota);
        m_button1.setOnClickListener(this::onClick);
        m_button2.setOnClickListener(this::onClick);
        if(m_bluetoothDevice != null)
        {
            m_textView1.setText(m_bluetoothDevice.getAddress());
        }
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

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            m_bluetoothDevice = getArguments().getParcelable(ARG_PARAM1);
            Map<String, UUID> map = (Map<String, UUID>) getArguments().getSerializable(ARG_PARAM2);
            SERVICE_UUID = map.get("SERVICE_UUID");
            WRITE_UUID = map.get("WRITE_UUID");
            READ_UUID = map.get("READ_UUID");
            CONFIG_UUID = map.get("CONFIG_UUID");
        }
    }

}
