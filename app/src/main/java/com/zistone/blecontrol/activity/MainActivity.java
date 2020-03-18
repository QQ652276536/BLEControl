package com.zistone.blecontrol.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.zistone.blecontrol.R;
import com.zistone.blecontrol.fragment.BluetoothFragment;
import com.zistone.blecontrol.fragment.BluetoothFragment_CommandTest;
import com.zistone.blecontrol.fragment.BluetoothFragment_DB;
import com.zistone.blecontrol.fragment.BluetoothFragment_List;
import com.zistone.blecontrol.fragment.BluetoothFragment_OTA;
import com.zistone.blecontrol.fragment.BluetoothFragment_PowerControl;
import com.zistone.blecontrol.fragment.BluetoothFragment_Temperature;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements BluetoothFragment.OnFragmentInteractionListener,
        BluetoothFragment_List.OnFragmentInteractionListener, BluetoothFragment_CommandTest.OnFragmentInteractionListener,
        BluetoothFragment_PowerControl.OnFragmentInteractionListener, BluetoothFragment_OTA.OnFragmentInteractionListener,
        BluetoothFragment_DB.OnFragmentInteractionListener, BluetoothFragment_Temperature.OnFragmentInteractionListener {
    public static final int ACTIVITYRESULT_WRITEVALUE = 1;
    public static final int ACTIVITYRESULT_PARAMSETTING = 2;
    public static final int ACTIVITYRESULT_OTA = 3;
    public static final int ACTIVITYRESULT_FILTER = 4;

    private boolean _isPermissionRequested = false;
    private BluetoothFragment _bluetoothFragment;

    /**
     * Android6.0之后需要动态申请权限
     */
    private void RequestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !_isPermissionRequested) {
            _isPermissionRequested = true;
            ArrayList<String> permissionsList = new ArrayList<>();
            String[] permissions = {Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.CAMERA};
            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
                    //进入到这里代表没有权限
                    permissionsList.add(perm);
                }
            }
            if (!permissionsList.isEmpty()) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 1);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //        _bluetoothFragment = BluetoothFragment.newInstance("", "");
        //        getSupportFragmentManager().beginTransaction().add(R.id.fragment_current, _bluetoothFragment, "bluetoothFragment").show(_bluetoothFragment).commitNow();
        RequestPermission();
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(2500);
                    Intent intent = new Intent(getApplicationContext(), BleDeviceList.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    /**
     * Fragment向Activtiy传递数据
     *
     * @param uri
     */
    @Override
    public void onFragmentInteraction(Uri uri) {
        //Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show();
    }
}