package com.coffee.tableapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * Created by Sheng-Han on 12/6/2014.
 */
public class TabletBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private Activity activity;

    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public TabletBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       Activity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TabletActivity.TAG, action);
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                Log.d("WifiP2p", "WifiDirect Enabled");
                Toast.makeText(activity, "WifiP2P enabled", Toast.LENGTH_SHORT);
            } else {
                Log.d("WifiP2p", "WifiDirect Disabled");
                Toast.makeText(activity, "Attempting to turn on wifi direct.", Toast.LENGTH_SHORT).show();
                WifiManager wifi  = (WifiManager)activity.getSystemService(activity.WIFI_SERVICE);
                wifi.setWifiEnabled(true);
                try {
                    Class<?> wifiManager = Class
                            .forName("android.net.wifi.p2p.WifiP2pManager");

                    Method method = wifiManager
                            .getMethod(
                                    "enableP2p",
                                    new Class[] { android.net.wifi.p2p.WifiP2pManager.Channel.class });

                    method.invoke(manager, channel);

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(activity, "Could not open Wifi Direct. Please check manually and press play.", Toast.LENGTH_SHORT).show();
                    Log.d("WifiManager", e.toString());
                }

            }
            Log.d("WifiP2p", "P2P state changed - " + state);
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP
                Log.d(TabletActivity.TAG,
                        "Connected to p2p network. Requesting list of group members");
                manager.requestConnectionInfo(channel,
                        (WifiP2pManager.ConnectionInfoListener) activity);
                //Need to also update connected peer-list here.
                ((TabletActivity) activity).updateUserList((WifiP2pGroup) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP));
            } else {
                // It's a disconnect
                ((TabletActivity) activity).updateUserList((WifiP2pGroup) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP));
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
                .equals(action)) {

            WifiP2pDevice device = (WifiP2pDevice) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            Log.d(TabletActivity.TAG, "Device status -" + device.status);

        }
    }
}
