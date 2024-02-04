package com.example.sensordemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.Manifest;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity {
    int Sampling_Period = 1000000; // in us (1Hz)
    private TextView textView_mag,textView_temp,textView_accel,textView_gyro,textView_hum,textView_gps,textView_prox,textView_ill;
    private GraphView graphView;
    private int REQUEST_PERMISSIONS_CODE=200;
    public int sampleCounter=0;
    public int maxSampleCount=10;
    public LineGraphSeries<DataPoint> accYSeries;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //define textViews for each sensor reading
        textView_mag=findViewById(R.id.textView_mag);
        textView_temp=findViewById(R.id.textView_temp);
        textView_gyro=findViewById(R.id.textView_gyro);
        textView_accel=findViewById(R.id.textView_accel);
        textView_hum=findViewById(R.id.textView_hum);
        textView_gps=findViewById(R.id.textView_gps);
        textView_prox=findViewById(R.id.textView_prox);
        textView_ill=findViewById(R.id.textView_light);
        graphView=findViewById(R.id.graph);
        //check and ask permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted; request it from the user
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_CODE);
        } else {
            // Permission is already granted; you can proceed with GPS access
        }
        //Create sensorManager and define sensors we will read
        SensorManager sensorManager =(SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor_hum = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        Sensor sensor_mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor sensor_temp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        Sensor sensor_accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor sensor_gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor sensor_prox = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        Sensor sensor_light= sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        //Setup GPS and plotter
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        accYSeries=new LineGraphSeries<>();
        graphView.addSeries(accYSeries);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                textView_gps.setText("GPS Latitude "+ location.getLatitude());
                accYSeries.appendData(new DataPoint(sampleCounter,location.getAltitude()),true,maxSampleCount);
                sampleCounter++;


            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,Sampling_Period/1000,0,locationListener);

        SensorEventListener sensorEventListenerProx = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                textView_prox.setText("Proximity "+event.values[0]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        SensorEventListener sensorEventListenerLight = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                textView_ill.setText("LUX "+event.values[0]);

                CameraManager cameraManager = ( CameraManager)getSystemService(CAMERA_SERVICE);
                try {
                    if (event.values[0] < 30) {
                        cameraManager.setTorchMode("0", true);
                    } else {
                        cameraManager.setTorchMode("0", false);
                    }
                } catch ( CameraAccessException e)
                {

                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        //Define each sensor event listener to set textView texts
        SensorEventListener sensorEventListenerHum = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                textView_hum.setText("Humidity"+event.values[0]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        SensorEventListener sensorEventListenerMag = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                textView_mag.setText("Magnetic Field"+"X:" + event.values[0]+",Y:"+event.values[1]+",Z"+event.values[2]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        SensorEventListener sensorEventListenerTemp = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                textView_temp.setText("Temperature"+event.values[0]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        SensorEventListener sensorEventListenerAccel = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                textView_accel.setText("Accel X:"+event.values[0]+", Y:"+event.values[1]+", Z:" + event.values[2]);

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        SensorEventListener sensorEventListenerGyro = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                textView_gyro.setText("Gyro X:"+event.values[0]+", Y:"+event.values[1]+", Z:" + event.values[2]);

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        //Register event listeners to sensors from manager to a specific sampling period in us
        sensorManager.registerListener(sensorEventListenerHum,sensor_hum,Sampling_Period);
        sensorManager.registerListener(sensorEventListenerTemp,sensor_temp,Sampling_Period);
        sensorManager.registerListener(sensorEventListenerAccel,sensor_accel,Sampling_Period);
        sensorManager.registerListener(sensorEventListenerGyro,sensor_gyro,Sampling_Period);
        sensorManager.registerListener(sensorEventListenerMag,sensor_mag,Sampling_Period);
        sensorManager.registerListener(sensorEventListenerLight,sensor_light,Sampling_Period);
        sensorManager.registerListener(sensorEventListenerProx,sensor_prox,Sampling_Period);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted; you can proceed with GPS access
            } else {
                // Permission is denied; handle the case where the user declines permission
            }
        }

    }
}