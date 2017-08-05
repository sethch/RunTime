package com.android.seth.runningapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private Toolbar toolbar;
    private Button begin_button;
    private ListView workouts_list;
    private ArrayList<Workout> workout_data;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseListAdapter<Workout> workout_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        begin_button = (Button) findViewById(R.id.push_button);
        begin_button.setOnClickListener(this);
        workouts_list = (ListView) findViewById(R.id.profile_listView);
        workouts_list.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Workout clicked_workout = workout_data.get(position);
                Intent pastWorkout = new Intent(ProfileActivity.this, PastWorkoutActivity.class);
                ArrayList<LatLng> locations_parcelable = new ArrayList<>();
                ArrayList<Lat_Lng> locations = clicked_workout.getLocations();
                if(locations != null) {
                    for (Lat_Lng curr : locations) {
                        locations_parcelable.add(new LatLng(curr.getLatitude(), curr.getLongitude()));
                    }
                }
                pastWorkout.putExtra("WORKOUT_DURATION", clicked_workout.getDuration());
                pastWorkout.putExtra("WORKOUT_DATE", clicked_workout.getDate());
                pastWorkout.putParcelableArrayListExtra("WORKOUT_LOCATIONS", locations_parcelable);
                pastWorkout.putIntegerArrayListExtra("WORKOUT_TIMES", clicked_workout.getTimes());
                pastWorkout.putExtra("WORKOUT_DISTANCE", clicked_workout.getDistance_miles());
                ProfileActivity.this.startActivity(pastWorkout);
            }
        });
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        workout_data = new ArrayList<>();


        // TODO: Fix datachange function
        databaseReference.child("users").child(user.getUid()).child("workouts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> test_list = new ArrayList<>();
                int i = 1;
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Workout workout = ds.getValue(Workout.class);
                    workout_data.add(workout);
                    float distance_miles = workout.getDistance_miles();
                    int duration_seconds = workout.getDuration();
                    String distance_decimals = new DecimalFormat(".##").format((float)distance_miles);
                    String distance = String.valueOf((int)distance_miles) + distance_decimals;
                    String duration = String.valueOf(duration_seconds);
                    String combined = i + ": Distance: " + distance + " Miles Duration: " + duration + " seconds";
                    i++;
                    test_list.add(combined);
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ProfileActivity.this, android.R.layout.simple_list_item_1, test_list);
                workouts_list.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * This is called upon clicking one of the RegisterActivity links
     * @param v which view has been clicked
     */
    @Override
    public void onClick(View v) {
        if(v == begin_button){
            Intent startRun = new Intent(ProfileActivity.this, RunActivity.class);
            ProfileActivity.this.startActivity(startRun);
        }
    }
}

// TODO: Populate a ListView with past workouts from user's DB, display in background
// TODO: Handle location tracking off