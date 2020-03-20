package com.zistone.blecontrol.baidutts.util;

import com.baidu.tts.client.TtsMode;

public interface IOfflineResourceConst {

    //离线女声
    String VOICE_FEMALE = "F";

    //离线男声
    String VOICE_MALE = "M";

    //度逍遥
    String VOICE_DUYY = "Y";

    //度丫丫
    String VOICE_DUXY = "X";

    //语音合成模型文件
    String TEXT_MODEL = "bd_etts_text.dat";

    //离线男声模型文件
    String VOICE_MALE_MODEL = "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat";

    //离线女声模型文件
    String VOICE_FEMALE_MODEL = "bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat";

    //度逍遥模型文件
    String VOICE_DUXY_MODEL = "bd_etts_common_speech_yyjw_mand_eng_high_am-mix_v3.0.0_20170512.dat";

    //度丫丫模型文件
    String VOICE_DUYY_MODEL = "bd_etts_common_speech_as_mand_eng_high_am_v3.0.0_20170516.dat";

    //TtsMode.MIX       离在线融合,在线优先,即纯在线模式不生效.
    //TtsMode.ONLINE    纯在线
    //TtsMode.OFFLINE   纯离线合成,需要纯离线SDK
    TtsMode DEFAULT_OFFLINE_TTS_MODE = TtsMode.MIX;

    String PARAM_SN_NAME = null;

}
