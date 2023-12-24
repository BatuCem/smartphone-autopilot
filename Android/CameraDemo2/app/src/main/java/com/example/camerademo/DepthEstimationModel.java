package com.example.camerademo;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;


import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.metadata.MetadataExtractor;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.support.tensorbuffer.TensorBufferFloat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DepthEstimationModel {

    private Interpreter tfliteInterpreter;
    public Map<Integer, Object> outputMap = new HashMap<>();
    private int imgDim =300;
    public DepthEstimationModel (Context context, int numThreads) throws IOException{
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
            //.add(new NormalizeOp(new float[] {123.675f ,  116.28f ,  103.53f}, new float[] {58.395f , 57.12f ,  57.375f}))
            .build();
    public Bitmap runInference(Bitmap bitmap)
    {
        long tInit= System.currentTimeMillis();
        TensorImage inputTensor = TensorImage.fromBitmap(bitmap);
        inputTensor=inputTensorProcessor.process(inputTensor);
        TensorBuffer outputTensor = TensorBufferFloat.createFixedSize(new int[] {imgDim,imgDim,1},DataType.FLOAT32);
        tfliteInterpreter.run(inputTensor.getBuffer(),outputTensor.getBuffer());
        long tFinal= System.currentTimeMillis();
        MainActivity.procTimeView.setText("Model Process Time" + Long.toString(tFinal-tInit) +"ms");
        return postShape(outputTensor.getFloatArray());

    }
    public void detectObjects(Bitmap bitmap)
    {
        long tInit= System.currentTimeMillis();
        TensorImage inputTensor = TensorImage.fromBitmap(bitmap);
        inputTensor=inputTensorProcessor.process(inputTensor);
        int NUM_DETECTIONS=10;
        float[][][] outputLocations = new float[1][NUM_DETECTIONS][4]; // Bounding box coordinates
        float[][] outputClasses = new float[1][NUM_DETECTIONS];        // Detection classes
        float[][] outputScores = new float[1][NUM_DETECTIONS];         // Confidence scores
        float[] numDetections = new float[1];                          // Number of detections

        outputMap.put(0, outputLocations);
        outputMap.put(1, outputClasses);
        outputMap.put(2, outputScores);
        outputMap.put(3, numDetections);
        tfliteInterpreter.runForMultipleInputsOutputs(new Object[]{inputTensor.getBuffer()},outputMap);
        long tFinal= System.currentTimeMillis();
        MainActivity.procTimeView.setText("Inference Time " + Long.toString(tFinal-tInit) +"ms");
    }
    private Bitmap postShape (float[] outData)
    {
        float[] normalizedDepth = new float[outData.length];
        for (int i = 0; i < outData.length; i++) {
            normalizedDepth[i] = 255 * (outData[i]); //assume num from 0 to 1 to 0 to 255
        }
        Bitmap bitmap = Bitmap.createBitmap(imgDim, imgDim, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < imgDim; y++) {
            for (int x = 0; x < imgDim; x++) {
                int depthValue = (int) normalizedDepth[y * imgDim + x];
                int pixel = 0xFF000000 | (depthValue << 16) | (depthValue << 8) | depthValue; // Gray scale
                bitmap.setPixel(x, y, pixel);
            }
        }
        return bitmap;
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

        }
        return labelList.toArray(new String[0]);
    }



}
