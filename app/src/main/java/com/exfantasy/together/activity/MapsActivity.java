package com.exfantasy.together.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.exfantasy.together.R;
import com.exfantasy.together.cnst.SharedPreferencesKey;
import com.exfantasy.together.components.adapter.MarkerInfoWindowAdapter;
import com.exfantasy.together.components.floatingActionButton.FloatingActionButton;
import com.exfantasy.together.components.recyclerview.ItemData;
import com.exfantasy.together.components.recyclerview.MyAdapter;
import com.exfantasy.together.components.recyclerview.SnappingRecyclerView;
import com.exfantasy.together.event.CreateEventDialog;
import com.exfantasy.together.event.MyEventRecordDialog;
import com.exfantasy.together.login.LoginDialog;
import com.exfantasy.together.register.UploadImgDialog;
import com.exfantasy.together.setting.SettingDialog;
import com.exfantasy.together.vo.Event;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.soundcloud.android.crop.Crop;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Collections;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        View.OnClickListener {

    public static final String TAG = MapsActivity.class.getSimpleName();

    private Resources mResources;
    private SharedPreferences mSharedPreferences;

    private DrawerLayout drawerLayout;

    private ImageView mProfileIcon;

    private SnappingRecyclerView mRecyclerView;

    // google map related
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mResources = getResources();
        mSharedPreferences = getSharedPreferences(SharedPreferencesKey.TOGEHER_KEY, Context.MODE_PRIVATE);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        // set up action bar
        setupActionBar();

        // set up menu buttons
        setupMenuButtons();

        // set up floating action buttons
        setupFloatingActionButton();

        // set up google map
        setupGoogleMap();

        // set up recycler view
        setupRecyclerView();
    }

    private void setupActionBar() {
//        ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.layout_action_bar, null);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayShowCustomEnabled(true);
//        actionBar.setCustomView(actionBarLayout);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBarDrawerToggle actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout, R.string.open_string, R.string.close_string);
        actionBarDrawerToggle.syncState();
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
    }

    private void setupMenuButtons() {
        // set up profile icon
        mProfileIcon = (ImageView) findViewById(R.id.menu_icon);
        mProfileIcon.setOnClickListener(this);

        final boolean alreadyLogined = mSharedPreferences.getBoolean(SharedPreferencesKey.ALREADY_LOGINED, false);

        // set up btn_login
        LinearLayout btnLogin = (LinearLayout) findViewById(R.id.btn_login);
        if (alreadyLogined) {
            btnLogin.setVisibility(View.GONE);
        }
        else {
            btnLogin.setOnClickListener(this);
        }

        // set up btn_recently_action
        LinearLayout btnRecentlyAction = (LinearLayout) findViewById(R.id.btn_recently_action);
        btnRecentlyAction.setOnClickListener(this);

        // set up btn_setup
        LinearLayout btnSetup = (LinearLayout) findViewById(R.id.btn_setup);
        btnSetup.setOnClickListener(this);

        // set up btn_logout
        LinearLayout btnLogout = (LinearLayout) findViewById(R.id.btn_logout);
        if (!alreadyLogined) {
            btnLogout.setVisibility(View.GONE);
        }
        else {
            btnLogout.setOnClickListener(this);
        }
    }

    private void setupFloatingActionButton() {
        FloatingActionButton fabCreateEvent = (FloatingActionButton) findViewById(R.id.fab_create_event);
        fabCreateEvent.setOnClickListener(this);

        FloatingActionButton fabSearchEvent = (FloatingActionButton) findViewById(R.id.fab_search_event);
        fabSearchEvent.setOnClickListener(this);

        FloatingActionButton fabRefreshMap = (FloatingActionButton) findViewById(R.id.fab_refresh_map);
        fabRefreshMap.setOnClickListener(this);
    }

    private void setupGoogleMap(){
        MapFragment mapFragment
                = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);

        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
    }

    private void setupRecyclerView() {
        mRecyclerView = (SnappingRecyclerView) findViewById(R.id.event_recycler_view);
        mRecyclerView.setSnapEnabled(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        ItemData itemsData[] = {
                new ItemData("Luffy",R.drawable.icon_onepiece_luffy),
                new ItemData("Zoro",R.drawable.icon_onepiece_zoro),
                new ItemData("Nami",R.drawable.icon_onepiece_nami),
                new ItemData("Sanji",R.drawable.icon_onepiece_sanji),
                new ItemData("Ussop",R.drawable.icon_onepiece_ussop),
                new ItemData("Chopper",R.drawable.icon_onepiece_chopper),
                new ItemData("Nico",R.drawable.icon_onepiece_nico),
                new ItemData("Franck",R.drawable.icon_onepiece_franck),
                new ItemData("Brook",R.drawable.icon_onepiece_brook)
        };

        MyAdapter mAdapter = new MyAdapter(itemsData);
        mRecyclerView.setAdapter(mAdapter);
    }

    private  void showUploadImageDialog(){
        UploadImgDialog uploadImgDialog = new UploadImgDialog();
        uploadImgDialog.setImgView(mProfileIcon);
        uploadImgDialog.show(getSupportFragmentManager(), "UploadImageDialog");
    }

    private void showLoginDialog() {
        DialogFragment loginDialog = new LoginDialog();
        loginDialog.show(getSupportFragmentManager(), "LoginDialog");
    }

    private void showMyEventsRecordDialog() {
        DialogFragment eventsRecordDialog = new MyEventRecordDialog();
        eventsRecordDialog.show(getSupportFragmentManager(), "MyEventRecordDialog");
    }

    private void showSetupDialog() {
        DialogFragment settingDialog = new SettingDialog();
        settingDialog.show(getSupportFragmentManager(), "SettingDialog");
    }

    private void showCreateEventDialog() {
        DialogFragment createEventFr = new CreateEventDialog();
        LatLng centerLatLng = getCenterLatLng();
        Bundle latlngBundle = new Bundle();
        latlngBundle.putDouble("lat", centerLatLng.latitude);
        latlngBundle.putDouble("lng", centerLatLng.longitude);
        createEventFr.setArguments(latlngBundle);
        createEventFr.show(getSupportFragmentManager(), "CreateEventDialog");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawers();
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 設定畫面初始位置
        double initLat = 23.942314;
        double initLng = 121.048767;
        CameraUpdate center=
                CameraUpdateFactory.newLatLngZoom(new LatLng(initLat, initLng), 8);
        mMap.moveCamera(center);

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true);

        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter(getApplicationContext()));

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                getCenterLatLng();
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                LatLng latLng = marker.getPosition();

                CameraUpdate cameraUpdate =
                        CameraUpdateFactory.newLatLngZoom(latLng, 17);

                mMap.animateCamera(cameraUpdate, 800, new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        marker.showInfoWindow();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                return true;
            }
        });

        new RefreshEventTask().execute();
    }

    private void handleNewLocation(Location location) {
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    public void showMarkerOnMap(Event[] eventsNearby) {
        mMap.clear();

        for (Event event: eventsNearby) {
            double lat = event.getLatitude();
            double lng = event.getLongitude();
            String eventName = event.getName();
            int attendeeNum = event.getAttendeeNum();
            long eventTime = event.getTime();

            MarkerOptions options =
                    new MarkerOptions().position(new LatLng(lat, lng))
                                       .title(eventName)
                                       .snippet(attendeeNum + ";" + eventTime);

            mMap.addMarker(options);
        }
    }

    // 獲取地圖中央定位點的座標
    private LatLng getCenterLatLng() {
        return mMap.getCameraPosition().target;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
              * Google Play services can resolve some errors it detects.
              * If the error has a resolution, try sending an Intent to
              * start a Google Play services activity that can resolve
              * error.
              */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                            * Thrown if Google Play services canceled the original
                            * PendingIntent
                            */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                Log.e(TAG, "Google map onConnectionFailed, cannot resolve by google play service activity, msg: " + e.getMessage(), e);
            }
        } else {
            /*
                     * If no resolution is available, display a dialog to the
                    * user with the error.
                      */
            Log.e(TAG, "Google map onConnectionFailed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
//        handleNewLocation(location);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_icon:
                mProfileIcon.setImageDrawable(null);
                Crop.pickImage(this);
                break;

            case R.id.btn_login:
                showLoginDialog();
                break;

            case R.id.btn_recently_action:
                showMyEventsRecordDialog();
                break;

            case R.id.btn_setup:
                showSetupDialog();
                break;

            case R.id.btn_logout:
                // TODO
                break;

            case R.id.fab_create_event:
                showCreateEventDialog();
                break;

            case R.id.fab_search_event:
                // TODO
                break;

            case R.id.fab_refresh_map:
                new RefreshEventTask().execute();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(result.getData());
        }
        else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, result);
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            mProfileIcon.setImageURI(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showMsgWithToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class RefreshEventTask extends AsyncTask<Void, Void, Event[]> {   //Params, Progress, Result
        private String Lat;
        private String Lng;

        @Override
        protected void onPreExecute() {
            LatLng latLng = getCenterLatLng();
            this.Lat = latLng.latitude + "";
            this.Lng = latLng.longitude + "";
        }

        @Override
        protected Event[] doInBackground(Void... params) {
            String url = mResources.getString(R.string.base_url) + mResources.getString(R.string.api_refresh_event);

            // Populate the HTTP Basic Authentitcation header with the username and password
            // HttpAuthentication authHeader = new HttpBasicAuthentication(account, password);

            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
            formData.add("latitude", Lat);
            formData.add("longitude", Lng);

            HttpEntity<MultiValueMap<String, String>> requestEntity
                    = new HttpEntity<MultiValueMap<String, String>>(formData, requestHeaders);

            Event[] events = null;
            try {
                ResponseEntity<Event[]> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Event[].class);

                events = response.getBody();

                return events;
            } catch (HttpClientErrorException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            } catch (ResourceAccessException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Event[] refreshEvents) {
            if (refreshEvents != null) {
                showMarkerOnMap(refreshEvents);
            }
            else {
                showMsgWithToast(getString(R.string.warn_network_error));
            }
        }
    }
}
