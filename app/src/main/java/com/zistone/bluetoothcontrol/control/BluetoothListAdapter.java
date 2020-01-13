package com.zistone.bluetoothcontrol.control;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zistone.bluetoothcontrol.R;
import com.zistone.bluetoothcontrol.pojo.MyBluetoothDevice;

import java.util.List;

public class BluetoothListAdapter extends BaseAdapter
{
    private static final String TAG = "BluetoothListAdapter";
    private Context m_context;
    private LayoutInflater m_layoutInflater;
    private List<MyBluetoothDevice> m_deviceList;
    private boolean m_isClick = false;

    public BluetoothListAdapter(Context context)
    {
        m_layoutInflater = LayoutInflater.from(context);
        m_context = context;
    }

    public List<MyBluetoothDevice> getM_deviceList()
    {
        return m_deviceList;
    }

    public void setM_deviceList(List<MyBluetoothDevice> m_deviceList)
    {
        this.m_deviceList = m_deviceList;
    }

    public boolean isM_isClick()
    {
        return m_isClick;
    }

    public void setM_isClick(boolean m_isClick)
    {
        this.m_isClick = m_isClick;
    }

    @Override
    public int getCount()
    {
        return m_deviceList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return m_deviceList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public void notifyDataSetChanged()
    {
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        if(convertView == null)
        {
            holder = new ViewHolder();
            convertView = m_layoutInflater.inflate(R.layout.item_bluetooth, null);
            holder.tv_blue_name = convertView.findViewById(R.id.tv_blue_name);
            holder.tv_blue_address = convertView.findViewById(R.id.tv_blue_address);
            holder.tv_blue_rssi = convertView.findViewById(R.id.tv_blue_rssi);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }
        MyBluetoothDevice device = m_deviceList.get(position);
        String name = device.get_name();
        String address = device.get_address();
        int rssi = device.get_rssi();
        if(name != null)
            holder.tv_blue_name.setText(name);
        else
            holder.tv_blue_name.setText("Null");
        holder.tv_blue_address.setText(address);
        holder.tv_blue_rssi.setText(rssi + "dBm");
        return convertView;
    }

    public final class ViewHolder
    {
        public TextView tv_blue_name;
        public TextView tv_blue_address;
        public TextView tv_blue_rssi;
    }

}
