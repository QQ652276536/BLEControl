package com.zistone.blecontrol.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import com.zistone.blecontrol.R;

public class SoundPlayUtils {

    private static Context _context;
    private static SoundPool _soundPool;
    private static SoundPlayUtils _soundPlayUtils;

    public SoundPlayUtils() {
        if (Build.VERSION.SDK_INT > 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            //传入音频数量
            builder.setMaxStreams(5);
            //AudioAttributes是一个封装音频各种属性的方法
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            //设置音频流的合适的属性
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_SYSTEM);
            //加载一个AudioAttributes
            builder.setAudioAttributes(attrBuilder.build());
            _soundPool = builder.build();
        } else {
            _soundPool = new SoundPool(5, AudioManager.STREAM_SYSTEM, 0);
        }
    }

    /**
     * 初始化
     *
     * @param context
     */
    public static SoundPlayUtils Init(Context context) {
        if (_soundPlayUtils == null) {
            _soundPlayUtils = new SoundPlayUtils();
        }
        _context = context;
        // 初始化声音
        _soundPool.load(_context, R.raw.dingdong, 1);
        _soundPool.load(_context, R.raw.didi, 1);
        return _soundPlayUtils;
    }

    /**
     * 播放声音
     *
     * @param soundID
     */
    public static void Play(int soundID) {
        _soundPool.play(soundID, 1, 1, 0, 0, 1);
    }

}

