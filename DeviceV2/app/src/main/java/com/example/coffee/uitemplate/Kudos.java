package com.example.coffee.uitemplate;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;


public class Kudos extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kudos);

        Button kudosBtn = (Button)findViewById(R.id.kudosBtn);
        kudosBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // insert code to implement Kudos here
                Socket socket = new Socket();
                OutputStreamWriter osw;
                String sendKudos = "Sending Kudos";

                OutputStream outputStream = null;
                InputStream inputStream = null;
                try {
                    /**
                     * Create a client socket with the host,
                     * port, and timeout information
                     */
                    socket.bind(null);
                    socket.connect((new InetSocketAddress(host, 4545)), 500);
                    //Note: "host" -- no idea how to access IP address, should have asked before
                    //May also need to move this to an AsyncTask outside of onCreate
                    //Got most of this from the WifiP2PDemo

                    outputStream = socket.getOutputStream();
                    inputStream = socket.getInputStream();
                    osw = new OutputStreamWriter(outputStream, "UTF-8");

                    //Send string of "Sending Kudos", fulfilling the "Kudos" needed to read a Kudos being sent
                    //May need to use DataInputStream readUTF() method on the receiving side
                    osw.write(sendKudos, 0, sendKudos.length());
                    osw.flush();
                    //outputStream.close();
                    //inputStream.close();
                } catch (IOException e) {
                    //catch logic
                    //e.printStackTrace();
                }
                /**
                 * Clean up any open sockets or streams when done
                 * transferring or if an exception occurred.
                 */
                finally {
                    if (socket != null) {
                        if (socket.isConnected()) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                //e.printStackTrace();
                            }
                        }
                    }

                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            //e.printStackTrace();
                        }
                    }

                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            //e.printStackTrace();
                        }
                    }
                }

                Toast.makeText(Kudos.this, "Kudos Sent!", Toast.LENGTH_SHORT).show();
            }
        });
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

        return super.onOptionsItemSelected(item);
    }

}
