package com.example.autopilot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InfoActivity extends AppCompatActivity {
    //Info activity display
    //TODO: Implement with battery info and such...
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Handler infoHandler = new Handler();
    private TextView BatteryVoltageView, BatteryPowerView, AngleLiDARView, DistanceLiDARView,
            DistanceUltrasonicView, CameraFPSView, RotationAngleView, GPSLatitudeView, GPSLongitudeView,
            GPSAltitudeView, GPSSpeedView, GPSDirectionView, GPSAccuracyView;
    private Runnable infoRunnable = new Runnable() {
        @Override
        public void run() {
            CameraFPSView.setText("Camera Processing FPS: " + Double.toString(MainActivity.FPS));
            RotationAngleView.setText("Rotation Angle: "+ SensorUtil.rotation);
            GPSLatitudeView.setText("GPS Latitude: "+ Double.toString(SensorUtil.GPSData[0]));
            GPSLongitudeView.setText("GPS Longitude: "+ Double.toString(SensorUtil.GPSData[1]));
            GPSAltitudeView.setText("GPS Altitude: "+ Double.toString(SensorUtil.GPSData[2]));
            GPSSpeedView.setText("GPS Speed: "+ Double.toString(SensorUtil.GPSData[3]));
            GPSDirectionView.setText("GPS Direction: "+ Double.toString(SensorUtil.GPSData[4]));
            GPSAccuracyView.setText("GPS Accuracy: "+ Double.toString(SensorUtil.GPSData[5]));
            DistanceLiDARView.setText("LiDAR Distance: " + WifiManager.distanceLiDAR);
            AngleLiDARView.setText("LiDAR Angle: " + WifiManager.angleLiDAR);
            infoHandler.postDelayed(infoRunnable,1);

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        BatteryVoltageView = findViewById(R.id.BatteryVoltageView);
        BatteryPowerView = findViewById(R.id.BatteryPowerView);
        AngleLiDARView = findViewById(R.id.AngleLiDARView);
        DistanceLiDARView = findViewById(R.id.DistanceLiDARView);
        DistanceUltrasonicView = findViewById(R.id.DistanceUltrasonicView);
        CameraFPSView = findViewById(R.id.CameraFPSView);
        RotationAngleView = findViewById(R.id.RotationAngleView);
        GPSLatitudeView = findViewById(R.id.GPSLatitudeView);
        GPSLongitudeView = findViewById(R.id.GPSLongitudeView);
        GPSAltitudeView = findViewById(R.id.GPSAltitudeView);
        GPSSpeedView = findViewById(R.id.GPSSpeedView);
        GPSDirectionView = findViewById(R.id.GPSDirectionView);
        GPSAccuracyView = findViewById(R.id.GPSAccuracyView);
        infoHandler.postDelayed(infoRunnable,100);
    }
}