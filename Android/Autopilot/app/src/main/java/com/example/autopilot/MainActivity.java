package com.example.autopilot;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainApp";
    private static final int ControlSampling = 50;
    private Toolbar appBar;
    private ImageCaptureManager imageCaptureManager;
    private DetectionTensorflow detectionTensorflow;
    private SensorUtil sensorUtil;
    private ImageView imageView;
    private AtomicBoolean isProcessing;
    public static long FPS;
    private Paint paint;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Handler systemHandler = new Handler();
    public static int distanceLiDAR, angleLiDAR;
    public static int [] lidarMap = new int[LidarActivity.lidarArraySize];
    private int driveState;
    private Double slope, slopeAbs;


    private Runnable systemRunnable = new Runnable() {
        @Override
        public void run() {
            try (Socket socket = new Socket("192.168.4.1", 80);
                 OutputStream outputStream = socket.getOutputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                int[] commands = new int[2];
                driveState = 0;


                    if((SettingsActivity.proximitySensorEnabled==true && SensorUtil.proximityCondition==true) || SettingsActivity.proximitySensorEnabled==false)
                    {
                        if(SettingsActivity.operationMode==0)
                        {
                            commands=ControlUtils.inferWifiCommands(50,255,240,255,255*2,1000,new int[]  {1,0,0},new int[] {1,0,0});
                            String messageToSend=ControlUtils.commandIntegers(commands[0],commands[1]);
                            outputStream.write(messageToSend.getBytes());
                            outputStream.flush();
                            Log.i(TAG, "0000 Command Sent: "+ messageToSend);
                        }
                        else
                        {
                            if(SensorUtil.distanceGPS>5)
                            {
                                int minLidarFront = IntStream.range(8,10).map(i -> lidarMap[i]).min().getAsInt();
                                if(minLidarFront <= 100)
                                {
                                    driveState = 1;
                                    slope = Math.atan2(lidarMap[80]*Math.sin(80)- lidarMap[100]*Math.sin(100)
                                            ,lidarMap[80]*Math.cos(80)-lidarMap[100]*Math.cos(100))*180/Math.PI;
                                    commands = ControlUtils.inferWifiCommandsForRotation(slope, SensorUtil.rotation,SensorUtil.distanceGPS,125,255, new double[]{1,0,0});
                                    String commandWifi = ControlUtils.commandIntegers(commands[0],commands[1]);
                                    WifiManager.requestToUrl(commandWifi,getBaseContext());
                                    Log.i(TAG, "obstacle 0001 Command Sent: "+ commandWifi);
                                }
                                else {
                                    if(driveState==0)
                                    {

                                        commands = ControlUtils.inferWifiCommandsForRotation(SensorUtil.bearingGPS, SensorUtil.rotation,SensorUtil.distanceGPS,125,255, new double[]{1,0,0});
                                        String commandWifi = ControlUtils.commandIntegers(commands[0],commands[1]);
                                        WifiManager.requestToUrl(commandWifi,getBaseContext());
                                        Log.i(TAG, "cruise 0001 Command Sent: "+ commandWifi);
                                    }
                                    else {
                                        commands = ControlUtils.inferWifiCommandsForRotation(slope, SensorUtil.rotation,SensorUtil.distanceGPS,125,255, new double[]{1,0,0});
                                        String commandWifi = ControlUtils.commandIntegers(commands[0],commands[1]);
                                        WifiManager.requestToUrl(commandWifi,getBaseContext());
                                        Log.i(TAG, "obstacle 0001 Command Sent: "+ commandWifi);
                                        if(((slope<0) &&(IntStream.range(0,1).map(i -> lidarMap[i]).min().getAsInt() > 100)) || ((slope>0) &&(IntStream.range(17,18).map(i -> lidarMap[i]).min().getAsInt() > 100)))
                                        {
                                            driveState = 0;
                                        }
                                        else {
                                            driveState = 1;
                                        }
                                    }

                                }

                            }
                            else{
                                String commandWifi = ControlUtils.commandIntegers(0,0);
                                WifiManager.requestToUrl(commandWifi,getBaseContext());
                                Log.i(TAG, "idle 0001 Command Sent: "+ commandWifi);
                            }

                            //commands = ControlUtils.inferWifiCommandsForRotation(0, SensorUtil.rotation,SensorUtil.distanceGPS,125,255, new double[]{2,0.0002,0});
                            //String commandWifi = ControlUtils.commandIntegers(commands[0],commands[1]);
                            //WifiManager.requestToUrl(commandWifi,getBaseContext());
                            //Log.i(TAG, "0001 Command Sent: "+ commandWifi);
                            // Reading the response from the server
                            String responseStr = reader.readLine();
                            System.out.println("Received from server: " + responseStr);
                            String[] responseStrSplit = responseStr.split(" ");
                            Log.i(TAG, "getUrl: "+ responseStr);
                            if(responseStrSplit.length >= 2)
                            {
                                try {
                                    distanceLiDAR = Integer.parseInt(responseStrSplit[0]);
                                    angleLiDAR = Integer.parseInt(responseStrSplit[1]);
                                    if(angleLiDAR >= 0 && angleLiDAR < 180)
                                    {
                                        lidarMap[(int) angleLiDAR/ LidarActivity.lidarResolution ] = distanceLiDAR;
                                    }
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                        }



                    }

            } catch (IOException e) {
                e.printStackTrace();
            }

            systemHandler.postDelayed(systemRunnable,ControlSampling);
        }
    };

    public static final int REQUEST_CAMERA_PERMISSION = 200;//code for camera permit
    public static final int REQUEST_GPS_PERMISSION = 220;//code for gps permit
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appBar=findViewById(R.id.appBar);
        imageView= findViewById(R.id.imageView);
        setSupportActionBar(appBar);
        paint = new Paint();
        isProcessing= new AtomicBoolean(false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted; request it from the user
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_GPS_PERMISSION);
        } else {
            //ask for necessary permissions (CAMERA)
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            try {
                detectionTensorflow = new DetectionTensorflow(this,2);
            } catch (IOException e)
            {
                Log.e(TAG, "onCreate: ");
            }
        }
    }

    public boolean onCreateOptionsMenu (Menu menu)
    {
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.settings:
                Intent intentSettings= new Intent(this, SettingsActivity.class);
                settingsResultLauncher.launch(intentSettings);
                break;
            case R.id.info:
                Intent intentInfo = new Intent(this, InfoActivity.class);
                startActivity(intentInfo);
                break;
            case R.id.maps:
                Intent intentMaps = new Intent(this, MapActivity.class);
                startActivity(intentMaps);
                break;
            case R.id.lidar:
                Intent intentLidar = new Intent(this, LidarActivity.class);
                startActivity(intentLidar);
                break;
            case R.id.remote:
                Intent intentRemote = new Intent(this, remoteActivity.class);
                startActivity(intentRemote);
                break;
        }
        return true;
    }
    private void processImageAndSetToView(Bitmap bitmap) {
        if (!isProcessing.compareAndSet(false, true)) {
            // Current image is skipped because another is still processing
            return;
        }

        executorService.execute(() -> {
            try {
                // Image processing logic here

                long tInit = System.currentTimeMillis();
                detectionTensorflow.detectObjects(bitmap);
                Bitmap outputBmp= ImageUtils.drawRectF(bitmap,0.5f,detectionTensorflow,paint);

                runOnUiThread(() -> imageView.setImageBitmap(outputBmp));
                Log.i(TAG, "onBitmapAvailable: TOTALTIMEMEASURED "+ Long.toString(System.currentTimeMillis()-tInit));
                FPS = 1000 / (System.currentTimeMillis() - tInit);

            } finally {
                isProcessing.set(false);
            }
        });
    }

    private void startSendingMessages() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (Socket socket = new Socket("192.168.4.1", 80);
                     OutputStream outputStream = socket.getOutputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    int[] commands = new int[2];
                    driveState = 0;
                    while (true) {


                        if((SettingsActivity.proximitySensorEnabled==true && SensorUtil.proximityCondition==true) || SettingsActivity.proximitySensorEnabled==false)
                        {
                            if(SettingsActivity.operationMode==0)
                            {
                                commands=ControlUtils.inferWifiCommands(50,255,240,255,255*2,1000,new int[]  {1,0,0},new int[] {0,0,0});
                                String messageToSend=ControlUtils.commandIntegers(commands[0],commands[1]);
                                outputStream.write(messageToSend.getBytes());
                                outputStream.flush();
                                Log.i(TAG, "0000 Command Sent: "+ messageToSend);
                            }
                            else if(SettingsActivity.operationMode == 1)
                            {
                                if(SensorUtil.distanceGPS>5)
                                {
                                    int minLidarFront = IntStream.range(LidarActivity.lidarArraySize/2-1,LidarActivity.lidarArraySize/2+1).map(i -> lidarMap[i]).min().getAsInt();
                                    if(minLidarFront <= LidarActivity.lidarLevel)
                                    {
                                        driveState = 1;
                                        slope = (Math.atan2(lidarMap[80/LidarActivity.lidarResolution]*Math.sin(1.3962634016)- lidarMap[100/LidarActivity.lidarResolution]*Math.sin(1.74532925199)
                                                ,lidarMap[80/LidarActivity.lidarResolution]*Math.cos(1.3962634016)-lidarMap[100/LidarActivity.lidarResolution]*Math.cos(1.74532925199))*180/Math.PI)%360;
                                        slopeAbs = (90 - slope + SensorUtil.rotation)%360;
                                        Log.i(TAG, "run: slope found : "+slope + "angle2: " + 80/LidarActivity.lidarResolution + "");
                                        commands = ControlUtils.inferWifiCommandsForRotation(slopeAbs, SensorUtil.rotation,SensorUtil.distanceGPS,125,255, new double[]{2,0.00002,0});
                                        String commandWifi = ControlUtils.commandIntegers(commands[0],commands[1]);
                                        outputStream.write(commandWifi.getBytes());
                                        outputStream.flush();
                                        Log.i(TAG, "obstacle found 0001 Command Sent: "+ commandWifi);
                                    }
                                    else {
                                        if(driveState==0)
                                        {
                                            commands = ControlUtils.inferWifiCommandsForRotation(SensorUtil.bearingGPS, SensorUtil.rotation,SensorUtil.distanceGPS,125,255, new double[]{2,0.00002,0});
                                            String commandWifi = ControlUtils.commandIntegers(commands[0],commands[1]);
                                            outputStream.write(commandWifi.getBytes());
                                            outputStream.flush();
                                            Log.i(TAG, "normal cruise 0001 Command Sent: "+ commandWifi);
                                        }
                                        else {
                                            commands = ControlUtils.inferWifiCommandsForRotation(slopeAbs, SensorUtil.rotation,SensorUtil.distanceGPS,125,255, new double[]{2,0.00002,0});
                                            String commandWifi = ControlUtils.commandIntegers(commands[0],commands[1]);
                                            outputStream.write(commandWifi.getBytes());
                                            outputStream.flush();
                                            Log.i(TAG, "obstacle 0001 Command Sent: "+ commandWifi);
                                            if(((slope<0) &&(IntStream.range(0,10/LidarActivity.lidarResolution).map(i -> lidarMap[i]).min().getAsInt() > LidarActivity.lidarLevel)) || ((slope>0) &&(IntStream.range(LidarActivity.lidarArraySize-10/LidarActivity.lidarResolution,LidarActivity.lidarArraySize).map(i -> lidarMap[i]).min().getAsInt() > LidarActivity.lidarLevel)))
                                            {
                                                driveState = 0;
                                            }
                                            else {
                                                driveState = 1;
                                            }
                                        }

                                    }

                                }
                                else{
                                    String commandWifi = ControlUtils.commandIntegers(0,0);
                                    outputStream.write(commandWifi.getBytes());
                                    outputStream.flush();
                                    Log.i(TAG, "0001 Command Sent: "+ commandWifi);
                                }

                            }
                            else
                            {
                                String commandWifi = ControlUtils.commandIntegers(remoteActivity.leftSignal,remoteActivity.rightSignal);
                                outputStream.write(commandWifi.getBytes());
                                outputStream.flush();
                                Log.i(TAG, "0002 Command Sent: "+ commandWifi);
                                // Reading the response from the server

                            }



                        }
                        String responseStr = reader.readLine();
                        System.out.println("Received from server: " + responseStr);
                        String[] responseStrSplit = responseStr.split(" ");
                        Log.i(TAG, "getUrl: "+ responseStr);
                        if(responseStrSplit.length >= 2)
                        {
                            try {
                                angleLiDAR = Integer.parseInt(responseStrSplit[0]);
                                distanceLiDAR = Integer.parseInt(responseStrSplit[1]);
                                if(angleLiDAR >= 0 && angleLiDAR < 180)
                                {
                                    lidarMap[(int)(angleLiDAR/LidarActivity.lidarResolution)] = distanceLiDAR;
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                        Thread.sleep(10); // Sleep
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    Log.i(TAG, "run thread: "+ e);
                }
            }
        });
        thread.start();
    }

    private final ActivityResultLauncher<Intent> settingsResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.i(TAG, "SettingsReturnToMain");
                    //systemHandler.postDelayed(systemRunnable,ControlSampling);
                    startSendingMessages();
                    if(imageCaptureManager!=null)
                    {
                        imageCaptureManager.closeCameras();
                    }
                    imageCaptureManager= new ImageCaptureManager(this);

                    sensorUtil = new SensorUtil(this);
                    imageCaptureManager.setBitmapAvailableListener(new ImageCaptureManager.BitmapAvailableListener() {
                        @Override
                        public void onBitmapAvailable(Bitmap bitmap, int index) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    processImageAndSetToView(bitmap);
                                }
                            });
                        }
                    });
                }
            });
}