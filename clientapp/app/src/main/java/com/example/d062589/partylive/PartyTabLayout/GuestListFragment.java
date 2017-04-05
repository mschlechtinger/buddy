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

public class GuestListFragment extends Fragment {

    // newInstance constructor for creating fragment with arguments
    public static GuestListFragment newInstance() {
        GuestListFragment guestFragment = new GuestListFragment();
        return guestFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_guest, container, false);
        return view;
    }
}

