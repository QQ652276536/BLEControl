package com.zistone.bluetoothtest.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zistone.bluetoothtest.R;

import java.util.ArrayList;
import java.util.List;

public class ParamSettingDialog extends DialogFragment implements View.OnClickListener, ViewPager.OnPageChangeListener
{
    private TabLayout m_tabLayout;
    private ViewPager m_viewPager;
    private View m_view;
    private List<Fragment> m_fragmentList;

    @Override
    public void onClick(View v)
    {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        m_view = LayoutInflater.from(getActivity()).inflate(R.layout.param_setting_dialog, null);
        m_tabLayout = m_view.findViewById(R.id.tablayout);
        m_viewPager = m_view.findViewById(R.id.viewpager);
        m_viewPager.setOffscreenPageLimit(m_fragmentList.size());
        m_viewPager.addOnPageChangeListener(this);
        m_viewPager.setAdapter(new FragmentPagerAdapter(getActivity().getSupportFragmentManager())
        {
            @Override
            public Fragment getItem(int i)
            {
                return m_fragmentList.get(i);
            }

            @Override
            public int getCount()
            {
                return m_fragmentList.size();
            }
        });
        m_tabLayout.setupWithViewPager(m_viewPager);
        m_tabLayout.getTabAt(0).setText("新建");
        m_tabLayout.getTabAt(1).setText("保存");
        builder.setView(m_view);
        return builder.create();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        m_fragmentList = new ArrayList<>();
        m_fragmentList.add(new ParamSettingFragment_New());
        m_fragmentList.add(new ParamSettingFragment_Load());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onPageScrolled(int i, float v, int i1)
    {
    }

    @Override
    public void onPageSelected(int i)
    {
    }

    @Override
    public void onPageScrollStateChanged(int i)
    {
    }

}
