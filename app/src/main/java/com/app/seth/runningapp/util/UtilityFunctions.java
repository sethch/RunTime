package com.app.seth.runningapp.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class UtilityFunctions {

    /**
     * Returns formatted string representing the pace in min/mile of a workout.
     *
     * @param pace The number of seconds per mile.
     * @return Formatted string representing pace in min/mile.
     */
    public static String getPaceString(float pace) {
        int paceMinutes = (int) pace / 60;
        int paceSeconds = (int) pace % 60;
        return paceMinutes + new DecimalFormat(".##").format((float) paceSeconds / 60) + " min/mi";
    }

    /**
     * Converts int number of seconds to an Hours:Minutes:Seconds format
     *
     * @param totalSeconds total number of seconds
     * @return String in correct time format
     */
    public static String getTimeString(int totalSeconds) {
        String toReturn;
        int totalMinutes = totalSeconds / 60;
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        int seconds = totalSeconds % 60;
        if (hours == 0) {
            toReturn = String.format(Locale.US, "%1$01d:%2$02d", minutes, seconds);
        } else {
            toReturn = String.format(Locale.US, "%1$01d:%2$02d:%3$02d", hours, minutes, seconds);
        }
        return toReturn;
    }

    /**
     * Converts float distance(miles) to a formatted string in format "x.xx miles".
     *
     * @param distance Distance in miles.
     * @return String in "x.xx miles" format.
     */
    public static String getDistanceString(float distance) {
        int wholeNumber = (int) Math.floor(distance);
        int firstTwoDecimals = (int) Math.floor((distance - wholeNumber) * 100);
        return wholeNumber + "." + String.format(Locale.US, "%02d", firstTwoDecimals) + " miles";
    }

    /**
     * Converts a time in milliseconds to a formatted string in date format.
     *
     * @param timeInMilliseconds Time in milliseconds to convert to a date.
     * @return String in date format.
     */
    public static String getDateString(long timeInMilliseconds) {
        Date date = new Date(timeInMilliseconds);
        SimpleDateFormat format = new SimpleDateFormat("mm-dd-yyyy \thh:mm a", Locale.US);
        return format.format(date);
    }

    /**
     * Reports current workout status as a String for TextToSpeech.
     *
     * @param pace                  Current pace in sec/mile.
     * @param distanceTraveledMiles Distance traveled in miles.
     * @param minutes               Minutes elapsed.
     * @param seconds               Seconds elapsed.
     * @return Workout status.
     */
    public static String getWorkoutStatusString(float pace, float distanceTraveledMiles, int minutes, int seconds) {
        int paceSeconds;
        int paceMinutes;
        if (distanceTraveledMiles > 0.1) {
            paceSeconds = (int) pace % 60;
            paceMinutes = (int) pace / 60;
        } else {
            paceSeconds = 0;
            paceMinutes = 0;
        }
        return "Time: " + minutes + " minutes, " + seconds + " seconds. Distance: " + getDistanceString(distanceTraveledMiles) + ". Pace: " + paceMinutes + "minutes " + paceSeconds + " seconds per mile";
    }
}
