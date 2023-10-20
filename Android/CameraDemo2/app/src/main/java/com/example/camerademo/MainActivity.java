package com.example.camerademo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


//Main Class
public class MainActivity extends AppCompatActivity {
    ////////////////////////////////DECLARE VARIABLES//////////////////////////////////
    private static final String TAG="AndroidCameraApi";//error handling tag
    private ImageCaptureManager imageCaptureManager;
    private TextureView[] textureViews;
    private static final int REQUEST_CAMERA_PERMISSION = 200;//code for camera permit


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set to main layout
        setContentView(R.layout.activity_main);
        //set up texture view windows
        textureViews[0] = findViewById(R.id.texture);
        textureViews[1] = findViewById(R.id.texture2);
        //ask for necessary permissions (CAMERA)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }
        imageCaptureManager=new ImageCaptureManager(this,2,1,15);
        

    }
}