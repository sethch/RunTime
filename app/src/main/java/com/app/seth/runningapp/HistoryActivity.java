package com.app.seth.runningapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.app.seth.runningapp.R;
import com.app.seth.runningapp.pastworkout.PastWorkout;
import com.app.seth.runningapp.pastworkout.PastWorkoutAdapter;
import com.app.seth.runningapp.util.Lat_Lng;
import com.app.seth.runningapp.util.UtilityFunctions;
import com.app.seth.runningapp.util.Workout;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private ArrayList<PastWorkout> pastWorkoutList;
    private ProgressDialog progressDialog;
    private PastWorkoutAdapter adapter;
    private DatabaseReference databaseReference;
    private FirebaseUser user;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseDatabase instance = FirebaseDatabase.getInstance();
        databaseReference = instance.getReference();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        initializeLayout();
        progressDialog = new ProgressDialog(this);
        setListViewOnClick(listView);
        pastWorkoutList = new ArrayList<>();
        adapter = new PastWorkoutAdapter(this, pastWorkoutList);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        listView.setAdapter(adapter);
        populateListView();
    }

    /**
     * Initializes layout and views for HistoryActivity.
     */
    private void initializeLayout() {
        setContentView(R.layout.activity_history);
        listView = (ListView) findViewById(R.id.history_listView);
    }

    /**
     * Sets onItemClick for each listView item. Clicking on a listView
     * item will call pastWorkoutActivity and send workout information.
     *
     * @param listView the listView to set onClickListener for
     */
    private void setListViewOnClick(ListView listView) {
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Workout clicked_workout = pastWorkoutList.get(position).getWorkout();
                Intent pastWorkout = new Intent(HistoryActivity.this, PastWorkoutActivity.class);
                ArrayList<LatLng> locations_parcelable = new ArrayList<>();
                ArrayList<Lat_Lng> locations = clicked_workout.getLocations();
                if (locations != null) {
                    for (Lat_Lng curr : locations) {
                        locations_parcelable.add(new LatLng(curr.getLatitude(), curr.getLongitude()));
                    }
                }
                pastWorkout.putExtra("WORKOUT_DURATION", clicked_workout.getDuration());
                pastWorkout.putExtra("WORKOUT_DATE", clicked_workout.getDate());
                pastWorkout.putParcelableArrayListExtra("WORKOUT_LOCATIONS", locations_parcelable);
                pastWorkout.putIntegerArrayListExtra("WORKOUT_TIMES", clicked_workout.getTimes());
                pastWorkout.putExtra("WORKOUT_DISTANCE", clicked_workout.getDistanceMiles());
                pastWorkout.putExtra("WORKOUT_PACE", clicked_workout.getPace());
                HistoryActivity.this.startActivity(pastWorkout);
                finish();
            }
        });
    }

    /**
     * Populates the ListView by pulling workout data from database,
     * formatting each into a string and adding to the PastWorkoutAdapter.
     */
    private void populateListView() {
        progressDialog.setMessage("Loading past workouts...");
        progressDialog.show();
        if (user != null) {
            databaseReference.child("users").child(user.getUid()).child("workouts").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    adapter.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Workout workout = ds.getValue(Workout.class);
                        if (workout != null) {
                            float distanceMiles = workout.getDistanceMiles();
                            int durationSeconds = workout.getDuration();
                            String formattedTimeString = "Time: " + UtilityFunctions.getTimeString(durationSeconds);
                            long workoutDateInMilliseconds = workout.getDate();
                            String formattedDate = UtilityFunctions.getDateString(workoutDateInMilliseconds);
                            String formattedDistance = UtilityFunctions.getDistanceString(distanceMiles);
                            String combined = " " + formattedDistance + " " + formattedTimeString + "\n " + formattedDate;
                            PastWorkout pastWorkout = new PastWorkout(combined, workout, ds.getKey());
                            adapter.add(pastWorkout);
                        }
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}

// TODO: Explore multi-threading (maybe RxJava)