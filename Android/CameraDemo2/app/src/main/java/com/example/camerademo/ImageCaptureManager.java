package com.example.camerademo;


import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageCaptureManager {
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
    private HandlerThread handlerThread;
    private Handler backgroundHandler;
    private ImageReader[] imageReaders;
    private Size[] imagesDimensions;

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
        this.imageReaders=new ImageReader[totalCameras];
        this.backCameraIds=findIDs(this.backCams,CameraCharacteristics.LENS_FACING_BACK,this.cameraCharacteristics[0]);
        this.frontCameraIds=findIDs(this.frontCams,CameraCharacteristics.LENS_FACING_FRONT,this.cameraCharacteristics[0]);
        int k=0;
        for (int i=k;i<backCams;i++)
        {
            openCamera(backCameraIds[i],i);
            k=i;
        }
        for(int i=k;i<frontCams+k;i++)
        {
            openCamera(frontCameraIds[i],i);
        }




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
    private void openCamera(String cameraId,int index){
        try {//try for opening camera handling permission


            cameraCharacteristics[index]=cameraManager.getCameraCharacteristics(cameraId);
            imagesDimensions[index]=cameraCharacteristics[index].get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
            //setup callback function of device
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override //when called back as open, create preview
                public void onOpened(CameraDevice cameraDevice) {
                    cameraDevices[index] = cameraDevice;
                    createCameraPreview(index);//?
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


}
