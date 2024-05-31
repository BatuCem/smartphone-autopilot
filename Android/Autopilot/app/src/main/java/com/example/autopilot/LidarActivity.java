package com.example.autopilot;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

public class LidarActivity extends AppCompatActivity {

    public static int lidarResolution = 5;
    public static int lidarArraySize = 180/lidarResolution;
    public static int lidarLevel = 100;
    private ImageView imageView;
    private final static String TAG = "LidarActivity";
    private Handler handler = new Handler();
    private Runnable updateLidarView = new Runnable() {
        @Override
        public void run() {
            Bitmap bitmap = createLidarBitmap(MainActivity.lidarMap);
            imageView.setImageBitmap(bitmap);
            handler.postDelayed(updateLidarView,1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lidar);
        imageView = findViewById(R.id.lidarView);
        handler.postDelayed(updateLidarView,1000);
        //int[] lidar = new int[180];
        //for(int i = 0; i< lidar.length; i++)
        //{
        //    lidar[i]=i*2;
        //}
        // Create and display the bitmap
    }

    private Bitmap createLidarBitmap(int[] data) {
        int radius = 500; // Maximum radius of LiDAR data
        Bitmap bitmap = Bitmap.createBitmap(radius * 2, radius * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.BLACK); // Background color

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(20);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPoint(radius, radius, paint);
        paint.setColor(Color.WHITE); // Point color
        paint.setStrokeWidth(10);


        // Draw each point
        for (int i = 0; i < data.length; i++) {
            Log.i(TAG, "run: "+i+" "+ data[i]);
            double rad = Math.toRadians(i*lidarResolution); // Convert angle to radians
            int x = (int) (radius + 10*data[i] * Math.cos(rad));
            int y = (int) (radius - 10*data[i] * Math.sin(rad));
            canvas.drawPoint(x, y, paint); // Draw each point
        }

        return bitmap;
    }
}