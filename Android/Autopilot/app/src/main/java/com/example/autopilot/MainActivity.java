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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainApp";
    private Toolbar appBar;
    private ImageCaptureManager imageCaptureManager;
    private ImageView imageView;

    public static final int REQUEST_CAMERA_PERMISSION = 200;//code for camera permit
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appBar=findViewById(R.id.appBar);
        imageView= findViewById(R.id.imageView);
        setSupportActionBar(appBar);
        //ask for necessary permissions (CAMERA)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
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
    private final ActivityResultLauncher<Intent> settingsResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.i(TAG, "SettingsReturnToMain");
                    if(imageCaptureManager!=null)
                    {
                        imageCaptureManager.closeCameras();
                    }
                    imageCaptureManager= new ImageCaptureManager(this);
                    imageCaptureManager.setBitmapAvailableListener(new ImageCaptureManager.BitmapAvailableListener() {
                        @Override
                        public void onBitmapAvailable(Bitmap bitmap, int index) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    imageView.setImageBitmap(bitmap);
                                }
                            });
                        }
                    });
                }
            });
}