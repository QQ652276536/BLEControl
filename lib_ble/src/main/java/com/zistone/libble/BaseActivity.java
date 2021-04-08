package com.zistone.libble;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * 所有Activity的基类
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    public static final String ARG_PARAM3 = "param3";
    public static final int SPRING_GREEN = Color.parseColor("#3CB371");

    /**
     * 显示键盘
     *
     * @param view
     */
    public void ShowKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != inputMethodManager) {
            view.requestFocus();
            inputMethodManager.showSoftInput(view, 0);
        }
    }

    /**
     * 隐藏键盘
     *
     * @param view
     */
    public void HideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != inputMethodManager) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 到TextView顶部
     *
     * @param txt
     */
    public void TxtToTop(TextView txt) {
        txt.scrollTo(0, 0);
    }

    /**
     * 到TextView底部
     *
     * @param txt
     */
    public void TxtToBottom(TextView txt) {
        int offset = txt.getLineCount() * txt.getLineHeight();
        if (offset > txt.getHeight()) {
            txt.scrollTo(0, offset - txt.getHeight());
        }
    }

    /**
     * 清除TextView的内容
     *
     * @param txt
     */
    public void TxtClear(TextView txt) {
        txt.setText("");
        txt.scrollTo(0, 0);
    }

    /**
     * 权限检查
     *
     * @param neededPermissions 需要的权限
     * @return 全部被允许
     */
    public boolean CheckPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉系统的TitleBar
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //Activity启动后就锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
    }
}
