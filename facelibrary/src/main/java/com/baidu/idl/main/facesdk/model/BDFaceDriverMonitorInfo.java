package com.baidu.idl.main.facesdk.model;

public class BDFaceDriverMonitorInfo {
    public float normal = 0;    // 行为正常
    public float calling = 0;   // 打电话
    public float drinking = 0;  // 喝水
    public float eating = 0;    // 吃东西
    public float smoking = 0;   // 抽烟

    public BDFaceDriverMonitorInfo(float normal,
                                   float calling,
                                   float drinking,
                                   float eating,
                                   float smoking) {
        this.normal = normal;
        this.calling = calling;
        this.drinking = drinking;
        this.eating = eating;
        this.smoking = smoking;
    }
}
