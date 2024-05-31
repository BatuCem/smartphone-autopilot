package com.example.autopilot;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
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
    //Class to handle Detection ML
    private static final String TAG="DetectionTensorflow";//logging tag for class
    private Interpreter tfliteInterpreter;
    public Map<Integer, Object> outputMap = new HashMap<>();
    private static int imgDim;
    public static String[] labels;

    private ImageProcessor inputTensorProcessor;
    public DetectionTensorflow (Context context, int numThreads) throws IOException
    {
        //build class
        //TODO: addDelegate option for extended operation with GPU
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(numThreads); //get interpreter options and set thread numbers
        MappedByteBuffer modelFile=loadModelFile(context);  //load the model file
        tfliteInterpreter= new Interpreter(modelFile,options);  //build interpreter from file and options
        imgDim = tfliteInterpreter.getInputTensor(0).shape()[1];
        labels=loadLabels(context);inputTensorProcessor = new ImageProcessor.Builder()  //build image processor
            .add(new ResizeOp(imgDim,imgDim, ResizeOp.ResizeMethod.BILINEAR))   //resize to model size
            //.add(new NormalizeOp(new float[] {123.675f ,  116.28f ,  103.53f}, new float[] {58.395f , 57.12f ,  57.375f}))  //normalize wrt model data
            .build();   //TODO: find a way to get normalization parametric to extend to different ML models, note NormalizeOp is also NOT a MUST-HAVE
        Log.i(TAG, "DetectionTensorflow: SUCCESSFUL" );

    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        //method to load model from file in assets
        //TODO: remove hardcoded file to extend functionality
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("SSDMobileNetV1.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    public void detectObjects(Bitmap bitmap)
    {
        //method to detect objects with ML model from a given bitmap
        //TODO: Is returning outputMap a better way than making public variable?, if so, implement
        long tInit= System.currentTimeMillis(); //initial time of measurement for interpretation
        TensorImage inputTensor = TensorImage.fromBitmap(bitmap);   //get image from bitmap
        inputTensor=inputTensorProcessor.process(inputTensor);      //resize and normalize tensor
        int NUM_DETECTIONS=10;      //dimensions to create output arrays
        float[][][] outputLocations = new float[1][NUM_DETECTIONS][4]; // Bounding box coordinates
        float[][] outputClasses = new float[1][NUM_DETECTIONS];        // Detection classes
        float[][] outputScores = new float[1][NUM_DETECTIONS];         // Confidence scores
        float[] numDetections = new float[1];                          // Number of detections
        //map outputs for public var
        outputMap.put(0, outputLocations);
        outputMap.put(1, outputClasses);
        outputMap.put(2, outputScores);
        outputMap.put(3, numDetections);
        tfliteInterpreter.runForMultipleInputsOutputs(new Object[]{inputTensor.getBuffer()},outputMap);//TODO: use with multi-objects or simplify expression
        long tFinal= System.currentTimeMillis(); //take interpret end time
        Log.i(TAG, "detectObjects: INTERPRET SUCCESSFUL w/ TIMEMEASURED (ms) " + Long.toString(tFinal-tInit));
    }

    public String[] loadLabels(Context context)
    {
        //method to load ML model labels (person, car etc.) from given txt file
        //TODO: remove hardcoded file to extend capability
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
        Log.i(TAG, "loadLabels complete ");
        return labelList.toArray(new String[0]);
    }
}
