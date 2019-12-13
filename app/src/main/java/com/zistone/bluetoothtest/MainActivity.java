package com.zistone.bluetoothtest;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.zistone.bluetoothtest.fragment.BluetoothFragment;
import com.zistone.bluetoothtest.fragment.BluetoothFragment_List;
import com.zistone.bluetoothtest.fragment.BluetoothFragment_PowerControl;
import com.zistone.bluetoothtest.fragment.BluetoothFragment_ReadWrite;

public class MainActivity extends AppCompatActivity implements BluetoothFragment.OnFragmentInteractionListener, BluetoothFragment_List.OnFragmentInteractionListener, BluetoothFragment_ReadWrite.OnFragmentInteractionListener, BluetoothFragment_PowerControl.OnFragmentInteractionListener
{
    public BluetoothFragment m_bluetoothFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitData();
    }

    private void InitData()
    {
        m_bluetoothFragment = BluetoothFragment.newInstance("", "");
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_current, m_bluetoothFragment, "bluetoothFragment").show(m_bluetoothFragment).commitNow();
    }

    /**
     * Fragment向Activtiy传递数据
     *
     * @param uri
     */
    @Override
    public void onFragmentInteraction(Uri uri)
    {
        Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show();
    }
}