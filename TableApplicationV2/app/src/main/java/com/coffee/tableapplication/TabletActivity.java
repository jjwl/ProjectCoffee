package com.coffee.tableapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
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

import org.apache.commons.lang.time.StopWatch;
//import org.apache.commons.lang3.time.StopWatch;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


//Settings->Wifi->...->Wifi direct->back to app

public class TabletActivity extends Activity implements WifiP2pManager.ConnectionInfoListener, Handler.Callback{
    public static final String TAG = "tableActivity";

    //public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String TABLE_SERVICE = "_coffee_server_service";
    public static final String DEVICE_SERVICE = "_coffee_client_service";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    public static final int SERVER_PORT = 4545;
    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;
    public static final int MANAGER_CLOSE = 0x400 + 3;

    private WifiP2pManager manager;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private WifiP2pDnsSdServiceInfo myService;
    private Handler handler = new Handler(this);
    private CoffeeServerSocketHandler msgManager = null;
    private UsersListAdapter adapter;
//    private Timer timer = null;
    private StopWatch stopWatch = null;
    private StopWatch gameLoopWatch = null;

//    private boolean videoPlaying = false;
    private boolean startedGame = false;
    private int kudosLimitTime = 10; //time between sending kudos in seconds
    private int roundTime = 2; //time in between rounds in minutes
    private int nextCM = 0;
    private int quitCounter = 0;
    private Random rand = null;

    private Activity thisContext = this;
    private BroadcastManager broadcastRepeater = null;

