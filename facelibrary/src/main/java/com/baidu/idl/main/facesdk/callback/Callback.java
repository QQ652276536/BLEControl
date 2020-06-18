package com.baidu.idl.main.facesdk.callback;

/**
 * Created by litonghui on 2018/10/21.
 */
public interface Callback {
    /**
     *  回调函数 code 0 : 成功；code 1 加载失败
     * @param code
     * @param response
     */
    void onResponse(int code, String response);
}