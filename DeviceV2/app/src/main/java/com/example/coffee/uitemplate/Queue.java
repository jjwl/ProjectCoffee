package com.example.coffee.uitemplate;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.LinkedList;


public class Queue extends Activity implements Handler.Callback {
    public static final String TAG = "tableActivity";

    private WifiP2pManager manager;

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;

    private Handler myHandler = new Handler(this);
    private MsgManager msgManager = null;

    public LinkedList<String> contentQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);

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
        createContentQueue();
    }

    public void createContentQueue() {
        contentQueue = new LinkedList<String>();
        showToast("Queue created.");
//>>>>>>> 6612411418db11df902bc1f3fca52dea2d33f19a
    }

    public void addToQueue(String url) {
        //should add the url to the queue
        contentQueue.add(url);
    }

    public void checkQueue() {
        //should return the head of the queue or null if the queue is empty
        contentQueue.peek();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_queue, menu);

        return true;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        //// Handle action bar item clicks here. The action bar will
        //// automatically handle clicks on the Home/Up button, so long
        //// as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        ////noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        //return super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.add:
                //Intended result to bring up a window that asks for a url and then adds url to the queue
                //(temporary, still figuring out embedded yt videos and/or youtube API)
                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                alert.setTitle("Add to Queue...");
                alert.setMessage("Enter URL to add to queue.");

                // Set an EditText view to get user input
                final EditText input = new EditText(this);
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newUrl = input.getText().toString();
                        addToQueue(newUrl);
                        showToast("Adding to queue.");
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                        showToast("Adding canceled.");
                    }
                });

                alert.show();

                return true;
            case R.id.view:
                //Intended result to display the current/top item of the queue using checkQueue()
                //For now, prints out the top of the queue
                String result = contentQueue.peek();
                if (result == null)
                    showToast("Queue is empty.");
                else {
                    System.out.println(contentQueue.peek());
                    showToast("Going to next in queue.");
                }
                return true;
            case R.id.next:
                //For now, returns a toast saying "Going to next in queue." Intended result to push
                //off the current/top item of the queue and go to the next item. Keeping this here
                //just in case skipping items on queue is desired.
                showToast("Going to next in queue.");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(Queue.this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

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
}
