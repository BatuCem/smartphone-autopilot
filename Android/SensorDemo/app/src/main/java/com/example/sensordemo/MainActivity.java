package com.example.sensordemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.Manifest;

public class MainActivity extends AppCompatActivity {

    private TextView textView_mag,textView_temp,textView_accel,textView_gyro,textView_hum;
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
        //Create sensorManager and define sensors we will read
        SensorManager sensorManager =(SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor_hum = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        Sensor sensor_mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor sensor_temp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        Sensor sensor_accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor sensor_gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

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
        int Sampling_Period = 1000000; // in us (1Hz)
        //Register event listeners to sensors from manager to a specific sampling period in us
        sensorManager.registerListener(sensorEventListenerHum,sensor_hum,Sampling_Period);
        sensorManager.registerListener(sensorEventListenerTemp,sensor_temp,Sampling_Period);
        sensorManager.registerListener(sensorEventListenerAccel,sensor_accel,Sampling_Period);
        sensorManager.registerListener(sensorEventListenerGyro,sensor_gyro,Sampling_Period);
        sensorManager.registerListener(sensorEventListenerMag,sensor_mag,Sampling_Period);
    }
}