package com.android.seth.runningapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.android.seth.runningapp.util.UtilityFunctions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PastWorkoutActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mGoogleMap;
    private ArrayList<LatLng> locations;
    private ArrayList<Integer> times;
    private ArrayList<Marker> markers;

    private float pace;

    private TextView distanceStat;
    private TextView paceStat;
    private TextView timeStat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        markers = new ArrayList<>();
        initializeLayout();
        if (googleServicesAvailable()) {
            initMap();
        }
        Intent intent = getIntent();
        float distanceMiles = intent.getFloatExtra("WORKOUT_DISTANCE", 0f);
        int duration = intent.getIntExtra("WORKOUT_DURATION", 0);
        locations = intent.getParcelableArrayListExtra("WORKOUT_LOCATIONS");
        times = intent.getIntegerArrayListExtra("WORKOUT_TIMES");
        pace = intent.getFloatExtra("WORKOUT_PACE", 0f);
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

        if (locations.size() > 0) {
            redrawPolyLines(locations);
            placeMarkers();
            initializeZoom();
        }
    }

    /**
     * Zooms to workout location to just cover the size of the workout.
     */
    private void initializeZoom() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 100;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mGoogleMap.animateCamera(cameraUpdate);
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
    private void redrawPolyLines(ArrayList<LatLng> location_array) {
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
     * Checks if current device has Google Play services APK up to date.
     *
     * @return true if the user has Google Play services APK up to date
     * false otherwise
     */
    private boolean googleServicesAvailable() {
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
        if (locations != null) {
            markers.add(mGoogleMap.addMarker(new MarkerOptions()
                    .position(locations.get(0))
                    .title("Start")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
            );
            markers.add(mGoogleMap.addMarker(new MarkerOptions()
                    .position(locations.get(locations.size() - 1))
                    .title("End")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
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
            if (dist > 0) {
                paceStat.setText(UtilityFunctions.getPaceString(pace));
            }
        }
        int i = 1;
        for (Integer index : mile_markers) {
            int totalSeconds = times.get(index);
            String time = "Time: " + UtilityFunctions.getTimeString(totalSeconds);
            markers.add(mGoogleMap.addMarker(new MarkerOptions()
                    .position(locations.get(index))
                    .title("Mile " + i + " At " + time))
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