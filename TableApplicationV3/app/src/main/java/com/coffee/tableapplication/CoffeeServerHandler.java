package com.coffee.tableapplication;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sheng-Han on 2/28/2015.
 */
public class CoffeeServerHandler extends Thread{
    ServerSocket socket = null;
    private final int THREAD_COUNT = 10;
    private Handler handler;
    private HashMap<String, DeviceSocketHandler> listOfSockets = new HashMap<String, DeviceSocketHandler>();

    /**
     * Constructor that initializes the server socket to which all devices connect to.
     *
     * @param handler The callback activity handler.
     * @throws IOException
     */
    public CoffeeServerHandler(Handler handler) throws IOException {
        try {
            socket = new ServerSocket(4545);
            this.handler = handler;
            Log.d("CoffeeServerSocketHandler", "Socket Started");
        } catch (IOException e) {
            e.printStackTrace();
            pool.shutdownNow();
            throw e;
        }

    }

    /**
     * A ThreadPool for client sockets.
     */
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            THREAD_COUNT, THREAD_COUNT, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());


    /**
     * Accepts all the incoming device connections and initializes seperate threads to handle them.
     */
    @Override
    public void run() {
        while (true) {
            try {
                // A blocking operation. Initiate a ChatManager instance when
                // there is a new connection
                DeviceSocketHandler manager = new DeviceSocketHandler(socket.accept(), handler);
                pool.execute(manager);
                Log.d(TabletActivity.TAG, "Launching the I/O handler");

            } catch (IOException e) {
                try {
                    if (socket != null && !socket.isClosed())
                        socket.close();
                } catch (IOException ioe) {

                }
                e.printStackTrace();
                pool.shutdownNow();
                break;
            }
        }
    }


    /**
     * Writes to all sockets attached to the server.
     *
     * @param buffer The messages written to the socket
     * @return Success or failure
     */
    public boolean write(byte[] buffer) {
        try {
            ArrayList<DeviceSocketHandler> list = new ArrayList<DeviceSocketHandler>(listOfSockets.values());
            for(DeviceSocketHandler e : list) {
                e.write(buffer);
            }
        }
        catch (Exception e) {
            Log.e(TabletActivity.TAG, "Exception during write", e);
            return false;
        }
        return true;

    }

    /**
     * A new socket has connected successfully and sent registration data.
     *
     * @param name The address of the connected client.
     * @param manager The thread that is managing the socket.
     */
    public void addSocket(String name, DeviceSocketHandler manager) {
        listOfSockets.put(name, manager);
    }

    /**
     * Remove socket from connection and the server list
     *
     * @param name The name of the socket to be removed.
     */
    public void removeSocket(String name) {
        if(listOfSockets.containsKey(name)) {
            listOfSockets.get(name).close();
        }
        listOfSockets.remove(name);
    }
}
