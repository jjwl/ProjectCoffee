package com.coffee.tableapplication;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
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
public class ScoreboardFragment extends TabletBaseFragment {
    ScoreboardListAdapter listAdapter;
    View view;

    //Sets the view.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_list, container, false);
        return view;
    }

    //Sets the adapter - show scoreboard list
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listAdapter = new ScoreboardListAdapter(this.getActivity(),
                R.layout.fragment_userscore_list, R.id.username,
                new ArrayList<User>());
        ((ListView)view.findViewById(R.id.scoreboard)).setAdapter(listAdapter);
    }
}
