package com.example.coffee.uitemplate;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;


public class Queue extends Activity implements Handler.Callback, YouTubePlayer.PlayerStateChangeListener {
    public static final String TAG = "tableActivity";

    private WifiP2pManager manager;

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private Handler myHandler = new Handler(this);
    private MsgManager msgManager = null;

    private ListView videoList;
    private QueueAdapter videoAdapter;
    public LinkedList<Video> contentQueue;
    private String jsonifiedVideo;

    private int drawableId;

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
        this.videoAdapter = new QueueAdapter(this, contentQueue);
        this.videoList = (ListView) findViewById(R.id.queueList);
        this.videoList.setAdapter(videoAdapter);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            jsonifiedVideo = bundle.getString("message");
        }
        if (jsonifiedVideo != null) {
            receiveVideo(jsonifiedVideo);
        }

        Video video;
        video = new Video("e-ORhEE9VVg", "Taylor Swift - Blank Space", "TaylorSwiftVEVO", "description", "thumbnailUrl");
        contentQueue.add(video);

        initListeners();

        Button addBtn = (Button)findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Queue.this, VideoSearch.class));
            }
        });
    }

    public void createContentQueue() {
        contentQueue = new LinkedList<Video>();
        showToast("Queue created.");
    }

    private void initListeners() {
        this.videoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String videoId = ((VideoView) view).getVideo().getVideoId();
                Video vid = ((VideoView) view).getVideo();

                /*Intent intent = new Intent(getApplicationContext(), VideoDetail.class);
                intent.putExtra("videoId", videoId);
                intent.putExtra("videoTitle", vid.getVideoTitle());
                intent.putExtra("channelTitle", vid.getVideoChannel());
                intent.putExtra("videoDescription", vid.getVideoDescription());
                intent.putExtra("thumbnailUrl", vid.getVideoThumbnailUrl());
                intent.putExtra("timestamp", vid.getTimestamp());
                startActivity(intent);*/

                Intent intent = YouTubeStandalonePlayer.createVideoIntent(Queue.this, DeveloperKey.DEVELOPER_KEY, vid.getVideoId());
                startActivity(intent);
            }
        });
    }

    /*public int getDrawableId() {
        return drawableId;
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_queue, menu);

        return true;
    }

    @Override
    protected void onStart() {
        MsgManager.getInstance().updateHandler(myHandler, manager, channel);
        super.onStart();
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

    public void showToast(String message) {
        Toast toast = Toast.makeText(Queue.this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void receiveVideo(String videoInfo)
    {
        HashMap<String, String> metadata = parseJsonResults(videoInfo);
        Video video = new Video(metadata.get("videoId"),
                metadata.get("videoTitle"),
                metadata.get("channelTitle"),
                metadata.get("videoDescription"),
                metadata.get("thumbnailUrl"),
                metadata.get("videoTimestamp"),
                metadata.get("name"));

        contentQueue.add(video);
    }

    /**
     * Parses the JSON object that is returned
     *
     * @param results the stringified JSON object
     * @return hashmap with metadata
     */
    private HashMap<String, String> parseJsonResults(String results) {
        HashMap<String, String> video = new HashMap<String, String>();

        Log.d("jsonresults", results);

        try {
            JSONObject jObject = new JSONObject(results);
            video.put("videoId", jObject.getString("videoId"));
            video.put("videoTitle", jObject.getString("videoTitle"));
            video.put("videoDescription", jObject.getString("videoDescription"));
            video.put("channelTitle", jObject.getString("channelTitle"));
            video.put("videoTimestamp", jObject.getString("videoTimestamp"));
            video.put("thumbnailUrl", jObject.getString("thumbnailUrl"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return video;
    }

    @Override
    public void onLoaded(String arg0) {

    }

    @Override
    public void onLoading() {

    }

    @Override
    public void onVideoEnded() {
        //Emmett put your stuff here
        //Note: this will run after every video that ends
        MsgManager.getInstance().write("VideoFinished".getBytes());

        //Play next video on the queue if it is available
        contentQueue.remove();
        if (contentQueue.peek() != null) {
            Intent intent = YouTubeStandalonePlayer.createVideoIntent(Queue.this, DeveloperKey.DEVELOPER_KEY, contentQueue.peek().getVideoId());
            startActivity(intent);
        }
    }

    @Override
    public void onVideoStarted() {
        MsgManager.getInstance().write("VideoStarted".getBytes());
    }

    @Override
    public void onAdStarted() {
        
    }

    @Override
    public void onError(com.google.android.youtube.player.YouTubePlayer.ErrorReason arg0) {

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
