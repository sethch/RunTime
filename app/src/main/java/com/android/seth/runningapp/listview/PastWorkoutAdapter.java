package com.android.seth.runningapp.listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.seth.runningapp.R;

import java.util.ArrayList;

/**
 * Created by Seth on 8/8/2017.
 */

public class PastWorkoutAdapter extends ArrayAdapter<PastWorkout> {
    public PastWorkoutAdapter(Context context, ArrayList<PastWorkout> pastWorkout){
        super(context, 0, pastWorkout);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        PastWorkout pastWorkout = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.history_listview, parent, false);
        }

        TextView test = (TextView) convertView.findViewById(R.id.history_list_string);
        if(pastWorkout != null) {
            test.setText(pastWorkout.getToDisplay());
        }
        ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.delete_btn);
        imageButton.setFocusable(false);
        return convertView;
    }
}