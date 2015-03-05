package com.example.coffee.uitemplate;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class WelcomeScreen extends Activity implements Handler.Callback {
    public static final String TAG = "tableActivity";

    private WifiP2pManager manager = null;

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel = null;
    private BroadcastReceiver receiver = null;

    private Handler myHandler = new Handler(this);
    private MsgManager msgManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        msgManager = MsgManager.getInstance();
        if(!msgManager.gameStarted) {
            startActivity(new Intent(WelcomeScreen.this, DeviceDiscoveryActivity.class));
        }

        Button queueBtn = (Button)findViewById(R.id.goQueueBtn);
        queueBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeScreen.this, Queue.class));
            }
        });
    }


    @Override
    protected void onStart() {
        MsgManager.getInstance().updateHandler(myHandler, manager, channel);
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        MsgManager.getInstance().stop();
        super.onDestroy();
    }

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome_screen, menu);
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
            case MsgManager.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, readMessage);
                msgManager.handleMsg(this, readMessage);
                break;

            case MsgManager.CONNECTION_SUCCESS:
                //Only when the entire thing has completed connection, go to welcome screen.

                break;
        }
        return true;
    }
}
