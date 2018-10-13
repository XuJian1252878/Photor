package com.xinlan.imageeditlibrary.editimage.task;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

/**
 * @author htwxujian@gmail.com
 * @date 2018/10/13 14:45
 */
public class ImageProcessingTask {

    private static final String TAG = "ImageProcessing";

    // 加载native代码库
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG + " - ERROR", "Unable to load OpenCV");
        } else {
            System.loadLibrary("photoprocessing");
        }
    }


//    public static Bitmap processImage(Bitmap bitmap, int effectType, int val) {
//    }

}
