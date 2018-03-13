package com.photor.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by xujian on 2018/3/2.
 */

public class FileUtils {

    public static String generateImgAbsPath() {
        if (TextUtils.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
            String imgFileName = "JPEG_" + timeStamp + ".jpg";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            if (!storageDir.exists()) {
                if (!storageDir.mkdirs()) {
                    Log.e("TAG", "Throwing Errors....");
                }
            }

            File image = new File(storageDir, imgFileName);
            String imgPath = image.getAbsolutePath();
            return imgPath;
        }
        return null;
    }


    public static String generateTempImgAbsPath(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imgFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.e("TAG", "Throwing Errors....");
            }
        }

        File image = new File(context.getExternalCacheDir(), imgFileName);
        String tempImgPath = image.getAbsolutePath();
        return tempImgPath;
    }


    public static boolean saveImgBitmap(String path, Bitmap bitmap) {
        File file = new File(path);
        try {
            if (!file.createNewFile()) {
                return false;
            }
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean delteFileByPath(String filePath) {
        File deleteFile = new File(filePath);
        if (deleteFile.exists()) {
            return deleteFile.delete();
        }

        return true;
    }

}
