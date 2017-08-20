package com.android.seth.runningapp;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.seth.runningapp.util.Lat_Lng;
import com.android.seth.runningapp.util.UtilityFunctions;
import com.android.seth.runningapp.util.Workout;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Locale;

public class RunActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener, TextToSpeech.OnInitListener {
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    private SharedPreferences sharedPreferences;

    private TextToSpeech textToSpeech;

    // TIMER SECTION
    private TextView timerTextView;
    private TextView distanceTextView;
    private TextView paceTextView;
    private Button resumePauseButton;
    private Button finishButton;
    private long millisecondTime, startTime, timeBuff = 0L;
    private Handler handler;
    private int seconds, minutes;

    // Will take info from mLastLocation and convert to latitude/longitude for polylines
    private LatLng lastLocation;
    private LatLng currLocation;
    private ArrayList<LatLng> locations;
    private ArrayList<Integer> times;

    private float distanceTraveledMeters = 0;
    private float distanceTraveledMiles = 0;
    private float currentMile = 1;

    private boolean begin = true;
    private boolean paused = true;
    private boolean started = false;
    private boolean audioEnabled;

    /**
     * Checks if google play services available.
     * If so, inflates layout xml file and calls initMap()
     *
     * @param savedInstanceState Previous instance state if another activity is started
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeLayout();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        handler = new Handler();
        setUpTextToSpeech();
        checkGPSandNetwork();
        checkLocationPermission();
        if (savedInstanceState != null) {
            restoreVariables(savedInstanceState);
        } else if (begin) {
            resumePauseButton.setTag(1);
            locations = new ArrayList<>();
            times = new ArrayList<>();
            resumePauseButton.setText(getString(R.string.start));
        }
        if (googleServicesAvailable()) {
            resumePauseButton.setOnClickListener(this);
            finishButton.setOnClickListener(this);
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
     * Initializes layout and views needed for RunActivity.
     */
    private void initializeLayout() {
        setContentView(R.layout.activity_run);
        timerTextView = (TextView) findViewById(R.id.timer);
        distanceTextView = (TextView) findViewById(R.id.distance);
        paceTextView = (TextView) findViewById(R.id.pace);
        resumePauseButton = (Button) findViewById(R.id.start_button);
        finishButton = (Button) findViewById(R.id.finish_button);
    }

    /**
     * Sets up TextToSpeech and determined whether volume should be enabled from settings.
     */
    private void setUpTextToSpeech() {
        textToSpeech = new TextToSpeech(this, this);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioEnabled = sharedPreferences.getBoolean("pref_audio_enabled", true);
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
        if (begin) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
            begin = false;
        }
        if (!paused) {
            if (mLastLocation != null) {
                distanceTraveledMeters = distanceTraveledMeters + location.distanceTo(mLastLocation);
                distanceTraveledMiles = distanceTraveledMeters / 1609.34f;
                if (distanceTraveledMiles > currentMile) {
                    float paceSecondsTotal = (seconds + (minutes * 60)) / distanceTraveledMiles;
                    CharSequence mileMarkWorkoutString = currentMile + "miles complete. " + UtilityFunctions.getWorkoutStatusString(paceSecondsTotal, distanceTraveledMiles, minutes, seconds);
                    speakIfEnabled(mileMarkWorkoutString, "Mile Mark");
                    currentMile += 1;
                }
            }
            mLastLocation = location;
            lastLocation = currLocation;
            currLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            locations.add(currLocation);
            times.add(new Integer(seconds + (minutes * 60)));
            if (lastLocation != null) {
                mGoogleMap.addPolyline(new PolylineOptions()
                        .add(lastLocation).add(currLocation).width(10).color(Color.RED));
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
        if (v == resumePauseButton) {
            int status = (int) v.getTag();
            // Workout was not paused.
            if (status == 1) {
                // Workout was not started.
                if (!started) {
                    CharSequence startedWorkoutString = "Workout Started";
                    speakIfEnabled(startedWorkoutString, "Workout Started");
                    started = true;
                } else {
                    CharSequence resumedWorkoutString = "Workout Resumed";
                    speakIfEnabled(resumedWorkoutString, "Workout Resumed");
                }
                startTime = SystemClock.uptimeMillis();
                handler.postDelayed(runnable, 0);
                resumePauseButton.setText(getString(R.string.pause));
                v.setTag(0);
                paused = false;
            }
            // Workout was paused.
            else {
                CharSequence pausedWorkoutString = "Workout Paused";
                speakIfEnabled(pausedWorkoutString, "Workout Paused");
                timeBuff += millisecondTime;
                handler.removeCallbacks(runnable);
                resumePauseButton.setText(getString(R.string.resume));
                v.setTag(1);
                paused = true;
            }
        }
        //  Workout finished.
        if (v == finishButton) {
            handler.removeCallbacks(runnable);
            storeWorkout();
        }
    }

    /**
     * Stores arrayList of locations and times to firebase database when user presses "finish" button.
     */
    private void storeWorkout() {
        float paceSecondsTotal = (seconds + (minutes * 60)) / distanceTraveledMiles;
        CharSequence finishedWorkoutString = "Workout Finished. " + UtilityFunctions.getWorkoutStatusString(paceSecondsTotal, distanceTraveledMiles, minutes, seconds);
        speakIfEnabled(finishedWorkoutString, "Workout Finished");
        while (textToSpeech.isSpeaking()) {
            // Waiting...
        }
        ArrayList<Lat_Lng> temp_locations = new ArrayList<>();
        for (LatLng l : locations) {
            temp_locations.add(new Lat_Lng(l.latitude, l.longitude));
        }
        long currentDate = System.currentTimeMillis();
        Workout workout = new Workout(temp_locations, times, currentDate, distanceTraveledMiles, seconds + minutes * 60);
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String key = databaseReference.child("users").child(user.getUid()).child("workouts").push().getKey();
            databaseReference.child("users").child(user.getUid()).child("workouts").child(key).setValue(workout);
        }
        Toast.makeText(this, "Workout saved", Toast.LENGTH_LONG).show();
        startHistoryActivity();
    }

