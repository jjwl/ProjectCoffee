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
    private int contentMaster = -1; //The current content master - -1 if no content master
    private int quitAck = 0; //How many quit acknowledgements recieved.
    private ArrayList<User> roundList = new ArrayList<User>(); //The current display list.
    private HashMap<String, User> playerList = new HashMap<String, User>(); //List of total players.


    /**
     * Resets the list to only the online players and kudos to 0.
     */
    public void resetList() {
        Collection<User> userList = playerList.values();
        for(User person : userList) {
            if(!person.online) {
                playerList.remove(person.device.deviceAddress);
            }
        }

        roundList = new ArrayList<User>(playerList.values());
    }

    /**
     * Registers the user with the address and if already registered, flag it online.
     *
     * @param address The address of the client user.
     * @param username The screen name of the client user.
     */
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

    /**
     * Flags the user with the corresponding address offline.
     *
     * @param address The address of the client user.
     */
    public void offlineUser(String address) {
        if(playerList.containsKey(address)) {
            playerList.get(address).online = false;
        }
    }

    /**
     * Removes the user from the list of known users.
     *
     * @param address The address of the client user.
     */
    public void removeUser(String address) {
        roundList.remove(playerList.remove(address));
    }

    /**
     * Removes the user from the list of known users.
     *
     * @param number The number of the user in the round.
     */
    public void removeUser(int number) {
        playerList.remove(roundList.remove(number).device.deviceAddress);
    }

    /**
     * Returns the current Content Master's name
     *
     * @return The string that is the current content master's name.
     */
    public String currentMasterName() {
        if(roundList.size() > contentMaster && contentMaster != -1) {
            return roundList.get(contentMaster).device.deviceName;
        }
        return "No content master set";
    }

    /**
     * Returns the address of the current content master.
     *
     * @return The address of the current content master.
     */
    public String currentContentMaster() {
        if(roundList.size() > contentMaster) {
            return roundList.get(contentMaster).device.deviceAddress;
        }
        return "";
    }

    /**
     * The number of players currently playing.
     *
     * @return Number of players currently playing in an int.
     */
    public int players() {
        return roundList.size();
    }

    /**
     * Update to the next content master and return his or her address.
     *
     * @return The address of the next content master.
     */
    public String nextContentMaster() {
        int originalMaster = contentMaster;
        if(contentMaster == -1) {
            Random randgen = new Random();
            contentMaster = randgen.nextInt(roundList.size());
        }
        else {
            contentMaster++;
        }
        if(contentMaster >= roundList.size()) {
            contentMaster = 0;
        }
        while(roundList.size() != 0
                && contentMaster != originalMaster
                && !playerList.get(roundList.get(contentMaster).device.deviceAddress).online) {
            removeUser(contentMaster);
            if(contentMaster >= roundList.size()) {
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

    /**
     * Increase the kudos of the current content master.
     */
    public void addKudos() {
        if(roundList.size() > contentMaster && contentMaster != -1) {
            roundList.get(contentMaster).kudos++;
        }
    }

    /**
     * Checks if all the players have quit.
     *
     * @param quit Whether or not there was a recent send of a quit acknowledge.
     * @return True if all the players have quit.
     */
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

    /**
     * Returns the  current list of users with their kudos data.
     * @return The current list of users with all data except online or offline.
     */
    public ArrayList<User> getUserList() {
        return roundList;
    }

    /**
     * The winners of the game or the people who got the highest score.
     * @return All the addresses of the users who got the highest score seperated by a period.
     */
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
