package com.coffee.tableapplication;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by Sheng-Han on 1/31/2015.
 */
public class User implements Comparable<User> {
    WifiP2pDevice device;
    boolean online = true;
    int kudos = 0;

    /**
     * Constructor for a user, constructs a User from client address and username.
     * @param address The address of the player.
     * @param username The username of the player.
     */
    public User(String address, String username) {
        device = new WifiP2pDevice();
        device.deviceName = username;
        device.deviceAddress = address;
        kudos = 0;
        online = true;
    }

    /**
     * Constructor for a new user, constructs a User from device information.
     * @param device The device of the player.
     */
    public User(WifiP2pDevice device) {
        kudos = 0;
        online = true;
        this.device = device;
    }

    /**
     * Comparison between two Users, difference determined by number of kudos followed by the name.
     * @param another The other user.
     * @return Positive if the user has more kudos, negative if less.
     */
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

    /**
     * If the address is equal to the User's address, both objects are equal including Strings.
     * @param other The object being compared to.
     * @return Whether or not they have the same address.
     */
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
