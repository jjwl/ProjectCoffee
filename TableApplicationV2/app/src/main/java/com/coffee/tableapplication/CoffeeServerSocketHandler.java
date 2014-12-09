package com.coffee.tableapplication;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sheng-Han on 12/6/2014.
 */
public class CoffeeServerSocketHandler extends Thread {
    ServerSocket socket = null;
    private final int THREAD_COUNT = 10;
    private Handler handler;
    private static final String TAG = "CoffeeServerSocketHandler";

    public CoffeeServerSocketHandler(Handler handler) throws IOException {
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

    @Override
    public void run() {
        while (true) {
            try {
                // A blocking operation. Initiate a ChatManager instance when
                // there is a new connection
                pool.execute(new MsgManager(socket.accept(), handler));
                Log.d(TAG, "Launching the I/O handler");

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
}
