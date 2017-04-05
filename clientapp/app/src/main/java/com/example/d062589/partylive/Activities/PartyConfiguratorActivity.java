package com.example.d062589.partylive.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.d062589.partylive.R;

/**
 * Created by D062589 on 15.03.2017.
 */

public class PartyConfiguratorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_configurator);

    }


    public void configurePartyDetails(View view) {
        Intent intent = new Intent(this, PartyConfiguratorActivity.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
