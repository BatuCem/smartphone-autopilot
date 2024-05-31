package com.example.autopilot;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ControlUtils {
//Class to implement controller utilities
    private static final String TAG = "ControlUtils"; //TAG for logging

    private static List<Float> centerMemory= new ArrayList<>(), areaMemory = new ArrayList<>();
    private static List<Float> distanceMemory= new ArrayList<>(), angleMemory = new ArrayList<>();
    private static float centerAccumulator = 0, areaAccumulator=0;
    private static float angleAccumulator = 0;
    public static String commandIntegers (int left, int right)
    {   //Method to implement creating a string command from left/right motor speeds
        if(SettingsActivity.driveMode == 0)
        {
            String leftString = (left >= 0 ? "+" : "-") + String.format("%03d", Math.abs(left));
            String rightString = (right >= 0 ? "+" : "-") + String.format("%03d", Math.abs(right));
            Log.i(TAG, "commandIntegers 4wd: "+leftString+rightString);//format"+017-195" type command
            return leftString+rightString+"\n";
        }
        else {
            //TODO: Make sure of operation
            int angle = Math.min(Math.max(80,90*(right-left)/(2*255) + 100),120);
            int speed = ((left+right)/2)%255;
            String angleString = (angle >= 0 ? "+" : "-") + String.format("%03d",Math.abs(angle));
            String speedString = (speed == 0 ? "+000" : (speed > 0 ? "+255" : "-255"));
            Log.i(TAG, "commandIntegers steering: "+angleString+speedString);//format"+017-195" type command
            return angleString+speedString+"\n";

        }
    }
    public static int[] inferWifiCommands(int minPwm,int maxPwm,int maxCentering, int maxAreaControl,int centerScaler,int areaScaler, Double[] controlParams)
    {
        Double[] controlParamsCenter = controlParams;
        Double[] controlParamsArea = controlParams;
        if(centerMemory.size()==0 || areaMemory.size()==0)
        {
            centerMemory.add(0f);
            areaMemory.add(0f);
        }
        int[] commands= new int[2];
        commands[0]=0;
        commands[1]=0;
        float errorCenter = 0.5f - ImageUtils.rectXCenter;
        addFloatToList(errorCenter,centerMemory,2);
        centerAccumulator+=errorCenter;
        Double centering = (errorCenter*controlParamsCenter[0] + centerAccumulator*controlParamsCenter[1] + (centerMemory.get(0)-centerMemory.get(1))*controlParamsCenter[2])*centerScaler;
        centering=Math.min(Math.max(-maxCentering,centering),maxCentering);
        Log.i(TAG, "SpeedCtrl centering: "+Double.toString(centering));
        commands[0]=(int)(-centering.intValue());
        commands[1]=(int)(centering.intValue());


        float errorArea=0.25f-ImageUtils.rectArea;
        addFloatToList(errorArea,areaMemory,2);
        areaAccumulator+=errorArea;
        Double speedCtrl = (errorArea*controlParamsArea[0] + areaAccumulator*controlParamsArea[1] + (areaMemory.get(0)- areaMemory.get(1))*controlParamsArea[2])*areaScaler;
        speedCtrl=Math.min(Math.max(-maxAreaControl,speedCtrl),maxAreaControl);
        Log.i(TAG, "SpeedCtrl area: "+"Error: "+errorArea + "Integral: "+areaAccumulator + "Derivative: " + (areaMemory.get(0)- areaMemory.get(1)));
        Log.i(TAG, "SpeedCtrl center: "+"Error: "+errorCenter + "Integral: "+centerAccumulator + "Derivative: " + (centerMemory.get(0)- centerMemory.get(1)));
        commands[0]+=(int)speedCtrl.intValue();
        commands[1]+=(int)speedCtrl.intValue();
        if(Math.abs(commands[0]) <=minPwm)
        {
            commands[0]=0;
        }
        if(Math.abs(commands[1])<=minPwm)
        {
            commands[1]=0;
        }
        commands[0]=Math.min(Math.max(-maxPwm,commands[0]),maxPwm);
        commands[1]=Math.min(Math.max(-maxPwm,commands[1]),maxPwm);
        Log.i(TAG, "inferWifiCommands: " + commands);
        return commands;
    }
    public static int[] inferWifiCommandsForRotation (double targetAngle, double currentAngle,double distance, int minPwm, int maxPwm, double[] controlParamsAngle)
    {
        //TODO: make sure that the multiplexed PID is working as intended
        if(angleMemory.size()==0)
        {
            angleMemory.add(0f);
        }
        double errorAngle = currentAngle- targetAngle;
        Log.i(TAG, "inferWifiCommandsForRotation: angle read: "+currentAngle);
        int[] commands= new int[2];
        double controller;
        if (distance <=5)
    {
        commands[0] = 0;
        commands[1] = 0;
        return commands;
    }
        Log.i(TAG, "inferWifiCommandsForRotation: " + errorAngle);
        if(errorAngle<=180)
        {
            angleAccumulator = Math.min(Math.max(-2048,(float) errorAngle + angleAccumulator),2048);
            addFloatToList((float)errorAngle,angleMemory,2);
            controller =255*(controlParamsAngle[0]*errorAngle/180
                    + controlParamsAngle[1]*angleAccumulator
                    + controlParamsAngle[2]*(angleMemory.get(0) - angleMemory.get(1)));
            Log.i(TAG, "SpeedCtrl area: "+"Error: "+errorAngle + "Integral: "+angleAccumulator + "Derivative: " + (angleMemory.get(0)- angleMemory.get(1)));

        }
        else
        {
            angleAccumulator = Math.min(Math.max(-2048,(float) errorAngle + angleAccumulator - 360),2048);
            addFloatToList((float) (errorAngle - 360.0),angleMemory,2);
            controller =255*(controlParamsAngle[0]*(errorAngle - 360)/180
                    + controlParamsAngle[1]*angleAccumulator
                    + controlParamsAngle[2]*(angleMemory.get(0) - angleMemory.get(1)));
            Log.i(TAG, "SpeedCtrl area: "+"Error: "+errorAngle + "Integral: "+angleAccumulator + "Derivative: " + (angleMemory.get(0)- angleMemory.get(1)));
        }
        commands[0]=(int)-controller;
        commands[1]=(int)controller;
        if(Math.abs(controller) <=minPwm)
        {
            commands[0]=200;
            commands[1]=200;
        }

        commands[0]=Math.min(Math.max(-maxPwm,commands[0]),maxPwm);
        commands[1]=Math.min(Math.max(-maxPwm,commands[1]),maxPwm);
        return commands;



    }
    private static void addFloatToList(Float floatToList,List<Float> floatList, int maxListSize) {
        if (floatList.size() >= maxListSize) {
            floatList.remove(0); // Remove the oldest entry
        }
        floatList.add(floatToList);
    }
}
