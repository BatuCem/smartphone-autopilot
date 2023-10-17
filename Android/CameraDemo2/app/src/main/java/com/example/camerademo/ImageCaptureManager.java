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
    private CameraDevice cameraDevice;
    private CameraCharacteristics cameraCharacteristics;
    private String[] backCameraIds;
    private String[] frontCameraIds;
    private CaptureRequest.Builder captureRequestBuilders;
    private CameraCaptureSession captureSession;
    private ImageReader imageReader;

    public ImageCaptureManager(Context context,int backCams,int frontCams, int FPS)
    {
        this.context=context;
        this.backCams=backCams;
        this.frontCams=frontCams;
        this.cameraManager=(CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
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


}
