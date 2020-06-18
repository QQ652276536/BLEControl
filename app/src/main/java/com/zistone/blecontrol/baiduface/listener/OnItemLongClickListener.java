package com.zistone.blecontrol.baiduface.listener;

import android.view.View;

@FunctionalInterface
public interface OnItemLongClickListener {
    void onLongItemClick(View view, int position);
}
