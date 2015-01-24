package com.example.coffee.uitemplate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerFragment;

public class YouTubePlayerDialogActivity extends YouTubeFailureRecoveryActivity {

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

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dialog_youtube_player);

        WindowManager.LayoutParams windowManager = getWindow().getAttributes();
        windowManager.dimAmount = (float) 0.0;
        windowManager.width = LayoutParams.MATCH_PARENT;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setAttributes(windowManager);

        this.addButton = (ImageButton) findViewById(R.id.button_airplane);
        this.backButton = (ImageButton) findViewById(R.id.button_back);
        initListeners();

        YouTubePlayerFragment youTubePlayerFragment =
                (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
        youTubePlayerFragment.initialize(DeveloperKey.DEVELOPER_KEY, this);
    }

    private void initListeners() {
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
                                /**
                                 * We want to send jsonVideo.toString() in a message here!!!
                                 */
                                break;
                            case R.id.control_item_submit_video_from_time:
                                int currentTimeInMillis = player.getCurrentTimeMillis();
                                int currentTimeInSec = currentTimeInMillis / 1000;

                                try {
                                    jsonVideo.put("videoTimestamp", currentTimeInSec);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                /**
                                 * We want to send jsonVideo.toString() in a message here!!!
                                 */
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
    }

    /**
     * Sending a message logic here!!!
     */

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
