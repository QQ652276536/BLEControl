package com.zistone.bluetoothtest.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.zistone.bluetoothtest.R;
import com.zistone.bluetoothtest.fragment.BluetoothFragment_List;
import com.zistone.bluetoothtest.fragment.BluetoothFragment_ReadWrite;

public class MainActivity extends AppCompatActivity implements BluetoothFragment_List.OnFragmentInteractionListener, BluetoothFragment_ReadWrite.OnFragmentInteractionListener
{
    //当前页,用来切换
    public Fragment m_currentFragment;
    public BluetoothFragment_List m_bluetoothFragment_list;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitData();
    }

    private void InitData()
    {
        m_bluetoothFragment_list = BluetoothFragment_List.newInstance("", "");
        m_currentFragment = m_bluetoothFragment_list;
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_current, m_currentFragment, "bluetoothFragment_list").show(m_currentFragment).commitNow();
    }

    @Override
    public void onFragmentInteraction(Uri uri)
    {
        Toast.makeText(this, "----------DuangDuangDuang------------", Toast.LENGTH_LONG).show();
    }
}
