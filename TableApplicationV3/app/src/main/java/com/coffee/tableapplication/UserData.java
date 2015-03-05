package com.coffee.tableapplication;

import android.net.wifi.p2p.WifiP2pDevice;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Sheng-Han on 3/1/2015.
 */
public class UserData {
    private int contentMaster = -1;
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
            int names = 0;
            for(User person : roundList) {
                if(person.device.deviceName.equals(username)){
                    names++;
                }
            }
            if(names > 0) {
                username = username + " - " + names;
            }
            playerList.put(address, new User(address, username));
            roundList.add(new User(address, username));
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
        if(roundList.size() > contentMaster && contentMaster != -1) {
            return roundList.get(contentMaster).device.deviceName;
        }
        return "No content master set";
    }
    public String currentContentMaster() {
        if(roundList.size() > contentMaster) {
            return roundList.get(contentMaster).device.deviceAddress;
        }
        return "";
    }

    public int players() {
        return roundList.size();
    }

    public String nextContentMaster() {
        int originalMaster = contentMaster;
        if(contentMaster == -1) {
            Random randgen = new Random();
            contentMaster = randgen.nextInt(roundList.size());
        }
        else {
            contentMaster++;
        }
        if(contentMaster > roundList.size()) {
            contentMaster = 0;
        }
        while(roundList.size() != 0
                && contentMaster != originalMaster
                && !playerList.get(roundList.get(contentMaster).device.deviceAddress).online) {
            removeUser(contentMaster);
            if(contentMaster > roundList.size()) {
                contentMaster = 0;
            }
        }
        if(roundList.size() > contentMaster) {
            return roundList.get(contentMaster).device.deviceAddress;
        }
        else {
            return "";
        }
    }

    public void addKudos() {
        roundList.get(contentMaster).kudos++;
    }

    public boolean quitDone(boolean quit) {
        if(quit) {
            quitAck++;
        }
        if(quitAck >= roundList.size()) {
            quitAck = 0;
            contentMaster = -1;
            return true;
        }
        return false;
    }

    public ArrayList<User> getUserList() {
        return roundList;
    }

    public String getWinners() {
        resetList();
        Collections.sort(roundList);
        Collections.reverse(roundList);
        String winner = "";
        if(roundList.size() > 0) {
            int maxKudos = roundList.get(0).kudos;
            for (User person : roundList) {
                if (person.kudos == maxKudos) {
                    winner = winner + "." + person.device.deviceAddress;
                }
            }
        }
        return winner;
    }
}
