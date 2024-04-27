package com.example.rotationvectortest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private final String TAG = "main";
    private Sensor rotationVectorSensor, linearAccelerationSensor;
    private boolean isListening = false; // Track whether the sensor should be listening
    TextView sensorDataView;
    public Handler handler = new Handler();
    float theta, xRot, xAcc, yRot, yAcc, zRot, zAcc;
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(isListening==true)
            {

                long timestamp = System.currentTimeMillis();
                saveToFile(timestamp,theta, xRot, yRot, zRot, xAcc, yAcc, zAcc);

            }

            handler.postDelayed(runnable , 10);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        linearAccelerationSensor= sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);
        Button resetButton = findViewById(R.id.resetButton);
        sensorDataView= findViewById(R.id.sensorDataView);

        startButton.setOnClickListener(v -> startSensor());
        stopButton.setOnClickListener(v -> stopSensor());
        resetButton.setOnClickListener(v -> resetSensor());

        createFile();
        if (rotationVectorSensor != null && linearAccelerationSensor != null) {
            // The sensor exists
            sensorManager.registerListener(sensorEventListener, linearAccelerationSensor, 1000*10,SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(sensorEventListener, rotationVectorSensor,1000*10,SensorManager.SENSOR_DELAY_FASTEST);

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
    private void saveToFile(long timestamp, float theta ,float xRot, float yRot, float zRot, float xAcc, float yAcc, float zAcc) {
        String data = timestamp + " " + theta + " " + xRot + " " + yRot + " " + zRot +" "+xAcc+" "+yAcc+" "+zAcc+ "\n";
        //eulerRollDeg =180/pi.* atan2((2*(quatSensor(:,1).*quatSensor(:,4) - quatSensor(:,2).* quatSensor(:,3))) , ( 1 - 2.* (quatSensor(:,2).*quatSensor(:,2) + quatSensor(:,4).*quatSensor(:,4))))
        double roll = 180/Math.PI*Math.atan2(2*(theta*zRot - xRot*yRot), 1 - 2* (xRot*xRot + zRot*zRot));
        sensorDataView.setText(Double.toString(roll));

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