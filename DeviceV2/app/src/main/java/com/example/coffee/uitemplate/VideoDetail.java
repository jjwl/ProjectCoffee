package com.example.coffee.uitemplate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerFragment;

public class VideoDetail extends YouTubeFailureRecoveryActivity {
    public static final String TAG = "VideoDetail";

    private String videoId;
    private String videoTitle;
    private String videoDescription;
    private String videoThumbnailUrl;
    private String videoChannelTitle;
    private int videoTimestamp;

    private ImageButton addButton;
    private ImageButton backButton;

    private Context context;
    private YouTubePlayer player;

    private MsgManager msgManager = null;

    private String jsonifiedVideo;
    public Intent intent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.videoId = (String) getIntent().getSerializableExtra("videoId");
        this.videoTitle = (String) getIntent().getSerializableExtra("videoTitle");
        this.videoDescription = (String) getIntent().getSerializableExtra("videoDescription");
        this.videoChannelTitle = (String) getIntent().getSerializableExtra("channelTitle");
        this.videoThumbnailUrl = (String) getIntent().getSerializableExtra("thumbnailUrl");
        try {
            this.videoTimestamp = Integer.parseInt((String) getIntent().getSerializableExtra("timestamp"));
        } catch (Exception e) {
            this.videoTimestamp = 0;
        }

        this.context = this;

        setContentView(R.layout.activity_video_detail);

        Bundle bundle = getIntent().getExtras();
        Boolean queueStart = bundle.getBoolean("queuestart");

        // handles back button
        ImageButton backBtn = (ImageButton)findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(VideoDetail.this, VideoSearch.class));
            }
        });

        // handles send/add button
        ImageButton sendBtn = (ImageButton)findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(VideoView.this, Queue.class));
                createDialog();
            }
        });

        //initListeners();

        YouTubePlayerFragment youTubePlayerFragment =
                (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
        youTubePlayerFragment.initialize(DeveloperKey.DEVELOPER_KEY, this);
    }

    private void createDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Add to Queue?");
        alert.setCancelable(false);
        final JSONObject jsonVideo = new JSONObject();
        try {
            jsonVideo.put("action", "VIDEO");
            jsonVideo.put("videoId", videoId);
            jsonVideo.put("videoTitle", videoTitle);
            jsonVideo.put("channelTitle", videoChannelTitle);
            jsonVideo.put("videoDescription", videoDescription);
            jsonVideo.put("thumbnailUrl", videoThumbnailUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // add to queue
                try {
                    jsonVideo.put("videoTimestamp", "0");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jsonifiedVideo = jsonVideo.toString();

                intent = new Intent(VideoDetail.this, Queue.class);
                intent.putExtra("message", jsonifiedVideo);
                startActivity(intent);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // auto-generated method stub
            }
        });
    }

    /*private void initListeners() {
        this.addButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, addButton);
                popup.getMenuInflater().inflate(R.menu.menu_controls, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        JSONObject jsonVideo = new JSONObject();
                        try {
                            jsonVideo.put("action", "VIDEO");
                            jsonVideo.put("videoId", videoId);
                            jsonVideo.put("videoTitle", videoTitle);
                            jsonVideo.put("channelTitle", videoChannelTitle);
                            jsonVideo.put("videoDescription", videoDescription);
                            jsonVideo.put("thumbnailUrl", videoThumbnailUrl);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        switch (item.getItemId()) {
                            case R.id.control_item_submit_video:
                                try {
                                    jsonVideo.put("videoTimestamp", "0");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                jsonifiedVideo = jsonVideo.toString();

                                intent = new Intent(VideoDetail.this, Queue.class);
                                intent.putExtra("message", jsonifiedVideo);
                                startActivity(intent);

                                break;
                            case R.id.control_item_submit_video_from_time:
                                int currentTimeInMillis = player.getCurrentTimeMillis();
                                int currentTimeInSec = currentTimeInMillis / 1000;

                                try {
                                    jsonVideo.put("videoTimestamp", currentTimeInSec);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                jsonifiedVideo = jsonVideo.toString();

                                intent = new Intent(VideoDetail.this, Queue.class);
                                intent.putExtra("message", jsonifiedVideo);
                                startActivity(intent);

                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });

                popup.show();
            }
        });

        this.backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_queue, menu);
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
            startActivity(new Intent(VideoDetail.this, Kudos.class));
            return true;
        }
        if (id == R.id.menu_queue) {
            // bring to queue page
            startActivity(new Intent(VideoDetail.this, Queue.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

        @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
        if (!wasRestored) {
            this.player = player;
            this.player.loadVideo(this.videoId, this.videoTimestamp * 1000);
        }
    }

    @Override
    protected Provider getYouTubePlayerProvider() {
        return (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
    }
}
