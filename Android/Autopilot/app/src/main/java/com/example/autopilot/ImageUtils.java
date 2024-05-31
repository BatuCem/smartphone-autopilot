package com.example.autopilot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.Image;
import android.util.Log;

import java.nio.ByteBuffer;

public class ImageUtils {
    //Method to include image utilities such as conversion from formats or conversion from image gui types
    private static final String TAG ="ImageUtils";
    public static float rectXCenter, rectArea;
    public static Bitmap imageToBitmap(Image image) {
        //method to get Image type to Bitmap
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
    }
    public static Bitmap drawRectF(Bitmap bitmap,float confidence,DetectionTensorflow detectionTensorflow, Paint paint)
    {
        long tinit=System.currentTimeMillis();
        Bitmap mutable = bitmap.copy(Bitmap.Config.ARGB_8888,true);
        //imageView.setImageBitmap(depthEstimationModel.runInference(bitmap));
        Canvas canvas = new Canvas(mutable);
        paint.setTextSize(mutable.getHeight()/15f);
        paint.setStrokeWidth(mutable.getHeight()/100f);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        float[][] rectArray = ((float[][][])detectionTensorflow.outputMap.get(0))[0];
        RectF[] rawDetectionArray = new RectF[10];
        for(int i = 0; i<10;i++)
        {
            if(((float [][]) detectionTensorflow.outputMap.get(2))[0][i]>=confidence && (int)(((float[][]) detectionTensorflow.outputMap.get(1))[0][i])==SettingsActivity.detectionType)
            {
                rawDetectionArray[i] = new RectF(rectArray[i][1],rectArray[i][0],rectArray[i][3],rectArray[i][2]);
                if((rawDetectionArray[i].centerX()<rectXCenter*1.2 && rawDetectionArray[i].centerX()>rectXCenter*0.8))
                        {
                            RectF detection=new RectF(rectArray[i][1]*bitmap.getWidth(),rectArray[i][0]*bitmap.getHeight(),rectArray[i][3]*bitmap.getWidth(),rectArray[i][2]*bitmap.getHeight());
                            rectXCenter=rawDetectionArray[i].centerX();
                            rectArea=(rawDetectionArray[i].right-rawDetectionArray[i].left)*(rawDetectionArray[i].bottom-rawDetectionArray[i].top);
                            canvas.drawRect(detection,paint);
                            paint.setStyle(Paint.Style.FILL);
                            paint.setTextSize(50f);
                            canvas.drawText(DetectionTensorflow.labels[(int)(((float[][]) detectionTensorflow.outputMap.get(1))[0][i])]+" "+Float.toString(((float[][]) detectionTensorflow.outputMap.get(2))[0][i]),rectArray[i][1]*bitmap.getWidth() ,rectArray[i][0]*bitmap.getHeight()-10,paint);
                            Log.i(TAG, "drawRectF: timemeasured"+(System.currentTimeMillis()-tinit));
                            return Bitmap.createBitmap(mutable);  //ensure only one object is tracked
                        }
            }
        }
        for(int i=0;i<10;i++)
        {
            if(((float [][]) detectionTensorflow.outputMap.get(2))[0][i]>=confidence && (int)(((float[][]) detectionTensorflow.outputMap.get(1))[0][i])==SettingsActivity.detectionType)//check scores and object type search condition
            {
                paint.setStyle(Paint.Style.STROKE);
                RectF rawDetection=new RectF(rectArray[i][1],rectArray[i][0],rectArray[i][3],rectArray[i][2]);
                RectF detection=new RectF(rectArray[i][1]*bitmap.getWidth(),rectArray[i][0]*bitmap.getHeight(),rectArray[i][3]*bitmap.getWidth(),rectArray[i][2]*bitmap.getHeight());
                rectXCenter=rawDetection.centerX();
                rectArea=(rawDetection.right-rawDetection.left)*(rawDetection.bottom-rawDetection.top);
                canvas.drawRect(detection,paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(50f);
                canvas.drawText(DetectionTensorflow.labels[(int)(((float[][]) detectionTensorflow.outputMap.get(1))[0][i])]+" "+Float.toString(((float[][]) detectionTensorflow.outputMap.get(2))[0][i]),rectArray[i][1]*bitmap.getWidth() ,rectArray[i][0]*bitmap.getHeight()-10,paint);
                break;  //ensure only one object is tracked
            }
        }
        Log.i(TAG, "drawRectF: timemeasured"+(System.currentTimeMillis()-tinit));

        return Bitmap.createBitmap(mutable);
        //return mutable;
    }

}
