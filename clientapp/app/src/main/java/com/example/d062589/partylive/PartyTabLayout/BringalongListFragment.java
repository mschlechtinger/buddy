package com.example.d062589.partylive.PartyTabLayout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.d062589.partylive.R;

/**
 * Created by D062589 on 21.03.2017.
 */

public class BringalongListFragment extends Fragment {

    // newInstance constructor for creating fragment with arguments
    public static BringalongListFragment newInstance() {
        BringalongListFragment bringalongFragment = new BringalongListFragment();
        return bringalongFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_bringalong, container, false);
        return view;
    }
}