    /**
     * Helper function for updating TextViews at top of app.
     */
    public void setTimerAndDistance() {
        float UpdateTime = timeBuff + millisecondTime;
        int totalSeconds = (int) (UpdateTime / 1000);
        minutes = totalSeconds / 60;
        seconds = totalSeconds % 60;
        String timeString = UtilityFunctions.getTimeString(totalSeconds);
        timerTextView.setText(timeString);
        distanceTextView.setText(UtilityFunctions.getDistanceString(distanceTraveledMiles));
        if (distanceTraveledMiles > 0) {
            float pace = (int) Math.floor((seconds + (minutes * 60)) / distanceTraveledMiles);
            paceTextView.setText(UtilityFunctions.getPaceString(pace));
        }
    }

    /**
     * Starts a thread keeping track of time while workout is ongoing.
     */
    public Runnable runnable = new Runnable() {
        public void run() {
            millisecondTime = SystemClock.uptimeMillis() - startTime;
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
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
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
     * @param googleMap GoogleMap to use
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
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000); // Location update interval set to 2000 ms (2 seconds)
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
     * Called when screen orientation is changed to save variables.
     *
     * @param outState Instance state to save
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("TAG", (int) resumePauseButton.getTag());
        outState.putFloat("MILES", distanceTraveledMiles);
        outState.putFloat("METERS", distanceTraveledMeters);
        outState.putBoolean("BEGIN", begin);
        outState.putBoolean("PAUSED", paused);
        outState.putBoolean("STARTED", started);
        outState.putIntegerArrayList("TIMES", times);
        outState.putParcelableArrayList("LOCATIONS", locations);
        outState.putLong("TIMEBUFF", timeBuff);
        outState.putLong("STARTTIME", startTime);
        outState.putParcelable("CURR_LOC", currLocation);
        outState.putParcelable("LAST_LOC", lastLocation);
        super.onSaveInstanceState(outState);
    }

    /**
     * Called when the screen orientation is changed while the app is running.
     *
     * @param savedInstanceState instance state parameter from onCreate
     */
    private void restoreVariables(Bundle savedInstanceState) {
        resumePauseButton.setTag(savedInstanceState.getInt("TAG"));
        distanceTraveledMiles = savedInstanceState.getFloat("MILES");
        distanceTraveledMeters = savedInstanceState.getFloat("METERS");
        begin = savedInstanceState.getBoolean("BEGIN");
        paused = savedInstanceState.getBoolean("PAUSED");
        started = savedInstanceState.getBoolean("STARTED");
        locations = savedInstanceState.getParcelableArrayList("LOCATIONS");
        times = savedInstanceState.getIntegerArrayList("TIMES");
        currLocation = savedInstanceState.getParcelable("CURR_LOC");
        lastLocation = savedInstanceState.getParcelable("LAST_LOC");
        timeBuff = savedInstanceState.getLong("TIMEBUFF");
        startTime = savedInstanceState.getLong("STARTTIME");
        if (begin) {
            resumePauseButton.setText(getString(R.string.start));
        } else if ((int) resumePauseButton.getTag() == 1) {
            resumePauseButton.setText(getString(R.string.resume));
            setTimerAndDistance();
        } else {
            resumePauseButton.setText(getString(R.string.pause));
            handler.postDelayed(runnable, 0);
        }
    }

    /**
     * Called upon orientation change or viewing past workouts.
     * Re-draws PolyLines on map according to stored list of Latitudes and Longitudes.
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
    public void onBackPressed() {
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
                startHistoryActivity();
            }
        });
        if (!begin) {
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            startHistoryActivity();
        }
    }

    /**
     * Determined whether user has location tracking enabled.
     * If not, asks user to enable location tracking or exits.
     */
    public void checkGPSandNetwork() {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            Log.e("RunActivity", ex.toString());
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Log.e("RunActivity", ex.toString());
        }

        if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Please enable location tracking");
            dialog.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    startHistoryActivity();
                }
            });
            dialog.show();
        }
    }

    /**
     * Starts HistoryActivity and finishes RunActivity.
     */
    private void startHistoryActivity() {
        Intent historyActivity = new Intent(RunActivity.this, HistoryActivity.class);
        startActivity(historyActivity);
        finish();
    }

    /**
     * Initializes TextToSpeech once it is ready.
     *
     * @param status
     */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported");
            }
        }
    }

    /**
     * Checks if audio is enabled before reading the input CharSequence from TextToSpeech.
     *
     * @param stringToSpeak String for TextToSpeech to read.
     * @param tag           Tag for CharSequence.
     */
    private void speakIfEnabled(CharSequence stringToSpeak, String tag) {
        if (audioEnabled) {
            textToSpeech.speak(stringToSpeak, TextToSpeech.QUEUE_FLUSH, null, tag);
        }
    }
}

// TODO: Improve AlertDialog appearance in onBackPressed() and checkGPSandNetwork()
// TODO: Potentially check whether user enabled location tracking in checkGPSandNetwork()
// TODO: Potentially fix onDataChange function to not duplicate elements when new workout is stored

// TODO: Volume controls
// TODO: Potentially use alarm clock activity rather than mimic functionality.