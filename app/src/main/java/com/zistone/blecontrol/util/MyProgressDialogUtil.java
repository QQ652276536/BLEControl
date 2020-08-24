package com.zistone.blecontrol.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.zistone.blecontrol.R;

public final class MyProgressDialogUtil {
    private static AlertDialog _alertDialog;

    public interface ProgressDialogListener {
        void OnDismiss();
    }

    public interface ConfirmListener {
        void OnConfirm();

        void OnCancel();
    }

    public interface WarningListener {
        void OnIKnow();
    }

    private MyProgressDialogUtil() {
    }

    public static void ShowConfirm(Context context, String title, String content, ConfirmListener listener) {
        //确保创建Dialog的Activity没有finish才显示
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(content);
            builder.setNegativeButton("好的", (dialog, which) -> {
                if (null != listener)
                    listener.OnConfirm();
            });
            builder.setPositiveButton("不了", (dialog, which) -> {
                if (null != listener)
                    listener.OnCancel();
            });
            builder.show();
        }
    }

    public static void ShowWarning(Context context, String title, String content, WarningListener listener) {
        //确保创建Dialog的Activity没有finish才显示
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(content);
            builder.setPositiveButton("知道了", (dialog, which) -> {
                if (null != listener)
                    listener.OnIKnow();
            });
            builder.show();
        }
    }

    public static void ShowProgressDialog(Context context, boolean touchOutSide, ProgressDialogListener listener, String str) {
        //确保创建Dialog的Activity没有finish才显示
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            _alertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
            View loadView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null);
            _alertDialog.setView(loadView, 0, 0, 0, 0);
            _alertDialog.setCanceledOnTouchOutside(touchOutSide);
            TextView textView = loadView.findViewById(R.id.txt_dialog);
            textView.setText(str);
            _alertDialog.show();
            _alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (null != listener)
                        listener.OnDismiss();
                }
            });
        }
    }

    public static void Dismiss() {
        if (_alertDialog != null) {
            _alertDialog.dismiss();
            _alertDialog = null;
        }
    }

}
