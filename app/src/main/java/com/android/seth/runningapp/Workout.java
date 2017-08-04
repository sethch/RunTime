package com.android.seth.runningapp;

import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Seth on 4/19/2017.
 */

public class Workout {
    private ArrayList<Lat_Lng> locations;
    private ArrayList<Integer> times;
    private String date;
    private float distance_miles;
    private int duration;

    public Workout(){

    }

    public Workout(ArrayList<Lat_Lng> locations, ArrayList<Integer> times, String date, float distance_miles, int duration){
        this.locations = locations;
        this.times = times;
        this.date = date;
        this.distance_miles = distance_miles;
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getDistance_miles() {
        return distance_miles;
    }

    public void setDistance_miles(float distance_miles) {
        this.distance_miles = distance_miles;
    }

    public String getDate() {

        return date;
    }

    public ArrayList<Lat_Lng> getLocations(){
        return locations;
    }

    public ArrayList<Integer> getTimes(){
        return times;
    }

    public void setLocations(ArrayList<Lat_Lng> locations){
        this.locations = locations;
    }

    public void setTimes(ArrayList<Integer> times){
        this.times = times;
    }
}