package com.example.autopilot;

import static java.util.Arrays.asList;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ImageCaptureManager extends AppCompatActivity {
    //class to handle image capturing from camera
    private Context context;
    private int backCams;
    private int frontCams;
    private int totalCameras;
    private CameraManager cameraManager;
    private CameraDevice[] cameraDevices;
    private CameraCharacteristics[] cameraCharacteristics;
    private String[] backCameraIds;
    private String[] frontCameraIds;
    private CaptureRequest.Builder[] captureRequestBuilders;
    private CameraCaptureSession[] captureSessions;
    private Surface[] surfaces;
    private HandlerThread handlerThread=new HandlerThread("CameraBackground");
    private Handler backgroundHandler;
    private ImageReader[] imageReaders;
    private Size[] imagesDimensions;
    public Image[] imageCapture;
    String TAG = "ImageCaptureManagement";

    public ImageCaptureManager(Context context)
    {
        //builder for class
        this.context=context;
        this.backCams=SettingsActivity.backCameraSpinnerValue; //get number of cameras from GUI
        this.frontCams=SettingsActivity.frontCameraSpinnerValue;
        this.totalCameras=backCams+frontCams;
        this.cameraManager=(CameraManager) context.getSystemService(Context.CAMERA_SERVICE);//open camera service
        this.cameraDevices=new CameraDevice[totalCameras];  //define array lengths for multi-camera handling
        this.cameraCharacteristics=new CameraCharacteristics[totalCameras];
        this.captureRequestBuilders=new CaptureRequest.Builder[totalCameras];
        this.captureSessions= new CameraCaptureSession[totalCameras];
        this.surfaces=new Surface[totalCameras];
        this.imageReaders = new ImageReader[totalCameras];
        this.imagesDimensions=new Size[totalCameras];
        this.imageCapture= new Image[totalCameras];
        //setup operation on background thread
        this.handlerThread= new HandlerThread("CameraBackground");
        this.handlerThread.start();
        this.backgroundHandler=new Handler(handlerThread.getLooper());
        //Implement if GUI settings to auto-find IDs are on, find IDs or get manual values
        if(SettingsActivity.backCameraAutoIdEnabled==true)
        {
            backCameraIds=findIDs(backCams,CameraCharacteristics.LENS_FACING_BACK);
        }
        else
        {
            backCameraIds=SettingsActivity.backCameraIds.toArray(new String[0]);
        }

        if(SettingsActivity.frontCameraAutoIdEnabled==true)
        {
            frontCameraIds=findIDs(frontCams,CameraCharacteristics.LENS_FACING_FRONT);
        }
        else
        {
            frontCameraIds=SettingsActivity.frontCameraIds.toArray(new String[0]);
        }
        //copy got back and front camera ids to indexes
        String[] cameraIds = new String[totalCameras];
        System.arraycopy(backCameraIds, 0, cameraIds, 0, backCams);
        System.arraycopy(frontCameraIds, 0, cameraIds, backCams, frontCams);
        for(int i=0;i<totalCameras;i++ )
        {
            //open up cameras from ids for each selected
            openCamera(cameraIds[i],i);
        }
    }
    public void openCamera(String cameraId,int index){
        try {//try for opening camera handling permission


            cameraCharacteristics[index]=cameraManager.getCameraCharacteristics(cameraId);
            imagesDimensions[index]=cameraCharacteristics[index].get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG)[0]; //possible unsafe usage of HighRes instead of normal sizes
            imageReaders[index]=ImageReader.newInstance((int) imagesDimensions[index].getWidth(),(int)imagesDimensions[index].getHeight(),ImageFormat.JPEG, 1);
            //setup callback function of device
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override //when called back as open, create preview
                public void onOpened(CameraDevice cameraDevice) {
                    cameraDevices[index] = cameraDevice;
                    createCaptureSession(cameraId,index);//?
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
    private void createCaptureSession(String cameraId,int index) {
        try {
            imageReaders[index] = ImageReader.newInstance(imagesDimensions[index].getWidth(), imagesDimensions[index].getHeight(), ImageFormat.JPEG, 1);
            imageReaders[index].setOnImageAvailableListener(reader -> {
                long tInit=System.currentTimeMillis();
                Image image = null;
                try {
                    image = reader.acquireLatestImage();
                    Bitmap bitmap = ImageUtils.imageToBitmap(image);
                    if (bitmapAvailableListener != null) {
                        bitmapAvailableListener.onBitmapAvailable(bitmap, index);
                    }

                } finally {
                    if (image != null) {
                        image.close();
                        Log.i(TAG, "createCaptureSession: TIMEMEASURED "+ Long.toString(System.currentTimeMillis()-tInit) +" "+ index);
                    }
                }
            }, backgroundHandler);

            captureRequestBuilders[index] = cameraDevices[index].createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilders[index].addTarget(imageReaders[index].getSurface());
            //captureRequestBuilders[index].set(CaptureRequest.JPEG_ORIENTATION, 90);
            cameraDevices[index].createCaptureSession(Arrays.asList(imageReaders[index].getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevices[index] == null) {
                        return;
                    }
                    captureSessions[index] = session;
                    try {
                        captureRequestBuilders[index].set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                        captureRequestBuilders[index].set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        captureRequestBuilders[index].set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                        captureRequestBuilders[index].set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
                        session.setRepeatingRequest(captureRequestBuilders[index].build(), null, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e("ImageCaptureManager", "Camera configuration failed.");
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public interface BitmapAvailableListener {
        void onBitmapAvailable(Bitmap bitmap, int index);
    }

    private BitmapAvailableListener bitmapAvailableListener;

    // Method to set the bitmap listener
    public void setBitmapAvailableListener(BitmapAvailableListener listener) {
        this.bitmapAvailableListener = listener;
    }
    private String[] findIDs(int lim, int lensFacing)
    {   //Method to find a given amount of camera IDs in a given facing(front/back/external) manually by sweeping
        Set<String> idHolder= new HashSet<>(); //variable to hold ids
        CameraCharacteristics cameraCharacteristics_temp;
        int k=0;    //added element counter
        int i=0;    //sweep counter
        try {
            while(k<lim) {
                //get Camera characteristics (is it a valid ID?)
                cameraCharacteristics_temp = cameraManager.getCameraCharacteristics(String.valueOf(i));
                //get lens direction (is it looking where we want it to be?)
                if(cameraCharacteristics_temp.get(CameraCharacteristics.LENS_FACING)==lensFacing)
                {
                    idHolder.add(String.valueOf(i));    //add to set of ids
                    k++;
                }
                i++;
            }
        }catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
        cameraCharacteristics_temp=null;
        return idHolder.toArray(new String[0]); //convert set to array for easier further use
    }
    public void closeCameras()
    {
        //close all cameras
        for(int i=0;i<totalCameras;i++ )
        {
            closeCamera(i);
        }
    }
    private void closeCamera(int index){
        //close specific camera
        if(cameraDevices[index]!=null)
        {
            cameraDevices[index].close();
        }
        if(captureSessions[index]!=null)
        {
            captureSessions[index].close();
        }
    }
    public String[] getCameraIds(boolean selectBack)
    {
        //method to get ids externally to class
        if(selectBack==true)//true returns backwards
        {
            return backCameraIds;
        }
        else        //default returns front
        {
            return frontCameraIds;
        }
    }
}

