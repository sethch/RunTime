package com.android.seth.runningapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.android.seth.runningapp.listview.PastWorkout;
import com.android.seth.runningapp.listview.PastWorkoutAdapter;
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

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private Toolbar toolbar;
    private Button begin_button;
    private ArrayList<Workout> workout_data;
    private ArrayList<PastWorkout> past_workout_list;

    private ListView listView;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseListAdapter<Workout> workout_adapter;
    private ProgressDialog progressDialog;
    private PastWorkoutAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        progressDialog = new ProgressDialog(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        begin_button = (Button) findViewById(R.id.push_button);
        begin_button.setOnClickListener(this);
        listView = (ListView) findViewById(R.id.profile_listView);

        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Workout clicked_workout = past_workout_list.get(position).getWorkout();
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

        progressDialog.setMessage("Loading past workouts...");
        progressDialog.show();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        past_workout_list = new ArrayList<PastWorkout>();
        adapter = new PastWorkoutAdapter(this, past_workout_list);
        listView.setAdapter(adapter);

        databaseReference.child("users").child(user.getUid()).child("workouts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 1;
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Workout workout = ds.getValue(Workout.class);
                    float distance_miles = workout.getDistance_miles();
                    int duration_seconds = workout.getDuration();;
                    String workout_time_string = getTime(duration_seconds);
                    String combined = i + ": Distance: " + new DecimalFormat("#.##").format(distance_miles) + " Miles Duration: " + workout_time_string;

                    PastWorkout pastWorkout = new PastWorkout(combined, workout);
                    adapter.add(pastWorkout);
                    i++;
                }
                //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ProfileActivity.this, android.R.layout.simple_list_item_1, test_list);

                progressDialog.dismiss();
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
            to_return = String.format("%1$01d:%2$02d", minutes, seconds);
        }
        else{
            to_return = String.format("%1$01d:%2$02d:%3$02d", hours, minutes, seconds);
        }
        return to_return;
    }
}

// TODO: Consider menu with start/past workouts seperate
// TODO: Explore multi-threading
// TODO: Improve code quality
// TODO: improve listview appearance
// TODO: add delete button functionality
// TODO: unify variable name style