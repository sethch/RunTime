package com.android.seth.runningapp.util;

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
        int pace_minutes = (int) pace / 60;
        int pace_seconds = (int) pace % 60;
        return pace_minutes + new DecimalFormat(".##").format((float) pace_seconds / 60) + " min/mi";
    }

    /**
     * Converts int number of seconds to an Hours:Minutes:Seconds format
     *
     * @param totalSeconds total number of seconds
     * @return String in correct time format
     */
    public static String getTimeString(int totalSeconds) {
        String toReturn;
        int total_minutes = totalSeconds / 60;
        int hours = total_minutes / 60;
        int minutes = total_minutes % 60;
        int seconds = totalSeconds % 60;
        if (hours == 0) {
            toReturn = "Time: " + String.format(Locale.US, "%1$01d:%2$02d", minutes, seconds);
        } else {
            toReturn = "Time: " + String.format(Locale.US, "%1$01d:%2$02d:%3$02d", hours, minutes, seconds);
        }
        return toReturn;
    }

    /**
     * Converts float distance(miles) to a formatted string in format "x.xx miles".
     *
     * @param distance  Distance in miles.
     * @return  String in "x.xx miles" format.
     */
    public static String getDistanceString(float distance){
        int whole_number = (int) Math.floor(distance);
        int first_two_decimals = (int) Math.floor((distance - whole_number) * 100);
        return whole_number + "." + String.format(Locale.US, "%02d", first_two_decimals) + " miles";
    }

    /**
     * Converts a time in milliseconds to a formatted string in date format.
     *
     * @param timeInMilliseconds    Time in milliseconds to convert to a date.
     * @return  String in date format.
     */
    public static String getDateString(long timeInMilliseconds){
        Date date = new Date(timeInMilliseconds);
        SimpleDateFormat format = new SimpleDateFormat("mm-dd-yyyy hh:mm a", Locale.US);
        return format.format(date);
    }
}
