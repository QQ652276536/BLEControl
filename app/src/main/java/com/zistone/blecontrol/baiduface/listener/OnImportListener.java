package com.zistone.blecontrol.baiduface.listener;

public interface OnImportListener {
    void startUnzip();

    void showProgressView();

    void onImporting(int finishCount, int successCount, int failureCount, float progress);

    void endImport(int finishCount, int successCount, int failureCount);

    void showToastMessage(String message);
}
