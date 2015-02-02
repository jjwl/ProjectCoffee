package com.example.coffee.uitemplate;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Build;
import android.os.Bundle;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Sheng-Han on 12/22/2014.
 */
public class MsgManager implements Runnable {
    private static MsgManager msgManager = null;
    private static WifiP2pDevice device = null;
    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int CONNECTION_SUCCESS = 0x400 + 2;
    private Socket socket = null;

    private Handler handler = null; //Current Activity Handler; Changed each time the activity changes.
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
//    private Context context;
    private int mStartCount = 0;

    private MsgManager() {
    }

    public MsgManager init(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
        msgManager = this;

        return msgManager;
    }

    private InputStream iStream;
    private OutputStream oStream;
    private static final String TAG = "ChatHandler";

    public void updateHandler(Handler handle, WifiP2pManager  manager, WifiP2pManager.Channel channel) {

        this.handler = handle;
        this.manager = manager;
        this.channel = channel;
    }

    public static MsgManager getInstance() {
        if(msgManager == null) {
            return new MsgManager();
        }
        return msgManager;
    }

    @Override
    public void run() {
        try {
            msgManager = this;
            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytes;

            handler.obtainMessage(CONNECTION_SUCCESS)
                    .sendToTarget();

            while (true) {
                try {
                    // Read from the InputStream
                    bytes = iStream.read(buffer);
                    if (bytes == -1) {
                        break;
                    }

                    // Send the obtained bytes to the UI Activity
                    Log.d(TAG, "Rec:" + String.valueOf(buffer));
                    handler.obtainMessage(MESSAGE_READ,
                            bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(byte[] buffer) {
        try {
            oStream.write(buffer);
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

    //Process All Your Messages Here - I pass every Message_read handle_message to here.
    public boolean handleMsg(Activity current, String message) {
        Log.d(TAG, "Message:" + message);
        if(message.substring(0,13).equals("ContentMaster")){
            //Build.DEVICE or Build.MODEL
            Log.d("Ms", Build.MODEL + " " + message);
            if(device != null && message.substring(13).equals(device.deviceAddress)) {
            //CM Loop
                current.startActivity(new Intent(current, Queue.class));
            }else{
            //Kudos Loop
                current.startActivity(new Intent(current, Kudos.class));
            }
        }
        return true;
    }


    public void stop() {
            // Bring down connection
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
    }

    public void setDevice(WifiP2pDevice device) {
        this.device = device;
    }
    public String getDevice() {
        if(device != null) {
            return device.deviceAddress;
        }
        return "";
    }
}