    private Map<String, User> discoveryDevice = new HashMap<String, User>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tablet);
        stopWatch = new StopWatch();
        gameLoopWatch = new StopWatch();
        stopWatch.start();
        gameLoopWatch = new StopWatch();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        adapter = new UsersListAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1,  new ArrayList<User>());
        ListView userList = (ListView) findViewById(R.id.regList);
        userList.setAdapter(adapter);

        Button playBtn = (Button)findViewById(R.id.playBtn);
        Button quitBtn = (Button)findViewById(R.id.quitBtn);

        quitBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitGame();
            }
        });

        playBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!startedGame) {
                    if (serviceRequest != null) {
                        manager.removeLocalService(channel, myService,
                                new WifiP2pManager.ActionListener() {

                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onFailure(int arg0) {
                                    }
                                });
                    }
                    registerServerService();
                }

                //Start Content Master thread.

                if(adapter.getSize() > 0 && !startedGame) {
                    rand = new Random();
                    nextCM = rand.nextInt(adapter.getSize());
                    Log.d(TabletActivity.TAG, "content master : " + adapter.getItemAddress(nextCM));
                    msgManager.write(("ContentMaster" + adapter.getItemAddress(nextCM)).getBytes());
                    gameLoopWatch.start();
                    startedGame = true;
                    setContentView(R.layout.activity_main_screen);
                    ListView userList = (ListView) findViewById(R.id.scoreboard);
                    userList.setAdapter(adapter);
                    receiver = new TabletBroadcastReceiver(manager, channel, thisContext);
                    registerReceiver(receiver, intentFilter);
                }
            }
        });

        playBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!startedGame) {
                    if (serviceRequest != null) {
                        manager.removeLocalService(channel, myService,
                                new WifiP2pManager.ActionListener() {

                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onFailure(int arg0) {
                                    }
                                });
                    }
                    registerServerService();
                }

                //Start Content Master thread.

                if(adapter.getSize() > 0 && !startedGame) {
                    rand = new Random();
                    nextCM = rand.nextInt(adapter.getSize());
                    Log.d(TabletActivity.TAG, "content master : " + adapter.getItemAddress(nextCM));
                    msgManager.write(("ContentMaster" + adapter.getItemAddress(nextCM)).getBytes());
                    gameLoopWatch.start();
                    startedGame = true;
                    setContentView(R.layout.activity_main_screen);
                    ListView userList = (ListView) findViewById(R.id.scoreboard);
                    userList.setAdapter(adapter);
                    receiver = new TabletBroadcastReceiver(manager, channel, thisContext);
                    registerReceiver(receiver, intentFilter);
                }
            }
        });


        registerServerService();
        broadcastRepeater = new BroadcastManager(this);
        broadcastRepeater.start();
        manager.createGroup(channel,new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {

                Toast.makeText(TabletActivity.this, "Group Created",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }

    public void quitGame(){
        String winnerAddress = "";
        int[] scores = new int[adapter.getSize()];

        for(int i = 0; i < adapter.getSize(); i++){
            scores[i] = adapter.getScore(i);
        }
        Arrays.sort(scores);
        for(int i = 0; i < adapter.getSize(); i++){
            if(scores[adapter.getSize() - 1] == adapter.getScore(i)){
                winnerAddress += "." + adapter.getItemAddress(i);
            }
        }
        msgManager.write(("Quit" + winnerAddress).getBytes());
        startedGame = false;
        //Calculate scores and display them

     }

    @Override
    protected void onDestroy() {
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
        super.onDestroy();
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

    public boolean updateList(WifiP2pDeviceList list) {
        ArrayList<String> offline = adapter.updateList(list);
        for(String address : offline) {
            msgManager.removeSocket(address);
        }
        return true;
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
        if (id == R.id.menu_quit) {
            //gameLoopWatch.stop();
            //stopWatch.reset();
            //gameLoopWatch.reset();


            quitGame();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

/*    public void updateUserList() {
        if(discoveryDevice.size() == 0 && startedGame) {
            stopWatch.stop();
            gameLoopWatch.stop();
            stopWatch.reset();
            gameLoopWatch.reset();
            startedGame = false;
        }
        adapter.clear();
        ArrayList<User> users = new ArrayList<User>(discoveryDevice.values());
        adapter.addAll(users);
        adapter.notifyDataSetChanged();
    }*/

    public void registerServerService() {

        Map<String, String> record = new HashMap<String, String>();
        //record.put(TXTRECORD_PROP_AVAILABLE, "visible");

        myService = WifiP2pDnsSdServiceInfo.newInstance(
                TABLE_SERVICE, SERVICE_REG_TYPE, record);
        manager.addLocalService(channel, myService, new WifiP2pManager.ActionListener() {

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
                        /*if(record.get("name") != null && !"Enter Name".equals(record.get("name"))){
                            adapter.setName(device.deviceAddress, record.get("name"));
                            discoveryDevice.put(device.deviceAddress,  record.get("name"));
                        }*/
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
                if (serviceRequest != null) {
                    manager.removeServiceRequest(channel, serviceRequest,
                            new WifiP2pManager.ActionListener() {

                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onFailure(int arg0) {
                                }
                            });
                }
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
                msgManager  = new CoffeeServerSocketHandler(this.handler);
                handler = msgManager;
                handler.start();
            } catch (IOException e) {
                Log.d(TAG,
                        "Failed to create a new server thread - " + e.getMessage());
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

                if(readMessage.contains("QuitAck")){
                    //Change Activity to Play Again Screen
                    quitCounter++;
                    if(quitCounter == adapter.getCount()) {
                        if(quitCounter > 2) {
                            setContentView(R.layout.activity_main_screen);
                            ListView userList = (ListView) findViewById(R.id.regList);
                            userList.setAdapter(adapter);
                            adapter.updateAll();
                            adapter.notifyDataSetChanged();

                            rand = new Random();
                            nextCM = rand.nextInt(adapter.getSize());
                            msgManager.write(("ContentMaster" + adapter.getItemAddress(nextCM)).getBytes());
                        }
                        else {
                            setContentView(R.layout.activity_tablet);
                            ListView userList = (ListView) findViewById(R.id.regList);
                            userList.setAdapter(adapter);
                            adapter.updateAll();
                            adapter.notifyDataSetChanged();

                            buttonSetup();
                        }
                        quitCounter = 0;
                    }
                }

                if(readMessage.contains("VideoFinished")) {
                    //Check current time, compare with start time, if > 2 min,
                    // send cm packet and update start time. Otherwise ignore.
                    if(gameLoopWatch.getTime() > 1000 * roundTime * 60){
                        int originalCM = nextCM;
                        nextCM++;
                        if (nextCM + 1 >= adapter.getSize()) {
                            nextCM = 0;
                        }
                        while(adapter.getCount() != 0 && !adapter.isOnline(nextCM)){
                            adapter.removePerson(nextCM);
                            nextCM++;
                            if (nextCM + 1 >= adapter.getSize()) {
                                nextCM = 0;
                            }
                        }

                        gameLoopWatch.stop();
                        gameLoopWatch.reset();
                        gameLoopWatch.start();
                        msgManager.write(("ContentMaster" + adapter.getItemAddress(nextCM)).getBytes());
                    }
                }

                if(readMessage.contains("Kudos") && stopWatch.getTime() > kudosLimitTime * 1000) {
                    stopWatch.stop();
                    stopWatch.reset();
                    stopWatch.start();
                    Toast.makeText(this, readMessage, Toast.LENGTH_LONG).show();
                    adapter.updateKudos(adapter.getItemAddress(nextCM));
                }

                if(readMessage.contains("User:")) {
                    String username = readMessage.substring(readMessage.indexOf("User: ") + 6, readMessage.indexOf(","));
                    String deviceAddress = readMessage.substring(readMessage.indexOf(", ") + 2);
                    WifiP2pDevice device = new WifiP2pDevice();
                    if(!adapter.hasPerson(deviceAddress)) {
                        device.deviceAddress = deviceAddress;
                        device.deviceName = username;
                        User person = new User(device);
                        adapter.addPerson(deviceAddress, person);
                    }
                    else {
                        adapter.updateConnected(deviceAddress, true);
                    }

                    Log.d(TAG, username + ": " + deviceAddress);
                }
                break;

            case MY_HANDLE:
                Log.d(TAG, "New Connection");
                Toast.makeText(this, "New Connection Found.", Toast.LENGTH_SHORT).show();
                break;
            case MANAGER_CLOSE:
                Log.d(TAG, "Disconnect");
                String address = (String) msg.obj;
                adapter.updateConnected(address, false);
                Log.d(TAG, adapter.getItemAddress(nextCM) + address);

               if(startedGame == false) {
                   adapter.removePerson(address);
               }
               else if(adapter.getCount() > 1 && adapter.getItemAddress(nextCM).equals(address)) {
                   Log.d(TAG, "Updating CM");
                   nextCM++;
                   if (nextCM + 1 >= adapter.getSize()) {
                       nextCM = 0;
                   }
                   while(adapter.getCount() != 0 && !adapter.isOnline(nextCM)){
                       adapter.removePerson(nextCM);
                       nextCM++;
                       if (nextCM + 1 >= adapter.getSize()) {
                           nextCM = 0;
                       }
                   }
                   msgManager.write(("ContentMaster" + adapter.getItemAddress(nextCM)).getBytes());
                   Log.d(TabletActivity.TAG, "Content master : " + adapter.getItemAddress(nextCM));
                }
                if(quitCounter == adapter.getCount()) {
                    if(quitCounter > 2) {
                        setContentView(R.layout.activity_main_screen);
                        ListView userList = (ListView) findViewById(R.id.regList);
                        userList.setAdapter(adapter);
                        adapter.updateAll();
                        adapter.notifyDataSetChanged();

                        rand = new Random();
                        nextCM = rand.nextInt(adapter.getSize());
                        msgManager.write(("ContentMaster" + adapter.getItemAddress(nextCM)).getBytes());
                    }
                    else {
                        setContentView(R.layout.activity_tablet);
                        ListView userList = (ListView) findViewById(R.id.regList);
                        userList.setAdapter(adapter);
                        adapter.updateAll();
                        adapter.notifyDataSetChanged();
                        buttonSetup();
                    }
                    quitCounter = 0;
                }
                registerServerService();
                break;
        }
        return true;
    }

    private void createDialog() {
        Toast.makeText(this, "Quit Dialog", Toast.LENGTH_LONG).show();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Play Again?");
        alert.setCancelable(false);
        alert.setPositiveButton("Yeah Baby", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // if player says yes to play again
                // do whatever changes you need / change layout or whatever it is
                setContentView(R.layout.activity_tablet);
                startedGame = false;
                ListView userList = (ListView) findViewById(R.id.regList);
                userList.setAdapter(adapter);
                adapter.updateAll();
                adapter.notifyDataSetChanged();
            }
        });
        alert.setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // if player says no to play again
                // auto-generated method stub
                setContentView(R.layout.activity_tablet);
                ListView userList = (ListView) findViewById(R.id.regList);
                userList.setAdapter(adapter);
                adapter.updateAll();
                adapter.notifyDataSetChanged();
            }
        });

        alert.show();
    }

    public void buttonSetup() {
        Button playBtn = (Button)findViewById(R.id.playBtn);
        Button quitBtn = (Button)findViewById(R.id.quitBtn);

        quitBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitGame();
            }
        });

        playBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!startedGame) {
                    if (serviceRequest != null) {
                        manager.removeLocalService(channel, myService,
                                new WifiP2pManager.ActionListener() {

                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onFailure(int arg0) {
                                    }
                                });
                    }
                    registerServerService();
                }

                //Start Content Master thread.

                if(adapter.getSize() > 0 && !startedGame) {
                    rand = new Random();
                    nextCM = rand.nextInt(adapter.getSize());
                    Log.d(TabletActivity.TAG, "content master : " + adapter.getItemAddress(nextCM));
                    msgManager.write(("ContentMaster" + adapter.getItemAddress(nextCM)).getBytes());
                    startedGame = true;
                    setContentView(R.layout.activity_main_screen);
                    ListView userList = (ListView) findViewById(R.id.scoreboard);
                    userList.setAdapter(adapter);
                    receiver = new TabletBroadcastReceiver(manager, channel, thisContext);
                    registerReceiver(receiver, intentFilter);
                }
            }
        });

        manager.createGroup(channel,new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {

                Toast.makeText(TabletActivity.this, "Group Created",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }
}
