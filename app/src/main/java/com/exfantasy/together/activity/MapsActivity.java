package com.exfantasy.together.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.exfantasy.together.R;
import com.exfantasy.together.cnst.SharedPreferencesKey;
import com.exfantasy.together.components.floatingActionButton.FloatingActionButton;
import com.exfantasy.together.components.recyclerview.ItemData;
import com.exfantasy.together.components.recyclerview.MyAdapter;
import com.exfantasy.together.components.recyclerview.SnappingRecyclerView;
import com.exfantasy.together.event.CreateEventDialog;
import com.exfantasy.together.event.MyEventRecordDialog;
import com.exfantasy.together.gcm.RegistrationIntentService;
import com.exfantasy.together.login.LoginDialog;
import com.exfantasy.together.setting.SettingDialog;
import com.exfantasy.together.util.DateTimeUtil;
import com.exfantasy.together.util.ImageUtils;
import com.exfantasy.together.vo.Event;
import com.exfantasy.together.vo.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        View.OnClickListener {

    public static final String TAG = MapsActivity.class.getSimpleName();

    private SharedPreferences mSharedPreferences;
    private boolean mAlreadyLogined;

    private DrawerLayout mDrawerLayout;

    private ImageView mProfileIcon;

    // google map related
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 8000;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    // Record events information
    private Map<Marker, Event> mEventsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences = getSharedPreferences(SharedPreferencesKey.TOGEHER_KEY, Context.MODE_PRIVATE);
        mAlreadyLogined = mSharedPreferences.getBoolean(SharedPreferencesKey.ALREADY_LOGINED, false);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        // set up action bar
        setupActionBar();

        // set up menu buttons
        setupMenuItems();

        // set up floating action buttons
        setupFloatingActionButton();

        // set up google map
        setupGoogleMap();

        // set up recycler view
        setupRecyclerView();

        // check if need to register gcm
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    private void setupActionBar() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeButtonEnabled(true);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open_string, R.string.close_string);
        actionBarDrawerToggle.syncState();

        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);
    }

    private void setupMenuItems() {
        // set up profile icon
        mProfileIcon = (ImageView) findViewById(R.id.menu_icon);
        mProfileIcon.setOnClickListener(this);
        if (mAlreadyLogined) {
            Bitmap bitmap = ImageUtils.loadProfileIcomFromExternalStorage();
            if (bitmap != null) {
                mProfileIcon.setImageBitmap(bitmap);
            }
            else {
                new DownloadPhotoTask().execute();
            }
        }

        // set up menu_username
        String name = mSharedPreferences.getString(SharedPreferencesKey.NAME, "");
        TextView tvUserName = (TextView) findViewById(R.id.tv_username_at_menu);
        tvUserName.setText(name);

        // set up menu_user_email
        String email = mSharedPreferences.getString(SharedPreferencesKey.EMAIL, "");
        TextView tvEmail = (TextView) findViewById(R.id.tv_user_email_at_menu);
        tvEmail.setText(email);

        // set up btn_login
        LinearLayout btnLogin = (LinearLayout) findViewById(R.id.btn_login_at_menu);
        btnLogin.setOnClickListener(this);
        if (mAlreadyLogined) {
            btnLogin.setVisibility(View.GONE);
        }

        // set up btn_recently_action
        LinearLayout btnRecentlyAction = (LinearLayout) findViewById(R.id.btn_recently_action_at_menu);
        btnRecentlyAction.setOnClickListener(this);

        // set up btn_setup
        LinearLayout btnSetup = (LinearLayout) findViewById(R.id.btn_setup_at_menu);
        btnSetup.setOnClickListener(this);

        // set up btn_logout
        LinearLayout btnLogout = (LinearLayout) findViewById(R.id.btn_logout_at_menu);
        btnLogout.setOnClickListener(this);
        if (!mAlreadyLogined) {
            btnLogout.setVisibility(View.GONE);
        }
    }

    private void setupFloatingActionButton() {
        FloatingActionButton fabCreateEvent = (FloatingActionButton) findViewById(R.id.fab_create_event);
        fabCreateEvent.setOnClickListener(this);
        if (!mAlreadyLogined) {
            fabCreateEvent.setVisibility(View.GONE);
        }

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
        int interval = 10 * 1000;
        int fastestInterval = 1000;
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(interval)        // 10 seconds, in milliseconds
                .setFastestInterval(fastestInterval); // 1 second, in milliseconds
    }

    private void setupRecyclerView() {
        SnappingRecyclerView snappingRecyclerView = (SnappingRecyclerView) findViewById(R.id.event_recycler_view);
        snappingRecyclerView.setSnapEnabled(true);
        snappingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        snappingRecyclerView.setItemAnimator(new DefaultItemAnimator());

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
        snappingRecyclerView.setAdapter(mAdapter);
    }

    private void showLoginDialog() {
        DialogFragment loginDialog = new LoginDialog();
        loginDialog.show(getSupportFragmentManager(), "LoginDialog");
    }

    private void showRecentlyActions() {
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
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawers();
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
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
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                // TODO show a detail dialog
                Log.i(TAG, ">>>>> you clicked on a marker info window");
            }
        });

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
                    public void onCancel() {}
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
        mEventsMap.clear();

        for (Event event: eventsNearby) {
            double lat = event.getLatitude();
            double lng = event.getLongitude();
            String eventName = event.getName();

            MarkerOptions options =
                    new MarkerOptions().position(new LatLng(lat, lng)).title(eventName);

            Marker marker = mMap.addMarker(options);
            mEventsMap.put(marker, event);
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
                mAlreadyLogined = mSharedPreferences.getBoolean(SharedPreferencesKey.ALREADY_LOGINED, false);
                if (mAlreadyLogined) {
                    Crop.pickImage(this);
                }
                else {
                    showMsgWithToast(getString(R.string.warn_pls_login));
                }
                break;

            case R.id.btn_login_at_menu:
                showLoginDialog();
                break;

            case R.id.btn_recently_action_at_menu:
                showRecentlyActions();
                break;

            case R.id.btn_setup_at_menu:
                showSetupDialog();
                break;

            case R.id.btn_logout_at_menu:
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
            Uri imageUri = Crop.getOutput(result);

            String imgStoredExternalStoragePath = ImageUtils.normalizeImageAndSave(getApplicationContext(), imageUri);

            mProfileIcon.setImageURI(null);
            mProfileIcon.setImageURI(imageUri);

            Log.i(TAG, "Image stored path: <" + imgStoredExternalStoragePath + ">");

            new UploadProfileImageTask().execute(imgStoredExternalStoragePath);
        }
        else if (resultCode == Crop.RESULT_ERROR) {
            showMsgWithToast(Crop.getError(result).getMessage());
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

    private class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private View mView;

        private TextView mTvEventTitle;
        private TextView mTvEventContent;
        private TextView mTvEventLatLng;
        private TextView mTvEventAttendeeNum;
        private TextView mTvEventAttendee;
        private TextView mTvEventDate;
        private TextView mTvEventTime;

        private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        private SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm");

        public MarkerInfoWindowAdapter(Context context) {
            findViews(context);
        }

        private void findViews(Context context) {
            mView = LayoutInflater.from(context).inflate(R.layout.dialog_marker, null);

            mTvEventTitle = (TextView) mView.findViewById(R.id.dlg_marker_tv_event_name);
            mTvEventContent = (TextView) mView.findViewById(R.id.dlg_marker_tv_event_content);
            mTvEventLatLng = (TextView) mView.findViewById(R.id.dlg_marker_tv_event_latlng);
            mTvEventAttendeeNum = (TextView) mView.findViewById(R.id.dlg_marker_tv_event_attendee_num);
            mTvEventAttendee = (TextView) mView.findViewById(R.id.dlg_marker_tv_event_attendee);
            mTvEventDate = (TextView) mView.findViewById(R.id.dlg_marker_tv_event_date);
            mTvEventTime = (TextView) mView.findViewById(R.id.dlg_marker_tv_event_time);

            // 設定 TextView 字型
            Typeface typeface = Typeface.createFromAsset(getAssets(), "SquareCircle.ttc");
            mTvEventContent.setTypeface(typeface);
            mTvEventLatLng.setTypeface(typeface);
            mTvEventAttendeeNum.setTypeface(typeface);
            mTvEventAttendee.setTypeface(typeface);
            mTvEventDate.setTypeface(typeface);
            mTvEventTime.setTypeface(typeface);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            Event event = mEventsMap.get(marker);

            String eventId = String.valueOf(event.getEventId());
            String createUserId = String.valueOf(event.getCreateUserId());

            String eventName = event.getName();
            mTvEventTitle.setText(eventName);

            String eventContent = event.getContent();
            mTvEventContent.setText("活動內容: " + eventContent);

            LatLng latLng = marker.getPosition();
            String latitude = String.valueOf(latLng.latitude);
            String longitude = String.valueOf(latLng.longitude);
            mTvEventLatLng.setText("經緯度: " + latitude + ", " + longitude);

            String attendeeNum = String.valueOf(event.getAttendeeNum());
            mTvEventAttendeeNum.setText("活動人數: " + attendeeNum);

            Iterator<User> it = event.getUsers().iterator();
            StringBuilder buffer = new StringBuilder();
            while (it.hasNext()) {
                User user = it.next();
                buffer.append("<").append(user.getUserId()).append("-").append(user.getName()).append(">");
            }
            mTvEventAttendee.setText("目前參與者: " + buffer.toString());

            int eventDate = event.getDate();
            Calendar calDate = DateTimeUtil.parseDateValue(eventDate);
            String showEventDate = mDateFormat.format(calDate.getTime());
            mTvEventDate.setText("活動日期: " + showEventDate);

            int eventTime = event.getTime();
            Calendar calTime = DateTimeUtil.parseTimeValue(eventTime);
            String showEventTime = mTimeFormat.format(calTime.getTime());
            mTvEventTime.setText("活動時間: " + showEventTime);

            return mView;
        }
    }

    private class RefreshEventTask extends AsyncTask<Void, Void, Event[]> {   //Params, Progress, Result
        private String currentLat;
        private String currentLng;

        @Override
        protected void onPreExecute() {
            LatLng latLng = getCenterLatLng();
            this.currentLat = latLng.latitude + "";
            this.currentLng = latLng.longitude + "";
        }

        @Override
        protected Event[] doInBackground(Void... params) {
            try {
                String url = getString(R.string.base_url) + getString(R.string.api_refresh_event);

                HttpHeaders requestHeaders = new HttpHeaders();
                requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
                formData.add("latitude", currentLat);
                formData.add("longitude", currentLng);

                HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, requestHeaders);

                Log.i(TAG, ">>>>> Prepare to refresh events by latitude: <" + currentLat + ">, longitude: <" + currentLng + ">");

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());

                ResponseEntity<Event[]> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Event[].class);

                Event[] events = response.getBody();

                Log.i(TAG, "<<<<< Refresh events by latitude: <" + currentLat + ">, longitude: <" + currentLng + "> done, events-length: <" + (events == null ? 0 : events.length) + ">");

                return events;
            } catch (Exception e) {
                Log.e(TAG, "<<<<< Refresh events by latitude: <" + currentLat + ">, longitude: <" + currentLng + "> failed, err-msg: <" + e.toString() + ">");
                return null;
            }
        }

        @Override
        protected void onPostExecute(Event[] refreshEvents) {
            if (refreshEvents != null) {
                showMarkerOnMap(refreshEvents);
            }
            else {
                showMsgWithToast(getString(R.string.error_network_abnormal));
            }
        }
    }

    private class UploadProfileImageTask extends AsyncTask<String, Void, String> {   //Params, Progress, Result
        private String mEmail;

        @Override
        protected void onPreExecute() {
            mEmail = mSharedPreferences.getString(SharedPreferencesKey.EMAIL, "");
        }

        @Override
        protected String doInBackground(String... filePaths) {
            try {
                String url = getString(R.string.base_url) + getString(R.string.api_upload_file);

                HttpHeaders requestHeaders = new HttpHeaders();
                requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

                MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
                formData.add("email", mEmail);
                formData.add("uploadfile", new FileSystemResource(filePaths[0]));

                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(formData, requestHeaders);

                Log.i(TAG, ">>>>> Prepare to upload profile image with email: <" + mEmail + ">");

                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

                String result = response.getBody();

                Log.i(TAG, "<<<<< Upload profile image with email: <" + mEmail + "> done, result: <" + result + ">");

                return result;
            } catch (Exception e) {
                Log.e(TAG, "<<<<< Upload profile image with email: <" + mEmail + "> failed, err-msg: <" + e.toString() + ">");
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {

            }
            else {
                showMsgWithToast(getString(R.string.error_network_abnormal));
            }
        }
    }

    private class DownloadPhotoTask extends AsyncTask<Void, Void, byte[]> {   //Params, Progress, Result
        private String mEmail;

        @Override
        protected void onPreExecute() {
            mEmail = mSharedPreferences.getString(SharedPreferencesKey.EMAIL, "");
        }

        @Override
        protected byte[] doInBackground(Void... params) {
            try {
                String url = getString(R.string.base_url) + getString(R.string.api_download_file);

                HttpHeaders requestHeaders = new HttpHeaders();
                requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

                MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
                formData.add("email", mEmail);
                formData.add("file-name", ImageUtils.PROFILE_ICON_NAME);

                HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, requestHeaders);

                Log.i(TAG, ">>>>> Prepare to download profile image with email: <" + mEmail + "> from server");

                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, byte[].class);

                byte[] downloadFile = response.getBody();

                Log.i(TAG, "<<<<< Download profile image with email: <" + mEmail + "> from server done, length: <" + downloadFile.length + ">");

                return downloadFile;
            } catch (Exception e) {
                Log.e(TAG, "<<<<< Download profile image with email: <" + mEmail + "> failed, err-msg: <" + e.toString() + ">");
                return null;
            }
        }

        @Override
        protected void onPostExecute(byte[] downloadFile) {
            if (downloadFile != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(downloadFile, 0, downloadFile.length);
                ImageUtils.saveToExternalStorage(bitmap);
                mProfileIcon.setImageBitmap(bitmap);
            }
            else {
                showMsgWithToast(getString(R.string.error_network_abnormal));
            }
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
