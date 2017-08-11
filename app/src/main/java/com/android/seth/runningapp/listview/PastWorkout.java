package com.android.seth.runningapp.listview;

import com.android.seth.runningapp.Workout;

/**
 * Created by Seth on 8/10/2017.
 */

public class PastWorkout {
    private String toDisplay;
    private Workout workout;

    public PastWorkout(String toDisplay, Workout workout){
        this.toDisplay= toDisplay;
        this.workout = workout;
    }

    public String getToDisplay(){
        return toDisplay;
    }

    public Workout getWorkout() {
        return workout;
    }
}
