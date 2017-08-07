package com.android.seth.runningapp;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Parcel;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.os.Handler;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class RunActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    // TIMER SECTION
    private TextView timer;
    private TextView distance;
    private TextView pace;
    private Button resume_pause_button;
    private Button finish_button;
    private long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;
    private Handler handler;
    private int Seconds, Minutes;

    // Will take info from mLastLocation and convert to latitude/longitude for polylines
    private LatLng last_location;
    private LatLng curr_location;
    private ArrayList<LatLng> locations;
    private ArrayList<Integer> times;

    private float distance_traveled_meters = 0;
    private float distance_traveled_miles = 0;
    private final float meters_in_mile = 1609.34f;

    private boolean begin = true;
    private boolean paused = true;
    private boolean workout_started = false;

    /**
     * Checks if google play services available.
     * If so, inflates layout xml file and calls initMap()
     *
     * @param savedInstanceState Previous instance state if another activity is started
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        timer = (TextView) findViewById(R.id.timer);
        distance = (TextView) findViewById(R.id.distance);
        pace = (TextView) findViewById(R.id.pace);
        resume_pause_button = (Button) findViewById(R.id.start_button);
        finish_button = (Button) findViewById(R.id.finish_button);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        handler = new Handler();
        checkGPSandNetwork();
        if(savedInstanceState != null){
            restoreVariables(savedInstanceState);
        }
        else{
            locations = new ArrayList<LatLng>();
            times = new ArrayList<Integer>();
            resume_pause_button.setTag(1);
            resume_pause_button.setText("Start");
        }
        if (googleServicesAvailable()) {
            resume_pause_button.setOnClickListener(this);
            finish_button.setOnClickListener(this);
            initMap();
        } else {
            Toast.makeText(this, "Google maps not supported", Toast.LENGTH_LONG).show();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(5);
        }
    }

    /**
     * Called from onCreate if Google Services are available.
     * Initializes the mapFragment (a wrapper around the map view contained in res/layout/activity_runxml).
     */
    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this); // initializes the maps system and view
    }

    /**
     * Called when client location has changed.
     *
     * @param location Updated location
     */
    @Override
    public void onLocationChanged(Location location) {
        if (begin == true) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
            begin = false;
        }
        if (paused == false) {
            if (mLastLocation != null) {
                distance_traveled_meters = distance_traveled_meters + location.distanceTo(mLastLocation);
                distance_traveled_miles = distance_traveled_meters / meters_in_mile;
            }
            mLastLocation = location;
            last_location = curr_location;
            curr_location = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            locations.add(curr_location);
            times.add(new Integer(Seconds + (Minutes*60)));
            if (last_location != null) {
                Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions()
                        .add(last_location).add(curr_location).width(10).color(Color.RED));
            }
        }
    }

    /**
     * This is called upon clicking one of the RegisterActivity links
     *
     * @param v which view has been clicked
     */
    @Override
    public void onClick(View v) {
        if (v == resume_pause_button) {
            int status = (int) v.getTag();
            if (status == 1) {
                StartTime = SystemClock.uptimeMillis();
                handler.postDelayed(runnable, 0);
                resume_pause_button.setText("Pause");
                v.setTag(0);
                paused = false;
                workout_started = true;
            } else {
                TimeBuff += MillisecondTime;
                handler.removeCallbacks(runnable);
                resume_pause_button.setText("Resume");
                v.setTag(1);
                paused = true;
            }
        }
        if (v == finish_button) {
            handler.removeCallbacks(runnable);
            storeWorkout();
        }
    }

    /**
     * Stores arrayList of locations and times to firebase database when user presses "finish" button.
     */
    private void storeWorkout() {
        ArrayList<Lat_Lng> temp_locations = new ArrayList<>();
        for(LatLng l : locations){
            temp_locations.add(new Lat_Lng(l.latitude, l.longitude));
        }
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0);
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm aaa");
        String formatted_date = format1.format(cal.getTime());
        Workout workout = new Workout(temp_locations, times, formatted_date, distance_traveled_miles, Seconds + Minutes*60);
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String key = databaseReference.child("users").child(user.getUid()).child("workouts").push().getKey();
        databaseReference.child("users").child(user.getUid()).child("workouts").child(key).setValue(workout);
        Toast.makeText(this, "Workout saved", Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * Helper function for updating timer and distance at top of app.
     */
    public void setTimerAndDistance(){
        UpdateTime = TimeBuff + MillisecondTime;
        Seconds = (int) (UpdateTime / 1000);
        Minutes = Seconds / 60;
        Seconds = Seconds % 60;
        timer.setText("Time:" + Minutes + ":"
                + String.format("%02d", Seconds));

        int whole_number = (int) Math.floor(distance_traveled_miles);
        int first_two_decimals = (int) Math.floor((distance_traveled_miles - whole_number) * 100);
        distance.setText(whole_number + "."
                + String.format("%02d", first_two_decimals) + " miles");

        // TODO: Increase accuracy of pace calculations A LOT
        if(distance_traveled_miles > 0) {
            int pace_seconds_total = (int) Math.floor((Seconds + (Minutes * 60)) / distance_traveled_miles);
            int pace_minutes = pace_seconds_total / 60;
            int pace_seconds = pace_seconds_total % 60;
            pace.setText(pace_minutes + new DecimalFormat(".##").format((float)pace_seconds/60) + " min/mile");
        }
    }

    /**
     * Runnable is an interface implemented by classes which are executed by a thread.
     */
    public Runnable runnable = new Runnable() {
        public void run() {
            MillisecondTime = SystemClock.uptimeMillis() - StartTime;
            setTimerAndDistance();
            handler.postDelayed(this, 0);
        }
    };

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
     * Called when the client is temporarily in a disconnected state.
     * TODO: Disable UI components that require connection
     *
     * @param cause The reason for the disconnection
     */
    @Override
    public void onConnectionSuspended(int cause) {

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
     * Called when the googleMap is ready to be used.
     * Initializes googlePlayServices
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }
        redrawPolyLines(locations);
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
     * Asynchronously invoked upon call to connect() after connect request is successfully completed.
     * Sets location update intervals and location accuracy, then requests location.
     *
     * @param bundle Data provided to clients through Google Play
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000); // Location update interval set to 2000 ms (2 seconds)
        mLocationRequest.setFastestInterval(500); // TODO: fine tune & test
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(20); // TODO: fine tune & test
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     * Called when screen orientation is changed to save variables
     *
     * @param outState Instance state to save
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("TAG", (int)resume_pause_button.getTag());
        outState.putFloat("MILES", distance_traveled_miles);
        outState.putFloat("METERS", distance_traveled_meters);
        outState.putBoolean("BEGIN", begin);
        outState.putBoolean("PAUSED", paused);
        outState.putIntegerArrayList("TIMES", times);
        outState.putParcelableArrayList("LOCATIONS", locations);
        outState.putLong("TIMEBUFF", TimeBuff);
        outState.putLong("STARTTIME", StartTime);
        outState.putParcelable("CURR_LOC", curr_location);
        outState.putParcelable("LAST_LOC", last_location);
        super.onSaveInstanceState(outState);
    }

    /**
     * Called when the screen orientation is changed while the app is running.
     *
     * @param savedInstanceState instance state pararmeter from onCreate
     */
    private void restoreVariables(Bundle savedInstanceState){
        resume_pause_button.setTag(savedInstanceState.getInt("TAG"));
        distance_traveled_miles = savedInstanceState.getFloat("MILES");
        distance_traveled_meters = savedInstanceState.getFloat("METERS");
        begin = savedInstanceState.getBoolean("BEGIN");
        paused = savedInstanceState.getBoolean("PAUSED");
        locations = savedInstanceState.getParcelableArrayList("LOCATIONS");
        times = savedInstanceState.getIntegerArrayList("TIMES");
        curr_location = savedInstanceState.getParcelable("CURR_LOC");
        last_location = savedInstanceState.getParcelable("LAST_LOC");
        TimeBuff = savedInstanceState.getLong("TIMEBUFF");
        StartTime = savedInstanceState.getLong("STARTTIME");
        if(begin == true){
            resume_pause_button.setText("Start");
        }
        else if((int)resume_pause_button.getTag() == 1) {
            resume_pause_button.setText("Resume");
            setTimerAndDistance();
        }
        else{
            resume_pause_button.setText("Pause");
            handler.postDelayed(runnable, 0);
        }
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

    /**
     * Called when back is pressed during RunActivity
     * If workout is not started, just ends activity.
     * If workout is started, AlertDialog asks user whether to:
     * 1. Store Workout
     * 2. Don't Store Workout
     * 3. Cancel
     */
    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Would you like to store this workout?");
        builder.setNeutralButton("Store", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                storeWorkout();
            }
        });
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               return;
            }
        });
        builder.setNegativeButton("Don't Store", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        if(workout_started) {
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else{
            finish();
        }
    }

    /**
     * Determined whether user has location tracking enabled.
     * If not, asks user to enable location tracking or exits.
     */
    public void checkGPSandNetwork(){
        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Please enable location tracking");
            dialog.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    finish();
                }
            });
            dialog.show();
        }
    }
}

// TODO: Improve AlertDialog appearance in onBackPressed() and checkGPSandNetwork()
// TODO: Potentially check whether user enabled location tracking in checkGPSandNetwork()
// TODO: Improve accuracy of location tracking (seems to miss a decent chunk sometimes)