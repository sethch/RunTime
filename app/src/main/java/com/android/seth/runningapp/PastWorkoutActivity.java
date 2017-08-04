package com.runningapp;

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
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PastWorkoutActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private TextView time_stat;
    private TextView pace_stat;
    private TextView distance_stat;

    private ArrayList<LatLng> locations;
    private ArrayList<Integer> times;
    private String date;
    private float distance_miles;
    private int duration;
    private float pace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_workout);
        Intent intent = getIntent();
        time_stat = (TextView) findViewById(R.id.time_stat);
        pace_stat = (TextView) findViewById(R.id.pace_stat);
        distance_stat = (TextView) findViewById(R.id.distance_stat);
        date = intent.getStringExtra("WORKOUT_DATE");
        distance_miles = intent.getFloatExtra("WORKOUT_DISTANCE", 0f);
        duration = intent.getIntExtra("WORKOUT_DURATION", 0);
        locations = intent.getParcelableArrayListExtra("WORKOUT_LOCATIONS");
        times = intent.getIntegerArrayListExtra("WORKOUT_TIMES");
        if(googleServicesAvailable()){
            initMap();
        }
        time_stat.setText("Total time: " + duration + " seconds");
        distance_stat.setText("Total Distance: " + new DecimalFormat("#.##").format(distance_miles) + " miles");
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

        this.mGoogleMap = googleMap;
        this.mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                this.mGoogleMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            this.mGoogleMap.setMyLocationEnabled(true);
        }
        redrawPolyLines(locations);
        placeMarkers();
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
    public void redrawPolyLines(ArrayList<LatLng> location_array){
        LatLng prev_location = null;
        for(LatLng location : location_array){
            if(location != null && prev_location != null) {
                Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions()
                        .add(prev_location).add(location).width(10).color(Color.RED));
            }
            prev_location = location;
        }
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * Asynchronously invoked upon call to connect() after connect request is successfully completed.
     * Sets location update intervals and location accuracy, then requests location.
     *
     * @param bundle Data provided to clients through Google Play
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000); // Location update interval set to 5000 ms (5 seconds)
        mLocationRequest.setFastestInterval(500); // TODO: fine tune & test
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(20); // TODO: fine tune & test
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     * Called when the client is temporarily in a disconnected state.
     * TODO: Disable UI components that require connection
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
     * Called when client location has changed.
     *
     * @param location Updated location
     */
    @Override
    public void onLocationChanged(Location location) {
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
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
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            //You can add here other case statements according to your requirement.
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
     * Calculates and places mile markers for the workout
     * Also sets pace text by calculating pace
     */
    private void placeMarkers(){
        ArrayList<Integer> mile_markers = new ArrayList<>();
        if(locations != null) {
            int i = 0;
            float dist = 0f;
            float prev_dist = 0f;
            Location previous = new Location(LocationManager.GPS_PROVIDER);
            Location current = new Location(LocationManager.GPS_PROVIDER);
            for (LatLng curr : locations) {
                current.setLatitude(curr.latitude);
                current.setLongitude(curr.longitude);
                if(i > 0) {
                    prev_dist = dist;
                    dist += current.distanceTo(previous) / 1609.34f;
                    if((int)prev_dist < (int)dist){
                        mile_markers.add(i);
                    }
                }
                previous.setLatitude(curr.latitude);
                previous.setLongitude(curr.longitude);
                i++;
            }
            int duration_temp = times.get(times.size()-1);
            pace = duration_temp / dist;
            int pace_minutes = (int)pace/60;
            int pace_seconds = (int)pace%60;
            pace_stat.setText(pace_minutes + new DecimalFormat(".##").format((float)pace_seconds/60) + " min/mile");
        }
        int i = 1;
        for(Integer index : mile_markers){
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(locations.get(index))
                    .title("Mile " + i + " At Time: " + times.get(i))
            );
            i++;
        }
    }
}

// TODO: past_workout_activity.xml in layouts