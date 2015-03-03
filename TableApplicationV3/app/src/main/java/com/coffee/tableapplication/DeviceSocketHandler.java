package com.coffee.tableapplication;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Sheng-Han on 2/28/2015.
 */
public class DeviceSocketHandler implements Runnable{
    public String deviceAddress = "";
    public String username = "";
    private Socket socket = null;
    private Handler handler = null;

    public DeviceSocketHandler(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    private InputStream iStream;
    private OutputStream oStream;

    @Override
    public void run() {
        try {
            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytes;

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
                        //If this is the login socket - we confirm login.
                        deviceAddress = readMessage.substring(readMessage.indexOf(", ") + 2);
                        username = readMessage.substring(readMessage.indexOf("User: ") + 6, readMessage.indexOf(","));
                        if(username.isEmpty()) {
                            username = deviceAddress;
                        }
                        handler.obtainMessage(TabletActivity.MANAGER_OPEN, this).sendToTarget();
                    }
                    else {
                        handler.obtainMessage(TabletActivity.MESSAGE_READ,
                                bytes, -1, buffer).sendToTarget();
                    }

                } catch (Exception e) {
                    Log.e(TabletActivity.TAG, "disconnected", e);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                Log.d(TabletActivity.TAG, "Sending message to remove device..." + deviceAddress);
                handler.obtainMessage(TabletActivity.MANAGER_CLOSE, this).sendToTarget();
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
            Log.d(TabletActivity.TAG, "Sending message to remove device..." + deviceAddress);
            //handler.obtainMessage(TabletActivity.MANAGER_CLOSE, deviceAddress).sendToTarget();
            Log.d(TabletActivity.TAG, "Closing socket...");
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
