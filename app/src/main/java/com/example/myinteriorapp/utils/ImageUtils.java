package com.example.myinteriorapp.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class ImageUtils {
    public static Bitmap rotateBitmap(Bitmap bitmap, int rotationDegree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}

