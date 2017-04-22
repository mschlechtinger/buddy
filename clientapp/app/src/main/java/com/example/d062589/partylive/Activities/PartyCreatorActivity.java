package com.example.d062589.partylive.Activities;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.d062589.partylive.Utils.MyListener;
import com.example.d062589.partylive.Utils.RestClient;
import com.example.d062589.partylive.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by D062589 on 15.03.2017.
 */

public class PartyCreatorActivity extends AppCompatActivity {

    private String sessionCookie;
    private String userId;
    private Location mLastKnownLocation;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_creator);

        context = getApplicationContext();

        // Get Intent information from Map Screen
        Bundle extras = getIntent().getExtras();
        sessionCookie = extras.getString("SESSION_COOKIE");
        userId = extras.getString("USER_ID");
        mLastKnownLocation = extras.getParcelable("LOCATION");
    }


    public void configurePartyDetails(View view) {
        Intent intent = new Intent(this, PartyConfiguratorActivity.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }


    /**
     * Just for testing reasons to create parties if there are none
     * TODO: DELETE WHEN GOING PRODUCTIVE
     */
    public void createSamplePartyHere(View view) throws JSONException {

        JSONObject payload = new JSONObject();

        payload.put("title", "Sample Party");
        payload.put("description", "so very samply");
        payload.put("imgUrl", "http://2static.fjcdn.com/large/pictures/3a/34/3a3451_5582801.jpg");

        // Location
        JSONObject location = new JSONObject();
        location.put("latitude", mLastKnownLocation.getLatitude());
        location.put("longitude", mLastKnownLocation.getLongitude());
        payload.put("location", location);

        payload.put("public", false);

        // Guests
        JSONArray guests = new JSONArray();
        guests.put("58ca60f96c3e73484a856531");
        guests.put("58d2a938edc99b1dcc648206");
        payload.put("guests", guests);

        // StartDate
        SimpleDateFormat startDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.ms'Z'"); //2017-03-31T10:50:42.389Z
        Date date = new Date();
        System.out.println(startDate.format(date));
        payload.put("startDate", startDate.format(date));

        // BringItems
        JSONArray bringItems = new JSONArray();
        // BringItem
        JSONObject bringItem = new JSONObject();
        bringItem.put("title", "Stuff");
        bringItem.put("quantity", 5);
        // Assignees
        JSONArray assignees = new JSONArray();
        assignees.put("58d2a938edc99b1dcc648206");
        bringItem.put("assignees", assignees);
        bringItems.put(bringItem);
        payload.put("bringItems", bringItems);

        RestClient.getInstance().post(userId, sessionCookie, payload, "/events", new MyListener<JSONObject>()
        {
            @Override
            public void getResult(JSONObject response) {
                if (response != null)
                {
                    testToast("Sample Party created at your position!");
                }
            }

        });
    }


    /**
     * used for testing instead of console statements
     * TODO: delete when not required anymore
     * @param text characters to be shown in toast
     */
    private void testToast(CharSequence text) {
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

}
