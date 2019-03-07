package com.example.focusstackinglib;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.List;

/**
 * @author htwxujian@gmail.com
 * @date 2018/10/18 11:41
 */
public class FocusStackProcessing {

    private static final String TAG = "FocusStack";
    private static native boolean nativeFocusStackImage(List<String> inputImagePaths, long outAddr, int bg_threshold, short kernels_size, float gaussian_sigma, String resImagePath);

    // 加载native代码库
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG + " - ERROR", "Unable to load OpenCV");
        } else {
            System.loadLibrary("focusstackinglib");
        }
    }

    /**
     * 景深合成函数
     * @param inputImagePaths
     * @param bg_threshold
     * @param kernels_size
     * @param gaussian_sigma
     * @return
     */
    public static Bitmap processImage(List<String> inputImagePaths, int bg_threshold, short kernels_size, float gaussian_sigma, String resImgPath) {
        if (inputImagePaths == null || inputImagePaths.size() <= 0) {
            return null;
        }
        // 获得原始图片的长宽等信息，以第一张图片为准
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(inputImagePaths.get(0), options);

        Mat outputMat = new Mat();
        nativeFocusStackImage(inputImagePaths, outputMat.getNativeObjAddr(), bg_threshold, kernels_size, gaussian_sigma, resImgPath);

        if (outputMat != null) {
            Bitmap outBit = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            Utils.matToBitmap(outputMat, outBit);
            outputMat.release();
            return outBit;
        }
        return null;
    }

    /**
     * 景深合成的native函数
     * @param inputImagePaths
     * @param outAddr
     * @param kernels_size
     * @param gaussian_sigma
     * @return
     */
    private static native boolean nativeFocusStackImage1(List<String> inputImagePaths, long outAddr, short kernels_size, float gaussian_sigma, String resImagePath);

}
