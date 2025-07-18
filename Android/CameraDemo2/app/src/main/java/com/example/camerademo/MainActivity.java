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
import android.graphics.Matrix;
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
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


//Main Class
public class MainActivity extends AppCompatActivity {
    ////////////////////////////////DECLARE VARIABLES//////////////////////////////////
    private static final String TAG="AndroidCameraApi";//error handling tag
    private ImageCaptureManager imageCaptureManager;
    private DepthEstimationModel depthEstimationModel;
    private TextureView textureView;
    private TextureView textureView2;
    public static TextView procTimeView;
    public static ImageView imageView;
    private Image[] images;
    private String[] backCameraIds;
    private String[] frontCameraIds;
    private Handler handler;
    private HandlerThread handlerThread=new HandlerThread("Main Thread");
    public static final int REQUEST_CAMERA_PERMISSION = 200;//code for camera permit
    private Paint paint;
    private String[] labels;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler systemHandler = new Handler();
    private Runnable systemRunnable = new Runnable() {
        @Override
        public void run() {
            int[] commands = new int[2];
            commands=inferWifiCommands(50,255,180,255,255,1000);
            String commandWifi=wifiManager.commandIntegers(commands[0],commands[1]);
            requestToUrl(commandWifi);
            Log.i(TAG, "Command Sent: "+ commandWifi);

            systemHandler.postDelayed(systemRunnable,10);
        }
    };
    private float rectXCenter;
    private boolean areaInit=false;
    private float rectArea;
    private float rectAreaRef;
    private Matrix rotMatrix = new Matrix();
    private Matrix rotMatrixComplement = new Matrix();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set to main layout
        setContentView(R.layout.activity_main);
        //set up texture view windows
        textureView = findViewById(R.id.textureView);
        textureView2 = new TextureView(this);
        procTimeView=findViewById(R.id.procTimeView);
        imageView=findViewById(R.id.imageView);
        paint=new Paint();
        rotMatrix.postRotate(-90);
        rotMatrixComplement.postRotate(90);



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
            labels=depthEstimationModel.loadLabels(this);
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

                long processTime=System.currentTimeMillis();
                Bitmap bitmap = textureView.getBitmap();
                bitmap= Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),rotMatrix,true);
                //imageCaptureManager.processImage(bitmap);
                depthEstimationModel.detectObjects(bitmap);

                imageView.setImageBitmap(drawRectF(bitmap,0.5f,0));
                processTime=System.currentTimeMillis()-processTime;
                Log.i(TAG, "onSurfaceTextureUpdated: TIMEMEASURED (ms) "+ Long.toString(processTime));


            }

        });
        systemHandler.postDelayed(systemRunnable,10);




    }
    public Bitmap drawRectF(Bitmap bitmap,float confidence,int labelCondition)
    {
        Bitmap mutable = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        //imageView.setImageBitmap(depthEstimationModel.runInference(bitmap));
        Canvas canvas = new Canvas(mutable);
        paint.setTextSize(mutable.getHeight()/15f);
        paint.setStrokeWidth(mutable.getHeight()/100f);
        paint.setColor(Color.RED);
        for(int i=0;i<10;i++)
        {
            if(((float [][]) depthEstimationModel.outputMap.get(2))[0][i]>=confidence && (int)(((float[][]) depthEstimationModel.outputMap.get(1))[0][i])==labelCondition)//check scores and object type search condition
            {
                paint.setStyle(Paint.Style.STROKE);
                float[] rectArray =((float[][][])depthEstimationModel.outputMap.get(0))[0][i];
                RectF rawDetection=new RectF(rectArray[1],rectArray[0],rectArray[3],rectArray[2]);
                RectF detection=new RectF(rectArray[1]*bitmap.getWidth(),rectArray[0]*bitmap.getHeight(),rectArray[3]*bitmap.getWidth(),rectArray[2]*bitmap.getHeight());
                rectXCenter=rawDetection.centerX();
                rectArea=(rawDetection.right-rawDetection.left)*(rawDetection.bottom-rawDetection.top);
                if(areaInit==false)
                {
                    rectAreaRef=rectArea;
                    areaInit=true;
                }
                canvas.drawRect(detection,paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(50f);
                canvas.drawText(labels[(int)(((float[][]) depthEstimationModel.outputMap.get(1))[0][i])]+" "+Float.toString(((float[][]) depthEstimationModel.outputMap.get(2))[0][i]),rectArray[1]*bitmap.getWidth() ,rectArray[0]*bitmap.getHeight()-10,paint);
                break;  //ensure only one object is tracked


            }
        }
        return Bitmap.createBitmap(mutable,0,0,mutable.getWidth(),mutable.getHeight(),rotMatrixComplement,true);
        //return mutable;
    }

    void requestToUrl(String command){      //make request from given command
        ConnectivityManager connectivityManager= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);  //Get Connectivity Service
        Network network=connectivityManager.getActiveNetwork();     //Get the Active network ->needs to be non-null
        NetworkCapabilities networkCapabilities= connectivityManager.getNetworkCapabilities(network); //get Capabilities, needs to be non-null with WiFi Transport
        if(network!=null && networkCapabilities!=null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ){ //validate connection case
            executor.execute(new Runnable() {   //set execution on executor thread
                @Override
                public void run() {     //execute when running
                    wifiManager.getUrl("http://" + "192.168.4.1" + "/" + command);   //format command by given ip address on previous intent
                }
            });
        }
        else {
            Toast.makeText(MainActivity.this, "Device Not Connected!", Toast.LENGTH_SHORT).show();    //give connection error via pop-up toast
        }
    }
    private int[] inferWifiCommands(int minPwm,int maxPwm,int maxCentering, int maxAreaControl,int centerScaler,int areaScaler)
    {
        int[] commands= new int[2];
        commands[0]=0;
        commands[1]=0;
        float centering = (rectXCenter-0.5f)*2*centerScaler;
        centering=Math.min(Math.max(-maxCentering,centering),maxCentering);
        Log.i(TAG, "SpeedCtrl centering: "+Float.toString(centering));
        commands[0]=(int)(centering);
        commands[1]=(int)(-centering);
        float areaError=0.25f-rectArea;
        float speedCtrl=areaError*areaScaler;
        speedCtrl=Math.min(Math.max(-maxAreaControl,speedCtrl),maxAreaControl);
        Log.i(TAG, "SpeedCtrl area: "+Float.toString(rectArea));
        Log.i(TAG, "SpeedCtrl error: "+Float.toString(areaError));
        Log.i(TAG, "SpeedCtrl ctrl: "+Float.toString(speedCtrl));
        commands[0]+=(int)speedCtrl;
        commands[1]+=(int)speedCtrl;
        if(Math.abs(commands[0]) <=minPwm)
        {
            commands[0]=0;
        }
        if(Math.abs(commands[1])<=minPwm)
        {
            commands[1]=0;
        }
        commands[0]=Math.min(Math.max(-maxPwm,commands[0]),maxPwm);
        commands[1]=Math.min(Math.max(-maxPwm,commands[1]),maxPwm);
        return commands;
    }
    }
