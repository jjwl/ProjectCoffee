package com.example.coffee.uitemplate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.LinkedList;


public class Queue extends Activity {

    public LinkedList<String> contentQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);

        createContentQueue();
    }

    public void createContentQueue() {
        contentQueue = new LinkedList<String>();
        showToast("Queue created.");
    }

    public void addToQueue(String url) {
        //should add the url to the queue
        contentQueue.add(url);
    }

    public void checkQueue() {
        //should return the head of the queue or null if the queue is empty
        contentQueue.peek();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_queue, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //// Handle action bar item clicks here. The action bar will
        //// automatically handle clicks on the Home/Up button, so long
        //// as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        ////noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        //return super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.add:
                //Intended result to bring up a window that asks for a url and then adds url to the queue
                //(temporary, still figuring out embedded yt videos and/or youtube API)
                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                alert.setTitle("Add to Queue...");
                alert.setMessage("Enter URL to add to queue.");

                // Set an EditText view to get user input
                final EditText input = new EditText(this);
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newUrl = input.getText().toString();
                        addToQueue(newUrl);
                        showToast("Adding to queue.");
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                        showToast("Adding canceled.");
                    }
                });

                alert.show();

                return true;
            case R.id.view:
                //Intended result to display the current/top item of the queue using checkQueue()
                //For now, prints out the top of the queue
                String result = contentQueue.peek();
                if (result == null)
                    showToast("Queue is empty.");
                else {
                    System.out.println(contentQueue.peek());
                    showToast("Going to next in queue.");
                }
                return true;
            case R.id.next:
                //For now, returns a toast saying "Going to next in queue." Intended result to push
                //off the current/top item of the queue and go to the next item. Keeping this here
                //just in case skipping items on queue is desired.
                showToast("Going to next in queue.");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(Queue.this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
