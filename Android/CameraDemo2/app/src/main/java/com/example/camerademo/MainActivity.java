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
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG="AndroidCameraApi";
    private TextureView textureView;
    private TextureView textureView2;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0,90);
        ORIENTATIONS.append(Surface.ROTATION_90,0);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270,180);
    }

    private CameraManager cameraManager;
    private String[] cameraIds;
    private CameraDevice[] cameraDevices = new CameraDevice[2];
    private TextureView[] textureViews = new TextureView[2];
    private CaptureRequest.Builder[] captureRequestBuilders = new CaptureRequest.Builder[2];
    private CameraCaptureSession[] captureSessions = new CameraCaptureSession[2];
    private Surface[] surfaces = new Surface[2];
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private Size[] imageDimensions = new Size[2];


    private static final int REQUEST_CAMERA_PERMISSION = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textureViews[0] = findViewById(R.id.texture);
        textureViews[1] = findViewById(R.id.texture2);
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            cameraIds = cameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }


        openCamera(cameraIds[0], 0);
        openCamera(cameraIds[1], 1);
        // Initialize background thread
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void openCamera(String cameraId, final int index){
        try {
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice cameraDevice) {
                    cameraDevices[index] = cameraDevice;
                    createCameraPreview(index);
                }

                @Override
                public void onDisconnected(CameraDevice cameraDevice) {
                    cameraDevice.close();
                    cameraDevices[index] = null;
                }

                @Override
                public void onError(CameraDevice cameraDevice, int error) {
                    cameraDevice.close();
                    cameraDevices[index] = null;
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }
    protected void createCameraPreview(final int index){
        try {
            SurfaceTexture texture = textureViews[index].getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(1920, 1080);
            surfaces[index] = new Surface(texture);

            captureRequestBuilders[index] = cameraDevices[index].createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilders[index].addTarget(surfaces[index]);

            cameraDevices[index].createCaptureSession(Arrays.asList(surfaces[index]), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (cameraDevices[index] == null) {
                        return;
                    }

                    captureSessions[index] = cameraCaptureSession;

                    try {
                        captureRequestBuilders[index].set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                        captureSessions[index].setRepeatingRequest(captureRequestBuilders[index].build(), null, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Log.e("MultiCameraActivity", "Camera configuration failed.");
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, reopen the cameras
                openCamera(cameraIds[0], 0);
                openCamera(cameraIds[1], 1);
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void closeCamera() {
        for (int i = 0; i < 2; i++) {
            if (cameraDevices[i] != null) {
                cameraDevices[i].close();
                cameraDevices[i] = null;
            }
        }
    }

    private void startBackgroundThread() {
        if (backgroundThread == null) {
            backgroundThread = new HandlerThread("CameraBackground");
            backgroundThread.start();
            backgroundHandler = new Handler(backgroundThread.getLooper());
        }
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}