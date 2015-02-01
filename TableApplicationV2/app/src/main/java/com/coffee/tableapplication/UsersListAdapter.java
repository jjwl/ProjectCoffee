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
public class UsersListAdapter extends ArrayAdapter<User> {

    private List<User> items;

    public UsersListAdapter(Context context, int resource, int textViewResourceId,
                               List<User> objects) {
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
        User person = items.get(position);
        WifiP2pDevice device = person.device;
        if (device != null) {
            TextView top = (TextView) v.findViewById(android.R.id.text1);
            TextView bottom = (TextView) v.findViewById(android.R.id.text2);
            if (top != null) {
                top.setText(device.deviceName);
            }
            if (bottom != null) {
                bottom.setText(person.kudos);
            }
        }

        return v;

    }

    public String getItemName(int index) { return items.get(index).device.deviceName;}

    public String getItemAddress(int index){
        return items.get(index).device.deviceAddress;
    }

    public int getSize(){
        return items.size();
    }

    //This gets called whenever kudos gets received by the tabletActivity.
    //String contentMaster: the address of the contentMaster who received the kudos.
    public void updateKudos(String contentMaster){
        for(int i = 0; i < items.size(); i++){
            if(items.get(i).device.deviceAddress == contentMaster){
                items.get(i).kudos++;
                notifyDataSetChanged();
                return;
            }
        }

    }

    //This function calculated who won the score.
    //You might want to re-sort the list in order of ranking.
    //returns the address of the winner.
    public String finalizeScore(){

        return "";
    }

    public void setName(String address, String name){
        for(int i = 0; i < items.size(); i++){
            if(items.get(i).device.deviceAddress == address){
                items.get(i).device.deviceName = name;
                notifyDataSetChanged();
                return;
            }
        }
    }
}
