package com.example.coffee.uitemplate;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Container class for data associated with YouTube videos
 */

public class Video {

    private String videoId;
    private String videoTitle;
    private String videoChannel;
    private String videoDescription;
    private String videoThumbnailUrl;
    private String videoTimestamp;
    private String playerName;

    public Video(String id, String title, String channel, String description, String thumbnailUrl) {
        this.videoId = id;
        this.videoTitle = title;
        this.videoChannel = channel;
        this.videoDescription = description;
        this.videoThumbnailUrl = thumbnailUrl;
    }

    public Video(String id, String title, String channel, String description, String thumbnailUrl, String timestamp, String name) {
        this.videoId = id;
        this.videoTitle = title;
        this.videoChannel = channel;
        this.videoDescription = description;
        this.videoThumbnailUrl = thumbnailUrl;
        this.videoTimestamp = timestamp;
        this.setPlayerName(name);

    }

    public String getTimestamp(){
        return this.videoTimestamp;
    }

    public void setTimestamp(String newTimestamp){
        this.videoTimestamp = newTimestamp;
    }

    public String getVideoId() {
        return this.videoId;
    }

    public String getVideoTitle() {
        return this.videoTitle;
    }

    public String getVideoChannel() {
        return this.videoChannel;
    }

    public String getVideoDescription() {
        return this.videoDescription;
    }

    public String getVideoThumbnailUrl() {
        return this.videoThumbnailUrl;
    }

    public void setPlayerName(String playerName)
    {
        this.playerName = playerName;
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public String toJsonString() {
        JSONObject jObjectData = new JSONObject();
        try
        {
            jObjectData.put("videoTitle", this.videoTitle);
            jObjectData.put("videoId", this.videoId);
            jObjectData.put("channelTitle", this.videoChannel);
            jObjectData.put("videoDescription", this.videoDescription);
            jObjectData.put("thumbnailUrl", this.videoThumbnailUrl);
            jObjectData.put("videoTimestamp", this.videoTimestamp);
            jObjectData.put("name", this.playerName);
        } catch(JSONException e)
        {
            e.printStackTrace();
        }

        return jObjectData.toString();
    }
}