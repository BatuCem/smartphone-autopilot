package com.example.camerademo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


//Main Class
public class MainActivity extends AppCompatActivity {
    ////////////////////////////////DECLARE VARIABLES//////////////////////////////////
    private static final String TAG="AndroidCameraApi";//error handling tag
    private ImageCaptureManager imageCaptureManager;
    private TextureView textureView;
    private ImageView imageView;
    private Image[] images;
    private String[] backCameraIds;
    private String[] frontCameraIds;
    private static final int REQUEST_CAMERA_PERMISSION = 200;//code for camera permit


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set to main layout
        setContentView(R.layout.activity_main);
        //set up texture view windows
        textureView = findViewById(R.id.textureView);

        //ask for necessary permissions (CAMERA)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }
        imageCaptureManager = new ImageCaptureManager(getBaseContext(), 2, 1, 1);
        backCameraIds = imageCaptureManager.getCameraIds(true);
        frontCameraIds = imageCaptureManager.getCameraIds(false);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                imageCaptureManager.openCamera(backCameraIds[0], 0, textureView);
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                Bitmap bitmap = textureView.getBitmap();
                if (bitmap != null) {
                }

            }
        });




    }




    }
