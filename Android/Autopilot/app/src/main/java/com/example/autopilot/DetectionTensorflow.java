package com.example.autopilot;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetectionTensorflow {
    private static final String TAG="DetectionTensorflow";//logging tag for class
    private Interpreter tfliteInterpreter;
    public Map<Integer, Object> outputMap = new HashMap<>();
    private int imgDim =300;
    public DetectionTensorflow (Context context, int numThreads) throws IOException {
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(numThreads);
        MappedByteBuffer modelFile=loadModelFile(context);
        tfliteInterpreter= new Interpreter(modelFile,options);
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("1.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    private ImageProcessor inputTensorProcessor = new ImageProcessor.Builder()
            .add(new ResizeOp(imgDim,imgDim, ResizeOp.ResizeMethod.BILINEAR))
            .add(new NormalizeOp(new float[] {123.675f ,  116.28f ,  103.53f}, new float[] {58.395f , 57.12f ,  57.375f}))
            .build();
    public void detectObjects(Bitmap bitmap)
    {
        long tInit= System.currentTimeMillis();
        TensorImage inputTensor = TensorImage.fromBitmap(bitmap);   //get image from bitmap
        inputTensor=inputTensorProcessor.process(inputTensor);      //resize and normalize tensor
        int NUM_DETECTIONS=10;      //dimensions to create output arrays
        float[][][] outputLocations = new float[1][NUM_DETECTIONS][4]; // Bounding box coordinates
        float[][] outputClasses = new float[1][NUM_DETECTIONS];        // Detection classes
        float[][] outputScores = new float[1][NUM_DETECTIONS];         // Confidence scores
        float[] numDetections = new float[1];                          // Number of detections

        outputMap.put(0, outputLocations);
        outputMap.put(1, outputClasses);
        outputMap.put(2, outputScores);
        outputMap.put(3, numDetections);
        tfliteInterpreter.runForMultipleInputsOutputs(new Object[]{inputTensor.getBuffer()},outputMap);//TODO: use with multi-objects or simplify expression
        long tFinal= System.currentTimeMillis();
        Log.i(TAG, "detectObjects: TIMEMEASURED (ms) " + Long.toString(tFinal-tInit));
    }

    public String[] loadLabels(Context context)
    {
        List<String> labelList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("labelmap.txt"))))
        {
            String line;
            while((line=reader.readLine())!=null)
            {
                labelList.add(line);
            }
        }catch (IOException e)
        {
            Log.e(TAG, "loadLabels: " );
        }
        return labelList.toArray(new String[0]);
    }
}
