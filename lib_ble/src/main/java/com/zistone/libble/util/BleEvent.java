package com.zistone.libble.util;

/**
 * 低功耗蓝牙的内部事件
 *
 * @author LiWei
 * @date 2021/3/31 14:05
 * @email 652276536@qq.com
 */
public final class BleEvent {

    /**
     * （禁止外部实例化）
     */
    private BleEvent() {
    }

    //内部日志对应事件
    public static final String[] BLE_EVENT_ARRAY = new String[]{"power_on", "power_off", "enter_sleep", "get_date_time", "wait_st_wakeup",
                                                                "report_ok", "report_timeout", "modi_rpt_st", "modi_rpt_dt", "wake_up", "exit_sleep",
                                                                "get_wakeup_dt", "plt-modi-tp", "get_gps_location", "get_sbd_rssi",
                                                                "batt_measure_done", "send_sbd_data", "receive_sbd_data", "antenna ok-open-short",
                                                                "modi-debug-config", "temp_test_done", "batt_scale_rpt", "temp_scale_rpt",
                                                                "temp_value_rpt", "gps reset", "sys reset", "sat rssi", "sat count", "gps_fix_wait",
                                                                "dt_wake_match", "set_wifi_reset", "get_wifi_ap_rssi", "wifi_no_ap", "wifi_restore",
                                                                "wifi_ap_connect", "IP_connect", "IP_disconnect", "IP_close", "download location",
                                                                "download dateTime", "modify tag head "};

}
