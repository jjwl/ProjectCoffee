package com.coffee.tableapplication;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import static android.app.PendingIntent.getActivity;

/**
 * Created by Sheng-Han on 12/6/2014.
 */
public class UsersListAdapter extends ArrayAdapter<User> {

    private List<User> items;
    private Map<String, User> discoveryDevice = new HashMap<String, User>();

    public UsersListAdapter(Context context, int resource, int textViewResourceId,
                               List<User> objects) {
        super(context, resource, textViewResourceId, objects);
        items = objects;
    }

    public void addPerson(String address, User person) {
        discoveryDevice.put(address, person);
        items.clear();
        items.addAll(discoveryDevice.values());
        this.sort(new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return lhs.compareTo(rhs);
            }
        });
        this.notifyDataSetChanged();
    }

    public void removePerson(int number) {
        discoveryDevice.remove(this.getItemAddress(number));
        items.clear();
        items.addAll(discoveryDevice.values());
        this.sort(new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return lhs.compareTo(rhs);
            }
        });
        notifyDataSetChanged();
    }
    public void removePerson(String address) {
        discoveryDevice.remove(address);
        items.clear();
        items.addAll(discoveryDevice.values());
        this.sort(new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                return lhs.compareTo(rhs);
            }
        });
        notifyDataSetChanged();
    }

    public void updateAll() {
        for(User person : items) {
            if(discoveryDevice.get(person.device.deviceAddress).online)
            {
                person.kudos = 0;
                discoveryDevice.put(person.device.deviceAddress, person);
            }
            else {
                discoveryDevice.remove(person.device.deviceAddress);
            }
        }
        this.clear();
        this.addAll(discoveryDevice.values());
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
                bottom.setText(person.kudos + "");
            }
        }

        return v;

    }

    public String getItemName(int index) { return this.getItem(index).device.deviceName;}

    public String getItemAddress(int index){
        return this.getItem(index).device.deviceAddress;
    }

    public int getSize(){
        return this.getCount();
    }

    //This gets called whenever kudos gets received by the tabletActivity.
    //String contentMaster: the address of the contentMaster who received the kudos.
    public void updateKudos(String contentMaster){
        User person = discoveryDevice.get(contentMaster);
        person.kudos++;
        discoveryDevice.put(contentMaster, person);
        items.clear();
        items.addAll(discoveryDevice.values());
        /*for(int i = 0; i < this.getCount(); i++){
            if(this.getItem(i).device.deviceAddress == contentMaster){
                this.getItem(i).kudos++;
                notifyDataSetChanged();
                return;
            }
        }*/

    }

    public int getScore(int num) {
        return items.get(num).kudos;
    }

    public int getScore(String address) {
        return discoveryDevice.get(address).kudos;
    }

    public void updateConnected(String address, boolean connected) {
        User person = discoveryDevice.get(address);
        person.online = connected;
    }

    public boolean isOnline(String address) {
        return discoveryDevice.get(address).online;
    }

    public boolean isOnline(int cM) {
        String address = this.getItemAddress(cM);
        return discoveryDevice.get(address).online;
    }


    public boolean hasPerson(String address) {
        return discoveryDevice.containsKey(address);
    }


    //This function calculated who won the score.
    //You might want to re-sort the list in order of ranking.
    //returns the address of the winner.
    public String finalizeScore(){
        String winner = "";
        int maxScore = -1;
        for(int i = 0; i < this.getCount(); i++){
            if(this.getItem(i).kudos > maxScore) {
                maxScore = this.getItem(i).kudos;
                winner = this.getItemAddress(i);
            }
        }
        this.sort(new Comparator<User>() {

            @Override
            public int compare(User lhs, User rhs) {
                return lhs.compareTo(rhs);
            }
        });
        return winner;
    }

    public void setName(String address, String name){
        for(int i = 0; i < this.getCount(); i++){
            if(this.getItem(i).device.deviceAddress.equals(address)){
                this.getItem(i).device.deviceName = name;
                notifyDataSetChanged();
                return;
            }
        }
    }
}
