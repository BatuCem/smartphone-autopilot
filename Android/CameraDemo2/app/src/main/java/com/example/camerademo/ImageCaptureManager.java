package com.example.camerademo;


import static java.util.Arrays.asList;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.OutputConfiguration;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageCaptureManager extends AppCompatActivity {
    private int FPS;//Frames per second
    private Context context;
    private int backCams;
    private int frontCams;
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
    private Image[] images;
    private Size[] imagesDimensions;

    public Image[] imageCapture;

    public ImageCaptureManager(Context context,int backCams,int frontCams, int FPS)
    {
        this.context=context;
        this.FPS=FPS;
        this.backCams=backCams;
        this.frontCams=frontCams;
        int totalCameras=backCams+frontCams;
        this.cameraManager=(CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        this.cameraDevices=new CameraDevice[totalCameras];
        this.cameraCharacteristics=new CameraCharacteristics[totalCameras];
        this.captureRequestBuilders=new CaptureRequest.Builder[totalCameras];
        this.captureSessions= new CameraCaptureSession[totalCameras];
        this.surfaces=new Surface[totalCameras];

        this.imagesDimensions=new Size[totalCameras];
        this.backCameraIds=findIDs(this.backCams,CameraCharacteristics.LENS_FACING_BACK,this.cameraCharacteristics[0]);
        this.frontCameraIds=findIDs(this.frontCams,CameraCharacteristics.LENS_FACING_FRONT,this.cameraCharacteristics[0]);
        this.imageCapture= new Image[totalCameras];
        this.handlerThread= new HandlerThread("CameraBackground");
        this.handlerThread.start();
        this.backgroundHandler=new Handler(handlerThread.getLooper());




    }



    //method to take in a needed amount of (lim) camera IDs into a variable, with selection given
    //the lens orientation
    private String[] findIDs(int lim, int lensFacing,CameraCharacteristics cameraCharacteristics_temp)
    {   //Method to find a given amount of camera IDs in a given facing(front/back/external) manually by sweeping
        Set<String> idHolder= new HashSet<>(); //variable to hold ids
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
    public void openCamera(String cameraId,int index,TextureView textureView){
        try {//try for opening camera handling permission


            cameraCharacteristics[index]=cameraManager.getCameraCharacteristics(cameraId);
            imagesDimensions[index]=cameraCharacteristics[index].get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);


            //setup callback function of device
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override //when called back as open, create preview
                public void onOpened(CameraDevice cameraDevice) {
                    cameraDevices[index] = cameraDevice;
                    createCaptureSession(index,textureView);//?
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
    private void createCaptureSession(int index,TextureView textureView)
    {

            try {
                SurfaceTexture texture = textureView.getSurfaceTexture();
                assert texture != null;
                texture.setDefaultBufferSize(imagesDimensions[index].getWidth(),imagesDimensions[index].getHeight());

                surfaces[index] = new Surface(texture);

                captureRequestBuilders[index] = cameraDevices[index].createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                captureRequestBuilders[index].addTarget(surfaces[index]);

                cameraDevices[index].createCaptureSession(asList(surfaces[index]), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        if (cameraDevices[index] == null) {
                            return;
                        }
                        try {
                            captureSessions[index] = session;
                            captureRequestBuilders[index].set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                            captureRequestBuilders[index].set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<>(FPS, FPS));
                            captureSessions[index].setRepeatingRequest(captureRequestBuilders[index].build(), null, backgroundHandler);

                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        Log.e("MultiCameraActivity", "Camera configuration failed.");
                        //TODO: Handle failed configuration
                    }
                }, backgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
    }
    public void closeCamera(int index){
        if(cameraDevices[index]!=null)
        {
            cameraDevices[index].close();
        }
        if(captureSessions[index]!=null)
        {
            captureSessions[index].close();
        }
        if(imageReaders[index]!=null)
        {
            imageReaders[index].close();
        }
    }

    public String[] getCameraIds(boolean selectBack)
    {
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
