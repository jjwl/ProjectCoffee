package com.example.coffee.uitemplate;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Class for creating and populating the list view
 */

public class QueueAdapter extends BaseAdapter{

    Context context;
    private List<Video> videoQueue;

    public QueueAdapter(Context context, List<Video> videos) {
        this.context = context;
        this.videoQueue = videos;
    }

    @Override
    public int getCount() {
        return this.videoQueue.size();
    }

    @Override
    public Object getItem(int position) {
        return this.videoQueue.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VideoView videoView = null;

        if (convertView == null) {
            videoView = new VideoView(this.context, this.videoQueue.get(position));
        }
        else {
            videoView = (VideoView) convertView;
        }

        videoView.setVideo(this.videoQueue.get(position));

        return videoView;
    }

    public void clearAdapter() {
        this.videoQueue.clear();
        notifyDataSetChanged();
    }
}
