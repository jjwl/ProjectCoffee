package com.example.coffee.uitemplate;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VideoView extends LinearLayout {
    private Video video;

    private ImageView thumbnail;
    private TextView title;
    private TextView channel;
    private TextView description;
    private ImageButton videoOptionsButton;

    private Context context;

    public VideoView(Context context, Video video) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.video_search_result, this, true);

        this.context = context;

        this.video = video;
        this.thumbnail = (ImageView) findViewById(R.id.video_thumbnail);
        this.title = (ImageView) findViewById(R.id.video_title);
        this.channel = (TextView) findViewById(R.id.video_channel);
        this.description = (TextView) findViewById(R.id.video_description);
        this.videoOptionsButton = (ImageButton) findViewById(R.id.button_video_submission_options);

        this.setVideo(video);

        initListeners();
    }

    public void setVideo(Video vid) {
        this.video = vid;

        this.title.setText(video.getVideoTitle());
        this.channel.setText(video.getVideoChannel());
        this.description.setText(video.getVideoDescription());
    }

    public Video getVideo() {
        return this.video;
    }

    private void initListeners() {
        this.videoOptionsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(VideoView.class.toString(), "Clicked video options");
            }
        });
    }
}
