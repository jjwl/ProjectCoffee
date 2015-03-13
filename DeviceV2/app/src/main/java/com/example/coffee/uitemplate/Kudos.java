package com.example.coffee.uitemplate;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Timer;
import android.widget.ImageButton;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;


public class Kudos extends Activity  implements WifiP2pManager.ChannelListener, Handler.Callback, WifiP2pManager.ConnectionInfoListener{
    public static final String TAG = "tableActivity";

    private WifiP2pManager manager;

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;

    private Handler myHandler = new Handler(this);
    private MsgManager msgManager = null;
    private long currentTime = 0;
    private long pastTime = 0;
    private int kudosDelay = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kudos);

        // add necessary intent values to be matched.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        msgManager = MsgManager.getInstance();

        ImageButton kudosBtn = (ImageButton)findViewById(R.id.kudosBtn);
        kudosBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTime = System.currentTimeMillis();
                if(currentTime - pastTime >= (kudosDelay * 1000)) {
                    MsgManager.getInstance().write("Kudos".getBytes());
                    Toast.makeText(Kudos.this, "Kudos Sent!", Toast.LENGTH_SHORT).show();
                    pastTime = System.currentTimeMillis();
                }
            }
        });
        currentTime = System.currentTimeMillis();
        if(currentTime >= (pastTime + kudosDelay * 1000)) {
            MsgManager.getInstance().write("Kudos".getBytes());
            Toast.makeText(Kudos.this, "Kudos Sent!", Toast.LENGTH_SHORT).show();
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_kudos, menu);
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
        if (id == R.id.menu_now_playing) {
            // bring to kudos page or content master mode ?
            // need logic to determine who is who
            startActivity(new Intent(Kudos.this, Kudos.class));
            return true;
        }
        if (id == R.id.menu_queue) {
            // bring to queue page
            startActivity(new Intent(Kudos.this, Queue.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * handleMessage gets called whenever a meessage gets sent through the socket.
     * This handleMessage just sends the message to the msgManager
     * @param  msg The message sent through the socket
     * @return      void
     */
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MsgManager.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                msgManager.handleMsg(this, readMessage);
                Log.d(TAG, readMessage);
                break;

            case MsgManager.CONNECTION_SUCCESS:
                //Only when the entire thing has completed connection, go to welcome screen.

                break;
        }
        return true;
    }


    @Override
    public void onChannelDisconnected() {

    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {

    }
}
