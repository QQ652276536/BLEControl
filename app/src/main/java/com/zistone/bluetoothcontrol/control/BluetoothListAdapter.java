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

public class BluetoothListAdapter extends BaseAdapter
{
    private static final String TAG = "BluetoothListAdapter";
    private Context m_context;
    private LayoutInflater m_layoutInflater;
    private List<BluetoothDevice> m_list;

    public BluetoothListAdapter(Context context, List<BluetoothDevice> blue_list)
    {
        m_layoutInflater = LayoutInflater.from(context);
        m_context = context;
        m_list = blue_list;
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

        return convertView;
    }

    public final class ViewHolder
    {
        public TextView tv_blue_name;
        public TextView tv_blue_address;
        public TextView tv_blue_state;
    }

}
