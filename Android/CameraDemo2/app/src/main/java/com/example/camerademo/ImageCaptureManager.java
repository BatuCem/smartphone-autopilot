package com.example.camerademo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.ImageAnalysisConfig;
import androidx.camera.core.impl.ImageCaptureConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

public class ImageCaptureManager {
    private final Context context;
    private final List<Bitmap> capturedImages= new ArrayList<>();
    private final int captureFps;
    private Executor cameraExecutor= Executors.newSingleThreadExecutor();
    private ImageCapture imageCapture;
    private int captureCount = 0;
    public ImageCaptureManager(Context context, int captureFps) {
        this.context = context;
        this.captureFps = captureFps;
        startCamera();
    }

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }
    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        //PreviewConfig previewConfig = new PreviewConfig.Builder().setTargetResolution(new Size(640, 480)).build();
        Preview preview = new Preview.Builder().setTargetResolution(new Size(640,480)).build();

        imageCapture = new ImageCapture.Builder().build();

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        HandlerThread handlerThread = new HandlerThread("ImageCaptureThread");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());



        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(640,480)).build();
        imageAnalysis.setAnalyzer(cameraExecutor, this::onImageAnalyzed);

        cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview, imageCapture, imageAnalysis);
    }
    private void onImageAnalyzed(ImageProxy imageProxy) {
        if (captureCount % captureFps == 0) {
            Image image = imageProxy.getImage();
            if (image != null) {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                capturedImages.add(bitmap);
            }
        }

        imageProxy.close();
    }
    public List<Bitmap> getCapturedImages() {
        return capturedImages;
    }
}
