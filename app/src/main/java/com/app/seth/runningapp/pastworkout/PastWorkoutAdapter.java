package com.app.seth.runningapp.pastworkout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.app.seth.runningapp.R;
import com.app.seth.runningapp.util.UtilityFunctions;
import com.app.seth.runningapp.util.Workout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PastWorkoutAdapter extends ArrayAdapter<PastWorkout> {
    private DatabaseReference databaseReference;
    private FirebaseUser user;
    private ArrayList<PastWorkout> pastWorkoutArrayList;

    public PastWorkoutAdapter(Context context, ArrayList<PastWorkout> pastWorkoutArrayList) {
        super(context, 0, pastWorkoutArrayList);
        this.pastWorkoutArrayList = pastWorkoutArrayList;
        FirebaseDatabase instance = FirebaseDatabase.getInstance();
        databaseReference = instance.getReference();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
    }

    /**
     * Initializes ListView items with layout and sets onClick listener for delete ImageButtons.
     *
     * @param position    Position of ListView item to initialize.
     * @param convertView Old view to reuse if needed.
     * @param parent      Parent that this view will be attached to.
     * @return ListView item View.
     */
    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        final PastWorkout pastWorkout = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_history, parent, false);
        }
        TextView dateTextView = (TextView) convertView.findViewById(R.id.history_list_date);
        TextView timeTextView = (TextView) convertView.findViewById(R.id.history_list_time);
        TextView distanceTextView = (TextView) convertView.findViewById(R.id.history_list_distance);
        TextView paceTextView = (TextView) convertView.findViewById(R.id.history_list_pace);
        ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.delete_btn);
        imageButton.setFocusable(false);
        if (pastWorkout != null) {
            Workout workout = pastWorkout.getWorkout();
            String dateString = UtilityFunctions.getDateString(workout.getDate());
            String timeString = UtilityFunctions.getTimeString(workout.getDuration());
            String distanceString = UtilityFunctions.getDistanceString(workout.getDistanceMiles());
            String paceString = UtilityFunctions.getPaceString(workout.getPace());
            dateTextView.setText(dateString);
            timeTextView.setText(timeString);
            distanceTextView.setText(distanceString);
            paceTextView.setText(paceString);
            imageButton.setOnClickListener(new View.OnClickListener() {
                /**
                 * Removes selected workout from HistoryActivity and database.
                 * @param view  Clicked view of ListView
                 */
                @Override
                public void onClick(View view) {
                    databaseReference.child("users").child(user.getUid()).child("workouts").child(pastWorkout.getKey()).removeValue();
                    Toast.makeText(getContext(), "Removed Workout", Toast.LENGTH_SHORT).show();
                    pastWorkoutArrayList.remove(position);
                    notifyDataSetChanged();
                }

            });
        }
        return convertView;
    }
}