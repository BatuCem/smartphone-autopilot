package com.example.rotationvectortest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private final String TAG = "main";
    public static final int REQUEST_GPS_PERMISSION = 220;//code for gps permit
    private Sensor rotationVectorSensor, linearAccelerationSensor;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean isListening = false; // Track whether the sensor should be listening
    TextView sensorDataView;
    private double gpsAcc;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public Handler handler = new Handler();
    float theta, xRot, xAcc, yRot, yAcc, zRot, zAcc;
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isListening == true) {

                long timestamp = System.currentTimeMillis();
                saveToFile(timestamp, theta, xRot, yRot, zRot, xAcc, yAcc, zAcc, gpsAcc);

            }

            handler.postDelayed(runnable, 10);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        linearAccelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
                .setIntervalMillis(1000)
                .setMinUpdateIntervalMillis(100)
                .build();


        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);
        Button resetButton = findViewById(R.id.resetButton);
        sensorDataView = findViewById(R.id.sensorDataView);

        startButton.setOnClickListener(v -> startSensor());
        stopButton.setOnClickListener(v -> stopSensor());
        resetButton.setOnClickListener(v -> resetSensor());

        createFile();
        if (rotationVectorSensor != null && linearAccelerationSensor != null) {
            // The sensor exists
            sensorManager.registerListener(sensorEventListener, linearAccelerationSensor, 1000 * 10, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(sensorEventListener, rotationVectorSensor, 1000 * 10, SensorManager.SENSOR_DELAY_FASTEST);

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted; request it from the user
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_GPS_PERMISSION);
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, executorService, locationListener);

            Toast.makeText(this, "Sensor is available", Toast.LENGTH_SHORT).show();
            handler.post(runnable);
        } else {
            // The sensor is not available
            Toast.makeText(this, "Sensor is not available", Toast.LENGTH_SHORT).show();
        }

    }
    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR && isListening) {
                // Capture the event timestamp as close to the reading as possible

                // Extract the x, y, and z values
                theta = event.values[3];
                xRot = event.values[0];
                yRot = event.values[1];
                zRot = event.values[2];

                // Save to a file (handled in the next section)
            }
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION && isListening) {
                xAcc = event.values[0];
                yAcc = event.values[1];
                zAcc = event.values[2];
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Handle sensor accuracy changes if necessary
        }
    };

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            gpsAcc = location.getAccuracy();
        }
    };
    private void saveToFile(long timestamp, float theta ,float xRot, float yRot, float zRot, float xAcc, float yAcc, float zAcc, double gpsAcc) {
        String data = timestamp + " " + theta + " " + xRot + " " + yRot + " " + zRot +" "+xAcc+" "+yAcc+" "+zAcc+" "+gpsAcc+ "\n";
        //eulerRollDeg =180/pi.* atan2((2*(quatSensor(:,1).*quatSensor(:,4) - quatSensor(:,2).* quatSensor(:,3))) , ( 1 - 2.* (quatSensor(:,2).*quatSensor(:,2) + quatSensor(:,4).*quatSensor(:,4))))
        double roll = 180/Math.PI*Math.atan2(2*(theta*zRot - xRot*yRot), 1 - 2* (xRot*xRot + zRot*zRot));
        sensorDataView.setText((data));

        // Assuming you have the necessary permissions and have handled runtime permissions
        File file = new File(getFilesDir(), "sensor_data.txt");
        //Toast.makeText(this,getExternalFilesDir(null).toString(), Toast.LENGTH_SHORT).show();
        Log.i(TAG, "saveToFile: "+ getExternalFilesDir(null));
        try (FileOutputStream fos = new FileOutputStream(file, true)) {
            fos.write(data.getBytes());
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,"unable to write", Toast.LENGTH_SHORT).show();

        }
    }
    private void createFile() {
        File file = new File(getFilesDir(), "sensor_data.txt");
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (created) {
                    Toast.makeText(this, "File created", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "File not created", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            file.delete();
            try {
                boolean created = file.createNewFile();
                if (created) {
                    Toast.makeText(this, "File created", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "File not created", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void startSensor() {
        if (!isListening && rotationVectorSensor != null) {
            sensorManager.registerListener(sensorEventListener, rotationVectorSensor, 1000*10,SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(sensorEventListener, linearAccelerationSensor,1000*10,SensorManager.SENSOR_DELAY_FASTEST);
            isListening = true;
            // Add any other actions to take when starting, e.g., showing a message
        }
    }
    private void stopSensor() {
        if (isListening) {
            sensorManager.unregisterListener(sensorEventListener);
            isListening = false;
            // Add any other actions to take when stopping, e.g., showing a message
        }
    }
    private void resetSensor() {
        if (isListening) {
            sensorManager.unregisterListener(sensorEventListener);
            isListening = false;
            createFile();
            // Add any other actions to take when stopping, e.g., showing a message
        }
    }



}