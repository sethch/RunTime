package com.android.seth.runningapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.android.seth.runningapp.util.UtilityFunctions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PastWorkoutActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<LatLng> locations;
    private ArrayList<Integer> times;

    private TextView distanceStat;
    private TextView paceStat;
    private TextView timeStat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeLayout();
        if (googleServicesAvailable()) {
            initMap();
        }
        Intent intent = getIntent();
        float distanceMiles = intent.getFloatExtra("WORKOUT_DISTANCE", 0f);
        int duration = intent.getIntExtra("WORKOUT_DURATION", 0);
        locations = intent.getParcelableArrayListExtra("WORKOUT_LOCATIONS");
        times = intent.getIntegerArrayListExtra("WORKOUT_TIMES");
        String time = "Time: " + UtilityFunctions.getTimeString(duration);
        timeStat.setText(time);
        distanceStat.setText("Distance: " + new DecimalFormat("#.##").format(distanceMiles) + " miles");
    }

    /**
     * Initializes layout and views for PastWorkoutActivity.
     */
    private void initializeLayout() {
        setContentView(R.layout.activity_past_workout);
        timeStat = (TextView) findViewById(R.id.time_stat);
        paceStat = (TextView) findViewById(R.id.pace_stat);
        distanceStat = (TextView) findViewById(R.id.distance_stat);
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
        this.mGoogleMap = googleMap;
        this.mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
            }
        } else {
            buildGoogleApiClient();
        }

        if (locations.size() > 0) {
            redrawPolyLines(locations);
            placeMarkers();
        }
    }

    /**
     * Called from onCreate if Google Services are available.
     * Initializes the mapFragment (a wrapper around the map view contained in res/layout/activity_run.xml).
     */
    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.past_workout_map);
        mapFragment.getMapAsync(this); // initializes the maps system and view
    }

    /**
     * Called upon orientation change or viewing past workouts.
     * Re-draws polylines on map according to stored list of Latitudes and Longitudes.
     *
     * @param location_array Array of LatLng objects with which to recreate the workout path
     */
    public void redrawPolyLines(ArrayList<LatLng> location_array) {
        LatLng prev_location = null;
        for (LatLng location : location_array) {
            if (location != null && prev_location != null) {
                mGoogleMap.addPolyline(new PolylineOptions()
                        .add(prev_location).add(location).width(10).color(Color.RED));
            }
            prev_location = location;
        }
        if (prev_location != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(prev_location, 16));
        }
    }

    /**
     * Asynchronously invoked upon call to connect() after connect request is successfully completed.
     * Sets location update intervals and location accuracy, then requests location.
     *
     * @param bundle Data provided to clients through Google Play
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    /**
     * Called when the client is temporarily in a disconnected state.
     *
     * @param cause The reason for the disconnection
     */
    @Override
    public void onConnectionSuspended(int cause) {

    }

    /**
     * Called upon connection failure.
     * Outputs connection error details in a toast.
     *
     * @param connectionResult Contains error codes for when the client fails to connect to Google Play services.
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Cannot connect to play services" + connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
    }

    /**
     * Checks whether the location tracking permission has been granted.
     */
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Callback for the result from requesting permissions.
     * Invoked for every call on requestPermissions.
     *
     * @param requestCode  The request code passed in requestPermissions
     * @param permissions  The requested permissions.
     * @param grantResults The grant results (PERMISSION_GRANTED or PERMISSION_DENIED) for the requested permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Configures the GoogleApiClient.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Checks if current device has Google Play services APK up to date.
     *
     * @return true if the user has Google Play services APK up to date
     * false otherwise
     */
    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cannot connect to play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    /**
     * Calculates and places mile markers for the workout.
     * Also sets pace text by calculating pace.
     */
    private void placeMarkers() {
        ArrayList<Integer> mile_markers = new ArrayList<>();
        Location start = new Location(LocationManager.GPS_PROVIDER);
        Location end = new Location(LocationManager.GPS_PROVIDER);
        if (locations != null) {
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(locations.get(0))
                    .title("Start")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            );
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(locations.get(locations.size() - 1))
                    .title("End")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            );
            int i = 0;
            float dist = 0f;
            float prevDist;
            Location previous = new Location(LocationManager.GPS_PROVIDER);
            Location current = new Location(LocationManager.GPS_PROVIDER);
            for (LatLng curr : locations) {
                current.setLatitude(curr.latitude);
                current.setLongitude(curr.longitude);
                if (i > 0) {
                    prevDist = dist;
                    dist += current.distanceTo(previous) / 1609.34f;
                    if ((int) prevDist < (int) dist) {
                        mile_markers.add(i);
                    }
                }
                previous.setLatitude(curr.latitude);
                previous.setLongitude(curr.longitude);
                i++;
            }
            int duration_temp = times.get(times.size() - 1);
            float pace = duration_temp / dist;
            if (dist > 0) {
                paceStat.setText(UtilityFunctions.getPaceString(pace));
            }
        }
        int i = 1;
        for (Integer index : mile_markers) {
            int totalSeconds = times.get(index);
            String time = UtilityFunctions.getTimeString(totalSeconds);
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(locations.get(index))
                    .title("Mile " + i + " At " + time)
            );
            i++;
        }
    }

    /**
     * Returns to HistoryActivity upon pressing back.
     */
    @Override
    public void onBackPressed() {
        Intent HistoryActivity = new Intent(PastWorkoutActivity.this, HistoryActivity.class);
        startActivity(HistoryActivity);
        finish();
    }
}

// TODO: add delete button to either history_listview or PastWorkoutActivity
// TODO: Improve mile marker looks/labeling