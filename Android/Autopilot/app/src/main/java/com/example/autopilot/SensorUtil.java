package com.example.autopilot;

import static android.content.Context.CAMERA_SERVICE;
import static android.content.Context.LOCATION_SERVICE;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.FusedOrientationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SensorUtil {

    private static final String TAG = "SensorUtil";
    private static SensorManager sensorManager;
    private static int SamplingPeriod = 1000000; //in us
    public static boolean torchCondition, proximityCondition;
    public static double rotation;
    public static double[] GPSData= new double[6];

    public static double bearingGPS, distanceGPS;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public SensorUtil(Context context)
    {
        sensorManager= (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensorLight= sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        Sensor sensorProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        Sensor sensorRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
                .setIntervalMillis(1000)
                .setMinUpdateIntervalMillis(100)
                .build();

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
        SensorEventListener sensorEventListenerRotation = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                double theta,xRot,yRot,zRot;
                //float[] rotationMatrix = new float[9];
                //SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                //float[] orientationAngles = new float[3];
                //SensorManager.getOrientation(rotationMatrix, orientationAngles);
                //rotation = orientationAngles[0];
                theta = event.values[3];
                xRot = event.values[0];
                yRot = event.values[1];
                zRot = event.values[2];
                rotation = (-(180/Math.PI*Math.atan2(2*(theta*zRot - xRot*yRot), 1 - 2* (xRot*xRot + zRot*zRot)) -90)+360)%360; //rotation in the ground plane
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                GPSData[0] = location.getLatitude();
                GPSData[1] = location.getLongitude();
                GPSData[2] = location.getAltitude();
                GPSData[3] = location.getSpeed();
                GPSData[4] = location.getBearing(); //TODO:MAKE SURE of the parameter
                GPSData[5] = location.getAccuracy();
                if(MapActivity.targetLatitude != null && MapActivity.targetLongitude !=null)
                {
                    double orgLatRad = location.getLatitude()*Math.PI/180;
                    double orgLonRad = location.getLongitude()*Math.PI/180;
                    double tarLatRad = MapActivity.targetLatitude*Math.PI/180;
                    double tarLonRad = MapActivity.targetLongitude*Math.PI/180;
                    bearingGPS = (180/Math.PI*Math.atan2(Math.sin(tarLonRad - orgLonRad)*Math.cos(tarLatRad)
                            , Math.cos(orgLatRad)*Math.sin(tarLatRad) - Math.sin(orgLatRad)*Math.cos(tarLatRad)* Math.cos(tarLonRad - orgLonRad))+360)%360;
                    Log.i(TAG, "bearing on GPS: "+ bearingGPS);
                    double a = Math.sin((tarLatRad-orgLatRad)/2)*Math.sin((tarLatRad-orgLatRad)/2)
                            + Math.cos(orgLatRad)*Math.cos(tarLatRad)
                            *Math.sin((tarLonRad - orgLonRad)/2)*Math.sin((tarLonRad - orgLonRad)/2);
                    double c = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
                    distanceGPS = 6371000 * c;
                    Log.i(TAG, "distance on GPS: "+ distanceGPS);
                }
                else
                {
                    bearingGPS=0;
                    distanceGPS=0; //40000000 circ. of earth for worst case reseting
                }
            }
        };

        sensorManager.registerListener(sensorEventListenerLight,sensorLight,SamplingPeriod);
        sensorManager.registerListener(sensorEventListenerProximity,sensorProximity,SamplingPeriod);
        sensorManager.registerListener(sensorEventListenerRotation,sensorRotationVector,SamplingPeriod);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,executorService,locationListener);
        Log.i(TAG, "SensorUtil: success ");
    }
}
