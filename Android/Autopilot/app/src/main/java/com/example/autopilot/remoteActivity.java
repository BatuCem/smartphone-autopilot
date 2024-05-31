package com.example.autopilot;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class remoteActivity extends AppCompatActivity {

    private Joystick joystick;
    private ImageView remoteView;
    private Handler handler = new Handler();
    private final static String TAG ="remoteActivity";
    public static int leftSignal,rightSignal;
    private Runnable updateRemoteView = new Runnable() {
        @Override
        public void run() {
            Bitmap bitmap = Bitmap.createBitmap(getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            joystick.update();
            joystick.draw(canvas);
            remoteView.setImageBitmap(bitmap);
            handler.postDelayed(updateRemoteView,20);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        joystick = new Joystick(getWindowManager().getDefaultDisplay().getWidth()/2,getWindowManager().getDefaultDisplay().getHeight()/2,120,400);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        remoteView = findViewById(R.id.remoteView);
        handler.postDelayed(updateRemoteView,20);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "onTouchEvent: DOWN");
                if(joystick.isPressed((double) event.getX(), (double) event.getY()))
                {
                    Log.i(TAG, "onTouchEvent: DOWNIF");
                    joystick.setIsPressed(true);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
            {
                Log.i(TAG, "onTouchEvent: MOVE");
                if(joystick.getIsPressed())
                {
                    joystick.setActuator((double) event.getX(), (double) event.getY());
                    leftSignal = Math.min(Math.max(-255,(int) (255*(-Joystick.actuatorY + Joystick.actuatorX))),255);
                    rightSignal = Math.min(Math.max(-255,(int) (255*(-Joystick.actuatorY -Joystick.actuatorX))),255);
                    Log.i(TAG, "onTouJoychEvent: MOVE"+ " "+leftSignal+" "+rightSignal);
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "onTouchEvent: UP");
                leftSignal = 0;
                rightSignal = 0;
                joystick.setIsPressed(false);
                joystick.resetActuator();
                return true;
        }
        return super.onTouchEvent(event);
    }
}