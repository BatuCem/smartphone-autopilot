package com.example.autopilot;

import static android.content.Context.CAMERA_SERVICE;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.util.Log;

public class SensorUtil {

    private static final String TAG = "SensorUtil";
    private static SensorManager sensorManager;
    private static int SamplingPeriod = 1000000; //in us
    public static boolean torchCondition, proximityCondition;
    public SensorUtil(Context context)
    {
        sensorManager= (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensorLight= sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        Sensor sensorProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        SensorEventListener sensorEventListenerLight = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if(torchCondition==true)
                {
                    torchCondition = (event.values[0] < SettingsActivity.flashThreshold*1.1);
                }
                else
                {
                    torchCondition = (event.values[0] < SettingsActivity.flashThreshold*0.9);
                }
                ImageCaptureManager.flashCameraControl(0,torchCondition);
                Log.i(TAG, "onSensorChanged: light "+ event.values[0]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        SensorEventListener sensorEventListenerProximity = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                proximityCondition = event.values[0] >2.5;
                Log.i(TAG, "onSensorChanged: prox "+ event.values[0]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(sensorEventListenerLight,sensorLight,SamplingPeriod);
        sensorManager.registerListener(sensorEventListenerProximity,sensorProximity,SamplingPeriod);
        Log.i(TAG, "SensorUtil: success ");
    }
}
