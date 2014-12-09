package com.coffee.tableapplication;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

//import static android.app.PendingIntent.getActivity;

/**
 * Created by Sheng-Han on 12/6/2014.
 */
public class UsersListAdapter extends ArrayAdapter<WifiP2pDevice> {

    private List<WifiP2pDevice> items;

    public UsersListAdapter(Context context, int resource, int textViewResourceId,
                               List<WifiP2pDevice> objects) {
        super(context, resource, textViewResourceId, objects);
        items = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(android.R.layout.simple_list_item_2, null);
        }
        WifiP2pDevice device = items.get(position);
        if (device != null) {
            TextView top = (TextView) v.findViewById(android.R.id.text1);
            TextView bottom = (TextView) v.findViewById(android.R.id.text2);
            if (top != null) {
                top.setText(device.deviceName);
            }
            if (bottom != null) {
                bottom.setText(device.deviceAddress);
            }
        }

        return v;

    }
}
