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

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainApp";
    private static final int ControlSampling = 50;
    private Toolbar appBar;
    private ImageCaptureManager imageCaptureManager;
    private DetectionTensorflow detectionTensorflow;
    private SensorUtil sensorUtil;
    private ImageView imageView;
    private AtomicBoolean isProcessing;
    private Paint paint;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Handler systemHandler = new Handler();


    private Runnable systemRunnable = new Runnable() {
        @Override
        public void run() {
            int[] commands = new int[2];
            if((SettingsActivity.proximitySensorEnabled==true && SensorUtil.proximityCondition==true) || SettingsActivity.proximitySensorEnabled==false)
            {
                    commands=ControlUtils.inferWifiCommands(50,255,240,255,255*2,1000,new int[]  {1,0,0},new int[] {1,0,0});
                    String commandWifi=ControlUtils.commandIntegers(commands[0],commands[1]);
                    WifiManager.requestToUrl(commandWifi,getBaseContext());
                    Log.i(TAG, "Command Sent: "+ commandWifi);

            }

            systemHandler.postDelayed(systemRunnable,ControlSampling);
        }
    };

    public static final int REQUEST_CAMERA_PERMISSION = 200;//code for camera permit
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appBar=findViewById(R.id.appBar);
        imageView= findViewById(R.id.imageView);
        setSupportActionBar(appBar);
        paint = new Paint();
        isProcessing= new AtomicBoolean(false);
        //ask for necessary permissions (CAMERA)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }
        try {
            detectionTensorflow = new DetectionTensorflow(this,4);
        } catch (IOException e)
        {
            Log.e(TAG, "onCreate: ");
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

            } finally {
                isProcessing.set(false);
            }
        });
    }


    private final ActivityResultLauncher<Intent> settingsResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.i(TAG, "SettingsReturnToMain");
                    systemHandler.postDelayed(systemRunnable,ControlSampling);
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