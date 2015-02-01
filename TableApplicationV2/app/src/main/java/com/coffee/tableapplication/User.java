package com.coffee.tableapplication;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by Sheng-Han on 1/31/2015.
 */
public class User {
    WifiP2pDevice device;
    int kudos = 0;

    public User(WifiP2pDevice device) {
        int kudos = 0;
        this.device = device;
    }
}
