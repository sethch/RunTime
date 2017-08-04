package com.android.seth.runningapp;

/**
 * Created by Seth on 4/19/2017.
 */

public class Lat_Lng {
    private double latitude;
    private double longitude;

    public Lat_Lng(){

    }

    public Lat_Lng(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }
}