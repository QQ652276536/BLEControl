package com.zistone.blecontrol.baidutts.util;

import com.baidu.tts.client.TtsMode;

public interface IOfflineResourceConst {
    String VOICE_FEMALE = "F";

    String VOICE_MALE = "M";

    String VOICE_DUYY = "Y";

    String VOICE_DUXY = "X";

    String TEXT_MODEL = "bd_etts_text.dat";

    String VOICE_MALE_MODEL = "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat";

    String VOICE_FEMALE_MODEL = "bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat";

    String VOICE_DUXY_MODEL = "bd_etts_common_speech_yyjw_mand_eng_high_am-mix_v3.0.0_20170512.dat";

    String VOICE_DUYY_MODEL = "bd_etts_common_speech_as_mand_eng_high_am_v3.0.0_20170516.dat";

    TtsMode DEFAULT_OFFLINE_TTS_MODE = TtsMode.MIX;

    String PARAM_SN_NAME = null;
}
