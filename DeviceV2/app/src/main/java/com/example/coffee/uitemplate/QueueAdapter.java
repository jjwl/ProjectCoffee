package com.example.coffee.uitemplate;

import java.util.LinkedList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class QueueAdapter extends BaseAdapter{

    Context context;

    private LinkedList<String> contentQueue;
    LayoutInflater inflater;

    public QueueAdapter(Context context, LinkedList<String> contentQueue) {
        this.contentQueue = contentQueue;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }

    public int getCount() {
        return contentQueue.size();
    }

    public String getItem(int position) {
        return contentQueue.get(position);
    }

    public long getItemId(int position) {
        return contentQueue.get(position).getDrawableId();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.row, null);
        }

        String urlInQueue = contentQueue.get(position);
        if (urlInQueue != null) {
            TextView tt = (TextView) v.findViewById(R.id.toptext);
            TextView bt = (TextView) v.findViewById(R.id.bottomtext);
            if (tt != null) {
                tt.setText("Content Title: ");
            }
            if (bt != null) {
                bt.setText("Other relevant about content here.");
                //Will have a better idea of how to work with these after I put in the youtube stuff
            }
        }

        return convertView;
    }
}
