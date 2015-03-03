package com.coffee.tableapplication;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Sheng-Han on 2/28/2015.
 */
public class ScoreboardListAdapter extends ArrayAdapter<User> {
    private List<User> players;
    public ScoreboardListAdapter(Context context, int resource, int textViewResourceId,
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
            v = vi.inflate(R.layout.row_scoreboard, null);
        }
        User person = players.get(position);
        WifiP2pDevice device = person.device;
        if (device != null) {
            TextView left = (TextView) v.findViewById(R.id.username);
            TextView right = (TextView) v.findViewById(R.id.kudos);
            if (left != null) {
                left.setText(device.deviceName);
            }
            if (right != null) {
                right.setText(person.kudos + "");
            }
        }

        return v;

    }

}
