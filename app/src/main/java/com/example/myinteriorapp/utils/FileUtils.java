package com.example.myinteriorapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    public static File saveBitmapToFile(Context context, Bitmap bitmap, String fileName) {
        File file = createImageFile(context, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static File createImageFile(Context context, String fileName) {
        String root = Environment.getExternalStorageDirectory().toString();
        File directory = new File(root + "/YourAppDirectoryName");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return new File(directory, fileName);
    }

    public static Bitmap loadBitmapFromFile(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }
}

