package com.coffee.tableapplication;

import android.net.wifi.p2p.WifiP2pDevice;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Sheng-Han on 3/1/2015.
 */
public class UserData {
    private int contentMaster = 0;
    private int quitAck = 0;
    private ArrayList<User> roundList = new ArrayList<User>();
    private HashMap<String, User> playerList = new HashMap<String, User>();

    public void resetList() {
        Collection<User> userList = playerList.values();
        for(User person : userList) {
            if(!person.online) {
                playerList.remove(person.device.deviceAddress);
            }
        }

        roundList = new ArrayList<User>(playerList.values());
    }

    public void addUser(String address, String username) {
        if(playerList.containsKey(address)) {
            playerList.get(address).online = true;
        }
        else {
            playerList.put(address, new User(address, username));
        }
    }

    public void offlineUser(String address) {
        if(playerList.containsKey(address)) {
            playerList.get(address).online = false;
        }
    }

    public void removeUser(String address) {
        roundList.remove(playerList.remove(address));
    }

    public void removeUser(int number) {
        playerList.remove(roundList.remove(number).device.deviceAddress);
    }

    public String currentMasterName() {
        return roundList.get(contentMaster).device.deviceName;
    }
    public String currentContentMaster() {
        return roundList.get(contentMaster).device.deviceAddress;
    }

    public int players() {
        return roundList.size();
    }

    public String nextContentMaster() {
        contentMaster++;
        if(contentMaster > roundList.size()) {
            contentMaster = 0;
        }
        while(!roundList.get(contentMaster).online) {
            removeUser(contentMaster);
            if(contentMaster > roundList.size()) {
                contentMaster = 0;
            }
        }
        if(roundList.size() != 0) {
            return roundList.get(contentMaster).device.deviceAddress;
        }
        else {
            return "";
        }
    }

    public void addKudos() {
        roundList.get(contentMaster).kudos++;
    }

    public boolean quit() {
        quitAck++;
        if(quitAck == roundList.size()) {
            quitAck = 0;
            return true;
        }
        return false;
    }

    public ArrayList<User> getUserList() {
        return roundList;
    }

    public String getWinners() {
        Collections.sort(roundList);
        Collections.reverse(roundList);
        String winner = "";
        int maxKudos = roundList.get(0).kudos;
        for(User person : roundList) {
            if(person.kudos == maxKudos) {
                winner = winner + "." + person.device.deviceAddress;
            }
        }
        return winner;
    }
}
