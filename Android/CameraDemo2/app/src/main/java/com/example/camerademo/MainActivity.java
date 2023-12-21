package com.example.camerademo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
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
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


//Main Class
public class MainActivity extends AppCompatActivity {
    ////////////////////////////////DECLARE VARIABLES//////////////////////////////////
    private static final String TAG="AndroidCameraApi";//error handling tag
    private ImageCaptureManager imageCaptureManager;
    private DepthEstimationModel depthEstimationModel;
    private TextureView textureView;
    public static TextView procTimeView;
    public static ImageView imageView;
    private Image[] images;
    private String[] backCameraIds;
    private String[] frontCameraIds;
    private Handler handler;
    private HandlerThread handlerThread=new HandlerThread("Main Thread");
    public static final int REQUEST_CAMERA_PERMISSION = 200;//code for camera permit
    private Paint paint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set to main layout
        setContentView(R.layout.activity_main);
        //set up texture view windows
        textureView = findViewById(R.id.textureView);
        procTimeView=findViewById(R.id.procTimeView);
        imageView=findViewById(R.id.imageView);



        //ask for necessary permissions (CAMERA)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }
        imageCaptureManager = new ImageCaptureManager(getBaseContext(), 2, 1, 0);
        backCameraIds = imageCaptureManager.getCameraIds(true);
        frontCameraIds = imageCaptureManager.getCameraIds(false);
        handlerThread.start();
        handler= new Handler();
        try {
            depthEstimationModel = new DepthEstimationModel(this, 4);
        } catch(IOException e)
        {
            Log.e(TAG, "onCreate: IOEX_depthEst" );
        }

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
                //imageCaptureManager.processImage(bitmap);
                depthEstimationModel.detectObjects(bitmap);
                Bitmap mutable = bitmap.copy(Bitmap.Config.ARGB_8888,true);
                //imageView.setImageBitmap(depthEstimationModel.runInference(bitmap));
                Canvas canvas = new Canvas(mutable);
                paint.setTextSize(mutable.getHeight()/15f);
                paint.setStrokeWidth(mutable.getHeight()/85f);
                paint.setColor(Color.RED);
                for(int i=0;i<10;i++)
                {
                    if(((float [][]) depthEstimationModel.outputMap.get(2))[0][i]>=0.5)//check scores
                    {
                        paint.setStyle(Paint.Style.STROKE);
                        float[4] rectArray = depthEstimationModel.outputMap.get(0)[0][i];
                    }
                }



            }

        });




    }
    }
