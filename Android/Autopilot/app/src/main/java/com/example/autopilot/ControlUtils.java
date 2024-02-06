package com.example.autopilot;

import android.util.Log;

public class ControlUtils {
//Class to implement controller utilities
    private static final String TAG = "ControlUtils"; //TAG for logging
    public static String commandIntegers (int left, int right)
    {   //Method to implement creating a string command from left/right motor speeds
        String leftString = (left >= 0 ? "+" : "-") + String.format("%03d", Math.abs(left));
        String rightString = (right >= 0 ? "+" : "-") + String.format("%03d", Math.abs(right));
        Log.i(TAG, "commandIntegers: "+leftString+rightString);//format"+017-195" type command
        return leftString+rightString;
    }
}
