package com.example.camerademo;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class DepthEstimationModel {
    private Interpreter tfliteInterpreter;
    public DepthEstimationModel (Context context, int numThreads) throws IOException{
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(numThreads);
        tfliteInterpreter= new Interpreter(loadModelFile(context),options);
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("midas.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    public Bitmap runInference(Bitmap bitmap)
    {
        tfliteInterpreter.inp
        ByteBuffer inputBuffer= preShape(bitmap);
        float[][][]depthOutput = new float[1][256][256];
        tfliteInterpreter.run(inputBuffer,depthOutput);
        return postShape(depthOutput[0]);

    }
    private ByteBuffer preShape(Bitmap bitmap)
    {
        ByteBuffer byteBuffer;
        byteBuffer = ByteBuffer.allocateDirect(4*256*256*3);
        byteBuffer.order(ByteOrder.nativeOrder());
        Bitmap resizedBmp = Bitmap.createScaledBitmap(bitmap,256,256,false);
        int[] intValues = new int[256 * 256];
        resizedBmp.getPixels(intValues, 0, resizedBmp.getWidth(), 0, 0, resizedBmp.getWidth(), resizedBmp.getHeight());

        for (int value : intValues) {
            byteBuffer.putFloat(((value >> 16) & 0xFF) / 255.0f);
            byteBuffer.putFloat(((value >> 8) & 0xFF) / 255.0f);
            byteBuffer.putFloat((value & 0xFF) / 255.0f);
        }
        return byteBuffer;
    }
    private Bitmap postShape (float[] outData)
    {
        float[] normalizedDepth = new float[outData.length];
        for (int i = 0; i < outData.length; i++) {
            normalizedDepth[i] = 255 * (outData[i]); //assume num from 0 to 1 to 0 to 255
        }
        Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                int depthValue = (int) normalizedDepth[y * 256 + x];
                int pixel = 0xFF000000 | (depthValue << 16) | (depthValue << 8) | depthValue; // Gray scale
                bitmap.setPixel(x, y, pixel);
            }
        }
        return bitmap;
    }


}
