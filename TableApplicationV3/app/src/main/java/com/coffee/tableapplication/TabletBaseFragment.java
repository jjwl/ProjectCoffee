package com.coffee.tableapplication;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Sheng-Han on 3/1/2015.
 */
public class TabletBaseFragment extends Fragment {

    View view; //The view.
    ArrayAdapter<User> listAdapter; //The array adapter; both fragmets use this
    Handler handler = null; //The handler callback function.

    public ArrayAdapter<User> getListAdapter() {
        return listAdapter;
    }

    /**
     * Refreshes the user list on the UI. Both fragments use this, so this is here.
     *
     * @param array The new list of users.
     */
    public void refreshList(ArrayList<User> array) {

            Log.d(TabletActivity.TAG, "Refreshing list...");
            listAdapter.clear();
            listAdapter.addAll(array);
            listAdapter.notifyDataSetChanged();

    }
}
