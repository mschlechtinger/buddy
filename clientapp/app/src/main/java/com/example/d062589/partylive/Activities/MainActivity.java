package com.example.d062589.partylive.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.d062589.partylive.Utils.MyListener;
import com.example.d062589.partylive.Utils.RestClient;
import com.example.d062589.partylive.Models.Party;
import com.example.d062589.partylive.PartyTabLayout.MyFragmentPagerAdapter;
import com.example.d062589.partylive.R;
import com.example.d062589.partylive.databinding.ActivityMainBinding;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by D062589 on 02.03.2017.
 */

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback,
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            GoogleMap.OnMarkerClickListener {


    //google maps variables
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;


    // The entry point to Google Play services, used by the Places API and Fused Location Provider.
    private GoogleApiClient mGoogleApiClient;

    // Default location & default zoom to use when location permission is not granted.
    private final LatLng mDefaultLocation = new LatLng(49.493060, 8.468930);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private final int mMaxEntries = 5;

    private static final String TAG = MainActivity.class.getSimpleName();
    GoogleMapOptions options = new GoogleMapOptions();

    // Layout & Bottom Sheet Variables
    private BottomSheetBehavior mBehavior;
    private View mBottomSheet;
    private Context context;
    private FloatingActionButton fab;
    ActivityMainBinding binding;
    RelativeLayout userIconsLayout;

    // Get density points scale
    float scale;

    // Animations
    private int fabImg;
    private static int MARKER_WIDTH;
    private static int MARKER_HEIGHT;
    private Marker activeMarker;

    // Network & JSON Variables
    private String sessionCookie;
    private String userId;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map & init Databinding.
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Build the Play services client for use by the Fused Location Provider and the Places API.
        // Use the addApi() method to request the Google Places API and the Fused Location Provider.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();

        // Obtain a reference to the Activity Context
        context = getApplicationContext();

        // Set Marker properties
        scale = context.getResources().getDisplayMetrics().density;
        MARKER_WIDTH = (int) (26 * scale + 0.5f);
        MARKER_HEIGHT = (int) (38 * scale + 0.5f);

        // Get Reference for Floating Action Button
        fab = (FloatingActionButton) findViewById(R.id.fab);

        // Create the bottom slide up window
        createBottomSheet();


        // Instantiate RestClient
        RestClient.getInstance(this);
    }



    /**
     * Hide Navbar and Statusbar for Fullscreen Map
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }


    /**
     * Retrieves and creates the bottom sheet for basic party information
     */
    private void createBottomSheet() {
        mBottomSheet = findViewById(R.id.layout_bottom_sheet);

        mBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        if (fabImg != R.drawable.plus) {
                            fabImg = R.drawable.plus;
                            fab.setImageResource(fabImg);
                        }
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.i("BottomSheetCallback", "slideOffset: " + slideOffset);
            }
        });

        mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);


        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        MyFragmentPagerAdapter pagerAdapter =
                new MyFragmentPagerAdapter(getSupportFragmentManager(), MainActivity.this);
        viewPager.setAdapter(pagerAdapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }


    /**
     * Builds the map when the Google Play services client is successfully connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Handles failure to connect to the Google Play services client.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the reference doc for ConnectionResult to see what error codes might
        // be returned in onConnectionFailed.
        Log.d(TAG, "Play services connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }


    /**
     * Handles suspension of the connection to the Google Play services client.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Play services connection suspended");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        try {
            // Customise the styling of the base map using a JSON
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // Get parties from JSON
        try {
            /* get parties from local json
            String partyJson = loadJSON(R.raw.parties_overview);
            ArrayList<Party> parties = getParties(partyJson);

            // Set markers for parties
            setPartyLocations(parties);*/

            login();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        updateDeviceLocation();
    }

    /**
     * Gets parties from input Json
     * @param json party object as json string
     * @return returns an ArrayList with all party objects within the json
     */
    private Party[] getParties(String json) {
        try {
            Gson gson = new Gson();
            Party[] parties = gson.fromJson(json, Party[].class);

            for (Party p:parties) {
                Log.d("ACTIVE_PARTIES", p.getTitle());
            }
            return parties;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets parties from input Json
     * @param json party object as json string
     * @return returns an ArrayList with all party objects within the json
     */
    private Party getParty(String json) {
        try {
            Gson gson = new Gson();
            Party party = gson.fromJson(json, Party.class);

            Log.d("TESTTESTTEST",party.getDescription());

            return party;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * load local JSON File from RAW and return it as a string value
     * @param file the JSON file as a value (e.g. R.raw.parties)
     * @return JSON as a String value
     */
    private String loadJSON(int file) {
        Resources res = getResources();
        InputStream is = res.openRawResource(file);
        Scanner scanner = new Scanner(is);
        StringBuilder builder = new StringBuilder();
        while(scanner.hasNextLine()) {
            builder.append(scanner.nextLine());
        }
        return builder.toString();
    }

    /**
     * set markers for parties
     */
    private void setPartyLocations(Party[] parties) {
        for (Party p:parties) {

            LatLng partyLocation = new LatLng(p.getLocation().getLatitude(), p.getLocation().getLongitude());

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(partyLocation)
                    .title(p.getTitle())
                    .icon(BitmapDescriptorFactory
                            .fromBitmap(resizeMapIcons(R.drawable.marker,
                                    MARKER_WIDTH,
                                    MARKER_HEIGHT))));
            marker.setTag(p);
        }
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


    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void updateDeviceLocation() {
        /*
         * Request location permission. handled by the callback onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        /*
         * Get location
         */
        if (mLocationPermissionGranted) {
            mLastKnownLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        }

        // Set the map's camera position to the current location of the device.
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastKnownLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        /*
         * Request location permission. Result is handled by the callback onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }
    }


    /**
     * Takes a drawable icon resource, converts it to a bitmap and resizes it
     * @param iconResource R.drawable resource
     * @param width the width of the outcoming bitmap
     * @param height the height of the outcoming bitmap
     * @return the resized bitmap
     */
    public Bitmap resizeMapIcons(int iconResource, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), iconResource);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    /**
     * Handles clicks on map marker
     * @param marker map marker
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {

        // Update camera position
        CameraUpdate markerLocation = CameraUpdateFactory.newLatLngZoom(
                marker.getPosition(),
                DEFAULT_ZOOM);
        mMap.animateCamera(markerLocation);

        // Store marker object in activeMarker for usage in other methods
        activeMarker = marker;

        // Update marker image
        marker.setIcon(BitmapDescriptorFactory
                .fromBitmap(resizeMapIcons(R.drawable.marker_active,
                        MARKER_WIDTH,
                        MARKER_HEIGHT)));

        // Switch fab icon
        if (fabImg != R.drawable.ic_navigation_black_24dp) {
            fabImg = R.drawable.ic_navigation_black_24dp;
            fab.setImageResource(fabImg);
            fab.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    navigateToParty(v);
                }
            });
        }


        // Apply binding to bottom sheet
        Party party = (Party) marker.getTag();



        binding.setParty(party);
        createPartyGuestList(party);


        // Show bottom sheet
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        getPartyDetails(party.get_id());
        return true;
    }


    /**
     * Login on server and invoke getPartiesFromServer() on successful Login
     * TODO: Put in Login view and just getInstance() of RestClient instead
     * @throws JSONException error while building the payload
     */
    public void login() throws JSONException {
        JSONObject payload = new JSONObject();

        // TODO: Delete hardcoded userdata and use the ones from the user
        payload.put("username", "michael@schlechtinger.de");
        payload.put("password", "ayylmao");

        String path = "/auth/login";


        RestClient.getInstance().post(payload, path, new MyListener<JSONObject>()
        {
            @Override
            public void getResult(JSONObject response) {
                if (response != null)
                {
                    // Get the session cookie & userId
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(response.toString());
                        userId = root.get("userId").toString();
                        sessionCookie = root.at("/headers").get("set-cookie").toString();

                        // Remove Quotes
                        userId = userId.substring(1, userId.length() - 1);
                        sessionCookie = sessionCookie.substring(1, sessionCookie.length() - 1);

                        getPartiesFromServer();

                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        });
    }


    private void getPartiesFromServer() {
        RestClient.getInstance().get(userId, sessionCookie, "/events", new MyListener<String>() {
            @Override
            public void getResult(String response) {
                if (response != null) {
                    // If there are no parties on the server, create new ones through post
                    Party[] parties = getParties(response);

                    // Set markers for parties
                    setPartyLocations(parties);
                }
            }
        });
    }


    private void getPartyDetails(String partyId) {
        String path = "/events/" + partyId;
        RestClient.getInstance().get(userId, sessionCookie, path, new MyListener<String>() {
            @Override
            public void getResult(String response) {
                if (response != null) {

                    Party party = getParty(response);
                    Log.d("TESTTEST", response);
                    binding.setParty(party);

                    // fill party object with acquired information
                    // parse json in jsonobject
                    // party == responseparty
                }
            }
        });
    }


    /**
     * Creates the preview for the party guests within the Bottom Sheet
     * @param party
     */
    private void createPartyGuestList(Party party) {
        // Get Partyguest images
        String[] guestImgs = party.getGuestImgs();
        // Get Viewgroup to bind the Images to
        userIconsLayout = (RelativeLayout) findViewById(R.id.guests_overview);


        // Values for User Layout
        int index = 0;
        int predecessorId = -1;
        int cardSize = 25;
        cardSize = (int) (cardSize * scale + 0.5f);
        int elevation = 5;
        elevation = (int) (elevation * scale + 0.5f);
        float cardCornerRadius = 12.5f;
        cardCornerRadius = (cardCornerRadius * scale + 0.5f);
        int cardMarginStart = -10;
        cardMarginStart = (int) (cardMarginStart * scale + 0.5f);
        int textMarginStart = 4;
        textMarginStart = (int) (textMarginStart * scale + 0.5f);


        // Create user icons
        for ( String img : guestImgs) {
            //create new cardview
            CardView card = new CardView(this);

            // Setup cardview alignment & styling
            card.setId(View.generateViewId());

            // Set Layout Params
            card.setElevation(elevation);
            card.setRadius(cardCornerRadius);

            RelativeLayout.LayoutParams cardParams = new RelativeLayout.LayoutParams(cardSize, cardSize);
            // If the current card is not the first card
            if (index > 0) {
                cardParams.addRule(RelativeLayout.END_OF, predecessorId);
                cardParams.setMarginStart(cardMarginStart);
            }

            // Causes layout update
            card.setLayoutParams(cardParams);

            predecessorId = card.getId();

            //create new imageview
            ImageView imgView = new ImageView(this);
            //String resource = "android.resource://com.example.d062589.partylive/drawable/" + img;
            Glide.with(imgView.getContext())
                    .load(img)
                    .thumbnail(Glide.with(imgView.getContext())
                            .load(R.drawable.loading_spinner)) //load gif as thumbnail
                    .placeholder(R.drawable.image_placeholder)
                    .crossFade()
                    .into(imgView);

            // Set Layout Params
            imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            RelativeLayout.LayoutParams imgParams = new RelativeLayout.LayoutParams(cardSize, cardSize);
            imgParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            imgParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            // Causes layout update
            imgView.setLayoutParams(imgParams);

            //adding view to layout
            card.addView(imgView);
            userIconsLayout.addView(card);

            index++;
        }

        // Add TextView for number of guestes
        TextView text = new TextView(this);

        int guestCount = party.getGuestCount();

        // Configure Text based on number of guests
        if (guestCount <= 4 && guestCount > 0) {

            int remainingGuests = guestCount - guestImgs.length;
            text.setText(R.string.some_participators);

        } else if (guestCount == 0) {
            text.setText(R.string.no_participators);

        } else if (guestCount > 4) {
            int remainingGuests = guestCount - guestImgs.length;
            text.setText("and " + remainingGuests + " others attending");

        }

        // Styling and positioning
        text.setTextColor(ContextCompat.getColor(context, R.color.colorText));
        text.setTextSize(getResources().getDimension(R.dimen.text_size_supertiny) /
                        getResources().getDisplayMetrics().density);
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        if (predecessorId != -1) {
            textParams.addRule(RelativeLayout.END_OF, predecessorId);
            textParams.setMarginStart(textMarginStart);
        }

        text.setLayoutParams(textParams);
        text.setGravity(Gravity.CENTER);

        // Add TextView to Layout
        userIconsLayout.addView(text);
    }

    /**
     * Hide bottom sheet when clicked outside
     * @param event used to get the tapevent (ACTION_DOWN)
     * @return
     */
    @Override public boolean dispatchTouchEvent(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED ||
                mBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {

                //get BottomSheet measures
                Rect outRect = new Rect();
                mBottomSheet.getGlobalVisibleRect(outRect);
                //get fab overlap
                int fabOverlap = fab.getHeight() / 2;


                // If touch is outside of BottomSheet
                if(!outRect.contains((int)event.getRawX(), (int)event.getRawY()+fabOverlap)) {
                    // Clean programmatically created user images
                    userIconsLayout.removeAllViews();


                    // Change icon of active marker
                    activeMarker.setIcon(BitmapDescriptorFactory
                            .fromBitmap(resizeMapIcons(R.drawable.marker,
                                    MARKER_WIDTH,
                                    MARKER_HEIGHT)));


                    // Update Icon and onClick of fab
                    fabImg = R.drawable.plus;
                    fab.setImageResource(fabImg);
                    fab.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            addParty(v);
                        }
                    });
                    mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        }

        return super.dispatchTouchEvent(event);
    }

    /**
     * add party on floatingActionButton click
     * @param view current view
     */
    public void addParty(View view) {
        Intent intent = new Intent(this, PartyCreatorActivity.class);
        Bundle extras = new Bundle();
        extras.putString("SESSION_COOKIE",sessionCookie);
        extras.putString("USER_ID",userId);
        extras.putParcelable("LOCATION",mLastKnownLocation);
        intent.putExtras(extras);
        startActivity(intent);
    }

    /**
     * google maps navigation
     * @param v view
     */
    private void navigateToParty(View v) {
        testToast("nav not implemented yet!");
    }
}
