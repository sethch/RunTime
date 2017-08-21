package com.android.seth.runningapp.util;

import java.util.ArrayList;

public class Workout {
    private ArrayList<Lat_Lng> locations;
    private ArrayList<Integer> times;
    private long date;
    private float distanceMiles;
    private int duration;

    public Workout() {

    }

    public Workout(ArrayList<Lat_Lng> locations, ArrayList<Integer> times, long date, float distanceMiles, int duration) {
        this.locations = locations;
        this.times = times;
        this.date = date;
        this.distanceMiles = distanceMiles;
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public float getDistanceMiles() {
        return distanceMiles;
    }

    public void setDistanceMiles(float distanceMiles) {
        this.distanceMiles = distanceMiles;
    }

    public long getDate() {
        return date;
    }

    public ArrayList<Lat_Lng> getLocations() {
        return locations;
    }

    public ArrayList<Integer> getTimes() {
        return times;
    }

    public void setLocations(ArrayList<Lat_Lng> locations) {
        this.locations = locations;
    }

    public void setTimes(ArrayList<Integer> times) {
        this.times = times;
    }
}