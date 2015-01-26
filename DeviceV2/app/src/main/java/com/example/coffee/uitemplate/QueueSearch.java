package com.example.coffee.uitemplate;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

public class QueueSearch extends Activity {

    public static final String TAG = "queueSearch";

    private EditText searchEditText;
    private ImageButton searchButton;
    private ListView searchResultsList;

    private QueueAdapter videoAdapter;
    private ArrayList<Video> videoQueue;

    private final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.youtube_search_screen);

        this.videoQueue = new ArrayList<Video>();
        this.videoAdapter = new QueueAdapter(this, videoQueue);

        this.searchButton = (ImageButton) findViewById(R.id.search_button);
        this.searchEditText = (EditText) findViewById(R.id.youtube_search_field);
        this.searchResultsList = (ListView) findViewById(R.id.query_results_list);
        this.searchResultsList.setAdapter(videoAdapter);

        initListeners();
    }

    private void initListeners() {
        this.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchEditText.getText().toString();
                if (query != null || query != "") {
                    videoQueue.clear();
                    videoAdapter.clearAdapter();
                    search(query);
                }

                // hide keyboard after pressing search button
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            }
        });

        this.searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = searchEditText.getText().toString();
                    if (query != null || query != "") {
                        videoQueue.clear();
                        videoAdapter.clearAdapter();
                        search(query);
                    }

                    // hide keyboard after pressing search button
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

                    return true;
                }
                return false;
            }
        });

        this.searchResultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String videoId = ((VideoView) view).getVideo().getVideoId();
                Video vid = ((VideoView) view).getVideo();

                Intent intent = new Intent(getApplicationContext(), YouTubePlayerDialogActivity.class);
                intent.putExtra("videoId", videoId);
                intent.putExtra("videoTitle", vid.getVideoTitle());
                intent.putExtra("channelTitle", vid.getVideoChannel());
                intent.putExtra("videoDescription", vid.getVideoDescription());
                intent.putExtra("thumbnailUrl", vid.getVideoThumbnailUrl());
                startActivity(intent);
            }
        });

        this.searchResultsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_choose_video);
                ImageButton dialogOkay = (ImageButton) dialog.findViewById(R.id.dialog_button_submit_okay);
                ImageButton dialogCancel = (ImageButton) dialog.findViewById(R.id.dialog_button_submit_cancel);

                dialogOkay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();

                        VideoView videoView = (VideoView) view;
                        Video video = videoView.getVideo();
                        JSONObject jsonVideo = new JSONObject();
                        try {
                            jsonVideo.put("action", "VIDEO");
                            jsonVideo.put("videoId", video.getVideoId());
                            jsonVideo.put("videoTitle", video.getVideoTitle());
                            jsonVideo.put("channelTitle", video.getVideoChannel());
                            jsonVideo.put("videoDescription", video.getVideoDescription());
                            jsonVideo.put("thumbnailUrl", video.getVideoThumbnailUrl());
                            jsonVideo.put("videoTimestamp", "0");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        String jsonifiedVideo = jsonVideo.toString();
                        Intent intent = new Intent(QueueSearch.this, Queue.class);
                        intent.putExtra("message", jsonifiedVideo);
                        startActivity(intent);
                    }
                });

                dialogCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("queue search", "Cancel clicked");
                        dialog.dismiss();
                    }
                });

                dialog.show();

                return true;
            }
        });
    }

    /**
     * Runs the request for the YouTube query in a separate thread.
     */
    private void search(final String query) {
        new AsyncTask<String, Void, Integer>() {
            String res;

            @Override
            protected Integer doInBackground(String... query) {
                int numVids = 0;

                this.res = parseSearchResults(getSearchResults(query[0]));

                return numVids;
            }

            @Override
            protected void onPostExecute(Integer result) {
                ArrayList<HashMap<String, String>> metadata = parseJsonResults(res);
                for (HashMap<String, String> video : metadata) {
                    Video vid = new Video(video.get("videoId"),
                            video.get("title"),
                            video.get("channel"),
                            video.get("description"),
                            video.get("thumbnailUrl"));
                    videoQueue.add(vid);
                }
                Log.d("queue search", "Adding videos to view");
                videoAdapter.notifyDataSetChanged();
            }
        }.execute(query);
    }

    /**
     * Queries YouTube's API endpoint by sending a GET request with the
     * appropriate parameters.
     *
     * @param query the search query (i.e. "cats")
     * @return the HttpResponse for the request
     */
    private HttpResponse getSearchResults(String query) {
        String devKey = DeveloperKey.DEVELOPER_KEY;

        HttpResponse response = null;
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            Uri uri = new Uri.Builder().scheme("https")
                    .authority("www.googleapis.com")
                    .path("youtube/v3/search")
                    .appendQueryParameter("part", "snippet")
                    .appendQueryParameter("maxResults", "20")
                    .appendQueryParameter("key", devKey)
                    .appendQueryParameter("q", query)
                    .build();

            request.setURI(new URI(uri.toString()));
            response = client.execute(request);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * Parses the HttpResponse from the GET request that is used to query
     * YouTube.
     *
     * @param response the HttpResponse from the request
     * @return a string of the raw JSON object
     */
    private String parseSearchResults(HttpResponse response) {
        String results = null;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));
            String line;
            StringBuffer buffer = new StringBuffer();

            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            in.close();
            results = buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Parses the JSON object that is returned from the YouTube query. Extracts
     * videoId, video title, channel name, thumbnail url, and description for
     * each result.
     *
     * @param results the stringified JSON object
     * @return an arraylist of hashmaps with metadata
     */
    private ArrayList<HashMap<String, String>> parseJsonResults(String results) {
        ArrayList<HashMap<String, String>> metadata = new ArrayList<HashMap<String, String>>();

        try {
            JSONObject jObject = new JSONObject(results);
            JSONArray jArray = jObject.getJSONArray("items");

            for (int i = 0; i < jArray.length(); i++) {
                HashMap<String, String> video = new HashMap<String, String>();

                // extract top level json objects
                JSONObject top = jArray.getJSONObject(i);
                JSONObject id = top.getJSONObject("id");
                JSONObject snippet = top.getJSONObject("snippet");
                JSONObject thumbnails = snippet.getJSONObject("thumbnails");
                JSONObject thumbnailHighRes = thumbnails.getJSONObject("high");

                // extract video fields and add to hash map
                String videoId = null;
                try {
                    // some objects don't have videoId since they are channel results
                    videoId = id.getString("videoId");
                } catch (JSONException e) {
                    continue;
                }
                video.put("videoId", videoId);
                video.put("title", snippet.getString("title"));
                video.put("description", snippet.getString("description"));
                video.put("channel", snippet.getString("channelTitle"));
                video.put("thumbnailUrl", thumbnailHighRes.getString("url"));

                metadata.add(video);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return metadata;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MsgManager.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, readMessage);
                break;

            case MsgManager.CONNECTION_SUCCESS:
                //Only when the entire thing has completed connection, go to welcome screen.

                break;
        }
        return true;
    }

}
