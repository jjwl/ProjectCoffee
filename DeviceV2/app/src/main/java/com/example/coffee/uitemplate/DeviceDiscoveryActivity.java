package com.example.coffee.uitemplate;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import java.io.IOException;
import java.util.*;

public class DeviceDiscoveryActivity extends Activity implements ChannelListener, WifiP2pManager.ConnectionInfoListener, Handler.Callback {
        public static final String TAG = "tableActivity";
        public static final String TABLE_SERVICE = "_coffee_server_service";
        public static final String DEVICE_SERVICE = "_coffee_client_service";
        public static final String SERVICE_REG_TYPE = "_presence._tcp";
        public static final int SERVER_PORT = 4545;

        private WifiP2pManager manager;
        private boolean isWifiP2pEnabled = false;
        private boolean retryChannel = false;
        private final IntentFilter intentFilter = new IntentFilter();
        private Channel channel;
        private BroadcastReceiver receiver = null;
        private WifiP2pDnsSdServiceRequest serviceRequest;
        private Handler myHandler = new Handler(this);
        private MsgManager msgManager = null;

        /**
         * @param isWifiP2pEnabled the isWifiP2pEnabled to set
         */
        public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
            this.isWifiP2pEnabled = isWifiP2pEnabled;
        }
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            // add necessary intent values to be matched.
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
            intentFilter
                    .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            intentFilter
                    .addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

            manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
            channel = manager.initialize(this, getMainLooper(), null);


            Button regBtn = (Button)findViewById(R.id.registerBtn);
            regBtn.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Registering device - trying to find place to register
                    discoverService();
                }
            });

            //startRegistrationAndDiscovery();
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
        @Override
        protected void onStart() {
            MsgManager.getInstance().updateHandler(myHandler, manager, channel);
            super.onStop();
        }
        @Override
        protected void onStop() {
            MsgManager.getInstance().stop();
            super.onStop();
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        /*
         * (non-Javadoc)
         * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
         */
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            }

            return super.onOptionsItemSelected(item);

        }

    private void startRegistrationAndDiscovery() {
        Map<String, String> record = new HashMap<String, String>();

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                DEVICE_SERVICE, SERVICE_REG_TYPE, record);
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
                        Toast.makeText(DeviceDiscoveryActivity.this, "Service Available: " + instanceName, Toast.LENGTH_SHORT).show();
                        if (instanceName.equalsIgnoreCase(TABLE_SERVICE)) {
                            //If it is, try to connect
                            Toast.makeText(DeviceDiscoveryActivity.this, "Found service. Trying to connect", Toast.LENGTH_SHORT).show();
                            connectP2p(srcDevice);
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
                Toast.makeText(DeviceDiscoveryActivity.this, "Searching for valid services...", Toast.LENGTH_SHORT).show();
                //appendStatus("Service discovery initiated");
            }
            @Override
            public void onFailure(int arg0) {
                Toast.makeText(DeviceDiscoveryActivity.this, "Could not search for services. Try turning on WifiP2P.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void connectP2p(WifiP2pDevice service) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = service.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (serviceRequest != null) {
            manager.removeServiceRequest(channel, serviceRequest,
                    new ActionListener() {

                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailure(int arg0) {
                        }
                    });
        }

        manager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Connecting to service");
            }

            @Override
            public void onFailure(int errorCode) {
                Log.d(TAG,"Failed connecting to service");
            }
        });
    }
        @Override
        public void onChannelDisconnected() {
            // we will try once more
            if (manager != null && !retryChannel) {
                Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
                retryChannel = true;
                manager.initialize(this, getMainLooper(), this);
            } else {
                Toast.makeText(this,
                        "Severe! Channel is probably lost permanently. Try Disable/Re-Enable P2P.",
                        Toast.LENGTH_LONG).show();
            }
        }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
        Thread handler = null;
        /*
         * The group owner accepts connections using a server socket and then spawns a
         * client socket for every client. This is handled by {@code
         * GroupOwnerSocketHandler}
         */

        if (!p2pInfo.isGroupOwner) {
            Log.d(TAG, "Connected as peer");
            handler = new ClientSocketHandler(
                    this.myHandler,
                    p2pInfo.groupOwnerAddress);
            handler.start();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MsgManager.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, readMessage);
                break;

            case MsgManager.CONNECTION_SUCCESS:
                //Only when the entire thing has completed connection, go to welcome screen.

                startActivity(new Intent(this, WelcomeScreen.class));
                break;
        }
        return true;
    }
}

