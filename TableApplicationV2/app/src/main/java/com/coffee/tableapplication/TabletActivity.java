package com.coffee.tableapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
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
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class TabletActivity extends Activity implements WifiP2pManager.ConnectionInfoListener, Handler.Callback{
    public static final String TAG = "tableActivity";

    //public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String TABLE_SERVICE = "_coffee_server_service";
    public static final String DEVICE_SERVICE = "_coffee_client_service";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    public static final int SERVER_PORT = 4545;
    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;

    private WifiP2pManager manager;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private Handler handler = new Handler(this);
    private MsgManager msgManager = null;
    private UsersListAdapter adapter;
    private Timer timer = null;

    private int minutes = 2;
    private int nextCM = 0;
    private Random rand = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tablet);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        Button playBtn = (Button)findViewById(R.id.playBtn);

        msgManager = MsgManager.getInstance();

        adapter = new UsersListAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1,  new ArrayList<WifiP2pDevice>());
        ListView userList = (ListView) findViewById(R.id.regList);
        userList.setAdapter(adapter);
        playBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start Content Master thread.
                manager.discoverServices(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        //appendStatus("Service discovery initiated");
                        Log.d(TabletActivity.TAG, "Service discovery initiated.");
                    }

                    @Override
                    public void onFailure(int arg0) {
                        //appendStatus("Service discovery failed");
                        Log.d(TabletActivity.TAG, "Service discovery failed.");

                    }
                });


                timer = new Timer();
                rand = new Random();
                nextCM = rand.nextInt(adapter.getSize() - 1);

                while(true){
                    timer.schedule( new TimerTask() {
                        public void run() {
                            nextCM++;
                            if(nextCM + 1 == adapter.getSize()){
                                nextCM = 0;
                            }
                            MsgManager.getInstance().write(("ContentMaster" + adapter.getItemName(nextCM)).getBytes());
                        }
                    }, 0, 60*1000*minutes);
                }


            }
        });

        registerServerService();
    }

    @Override
    protected void onStop() {
        if (manager != null && channel != null) {
            manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

                @Override
                public void onFailure(int reasonCode) {
                    Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
                }

                @Override
                public void onSuccess() {
                }

            });
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new TabletBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tablet, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateUserList(WifiP2pGroup devices) {
        adapter.clear();
        adapter.addAll(devices.getClientList());
        adapter.notifyDataSetChanged();
    }

    private void registerServerService() {

        Map<String, String> record = new HashMap<String, String>();
        //record.put(TXTRECORD_PROP_AVAILABLE, "visible");

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                TABLE_SERVICE, SERVICE_REG_TYPE, record);
        manager.addLocalService(channel, service, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TabletActivity.TAG, "Added service.");
            }

            @Override
            public void onFailure(int error) {
                Log.d(TabletActivity.TAG, "Failed to add service...");
            }
        });
        startDiscovery();
    }

    private void startDiscovery() {
        manager.setDnsSdResponseListeners(channel,
                new WifiP2pManager.DnsSdServiceResponseListener() {

                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                                                        String registrationType, WifiP2pDevice srcDevice) {

                        // A service has been discovered. Is this our app?

                        if (instanceName.equalsIgnoreCase(DEVICE_SERVICE)) {

                            // update the UI and add the item the discovered
                            // device.
                            Log.d(TabletActivity.TAG, "Found device.");

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
                        Log.d(TAG,
                                device.deviceName + " detected");

                    }
                });

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel, serviceRequest,
                new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        //appendStatus("Added service discovery request");
                        Log.d(TabletActivity.TAG, "Added service discovery request.");
                    }

                    @Override
                    public void onFailure(int arg0) {
                        //appendStatus("Failed adding service discovery request");
                        Log.d(TabletActivity.TAG, "Failed to add service discovery request.");
                    }
                });
        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //appendStatus("Service discovery initiated");
                Log.d(TabletActivity.TAG, "Service discovery initiated.");
            }

            @Override
            public void onFailure(int arg0) {
                //appendStatus("Service discovery failed");
                Log.d(TabletActivity.TAG, "Service discovery failed.");

            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Thread handler = null;
        /*
         * The group owner accepts connections using a server socket and then spawns a
         * client socket for every client. This is handled by {@code
         * GroupOwnerSocketHandler}
         */

        if (info.isGroupOwner) {
            Log.d(TAG, "Connected as group owner");
            try {
                handler = new CoffeeServerSocketHandler(this.handler);
                handler.start();
            } catch (IOException e) {
                Log.d(TAG,
                        "Failed to create a server thread - " + e.getMessage());
                return;
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, readMessage);
                if(readMessage.contains("Kudos")) {
                    Toast.makeText(this, readMessage, Toast.LENGTH_LONG).show();
                }
                break;

            case MY_HANDLE:
                //Practically unnecessary; socket should be the same each time due to
                //it being a server-socket.
                Object obj = msg.obj;
                msgManager = (MsgManager) obj;
                break;
        }
        return true;
    }
}
