package com.xinlan.imageeditlibrary.editimage.task;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

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


    public static Bitmap processImage(Bitmap bitmap, int effectType, int val) {
        Mat inputMat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC3);
        Mat outputMat = new Mat();
        Utils.bitmapToMat(bitmap, inputMat);

        if (isEnhance(effectType)) {
            nativeEnhanceImage(effectType % 100, val, inputMat.getNativeObjAddr(), outputMat.getNativeObjAddr());
        }

        inputMat.release();

        if (outputMat != null) {
            Bitmap outbit = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            Utils.matToBitmap(outputMat, outbit);
            outputMat.release();
            return outbit;
        }

        return bitmap.copy(bitmap.getConfig(), true);
    }

    private static boolean isEnhance(int effectType) {
        return (effectType / 300 == 0);
    }

    private static native void nativeEnhanceImage(int mode, int val, long inpAddr, long outAddr);

}
