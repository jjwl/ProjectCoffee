package com.coffee.tableapplication;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by Sheng-Han on 1/31/2015.
 */
public class User implements Comparable<User> {
    WifiP2pDevice device;
    boolean online = true;
    int kudos = 0;

    public User(String address, String username) {
        device = new WifiP2pDevice();
        device.deviceName = username;
        device.deviceAddress = address;
        kudos = 0;
        online = true;
    }
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

    @Override
    public boolean equals(Object other) {
        if(other instanceof String) {
            return ((String) other).equalsIgnoreCase(this.device.deviceAddress);
        }
        else if(other instanceof User) {
            return ((User) other).device.deviceAddress.equalsIgnoreCase(this.device.deviceAddress);
        }
        else if(other instanceof WifiP2pDevice) {
            return ((WifiP2pDevice) other).deviceAddress.equalsIgnoreCase(this.device.deviceAddress);
        }
        else {
            return false;
        }
    }
}
