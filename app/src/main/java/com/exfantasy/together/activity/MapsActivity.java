package com.exfantasy.together.activity;

import android.content.IntentSender;
import android.content.res.Resources;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.exfantasy.together.R;
import com.exfantasy.together.event.CreateEventDialog;
import com.exfantasy.together.login.LoginDialog;
import com.exfantasy.together.event.MyEventRecordDialog;
import com.exfantasy.together.components.floatingActionButton.FloatingActionButton;
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
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final String TAG = MapsActivity.class.getSimpleName();
    DrawerLayout drawerLayout;
    FloatingActionButton mFabCreatEvent;
    FloatingActionButton mFabSearchEvent;

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Resources resources;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resources = this.getResources();
        drawerLayout= (DrawerLayout) findViewById(R.id.drawer);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBarDrawerToggle actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.open_string,R.string.close_string);
        actionBarDrawerToggle.syncState();
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        // set up btn_login_logout
        LinearLayout btnLogin = (LinearLayout) findViewById(R.id.btn_login_logout);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
            }
        });

        // set up btn_recently_action
        LinearLayout btnRecentlyAction = (LinearLayout) findViewById(R.id.btn_recently_action);
        btnRecentlyAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMyEventsRecordDialog();
            }
        });


        // set up btn_setup
        LinearLayout btnSetup = (LinearLayout) findViewById(R.id.btn_setup);
        btnSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetupDialog();
            }
        });


        findViewsAndSetListener();
        initMapFr();
    }
    // for login Dialog
    private void showLoginDialog() {

        DialogFragment newFragment = new LoginDialog();
        newFragment.show(getSupportFragmentManager(), "LoginDialog");
    }

    // for Setup Dialog
    private void showMyEventsRecordDialog() {

        DialogFragment eventsRecordDialog = new MyEventRecordDialog();
        eventsRecordDialog.show(getSupportFragmentManager(), "MyEventRecordDialog");
    }

    // for Setup Dialog
    private void showSetupDialog() {

        DialogFragment settingDialog = new SettingDialog();
        settingDialog.show(getSupportFragmentManager(), "SettingDialog");
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
            if(  drawerLayout.isDrawerOpen(GravityCompat.START)
                    ){
                drawerLayout.closeDrawers();
            }else{
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void findViewsAndSetListener() {
        mFabCreatEvent = (FloatingActionButton) findViewById(R.id.fab_create_event);
        mFabSearchEvent = (FloatingActionButton) findViewById(R.id.fab_search_event);
        mFabCreatEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "ToGetHer Created!", Toast.LENGTH_SHORT).show();
                DialogFragment createEventFr = new CreateEventDialog();
                LatLng centerLatLng = getCenterLatLng();
                Bundle latlngBundle = new Bundle();
                latlngBundle.putDouble("lat", centerLatLng.latitude);
                latlngBundle.putDouble("lng", centerLatLng.longitude);
                createEventFr.setArguments(latlngBundle);
                createEventFr.show(getSupportFragmentManager(), "CreateEventDialog");
            }
        });

        mFabSearchEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "ToGetHer Searched!", Toast.LENGTH_SHORT).show();
                new RefreshEventTask().execute();
            }
        });

    }

    private void initMapFr(){

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
        Double toLat = 120.982024;
        Double toLng = 23.973875;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true);
        UiSettings mapSetting = mMap.getUiSettings();
//        mapSetting.setZoomControlsEnabled(true);
        mapSetting.setAllGesturesEnabled(true);
        mapSetting.setMyLocationButtonEnabled(true);

        CameraUpdate center=
                CameraUpdateFactory.newLatLngZoom(new LatLng(toLat, toLng), 15);
        mMap.moveCamera(center);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                getCenterLatLng();
            }
        });
        new RefreshEventTask().execute();

    }


    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));
//        MarkerOptions options = new MarkerOptions()
//                .position(latLng)
//                .title("I am here!");
//        mMap.addMarker(options);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }


    public void showMarkerOnMap(Event[] eventsNearby) {

        mMap.clear();
        double lat;
        double lng;
        String eventName;
        int attendeeNum;
        long eventTime;

        for(Event event: eventsNearby) {
            lat = event.getLatitude();
            lng = event.getLongitude();
            eventName = event.getName();
            attendeeNum = event.getAttendeeNum();
            eventTime = event.getTime();

            mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title(eventName + ", " + attendeeNum + "人, " + eventTime));

        }

    }

    //獲取地圖中央定位點的座標
    private LatLng getCenterLatLng() {
        LatLng latLng = mMap.getCameraPosition().target;
        Log.i("daniel", "Center LatLng = " + latLng);

        return latLng;
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
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

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
            String url = resources.getString(R.string.base_url) + resources.getString(R.string.api_refresh_event);

            // Populate the HTTP Basic Authentitcation header with the username and password
            // HttpAuthentication authHeader = new HttpBasicAuthentication(account, password);

            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate(true);
            restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
            formData.add("latitude", Lat);
            formData.add("longitude", Lng);

            HttpEntity<MultiValueMap<String, String>> requestEntity
                    = new HttpEntity<MultiValueMap<String, String>>(formData, requestHeaders);
            try {
                ResponseEntity<Event[]> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Event[].class);
                return response.getBody();
            } catch (HttpClientErrorException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            } catch (ResourceAccessException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Event[] RefreshEvents) {
            if (RefreshEvents != null) {
//               for (Event event : RefreshEvents) {
//                    Log.i(TAG, "EvendId:" + event.getId() + ", EventName:" + event.getName() +
//                            ", EventContent:" + event.getContent() + ", AttendeeNum:" + event.getAttendeeNum() +
//                            ", Latitude:" + event.getLatitude() + ", Lontitude:" + event.getLongitude() +
//                            ", Time:" + event.getTime());
//                }
                showMarkerOnMap(RefreshEvents);

            }
        }
    }
}
