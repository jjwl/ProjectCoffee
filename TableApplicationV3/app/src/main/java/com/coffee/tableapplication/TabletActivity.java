package com.coffee.tableapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.apache.commons.lang.time.StopWatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class TabletActivity extends Activity implements Handler.Callback, WifiP2pManager.ConnectionInfoListener {
    public static final String TAG = "ProjectCoffeeServerApp";
    //WifiP2p managing
    public static final String TABLE_SERVICE = "_coffee_server_service";
    public static final String DEVICE_SERVICE = "_coffee_client_service";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    private WifiP2pManager manager;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private BroadcastManager broadcastRepeater = null;
    private WifiP2pDnsSdServiceInfo myService;

    //Message passing to activity.
    private Handler handler = new Handler(this);
    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int GAME_START = 0x400 + 2;
    public static final int GAME_STOP = 0x400 + 3;
    public static final int MANAGER_OPEN = 0x400 + 4;
    public static final int MANAGER_CLOSE = 0x400 + 5;

    //User managing
    private CoffeeServerHandler msgManager;
    private UserData userlist = new UserData();
    private HashMap<String, User> playerList = new HashMap<String, User>();
    private ArrayList<User> roundList = new ArrayList<User>();
    private RegistrationListFragment registrationList;
    private ScoreboardFragment scoreboardFragment;
    private static String pageName = "gamepage";

    //Game management
    private StopWatch stopWatch = null;
    private StopWatch gameLoopWatch = null;

    private boolean startedGame = false;
    private int kudosLimitTime = 10; //time between sending kudos in seconds
    private int roundTime = 2; //time in between rounds in minutes
    private int nextCM = 0;
    private int quitCounter = 0;
    private Random rand = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tablet);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        registrationList = new RegistrationListFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.container_root, registrationList, pageName).commit();

        registerServerService();
        broadcastRepeater = new BroadcastManager(this);
        broadcastRepeater.start();
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
                    if(userlist.quit()){
                        getFragmentManager().beginTransaction()
                                .replace(R.id.container_root, registrationList, pageName).commit();
                    }
                }

                if(readMessage.contains("VideoFinished")) {
                    //Check current time, compare with start time, if > 2 min,
                    // send cm packet and update start time. Otherwise ignore.
                    if(gameLoopWatch.getTime() > 1000 * roundTime * 60){
                        Log.d(TAG, "Updating CM");
                        String contentMaster = userlist.nextContentMaster();
                        Log.d(TabletActivity.TAG, "Content master : " + contentMaster);
                        msgManager.write(("ContentMaster" + roundList.get(nextCM).device.deviceAddress).getBytes());
                        ((TabletBaseFragment) getFragmentManager().findFragmentByTag(pageName))
                                .refreshList(userlist.getUserList());
                    }

                }

                if(readMessage.contains("Kudos") && stopWatch.getTime() > kudosLimitTime * 1000) {
                    ScoreboardListAdapter adapter = (ScoreboardListAdapter) scoreboardFragment.listAdapter;
                    stopWatch.stop();
                    stopWatch.reset();
                    stopWatch.start();
                    Toast.makeText(this, readMessage, Toast.LENGTH_LONG).show();
                    userlist.addKudos();
                    ((TabletBaseFragment) getFragmentManager().findFragmentByTag(pageName))
                            .refreshList(userlist.getUserList());
                }
                break;
            case GAME_START:
                startedGame = true;
                scoreboardFragment = new ScoreboardFragment();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container_root, scoreboardFragment).commit();
                userlist.resetList();
                ((TabletBaseFragment) getFragmentManager().findFragmentByTag(pageName))
                        .refreshList(userlist.getUserList());
                break;
            case GAME_STOP:
                quitGame();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container_root, registrationList, pageName).commit();
                userlist.resetList();
                ((TabletBaseFragment) getFragmentManager().findFragmentByTag(pageName))
                        .refreshList(userlist.getUserList());
                break;
            case MANAGER_OPEN:
                DeviceSocketHandler deviceSocket = (DeviceSocketHandler) msg.obj;
                String address = deviceSocket.deviceAddress;
                String username = deviceSocket.username;
                msgManager.addSocket(username, deviceSocket);
                userlist.addUser(address, username);
                ((TabletBaseFragment) getFragmentManager().findFragmentByTag(pageName))
                        .refreshList(userlist.getUserList());
                Log.d(TAG, username + ": " + address);
                break;
            case MANAGER_CLOSE:
                deviceSocket = (DeviceSocketHandler) msg.obj;
                address = deviceSocket.deviceAddress;
                msgManager.removeSocket(address);
                userlist.offlineUser(address);

                if(startedGame == false) {
                    userlist.removeUser(address);
                }
                else {
                    if(userlist.players() > 1 && address.equals(userlist.currentContentMaster())) {
                        Log.d(TAG, "Updating CM");
                        String contentMaster = userlist.nextContentMaster();
                        msgManager.write(("ContentMaster" + contentMaster).getBytes());
                        Log.d(TabletActivity.TAG, "Content master : " + contentMaster);
                    }
                }
                ((TabletBaseFragment) getFragmentManager().findFragmentByTag(pageName))
                        .refreshList(userlist.getUserList());
                break;
        }
        return false;
    }

    public Handler getHandler() {
        return this.handler;
    }

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
        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //appendStatus("Service discovery initiated");
                Log.d(TabletActivity.TAG, "Service discovery initiated.");
            }

            @Override
            public void onFailure(int arg0) {
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
                msgManager  = new CoffeeServerHandler(this.handler);
                handler = msgManager;
                handler.start();
            } catch (IOException e) {
                Log.d(TAG,
                        "Failed to create a new server thread - " + e.getMessage());
                return;
            }
        }
    }

    public void updateList(WifiP2pDeviceList deviceList) {
        ArrayList<User> users = userlist.getUserList();
        ArrayList<WifiP2pDevice> devices = new ArrayList<WifiP2pDevice>(deviceList.getDeviceList());
        for(User person : users) {
            boolean offline = true;
            for(WifiP2pDevice device : devices) {
                if(person.equals(device)){
                    offline = false;
                }
            }
            if(offline){
                //Remove socket here.
                msgManager.removeSocket(person.device.deviceAddress);
            }
        }
    }

    public void quitGame(){
        msgManager.write(("Quit" + userlist.getWinners()).getBytes());
        startedGame = false;
        //Calculate scores and display them
    }
}