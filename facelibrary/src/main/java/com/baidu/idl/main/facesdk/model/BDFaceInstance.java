package com.baidu.idl.main.facesdk.model;


/**
 * Created by v_shishuaifeng on 2019/9/19.
 */

public class BDFaceInstance {
    private long index = 0;

    // 获取instance 地址
    public long getIndex() {
        return index;
    }

    private void setIndex(long index) {
        this.index = index;
    }

    // 创建instance
    public void creatInstance() {
        index = nativeCreateInstance();
        setIndex(index);
    }

    public void getDefautlInstance() {
        long defautlInstanceIndex = nativeGetDefautlInstance();
        setIndex(defautlInstanceIndex);
    }

    // 销毁instance
    public native int destory();

    private native long nativeCreateInstance();

    private native long nativeGetDefautlInstance();


}
