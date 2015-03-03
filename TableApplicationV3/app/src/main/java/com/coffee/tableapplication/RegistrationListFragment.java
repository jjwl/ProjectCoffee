package com.coffee.tableapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the
 * interface.
 */
public class RegistrationListFragment extends TabletBaseFragment {
    //MsgManager msgManager = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_list, container, false);
        Button playBtn = (Button)view.findViewById(R.id.playBtn);
        Button quitBtn = (Button)view.findViewById(R.id.quitBtn);

        playBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(handler != null) {
                    handler.obtainMessage(TabletActivity.GAME_START).sendToTarget();
                }
            }
        });

        quitBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(handler != null) {
                    handler.obtainMessage(TabletActivity.GAME_STOP).sendToTarget();
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listAdapter = new RegistrationListAdapter(this.getActivity(),
                android.R.layout.simple_list_item_2, android.R.id.text1,
                new ArrayList<User>());
        ListView scoreList = (ListView) view.findViewById(R.id.regList);
        scoreList.setAdapter(listAdapter);
        if(handler != null) {
            handler.obtainMessage(TabletActivity.GET_USERS).sendToTarget();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof TabletActivity) {
            handler =  ((TabletActivity) activity).getHandler();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        handler = null;
    }
}
