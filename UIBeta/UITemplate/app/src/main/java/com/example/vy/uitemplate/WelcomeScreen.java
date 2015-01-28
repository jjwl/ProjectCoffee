package com.example.vy.uitemplate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.Queue;


public class WelcomeScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        Button kudosBtn = (Button)findViewById(R.id.goKudosBtn);
        kudosBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
             startActivity(new Intent(WelcomeScreen.this, NowPlayingKudos.class));
            }
        });

        Button queueBtn = (Button)findViewById(R.id.goQueueBtn);
        queueBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
             startActivity(new Intent(WelcomeScreen.this, Queue.class));
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome_screen, menu);
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
        return super.onOptionsItemSelected(item);
    }
}
