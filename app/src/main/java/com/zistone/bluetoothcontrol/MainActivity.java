package com.zistone.bluetoothcontrol;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.zistone.bluetoothcontrol.fragment.BluetoothFragment;
import com.zistone.bluetoothcontrol.fragment.BluetoothFragment_CommandTest;
import com.zistone.bluetoothcontrol.fragment.BluetoothFragment_List;
import com.zistone.bluetoothcontrol.fragment.BluetoothFragment_OTA;
import com.zistone.bluetoothcontrol.fragment.BluetoothFragment_PowerControl;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements BluetoothFragment.OnFragmentInteractionListener, BluetoothFragment_List.OnFragmentInteractionListener, BluetoothFragment_CommandTest.OnFragmentInteractionListener, BluetoothFragment_PowerControl.OnFragmentInteractionListener, BluetoothFragment_OTA.OnFragmentInteractionListener
{
    public static final int ACTIVITYRESULT_WRITEVALUE = 1;
    public static final int ACTIVITYRESULT_PARAMSETTING = 2;
    public static final int ACTIVITYRESULT_OTA = 3;
    public static final int ACTIVITYRESULT_FILTER = 4;

    private boolean m_isPermissionRequested = false;
    private BluetoothFragment m_bluetoothFragment;

    /**
     * Android6.0之后需要动态申请权限
     */
    private void RequestPermission()
    {
        if(Build.VERSION.SDK_INT >= 23 && !m_isPermissionRequested)
        {
            m_isPermissionRequested = true;
            ArrayList<String> permissionsList = new ArrayList<>();
            String[] permissions = {
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
            for(String perm : permissions)
            {
                if(PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm))
                {
                    //进入到这里代表没有权限
                    permissionsList.add(perm);
                }
            }
            if(!permissionsList.isEmpty())
            {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 1);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_bluetoothFragment = BluetoothFragment.newInstance("", "");
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_current, m_bluetoothFragment, "bluetoothFragment").show(m_bluetoothFragment).commitNow();
        RequestPermission();
    }

    /**
     * Fragment向Activtiy传递数据
     *
     * @param uri
     */
    @Override
    public void onFragmentInteraction(Uri uri)
    {
        //Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show();
    }
}