package com.zistone.bluetoothcontrol.control;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zistone.bluetoothcontrol.R;

import java.util.List;

public class BluetoothListAdapter extends BaseAdapter
{
    private static final String TAG = "BluetoothListAdapter";
    private Context m_context;
    private LayoutInflater m_layoutInflater;
    private List<BluetoothDevice> m_list;
    private boolean m_isClick = false;
    private int m_currentIem = 0;

    public List<BluetoothDevice> GetM_list()
    {
        return m_list;
    }

    public void SetM_list(List<BluetoothDevice> m_list)
    {
        this.m_list = m_list;
    }

    public boolean GetM_isClick()
    {
        return m_isClick;
    }

    public void SetM_isClick(boolean m_isClick)
    {
        this.m_isClick = m_isClick;
    }

    public int GetM_currentIem()
    {
        return m_currentIem;
    }

    public void SetM_currentIem(int m_currentIem)
    {
        this.m_currentIem = m_currentIem;
    }

    public BluetoothListAdapter(Context context)
    {
        m_layoutInflater = LayoutInflater.from(context);
        m_context = context;
    }

    @Override
    public int getCount()
    {
        return m_list.size();
    }

    @Override
    public Object getItem(int position)
    {
        return m_list.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
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
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }
        final BluetoothDevice device = m_list.get(position);
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
        if(position == m_currentIem && m_isClick)
        {
            holder.tv_blue_name.setTextColor(Color.argb(255, 0, 133, 119));
            holder.tv_blue_address.setTextColor(Color.argb(255, 0, 133, 119));
            holder.tv_blue_state.setTextColor(Color.argb(255, 0, 133, 119));
        }
        else
        {
            holder.tv_blue_name.setTextColor(Color.BLACK);
            holder.tv_blue_address.setTextColor(Color.BLACK);
            holder.tv_blue_state.setTextColor(Color.BLACK);
        }
        return convertView;
    }

    public final class ViewHolder
    {
        public TextView tv_blue_name;
        public TextView tv_blue_address;
        public TextView tv_blue_state;
    }

}
