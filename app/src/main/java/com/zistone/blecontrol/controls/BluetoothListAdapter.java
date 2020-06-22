package com.zistone.blecontrol.controls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zistone.blecontrol.R;
import com.zistone.blecontrol.pojo.MyBluetoothDevice;

import java.util.List;

public class BluetoothListAdapter extends BaseAdapter {
    private static final String TAG = "BluetoothListAdapter";
    private Context _context;
    private LayoutInflater _layoutInflater;
    private List<MyBluetoothDevice> _deviceList;
    private boolean _isClick = false;

    public BluetoothListAdapter(Context context) {
        _layoutInflater = LayoutInflater.from(context);
        _context = context;
    }

    public List<MyBluetoothDevice> getM_deviceList() {
        return _deviceList;
    }

    public void setM_deviceList(List<MyBluetoothDevice> _deviceList) {
        this._deviceList = _deviceList;
    }

    public boolean isM_isClick() {
        return _isClick;
    }

    public void setM_isClick(boolean _isClick) {
        this._isClick = _isClick;
    }

    @Override
    public int getCount() {
        return _deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return _deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = _layoutInflater.inflate(R.layout.item_bluetooth, null);
            holder._txt1 = convertView.findViewById(R.id.txt_bluetoothItem1);
            holder._txt2 = convertView.findViewById(R.id.txt_bluetoothItem2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        MyBluetoothDevice device = _deviceList.get(position);
        String name = device.getName();
        String address = device.getAddress();
        int rssi = device.getRssi();
        String str;
        if (name != null)
            str = name;
        else
            str = "Null";
        str += "\r\n" + address;
        holder._txt1.setText(str);
        holder._txt2.setText("\r\n" + rssi + "dBm");
        return convertView;
    }

    public final class ViewHolder {
        public TextView _txt1;
        public TextView _txt2;
    }

}
