package com.example.autopilot;

public class ControlUtils {

    public static String commandIntegers (int a, int b)
    {
        String aStr = (a >= 0 ? "+" : "-") + String.format("%03d", Math.abs(a));
        String bStr = (b >= 0 ? "+" : "-") + String.format("%03d", Math.abs(b));
        return aStr + bStr;
    }
}
