package com.android.seth.runningapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.android.seth.runningapp.listview.PastWorkout;
import com.android.seth.runningapp.listview.PastWorkoutAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private Button beginButton;
    private ArrayList<PastWorkout> pastWorkoutList;
    private ProgressDialog progressDialog;
    private PastWorkoutAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Date d = new Date();
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        beginButton = (Button) findViewById(R.id.push_button);
        beginButton.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        ListView listView = (ListView) findViewById(R.id.profile_listView);
        setListViewOnClick(listView);
        pastWorkoutList = new ArrayList<>();
        adapter = new PastWorkoutAdapter(this, pastWorkoutList);
        listView.setAdapter(adapter);
        populateListView();
    }

    /**
     * Sets onItemClick for each listView item. Clicking on a listView
     * item will call pastWorkoutActivity and send workout information.
     * @param listView  the listView to set onClickListener for
     */
    private void setListViewOnClick(ListView listView) {
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Workout clicked_workout = pastWorkoutList.get(position).getWorkout();
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
                pastWorkout.putExtra("WORKOUT_DISTANCE", clicked_workout.getDistanceMiles());
                ProfileActivity.this.startActivity(pastWorkout);
                finish();
            }
        });
    }

    /**
     * Populates the ListView by pulling workout data from database,
     * formatting each into a string and adding to the PastWorkoutAdapter.
     * TODO: verify connection
     */
    private void populateListView() {
        FirebaseDatabase instance = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = instance.getReference();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        progressDialog.setMessage("Loading past workouts...");
        progressDialog.show();
        if(user != null) {
            databaseReference.child("users").child(user.getUid()).child("workouts").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int i = 1;
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Workout workout = ds.getValue(Workout.class);
                        float distanceMiles = workout.getDistanceMiles();
                        int durationSeconds = workout.getDuration();
                        String workoutTimeString = getTime(durationSeconds);
                        String combined = " " + new DecimalFormat("#.##").format(distanceMiles) + " mi Time: " + workoutTimeString + "\n " + workout.getDate();
                        PastWorkout pastWorkout = new PastWorkout(combined, workout);
                        adapter.add(pastWorkout);
                        i++;
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * This is called upon clicking one of the RegisterActivity links
     * @param v which view has been clicked
     */
    @Override
    public void onClick(View v) {
        if(v == beginButton){
            Intent startRun = new Intent(ProfileActivity.this, RunActivity.class);
            ProfileActivity.this.startActivity(startRun);
        }
    }

    /**
     * Converts int number of seconds to an Hours:Minutes:Seconds format
     * @param total_seconds total number of seconds
     * @return String in correct time format
     */
    private String getTime(int total_seconds){
        String to_return;
        int total_minutes = total_seconds / 60;
        int hours = total_minutes / 60;
        int minutes = total_minutes % 60;
        int seconds = total_seconds % 60;
        if(hours == 0){
            to_return = String.format(Locale.US, "%1$01d:%2$02d", minutes, seconds);
        }
        else{
            to_return = String.format(Locale.US, "%1$01d:%2$02d:%3$02d", hours, minutes, seconds);
        }
        return to_return;
    }
}

// TODO: Consider menu with start/past workouts seperate
// TODO: Explore multi-threading
// TODO: improve listview appearance
// TODO: add delete button functionality
// TODO: use runkeeper android listview for inspiration
// TODO: Instead of date use time from present