package com.example.autopilot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;

import java.nio.ByteBuffer;

public class ImageUtils {
    //Method to include image utilities such as conversion from formats or conversion from image gui types
    public static Bitmap imageToBitmap(Image image) {
        //method to get Image type to Bitmap
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
    }
}
