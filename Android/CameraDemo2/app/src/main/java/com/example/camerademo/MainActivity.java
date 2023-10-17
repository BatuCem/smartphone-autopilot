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
import java.util.HashSet;
import java.util.Set;


//Main Class
public class MainActivity extends AppCompatActivity {
    ////////////////////////////////DECLARE VARIABLES//////////////////////////////////
    private static final String TAG="AndroidCameraApi";//error handling tag
    private CameraManager cameraManager;//setup camera manager var from camera2 API
    private CameraCharacteristics[] cameraCharacteristics;//array of camera characteristics
                                                            //to hold on to objects of sizes, ids...
    private String[] backCameraIds;//String array for logical cameras
    private String[] frontCameraIds;
    private CameraDevice[] cameraDevices = new CameraDevice[2];//2 camera devices for 2 texture fields
    private TextureView[] textureViews = new TextureView[2];//texture fields from main layout
    private CaptureRequest.Builder[] captureRequestBuilders = new CaptureRequest.Builder[2];//
    private CameraCaptureSession[] captureSessions = new CameraCaptureSession[2];//Capturing Sessions for textures
    private Surface[] surfaces = new Surface[2];//Captured individual surfaces
    private HandlerThread backgroundThread;//Handler thread for camera operations
    private Handler backgroundHandler;//Handler that will connect with backgroundThread
    private Size[] imageDimensions = new Size[2];//image dimensions for display and saving
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
        //init camera manager
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        cameraCharacteristics= new CameraCharacteristics[2];
        backCameraIds=findIDs(2, CameraCharacteristics.LENS_FACING_BACK,cameraCharacteristics[0]);
        frontCameraIds=findIDs(1,CameraCharacteristics.LENS_FACING_FRONT,cameraCharacteristics[0]);
        openCamera(backCameraIds[0],0);//open first camera at textView indexed 0
        openCamera(backCameraIds[1],1);//open second camera at textView indexed 1
        // Initialize background thread
        backgroundThread = new HandlerThread("CameraBackground");//setup handler thread
        backgroundThread.start();   //init handler thread
        //connect thread to handler, loop the software
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    //method to take in a needed amount of (lim) camera IDs into a variable, with selection given
    //the lens orientation
    private String[] findIDs(int lim, int lensFacing,CameraCharacteristics cameraCharacteristics)
    {
        Set<String> idHolder= new HashSet<>(); //variable to hold ids
        int k=0;    //added element counter
        int i=0;    //sweep counter
        try {
            while(k<lim) {
                //
                cameraCharacteristics = cameraManager.getCameraCharacteristics(String.valueOf(i));

                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)==lensFacing)
                {
                    idHolder.add(String.valueOf(i));
                    k++;
                }
                i++;
            }
        }catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
        cameraCharacteristics=null;
        return idHolder.toArray(new String[0]);
    }
    //Method to open the camera with given camera id and texture index
    private void openCamera(String cameraId,int index){
        try {//try for opening camera handling permission


            cameraCharacteristics[index]=cameraManager.getCameraCharacteristics(cameraId);
            imageDimensions[index]=cameraCharacteristics[index].get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
            //setup callback function of device
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override //when called back as open, create preview
                public void onOpened(CameraDevice cameraDevice) {
                    cameraDevices[index] = cameraDevice;
                    createCameraPreview(index);
                }

                @Override //if calls back disconnected, close
                public void onDisconnected(CameraDevice cameraDevice) {
                    cameraDevice.close();
                    cameraDevices[index] = null;
                }

                @Override   //when called back with error, close
                public void onError(CameraDevice cameraDevice, int error) {
                    cameraDevice.close();
                    cameraDevices[index] = null;
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    //Method to create preview from in textureView
    protected void createCameraPreview(final int index){
        try {//try with handling permission

            //get surface texture from indexed view
            SurfaceTexture texture = textureViews[index].getSurfaceTexture();
            //throw assertion error if textures are null
            assert texture != null;
            //set buffer sizes to image dimensions
            texture.setDefaultBufferSize(imageDimensions[index].getWidth(),imageDimensions[index].getHeight());
            //create surfaces
            surfaces[index] = new Surface(texture);
            //send capture request and target surfaces
            captureRequestBuilders[index] = cameraDevices[index].createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilders[index].addTarget(surfaces[index]);
            //capture session callback setup
            cameraDevices[index].createCaptureSession(Arrays.asList(surfaces[index]), new CameraCaptureSession.StateCallback() {
                @Override //when configured and nonnull device, capture
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (cameraDevices[index] == null) {
                        return;
                    }

                    captureSessions[index] = cameraCaptureSession;

                    try {   //send repeating capture requests, handling permissions
                        captureRequestBuilders[index].set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                        captureSessions[index].setRepeatingRequest(captureRequestBuilders[index].build(), null, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override //when configure has failed to capture, log the error
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Log.e("MultiCameraActivity", "Camera configuration failed.");
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    @Override //handle permission results
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, reopen cameras
                openCamera(backCameraIds[0], 0);
                openCamera(backCameraIds[1], 1);
            } else {
                // TODO: handle PERMISSION DENIAL
            }
        }
    }

    @Override //start background threading when resumed after pause
    protected void onResume() {
        //restart thread
        startBackgroundThread();
        super.onResume();
    }

    @Override   //task on pause (lock screen etc.)
    protected void onPause() {
        //shut down camera (commented out)
        closeCamera();
        //stop thread
        stopBackgroundThread();
        super.onPause();
    }

    private void closeCamera() {//safely close cameras
        for (int i = 0; i < 2; i++) {
            if (cameraDevices[i] != null) {
                //close devices
                cameraDevices[i].close();
                //nullify cameras
                cameraDevices[i] = null;
            }
        }
    }

    private void startBackgroundThread() {//background thread restart
        if (backgroundThread == null) {
            //rename thread
            backgroundThread = new HandlerThread("CameraBackground restart");
            //restart thread
            backgroundThread.start();
            //renew handler connected to thread
            backgroundHandler = new Handler(backgroundThread.getLooper());
        }
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            //stop threading safely
            backgroundThread.quitSafely();
            try {
                //wait until thread quits
                backgroundThread.join();
                //nullify thread and handler
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}