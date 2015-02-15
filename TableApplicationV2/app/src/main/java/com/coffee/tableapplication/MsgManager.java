package com.coffee.tableapplication;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Sheng-Han on 12/6/2014.
 */
public class MsgManager implements Runnable {
    private CoffeeServerSocketHandler serverManager = null;
    private String deviceAddress = "";
    private String socketAddress = "";
    private Socket socket = null;
    private Handler handler;

    public MsgManager(Socket socket, Handler handler, CoffeeServerSocketHandler server) {
        this.socket = socket;
        this.handler = handler;
        this.serverManager = server;
        socketAddress = socket.getRemoteSocketAddress().toString();
    }

    private InputStream iStream;
    private OutputStream oStream;
    private static final String TAG = "MsgHandler";

    @Override
    public void run() {
        try {
            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytes;
            handler.obtainMessage(TabletActivity.MY_HANDLE, this)
                    .sendToTarget();

            while (true) {
                try {
                    // Read from the InputStream
                    bytes = iStream.read(buffer);
                    if (bytes == -1) {
                        break;
                    }

                    // Send the obtained bytes to the UI Activity
                    String readMessage = new String(buffer, 0, bytes);
                    if(readMessage.contains("User:")) {
                       deviceAddress = readMessage.substring(readMessage.indexOf(", ") + 2);
                       serverManager.addSocket(deviceAddress, this);
                    }
                    handler.obtainMessage(TabletActivity.MESSAGE_READ,
                            bytes, -1, buffer).sendToTarget();

                } catch (Exception e) {
                    Log.e(TAG, "disconnected", e);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Log.d(TAG, "Sending message to remove device..." + deviceAddress);
                handler.obtainMessage(TabletActivity.MANAGER_CLOSE, deviceAddress).sendToTarget();
                Log.d(TAG, "Removing from device list...");
                serverManager.removeSocket(deviceAddress);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(byte[] buffer) throws IOException {
            oStream.write(buffer);
    }

    public void close() {
        try {
            Log.d(TAG, "Sending message to remove device..." + deviceAddress);
            //handler.obtainMessage(TabletActivity.MANAGER_CLOSE, deviceAddress).sendToTarget();
            Log.d(TAG, "Closing socket...");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
