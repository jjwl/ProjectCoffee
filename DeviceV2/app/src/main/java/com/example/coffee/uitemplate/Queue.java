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
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;


public class Queue extends ActionBarActivity implements Handler.Callback, YouTubePlayer.PlayerStateChangeListener {
    public static final String APP_ID = "ABB60841";
    public static final String CCTAG = "Chromecast";
    public static final String TAG = "tableActivity";
    public static final int addVideo = 0;
    //public static final int playingVideo = 1;

    private WifiP2pManager manager;

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private Handler myHandler = new Handler(this);
    private MsgManager msgManager = null;

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private CastDevice mSelectedDevice;
    private GoogleApiClient mApiClient;
    private Cast.Listener mCastClientListener;
    private VideoChannel mVideoChannel;

    private boolean mWaitingForReconnect = false;
    private boolean mApplicationStarted = false;

    private ListView videoList;
    private QueueAdapter videoAdapter;
    public LinkedList<Video> contentQueue;
    private String jsonifiedVideo;
    public int index;

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

        initListeners();
        initMediaRouter();

        Button addBtn = (Button)findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Queue.this, VideoSearch.class);
                startActivityForResult(intent, addVideo);
            }
        });
    }

    private void initMediaRouter() {
        //Configure Cast device discovery
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(APP_ID))
                .build();
        mMediaRouterCallback = new MediaRouterCallback();
    }

    public void createContentQueue() {
        contentQueue = new LinkedList<Video>();
    }

    private void initListeners() {
        this.videoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String videoId = ((VideoView) view).getVideo().getVideoId();
                Video vid = ((VideoView) view).getVideo();
                index = position;

                /*Intent intent = new Intent(getApplicationContext(), VideoDetail.class);
                intent.putExtra("videoId", videoId);
                intent.putExtra("videoTitle", vid.getVideoTitle());
                intent.putExtra("channelTitle", vid.getVideoChannel());
                intent.putExtra("videoDescription", vid.getVideoDescription());
                intent.putExtra("thumbnailUrl", vid.getVideoThumbnailUrl());
                intent.putExtra("timestamp", vid.getTimestamp());
                startActivityForResult(intent, playingVideo);*/

                //Intent intent = YouTubeStandalonePlayer.createVideoIntent(Queue.this, DeveloperKey.DEVELOPER_KEY, vid.getVideoId());
                //startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == addVideo) {
                if (data.hasExtra("message")) {
                    String videoToAdd = data.getStringExtra("message");
                    Log.d("onActivityResultCheck", videoToAdd);
                    receiveVideo(videoToAdd);
                }
            }
            /*if (requestCode == playingVideo) {
                if (data.hasExtra("message")) {
                    //for now, do nothing
                    //if queue or something else needs to update after a video ends
                    //put that logic here
                    Boolean check = data.getBooleanExtra("updateQueue", false);
                    if (check && (index != contentQueue.size()-1)) {
                        index++;

                        Video vid = contentQueue.get(index);
                        Intent intent = new Intent(getApplicationContext(), VideoDetail.class);
                        intent.putExtra("videoId", vid.getVideoId());
                        intent.putExtra("videoTitle", vid.getVideoTitle());
                        intent.putExtra("channelTitle", vid.getVideoChannel());
                        intent.putExtra("videoDescription", vid.getVideoDescription());
                        intent.putExtra("thumbnailUrl", vid.getVideoThumbnailUrl());
                        intent.putExtra("timestamp", vid.getTimestamp());
                        startActivityForResult(intent, playingVideo);
                    }
                }
            }*/
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_queue, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider)
                MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String videos = "";
        for (Video video : this.contentQueue) {
            String vid = video.toJsonString();
            Log.d("onsavedinstancestate", vid);
            videos += vid;
        }

        Log.d("fullString", videos);
        outState.putString("videos", videos);
    }

    @Override
    protected void onStart() {
        MsgManager.getInstance().updateHandler(myHandler, manager, channel);
        super.onStart();
    }
    @Override
    protected void onDestroy() {
        MsgManager.getInstance().stop();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        MsgManager.getInstance().updateHandler(myHandler, manager, channel);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);

        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    @Override
    public void onPause() {
        if (isFinishing()) {
            // End media router discovery
            mMediaRouter.removeCallback( mMediaRouterCallback );
        }
        super.onPause();
        unregisterReceiver(receiver);
    }

    private class MediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo info) {
            if (mSelectedDevice == null
                    && info.supportsControlCategory(CastMediaControlIntent.categoryForCast(APP_ID))) {
                Queue.this.onRouteSelected(info);
            }
        }

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            Log.d(CCTAG, "onRouteSelected: " + info);
            Queue.this.onRouteSelected(info);

            initCastClientListener();

            mSelectedDevice = CastDevice.getFromBundle( info.getExtras() );

            launchReceiver();
        }

        @Override
        public void onRouteUnselected( MediaRouter router, MediaRouter.RouteInfo info ) {
            Log.d(CCTAG, "onRouteUnselected: " + info);

            teardown();
            mSelectedDevice = null;
        }
    }

    private void onRouteSelected(MediaRouter.RouteInfo info) {

        Log.d(CCTAG, "onRouteSelected: " + info.getName());

        mMediaRouter.selectRoute(info);
    }

    private void initCastClientListener() {
        mCastClientListener = new Cast.Listener() {
            @Override
            public void onApplicationStatusChanged() {
                if (mApiClient != null) {
                    Log.d(CCTAG, "onApplicationStatusChanged: "
                            + Cast.CastApi.getApplicationStatus(mApiClient));
                }
            }

            @Override
            public void onVolumeChanged() {
                if (mApiClient != null) {
                    Log.d(CCTAG, "onVolumeChanged: " + Cast.CastApi.getVolume(mApiClient));
                }
            }

            @Override
            public void onApplicationDisconnected( int statusCode ) {
                teardown();
            }
        };
    }

    private void launchReceiver() {
        Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                .builder( mSelectedDevice, mCastClientListener );

        ConnectionCallbacks mConnectionCallbacks = new ConnectionCallbacks();
        ConnectionFailedListener mConnectionFailedListener = new ConnectionFailedListener();
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Cast.API, apiOptionsBuilder.build() )
                .addConnectionCallbacks( mConnectionCallbacks )
                .addOnConnectionFailedListener( mConnectionFailedListener )
                .build();

        mApiClient.connect();
    }

    private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected( Bundle hint ) {
            if( mWaitingForReconnect ) {
                mWaitingForReconnect = false;
                reconnectChannels( hint );
            } else if (mApiClient != null && mApplicationStarted) {
                Cast.CastApi.joinApplication(mApiClient, APP_ID)
                        .setResultCallback(new ResultCallback<Cast.ApplicationConnectionResult>() {
                            @Override
                            public void onResult(Cast.ApplicationConnectionResult applicationConnectionResult) {
                                Status status = applicationConnectionResult.getStatus();
                                if (status.isSuccess()) {
                                    mVideoChannel = new VideoChannel();
                                    try {
                                        Cast.CastApi.setMessageReceivedCallbacks(mApiClient,
                                                mVideoChannel.getNamespace(),
                                                mVideoChannel);
                                    } catch (IOException e) {
                                        Log.e(CCTAG, "Exception while creating channel", e);
                                    }

                                }
                            }
                        });
            }
            else {
                try {
                    Cast.CastApi.launchApplication( mApiClient, APP_ID, false )
                            .setResultCallback(
                                    new ResultCallback<Cast.ApplicationConnectionResult>() {
                                        @Override
                                        public void onResult(
                                                Cast.ApplicationConnectionResult applicationConnectionResult) {
                                            Status status = applicationConnectionResult.getStatus();
                                            if( status.isSuccess() ) {
                                                //Values that can be useful for storing/logic
                                                ApplicationMetadata applicationMetadata =
                                                        applicationConnectionResult.getApplicationMetadata();
                                                String sessionId =
                                                        applicationConnectionResult.getSessionId();
                                                String applicationStatus =
                                                        applicationConnectionResult.getApplicationStatus();
                                                boolean wasLaunched =
                                                        applicationConnectionResult.getWasLaunched();

                                                mApplicationStarted = true;
                                                reconnectChannels( null );

                                                mVideoChannel = new VideoChannel();
                                                try {
                                                    Cast.CastApi.setMessageReceivedCallbacks(mApiClient,
                                                            mVideoChannel.getNamespace(),
                                                            mVideoChannel);
                                                } catch (IOException e) {
                                                    Log.e(CCTAG, "Exception while creating channel", e);
                                                }
                                            } else {
                                                teardown();
                                            }
                                        }
                                    }
                            );
                } catch ( Exception e ) {
                    Log.e(CCTAG, "Failed to launch application", e);
                }
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            mWaitingForReconnect = true;
        }
    }

    class VideoChannel implements Cast.MessageReceivedCallback {
        public String getNamespace() {
            return "urn:x-cast:com.coffee.vchannel";
        }

        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
            //logic for dealing with received messages here
            Log.d(CCTAG, "onMessageReceived: " + message);
            onVideoEnded();
        }
    }

    private void sendMessage(String message) {
        if (mApiClient != null && mVideoChannel != null) {
            try {
                Cast.CastApi.sendMessage(mApiClient, mVideoChannel.getNamespace(), message)
                        .setResultCallback(
                                new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(Status status) {
                                        if (!status.isSuccess()) {
                                            Log.e(CCTAG, "Sending message failed");
                                        }
                                    }
                                }
                        );
            } catch (Exception e) {
                Log.e(CCTAG, "Exception while sending message", e);
            }
        }
    }

    private void reconnectChannels( Bundle hint ) {
        if( ( hint != null ) && hint.getBoolean( Cast.EXTRA_APP_NO_LONGER_RUNNING ) ) {
            //Log.e( TAG, "App is no longer running" );
            teardown();
        } else {
            try {
                Cast.CastApi.setMessageReceivedCallbacks( mApiClient, mVideoChannel.getNamespace(), mVideoChannel);
            } catch( IOException e ) {
                //Log.e( TAG, "Exception while creating media channel ", e );
            } catch( NullPointerException e ) {
                //Log.e( TAG, "Something wasn't reinitialized for reconnectChannels" );
            }
        }
    }

    private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed( ConnectionResult connectionResult ) {
            teardown();
        }
    }

    private void teardown() {
        if( mApiClient != null ) {
            if( mApplicationStarted ) {
                try {
                    Cast.CastApi.stopApplication( mApiClient );
                    if( mVideoChannel != null ) {
                        Cast.CastApi.removeMessageReceivedCallbacks( mApiClient, mVideoChannel.getNamespace());
                        mVideoChannel = null;
                    }
                } catch( IOException e ) {
                    //Log.e( TAG, "Exception while removing application " + e );
                }
                mApplicationStarted = false;
            }
            if( mApiClient.isConnected() )
                mApiClient.disconnect();
            mApiClient = null;
        }
        mSelectedDevice = null;
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
        sendMessage(video.getVideoId());
        Log.d("receiveVideo", "video added to queue");
        videoAdapter.notifyDataSetChanged();
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
