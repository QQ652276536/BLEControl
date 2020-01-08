package com.zistone.bluetoothcontrol.control;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zistone.bluetoothcontrol.R;

import java.util.List;
import java.util.Map;

public class BluetoothListAdapter extends BaseAdapter
{
    private static final String TAG = "BluetoothListAdapter";
    private Context m_context;
    private LayoutInflater m_layoutInflater;
    private List<BluetoothDevice> m_deviceList;
    private boolean m_isClick = false;
    private Map<String, Integer> m_rssiMap;

    public Map<String, Integer> GetM_rssiMap()
    {
        return m_rssiMap;
    }

    public void SetM_rssiMap(Map<String, Integer> m_rssiMap)
    {
        this.m_rssiMap = m_rssiMap;
    }

    public List<BluetoothDevice> GetM_list()
    {
        return m_deviceList;
    }

    public boolean GetM_isClick()
    {
        return m_isClick;
    }

    public void SetM_isClick(boolean m_isClick)
    {
        this.m_isClick = m_isClick;
    }

    public void SetM_list(List<BluetoothDevice> m_deviceList)
    {
        this.m_deviceList = m_deviceList;
    }

    public BluetoothListAdapter(Context context)
    {
        m_layoutInflater = LayoutInflater.from(context);
        m_context = context;
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
            holder.tv_blue_state = convertView.findViewById(R.id.tv_blue_state);
            holder.tv_blue_rssi = convertView.findViewById(R.id.tv_blue_rssi);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }
        final BluetoothDevice device = m_deviceList.get(position);
        Integer rssi = 0;
        if(device.getAddress() != null && !device.getAddress().trim().equals(""))
        {
            rssi = m_rssiMap.get(device.getAddress());
        }
        holder.tv_blue_name.setText(device.getName());
        holder.tv_blue_address.setText(device.getAddress());
        switch(device.getBondState())
        {
            case BluetoothDevice.BOND_NONE:
                holder.tv_blue_state.setText("未绑定");
                break;
            case BluetoothDevice.BOND_BONDING:
                holder.tv_blue_state.setText("绑定中");
                break;
            case BluetoothDevice.BOND_BONDED:
                holder.tv_blue_state.setText("已绑定");
                break;
            default:
                holder.tv_blue_state.setText("");
        }
        holder.tv_blue_rssi.setText(rssi + "dBm");
        return convertView;
    }

    public final class ViewHolder
    {
        public TextView tv_blue_name;
        public TextView tv_blue_address;
        public TextView tv_blue_state;
        public TextView tv_blue_rssi;
    }

}
