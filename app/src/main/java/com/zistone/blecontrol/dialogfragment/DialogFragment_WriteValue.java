package com.zistone.blecontrol.dialogfragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.zistone.blecontrol.activity.MainActivity;
import com.zistone.blecontrol.R;

public class DialogFragment_WriteValue extends DialogFragment implements View.OnClickListener, TabLayout.OnTabSelectedListener {

    private static final String TAG = "DialogFragment_WriteValue";
    private static final String ARG_PARAM1 = "param1";
    private TabLayout _tabLayout;
    private View _view;
    private Context _context;
    private Button _btn1;
    private Button _btn2;
    private Button _btn3;
    private Button _btn4;
    private TableLayout _table;

    public static DialogFragment_ParamSetting newInstance(String[] strArray) {
        DialogFragment_ParamSetting fragment = new DialogFragment_ParamSetting();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, strArray);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1_devlceFilter:
                break;
            case R.id.writevalue_btn_addvalue: {
                TableRow row = new TableRow(_context);
                row.setGravity(Gravity.CENTER_VERTICAL);
                TextView textView1 = new TextView(_context);
                textView1.setText("TextView");
                textView1.setVisibility(View.INVISIBLE);
                TextView textView2 = new TextView(_context);
                textView2.setText("0X");
                textView2.setWidth(50);
                EditText editText = new EditText(_context);
                editText.setHint("New value");
                editText.setEms(5);
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                TextView textView3 = new TextView(_context);
                textView3.setText("BYTE");
                textView3.setWidth(50);
                ImageButton imageButton = new ImageButton(_context);
                imageButton.setImageDrawable(getResources().getDrawable(R.drawable.close1));
                imageButton.getBackground().setAlpha(0);
                imageButton.setOnClickListener(v1 -> {
                    TableRow tableRow = (TableRow) v1.getParent();
                    _table.removeView(tableRow);
                });
                row.addView(textView1);
                row.addView(textView2);
                row.addView(editText);
                row.addView(textView3);
                row.addView(imageButton);
                _table.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
            }
            break;
            case R.id.writevalue_btn_save:
                break;
            case R.id.writevalue_btn_cancel:
                dismiss();
                break;
            case R.id.writevalue_btn_send: {
                String data = "";
                for (int i = 0; i < _table.getChildCount(); i++) {
                    TableRow row = (TableRow) _table.getChildAt(i);
                    EditText editText = (EditText) row.getChildAt(2);
                    data += editText.getText().toString();
                }
                Intent intent = new Intent();
                intent.putExtra("WriteValue", data);
                getTargetFragment().onActivityResult(MainActivity.ACTIVITYRESULT_WRITEVALUE, Activity.RESULT_OK, intent);
            }
            break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        _view = LayoutInflater.from(getActivity()).inflate(R.layout.dialogfragment_writevalue, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        _tabLayout = _view.findViewById(R.id.tablayout);
        _table = _view.findViewById(R.id.writevalue_table);
        _tabLayout.getTabAt(0).setText("NEW");
        _tabLayout.getTabAt(1).setText("LOAD");
        _tabLayout.addOnTabSelectedListener(this);
        _btn1 = _view.findViewById(R.id.writevalue_btn_addvalue);
        _btn1.setOnClickListener(this::onClick);
        _btn2 = _view.findViewById(R.id.writevalue_btn_save);
        _btn2.setOnClickListener(this::onClick);
        _btn3 = _view.findViewById(R.id.writevalue_btn_cancel);
        _btn3.setOnClickListener(this::onClick);
        _btn4 = _view.findViewById(R.id.writevalue_btn_send);
        _btn4.setOnClickListener(this::onClick);
        builder.setView(_view);
        return builder.create();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        _context = getContext();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }
}
