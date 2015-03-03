package com.coffee.tableapplication;

import android.util.Log;

/**
 * Created by Sheng-Han on 2/15/2015.
 */
public class BroadcastManager extends Thread{
    boolean running = true;
    TabletActivity activity = null;
    public BroadcastManager(TabletActivity activity) {
        this.activity = activity;
    }

    @Override
    public void run() {
        while(running) {
            try {
                activity.startDiscovery();


                try {
                    Thread.sleep(1000 * 60);                 //1000 milliseconds is one second.
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                Log.d(TabletActivity.TAG, "Registering Server Service.");
            }
            catch(Exception e) {
                Log.d("BroadcastManager", e.toString());
            }
        }
    }

    public void kill() {
        running = false;
    }
}
