package com.zistone.bluetoothcontrol.util;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.zistone.bluetoothcontrol.R;

public class ProgressDialogUtil
{
    public interface Listener
    {
        void OnDismiss();
    }

    private static AlertDialog _alertDialog;
    private static Listener _listener;

    public static void ShowProgressDialog(Context context, Listener listener, String str)
    {
        if(_alertDialog == null)
            _alertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
        _listener = listener;
        View loadView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null);
        _alertDialog.setView(loadView, 0, 0, 0, 0);
        _alertDialog.setCanceledOnTouchOutside(true);
        TextView textView = loadView.findViewById(R.id.text_dialog);
        textView.setText(str);
        _alertDialog.show();
    }

    public static void ShowProgressDialog(Context context, String str)
    {
        if(_alertDialog == null)
            _alertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
        View loadView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null);
        _alertDialog.setView(loadView, 0, 0, 0, 0);
        _alertDialog.setCanceledOnTouchOutside(true);
        TextView textView = loadView.findViewById(R.id.text_dialog);
        textView.setText(str);
        _alertDialog.show();
    }

    public static void Dismiss()
    {
        if(_alertDialog != null && _alertDialog.isShowing())
        {
            _alertDialog.dismiss();
            if(_listener != null)
                _listener.OnDismiss();
        }
    }

}
