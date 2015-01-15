package com.example.vy.uitemplate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class NowPlayingCM extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cm);

        Button kudosBtn = (Button)findViewById(R.id.kudosBtn);
        kudosBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // insert code to implement Kudos here ?
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cm, menu);
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
            startActivity(new Intent(NowPlayingCM.this, NowPlayingCM.class));
            return true;
        }
        if (id == R.id.menu_queue) {
            // bring to queue page
            startActivity(new Intent(NowPlayingCM.this, Queue.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
