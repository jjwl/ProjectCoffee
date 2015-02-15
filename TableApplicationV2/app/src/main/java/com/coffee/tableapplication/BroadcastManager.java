package com.coffee.tableapplication;

import android.util.Log;

/**
 * Created by Sheng-Han on 2/15/2015.
 */
public class BroadcastManager extends Thread{
    TabletActivity activity = null;
    public BroadcastManager(TabletActivity activity) {
        this.activity = activity;
    }

    @Override
    public void run() {
        while(true) {
            try {
                activity.registerServerService();


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
            finally {
                break;
            }
        }
    }
}
