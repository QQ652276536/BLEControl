package com.zistone.blecontrol.dialogfragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.zistone.blecontrol.R;
import com.zistone.blecontrol.util.DialogFragmentListener;

/**
 * 修改基站参数
 */
public class DialogFragment_Station extends DialogFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "DialogFragment_Station";
    private static final String ARG_PARAM1 = "param1";
    private static DialogFragmentListener _dialogFragmentListener;
    private Context _context;
    private View _view;
    private Button _btn1;
    private CheckBox _chk1, _chk2, _chk3, _chk4, _chk5, _chk6, _chk7, _chk8, _chk9, _chk10;

    public static DialogFragment_Station newInstance(String[] strArray, DialogFragmentListener listener) {
        _dialogFragmentListener = listener;
        DialogFragment_Station fragment = new DialogFragment_Station();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, strArray);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1_ok:

                dismiss();
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    dismiss();
                }
                return false;
            }
        });
        _btn1 = _view.findViewById(R.id.btn1_ok);
        _btn1.setOnClickListener(this::onClick);
        _chk1 = _view.findViewById(R.id.chk1_devicelist);
        _chk2 = _view.findViewById(R.id.chk2_devicelist);
        _chk3 = _view.findViewById(R.id.chk3_devicelist);
        _chk4 = _view.findViewById(R.id.chk4_devicelist);
        _chk5 = _view.findViewById(R.id.chk5_devicelist);
        _chk6 = _view.findViewById(R.id.chk6_devicelist);
        _chk7 = _view.findViewById(R.id.chk7_devicelist);
        _chk8 = _view.findViewById(R.id.chk8_devicelist);
        _chk9 = _view.findViewById(R.id.chk9_devicelist);
        _chk10 = _view.findViewById(R.id.chk10_devicelist);
        _chk1.setOnCheckedChangeListener(this::onCheckedChanged);
        _chk2.setOnCheckedChangeListener(this::onCheckedChanged);
        _chk3.setOnCheckedChangeListener(this::onCheckedChanged);
        _chk4.setOnCheckedChangeListener(this::onCheckedChanged);
        _chk5.setOnCheckedChangeListener(this::onCheckedChanged);
        _chk6.setOnCheckedChangeListener(this::onCheckedChanged);
        _chk7.setOnCheckedChangeListener(this::onCheckedChanged);
        _chk8.setOnCheckedChangeListener(this::onCheckedChanged);
        _chk9.setOnCheckedChangeListener(this::onCheckedChanged);
        _chk10.setOnCheckedChangeListener(this::onCheckedChanged);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        _view = LayoutInflater.from(getActivity()).inflate(R.layout.dialogfragment_devicelistsetting, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setView(_view);
        return builder.create();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.chk1_devicelist:
                break;
            case R.id.chk2_devicelist:
                break;
            case R.id.chk3_devicelist:
                break;
            case R.id.chk4_devicelist:
                break;
            case R.id.chk5_devicelist:
                break;
            case R.id.chk6_devicelist:
                break;
            case R.id.chk7_devicelist:
                break;
            case R.id.chk8_devicelist:
                break;
            case R.id.chk9_devicelist:
                break;
            case R.id.chk10_devicelist:
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        _context = getContext();
    }

}
