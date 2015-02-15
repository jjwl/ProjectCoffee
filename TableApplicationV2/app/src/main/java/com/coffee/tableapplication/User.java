package com.coffee.tableapplication;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by Sheng-Han on 1/31/2015.
 */
public class User implements Comparable<User> {
    WifiP2pDevice device;
    boolean online = true;
    int kudos = 0;

    public User(WifiP2pDevice device) {
        kudos = 0;
        online = true;
        this.device = device;
    }

    @Override
    public int compareTo(User another) {
        if(another == null) {
            return 1;
        }
        if(this.kudos == another.kudos) {
            return device.deviceName.compareTo(another.device.deviceName);
        }
        return this.kudos - another.kudos;
    }
}
