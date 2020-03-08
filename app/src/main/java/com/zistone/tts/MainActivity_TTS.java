package com.zistone.tts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.zistone.blecontrol.R;

import java.util.ArrayList;

/**
 * SynthActivity 离线合成
 * MiniActivity 精简版合成
 * SaveFileActivity 保存合成后的音频
 */
public class MainActivity_TTS extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tts);
        initButtons();
        initPermission();
    }

    private void initButtons() {
        Button b1 = findViewById(R.id.synthButton);
        Button b2 = findViewById(R.id.miniButton);
        Button b3 = findViewById(R.id.saveTtsFileButton);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAct(SynthActivity.class);
            }
        }); // 语音合成
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAct(MiniActivity.class);
            }
        }); // 精简版合成
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAct(SaveFileActivity.class);
            }
        }); // 保存合成后的音频
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE // demo使用
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }

    private void startAct(Class activityClass) {
        startActivity(new Intent(this, activityClass));
    }

}