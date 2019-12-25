package com.zistone.bluetoothtest.fragment;

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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.zistone.bluetoothtest.R;

public class FragmentDialog_WriteValue extends DialogFragment implements View.OnClickListener,
        TabLayout.OnTabSelectedListener
{
    private TabLayout m_tabLayout;
    private View m_view;
    private FrameLayout m_frameLayout;
    private Context m_context;
    private Button m_button1;
    private Button m_button2;
    private Button m_button3;
    private Button m_button4;
    private TableLayout m_table;

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.writevalue_btn_delvalue:
            {
                break;
            }
            case R.id.writevalue_btn_addvalue:
            {
                TableRow row = new TableRow(m_context);
                row.setGravity(Gravity.CENTER_VERTICAL);
                TextView textView1 = new TextView(m_context);
                textView1.setText("TextView");
                textView1.setVisibility(View.INVISIBLE);
                TextView textView2 = new TextView(m_context);
                textView2.setText("0X");
                textView2.setWidth(50);
                EditText editText = new EditText(m_context);
                editText.setHint("New value");
                editText.setEms(5);
                editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                TextView textView3 = new TextView(m_context);
                textView3.setText("BYTE");
                textView3.setWidth(50);
                ImageButton imageButton = new ImageButton(m_context);
                imageButton.setImageDrawable(getResources().getDrawable(R.drawable.close3));
                imageButton.getBackground().setAlpha(0);
                imageButton.setOnClickListener(v1 ->
                {
                    TableRow tableRow = (TableRow) v1.getParent();
                    m_table.removeView(tableRow);
                });
                row.addView(textView1);
                row.addView(textView2);
                row.addView(editText);
                row.addView(textView3);
                row.addView(imageButton);
                m_table.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT));
                break;
            }
            case R.id.writevalue_btn_save:
            {
                break;
            }
            case R.id.writevalue_btn_cancel:
                dismiss();
                break;
            case R.id.writevalue_btn_send:
            {
                String data = "";
                for(int i = 0; i < m_table.getChildCount(); i++)
                {
                    TableRow row = (TableRow) m_table.getChildAt(i);
                    EditText editText = (EditText) row.getChildAt(2);
                    data += editText.getText().toString();
                }
                Intent intent = new Intent();
                intent.putExtra("WriteValue", data);
                getTargetFragment().onActivityResult(1, Activity.RESULT_OK, intent);
                break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //设置背景透明
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        m_view = LayoutInflater.from(getActivity()).inflate(R.layout.dialogfragment_writevalue, null);
        m_context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        m_tabLayout = m_view.findViewById(R.id.tablayout);
        m_frameLayout = m_view.findViewById(R.id.writevalue_framelayout);
        m_table = m_view.findViewById(R.id.writevalue_table);
        m_tabLayout.getTabAt(0).setText("NEW");
        m_tabLayout.getTabAt(1).setText("LOAD");
        m_tabLayout.addOnTabSelectedListener(this);
        m_button1 = m_view.findViewById(R.id.writevalue_btn_addvalue);
        m_button1.setOnClickListener(this::onClick);
        m_button2 = m_view.findViewById(R.id.writevalue_btn_save);
        m_button2.setOnClickListener(this::onClick);
        m_button3 = m_view.findViewById(R.id.writevalue_btn_cancel);
        m_button3.setOnClickListener(this::onClick);
        m_button4 = m_view.findViewById(R.id.writevalue_btn_send);
        m_button4.setOnClickListener(this::onClick);
        builder.setView(m_view);
        return builder.create();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab)
    {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab)
    {
    }
}
