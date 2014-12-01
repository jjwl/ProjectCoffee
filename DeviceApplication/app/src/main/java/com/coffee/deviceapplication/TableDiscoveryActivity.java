package com.coffee.deviceapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.coffee.deviceapplication.DeviceListFragment.DeviceActionListener;
import com.coffee.deviceapplication.DeviceListFragment.WiFiPeerListAdapter;

import java.util.HashMap;
import java.util.Map;

public class TableDiscoveryActivity extends Activity implements ChannelListener, DeviceActionListener {
        public static final String TAG = "tableActivity";
        private static final String DEVICE_SERVICE = "_tableService";
        public static final String SERVICE_INSTANCE = "_tableService";
        public static final String SERVICE_REG_TYPE = "_presence._tcp";
        private WifiP2pManager manager;
        private boolean isWifiP2pEnabled = false;
        private boolean retryChannel = false;
        private final IntentFilter intentFilter = new IntentFilter();
        private Channel channel;
        private BroadcastReceiver receiver = null;
        private WifiP2pDnsSdServiceRequest serviceRequest;

        /**
         * @param isWifiP2pEnabled the isWifiP2pEnabled to set
         */
        public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
            this.isWifiP2pEnabled = isWifiP2pEnabled;
        }
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(com.coffee.deviceapplication.R.layout.main);
            // add necessary intent values to be matched.
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

            manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
            channel = manager.initialize(this, getMainLooper(), null);

            startRegistrationAndDiscovery();
        }
        /** register the BroadcastReceiver with the intent values to be matched */
        @Override
        public void onResume() {
            super.onResume();
            receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
            registerReceiver(receiver, intentFilter);
        }
        @Override
        public void onPause() {
            super.onPause();
            unregisterReceiver(receiver);
        }
        /**
         * Remove all peers and clear all fields. This is called on
         * BroadcastReceiver receiving a state change event.
         */
        public void resetData() {
            DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(com.coffee.deviceapplication.R.id.frag_list);
            DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                    .findFragmentById(com.coffee.deviceapplication.R.id.frag_detail);
            if (fragmentList != null) {
                fragmentList.clearPeers();
            }
            if (fragmentDetails != null) {
                fragmentDetails.resetViews();
            }
        }
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(com.coffee.deviceapplication.R.menu.action_items, menu);
            return true;
        }
        /*
         * (non-Javadoc)
         * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
         */
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case com.coffee.deviceapplication.R.id.atn_direct_enable:
                    if (manager != null && channel != null) {
                        // Since this is the system wireless settings activity, it's
                        // not going to send us a result. We will be notified by
                        // WiFiDeviceBroadcastReceiver instead.
                        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    } else {
                        Log.e(TAG, "channel or manager is null");
                    }
                    return true;
                case com.coffee.deviceapplication.R.id.atn_direct_discover:
                    if (!isWifiP2pEnabled) {
                        Toast.makeText(TableDiscoveryActivity.this, com.coffee.deviceapplication.R.string.p2p_off_warning,
                                Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                            .findFragmentById(com.coffee.deviceapplication.R.id.frag_list);
                    fragment.onInitiateDiscovery();
                    manager.discoverServices(channel, new ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(TableDiscoveryActivity.this, "Discovery Initiated",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int reasonCode) {
                            Toast.makeText(TableDiscoveryActivity.this, "Discovery Failed : " + reasonCode,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

    private void startRegistrationAndDiscovery() {
        Map<String, String> record = new HashMap<String, String>();

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
        manager.addLocalService(channel, service, new ActionListener() {

            @Override
            public void onSuccess() {
                //appendStatus("Added Local Service");
            }

            @Override
            public void onFailure(int error) {
                //appendStatus("Failed to add a service");
            }
        });

        discoverService();

    }

    private void discoverService() {
        /*
         * Register listeners for DNS-SD services. These are callbacks invoked
         * by the system when a service is actually discovered.
         */
        manager.setDnsSdResponseListeners(channel,
                new WifiP2pManager.DnsSdServiceResponseListener() {
                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                                                        String registrationType, WifiP2pDevice srcDevice) {
                        // A service has been discovered. Is this our app?
                        Log.d(TAG, "Service Available: " + instanceName);
                        if (instanceName.equalsIgnoreCase(DEVICE_SERVICE)) {
                            // update the UI and add the item the discovered
                            // device.
                            DeviceListFragment fragment = (DeviceListFragment) getFragmentManager().findFragmentById(com.coffee.deviceapplication.R.id.frag_list);
                            if (fragment != null) {
                                WiFiPeerListAdapter adapter = (WiFiPeerListAdapter) fragment
                                        .getListAdapter();
                                adapter.add(srcDevice);
                                adapter.notifyDataSetChanged();
                                Log.d(TAG, "onBonjourServiceAvailable "
                                        + instanceName);
                            }
                        }
                    }
                }, new WifiP2pManager.DnsSdTxtRecordListener() {
                    /**
                     * A new TXT record is available. Pick up the advertised
                     * buddy name.
                     */
                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomainName, Map<String, String> record,
                            WifiP2pDevice device) {
                  //      Log.d(TAG,
                   //             device.deviceName + " is "
                   //                     + record.get(TXTRECORD_PROP_AVAILABLE));
                    }
                });
        // After attaching listeners, create a service request and initiate
        // discovery.
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel, serviceRequest,
                new ActionListener() {
                    @Override
                    public void onSuccess() {
                        //appendStatus("Added service discovery request");
                    }
                    @Override
                    public void onFailure(int arg0) {
                        //appendStatus("Failed adding service discovery request");
                    }
                });
        manager.discoverServices(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                //appendStatus("Service discovery initiated");
            }
            @Override
            public void onFailure(int arg0) {
                //appendStatus("Service discovery failed");
            }
        });
    }


        @Override
        public void showDetails(WifiP2pDevice device) {
            DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                    .findFragmentById(com.coffee.deviceapplication.R.id.frag_detail);
            fragment.showDetails(device);
        }
        @Override
        public void connect(WifiP2pConfig config) {
            manager.connect(channel, config, new ActionListener() {
                @Override
                public void onSuccess() {
                    // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                }
                @Override
                public void onFailure(int reason) {
                    Toast.makeText(TableDiscoveryActivity.this, "Connect failed. Retry.",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        @Override
        public void disconnect() {
            final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                    .findFragmentById(com.coffee.deviceapplication.R.id.frag_detail);
            fragment.resetViews();
            manager.removeGroup(channel, new ActionListener() {
                @Override
                public void onFailure(int reasonCode) {
                    Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
                }
                @Override
                public void onSuccess() {
                    fragment.getView().setVisibility(View.GONE);
                }
            });
        }
        @Override
        public void onChannelDisconnected() {
            // we will try once more
            if (manager != null && !retryChannel) {
                Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
                resetData();
                retryChannel = true;
                manager.initialize(this, getMainLooper(), this);
            } else {
                Toast.makeText(this,
                        "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                        Toast.LENGTH_LONG).show();
            }
        }
        @Override
        public void cancelDisconnect() {
        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
            if (manager != null) {
                final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                        .findFragmentById(com.coffee.deviceapplication.R.id.frag_list);
                if (fragment.getDevice() == null
                        || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                    disconnect();
                } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                        || fragment.getDevice().status == WifiP2pDevice.INVITED) {
                    manager.cancelConnect(channel, new ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(TableDiscoveryActivity.this, "Aborting connection",
                                    Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(int reasonCode) {
                            Toast.makeText(TableDiscoveryActivity.this,
                                    "Connect abort request failed. Reason Code: " + reasonCode,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }

