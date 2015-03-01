package com.coffee.tableapplication;

import android.content.Context;
import android.database.DataSetObserver;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Sheng-Han on 2/28/2015.
 */
public class RegistrationListAdapter extends ArrayAdapter<User> {
    private List<User> players;
    public RegistrationListAdapter(Context context, int resource, int textViewResourceId,
                            List<User> objects) {
        super(context, resource, textViewResourceId, objects);
        players = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(android.R.layout.simple_list_item_2, null);
        }
        User person = players.get(position);
        WifiP2pDevice device = person.device;
        if (device != null) {
            TextView top = (TextView) v.findViewById(android.R.id.text1);
            TextView bottom = (TextView) v.findViewById(android.R.id.text2);
            if (top != null) {
                top.setText(device.deviceName);
            }
            if (bottom != null) {
                bottom.setText(device.deviceAddress + "");
            }
        }

        return v;

    }

}
