package com.example.d062589.buddy.Activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.d062589.buddy.Models.Drop;
import com.example.d062589.buddy.R;
import com.example.d062589.buddy.Utils.MyListener;
import com.example.d062589.buddy.Utils.RestClient;
import com.example.d062589.buddy.databinding.ActivityMainBinding;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Scanner;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;


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


    private static final String TAG = MainActivity.class.getSimpleName();

    // Layout Variables
    private Context context;
    ActivityMainBinding binding;
    MyInfoWindowAdapter infoViewAdapter;

    // Get density points scale
    float scale;

    // Animations
    private static int MARKER_WIDTH, MARKER_HEIGHT;

    // Network & JSON Variables
    private String sessionCookie;
    private String userId;

    // MediaPlayer & VideoView
    boolean isPLAYING = false;
    MediaPlayer mp;
    VideoView videoView;

    // FullscreenImgs & Videos + Container
    ImageView fullscreenImg;
    RelativeLayout fullscreenImgContainer;
    RelativeLayout fullscreenVideoContainer;

    // FAB Stuff
    private Boolean isFabOpen = false;
    private FloatingActionButton fab,fab1,fab2,fab3;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;
    private int PHOTO_REQUEST_CODE = 2000;
    private int VIDEO_REQUEST_CODE = 1000;

    // Taking Photos and shooting Videos
    private Uri mImageUri;



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
        MARKER_WIDTH = (int) (39 * scale + 0.5f);
        MARKER_HEIGHT = (int) (57 * scale + 0.5f);

        // Instantiate RestClient
        RestClient.getInstance(this);

        // FullscreenImgs & Videos + Container
        fullscreenImg = (ImageView) findViewById(R.id.fullscreen_image);
        fullscreenImgContainer = (RelativeLayout) findViewById(R.id.fullscreen_image_container);
        fullscreenVideoContainer = (RelativeLayout) findViewById(R.id.fullscreen_video_container);

        // FAB Stuff
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab1 = (FloatingActionButton)findViewById(R.id.fab1);
        fab2 = (FloatingActionButton)findViewById(R.id.fab2);
        fab3 = (FloatingActionButton)findViewById(R.id.fab3);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
    }


    /**
     * Open other
     * @param view
     */
    public void animateFAB(View view){
        if(isFabOpen){
            view.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab3.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            fab3.setClickable(false);
            isFabOpen = false;
        } else {
            view.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab3.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            fab3.setClickable(true);
            isFabOpen = true;
        }
    }

    /**
     * Open Camera with external storage
     * @param view
     */
    public void openPhotoCamera(View view) {
        Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = null;
        try
        {
            // place where to store camera taken picture
            photo = this.createTemporaryFile("picture", ".jpg");
            photo.delete();


            mImageUri = Uri.fromFile(photo);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            //start camera intent
            startActivityForResult(cameraIntent, PHOTO_REQUEST_CODE);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.v(TAG, "Can't create file to take picture!");
        }
    }

    /**
     * Create (image) File on external Storage
     * @param part file prefix
     * @param ext file extension
     * @return created file
     */
    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir = new File(getExternalCacheDir(), "photoCache");
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        return File.createTempFile(part, ext, tempDir);

    }



    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }


    public void openVideoCamera(View view) {
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(videoIntent, VIDEO_REQUEST_CODE);
    }

    public void openSoundRecorder(View view) {

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            // FAB animation & Onclick stuff
            animateFAB(fab);
            fab.setImageResource(R.drawable.send);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFAB(v);
                }
            });

            // Show the Image
            Picasso.with(context)
                    .load(mImageUri)
                    .rotate(0f)
                    .into(fullscreenImg);
            fullscreenImgContainer.setVisibility(View.VISIBLE);
        }
    }

    private void sendImg(View view) {
        // get imgdata
        // put in formdata request
        // start Progressdialog
        // close fullscreenImgContainer

        //POST AUF /BUDDY/DROPS
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
        infoViewAdapter = new MyInfoWindowAdapter();
        mMap.setInfoWindowAdapter(infoViewAdapter);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                infoOnClick(marker);
            }
        });


        // Get parties from JSON
        try {
            //get parties from local json
            String partyJson = loadJSON(R.raw.drops_overview);
            Drop[] drops = getDrops(partyJson);

            // Set markers for parties
            setDropLocations(drops);

            //login();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        updateDeviceLocation();
    }

    /**
     * Gets drops from input Json
     * @param json party object as json string
     * @return returns an ArrayList with all party objects within the json
     */
    private Drop[] getDrops(String json) {
        Gson gson = new Gson();
        Drop[] drops = gson.fromJson(json, Drop[].class);

        for (Drop d:drops) {
            Log.d("ACTIVE_DROPS", d.getComment());
        }
        return drops;
    }

    /**
     * Gets specific drop from input Json
     * @param json drop object as json string
     * @return returns the drop Object
     */
    private Drop getDrop(String json) {
        Gson gson = new Gson();
        Drop drop = gson.fromJson(json, Drop.class);

        return drop;
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
    private void setDropLocations(Drop[] drops) {
        for (Drop d:drops) {

            LatLng dropLocation = new LatLng(d.getLatitude(), d.getLongitude());


            int iconResource = setMarkerImg(d, false);

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(dropLocation)
                    .title(d.getComment())
                    .icon(BitmapDescriptorFactory
                            .fromBitmap(resizeMapIcons(iconResource,
                                    MARKER_WIDTH,
                                    MARKER_HEIGHT))));
            marker.setTag(d);
        }
    }

    private int setMarkerImg(Drop d, boolean active) {
        // Set Icon for Marker
        String activeString = "";

        if (active) {
            activeString = "_active";
        }

        if (!d.isHideable()) {
            switch (d.getDropType()) {
                case "Sound":
                    return context.getResources().getIdentifier("marker_sound"+activeString,
                            "drawable", context.getPackageName());
                case "Image":
                    return context.getResources().getIdentifier("marker_img"+activeString,
                            "drawable", context.getPackageName());
                case "Video":
                    return context.getResources().getIdentifier("marker_video"+activeString,
                            "drawable", context.getPackageName());
                default:
                    return context.getResources().getIdentifier("marker"+activeString,
                            "drawable", context.getPackageName());
            }
        } else {
            return context.getResources().getIdentifier("marker_hideable"+activeString,
                    "drawable", context.getPackageName());
        }

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


        // Apply binding to bottom sheet
        Drop drop = (Drop) marker.getTag();

        // Update marker image
        int iconResource = setMarkerImg(drop, true);
        marker.setIcon(BitmapDescriptorFactory
                .fromBitmap(resizeMapIcons(iconResource,
                        MARKER_WIDTH,
                        MARKER_HEIGHT)));

        binding.setDrop(drop);

        marker.showInfoWindow();


        //getDropDetails(drop.get_id());
        return true;
    }

    private void infoOnClick(Marker marker) {
        Drop drop = (Drop) marker.getTag();

        if (!drop.isHideable()) {
            switch (drop.getDropType()) {
                case "Sound":
                    playSound(drop);
                    break;
                case "Image":
                    Glide.with(context)
                            .load(drop.getContentUrl())
                            .placeholder(R.drawable.image_placeholder)
                            .crossFade()
                            .into(fullscreenImg);
                    fullscreenImgContainer.setVisibility(View.VISIBLE);
                    break;
                case "Video":
                    playVideo(drop);
                    break;
                default: break;
            }
        } else {
            // Do sth with the hideable
        }

    }


    /**
     * Play the audio file
     * @param d drop
     */
    private void playSound(Drop d) {
        if (!isPLAYING) {
            isPLAYING = true;
            mp = new MediaPlayer();
            try {
                mp.setDataSource(d.getContentUrl());
                mp.prepare();
                mp.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            isPLAYING = false;
            mp.release();
            mp = null;
        }


        Glide.with(context)
                .load(R.drawable.sound_stop)
                .placeholder(R.drawable.image_placeholder)
                .crossFade()
                .into(fullscreenImg);
        fullscreenImgContainer.setVisibility(View.VISIBLE);
    }



    private void playVideo(Drop d) {
        fullscreenVideoContainer.setVisibility(View.VISIBLE);

        videoView = (VideoView) findViewById(R.id.video_view);
        //Use a media controller so that you can scroll the video contents
        //and also to pause, start the video.
        //MediaController mediaController = new MediaController(this);
        //mediaController.setAnchorView(videoView);
        //videoView.setMediaController(mediaController);
        videoView.setVideoURI(Uri.parse(d.getContentUrl()));
        videoView.start();
    }


    public void hideImg(View view) {
        view.setVisibility(View.INVISIBLE);
        if (mp != null) {
            mp.release();
            mp = null;
        }

        // Hide when camera image is unsifficient
        if (!isFabOpen) {
            fab.setImageResource(R.drawable.plus);
        }
    }

    public void hideVideo(View view) {
        view.setVisibility(View.INVISIBLE);
        if (videoView != null) {
            videoView.stopPlayback();
            videoView = null;
        }
    }


    /**
     * Login on server and invoke getPartiesFromServer() on successful Login
     * @throws JSONException error while building the payload
     */
    /*
    public void sendData() throws JSONException {
        JSONObject payload = new JSONObject();

        // TODO: Delete hardcoded userdata and use the ones from the user
        payload.put("username", "michael@schlechtinger.de");
        payload.put("password", "ayylmao");

        String path = "/auth/login";


        @Multipart
        @POST("drops")
        Call<Response> updateUser(@Part MultipartBody.Part photo,
                @Part("string") RequestBody string);


    }*/


    private void getDropsFromServer() {
        RestClient.getInstance().get(userId, sessionCookie, "/drops", new MyListener<String>() {
            @Override
            public void getResult(String response) {
                if (response != null) {
                    // If there are no parties on the server, create new ones through post
                    Drop[] drops = getDrops(response);

                    // Set markers for parties
                    setDropLocations(drops);
                }
            }
        });
    }


    private void getDropDetails(String partyId) {
        String path = "/drops/" + partyId;
        RestClient.getInstance().get(userId, sessionCookie, path, new MyListener<String>() {
            @Override
            public void getResult(String response) {
                if (response != null) {

                    Drop drop = getDrop(response);
                    Log.d("TESTTEST", response);
                    binding.setDrop(drop);

                    // fill drop object with acquired information
                    // parse json in jsonobject
                    // drop == responseparty
                }
            }
        });
    }



    /**
     * InfoWindow Subclass
     */
    private class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View markerInfoView;
        Marker previousMarker;

        MyInfoWindowAdapter(){
            markerInfoView = getLayoutInflater().inflate(R.layout.marker_info_window, null);
        }


        /**
         * Show preview Info Window above marker
         * @param marker selected maps marker
         * @return infoWindow View
         */
        @Override
        public View getInfoContents(final Marker marker) {

            TextView title = ((TextView)markerInfoView.findViewById(R.id.comment));
            title.setText(marker.getTitle());

            // Get ModelData
            Drop drop = (Drop) marker.getTag();

            TextView creatorName = ((TextView) markerInfoView.findViewById(R.id.creator_name));
            creatorName.setText("by " + drop.getAuthor());

            ImageView preview = ((ImageView)markerInfoView.findViewById(R.id.preview));


            if (!drop.isHideable()) {
                switch (drop.getDropType()) {
                    case "Sound": loadPreviewImgLocal(preview, R.drawable.sound_preview); break;
                    case "Video": loadPreviewImg(marker, preview, drop.getPreviewImg(), drop); break;
                    case "Image": loadPreviewImg(marker, preview, drop.getPreviewImg(), drop); break;
                    default: break;
                }
            } else {
                loadPreviewImgLocal(preview, R.drawable.question);
                title.setText("come closer to see this content!");
                creatorName.setText("by ???");
            }


            return markerInfoView;
        }


        private void loadPreviewImg(Marker marker, ImageView target, String source, Drop d) {
            if (d.isInfoWindowOpened()) {
                Picasso.with(MainActivity.this).load(source).into(target);
            } else { // if it's the first time, load the image with the callback set
                d.setInfoWindowOpened(true);
                Picasso.with(MainActivity.this).load(source).into(target, new InfoWindowRefresher(marker));
            }
        }


        private void loadPreviewImgLocal(ImageView target, int source) {
            target.setImageResource(source);
        }



        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

    }

    /**
     * Callback for Picasso img loaded
     */
    private class InfoWindowRefresher implements Callback {
        private Marker markerToRefresh;

        private InfoWindowRefresher(Marker markerToRefresh) {
            this.markerToRefresh = markerToRefresh;
        }

        @Override
        public void onSuccess() {
            markerToRefresh.showInfoWindow();
        }

        @Override
        public void onError() {}
    }

}
