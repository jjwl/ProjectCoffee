package com.coffee.tableapplication;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
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

    View view;
    ArrayAdapter<User> listAdapter;
    Handler handler = null;
    //MsgManager msgManager = null;

    public ArrayAdapter<User> getListAdapter() {
        return listAdapter;
    }

    public void refreshList(ArrayList<User> array) {
        listAdapter.clear();
        listAdapter.addAll(array);
        listAdapter.notifyDataSetChanged();
    }
}
