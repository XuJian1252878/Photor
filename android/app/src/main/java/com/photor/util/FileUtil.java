package com.photor.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by xujian on 2018/3/2.
 */

public class FileUtil {

    public static String generateImgAbsPath() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imgFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.e("TAG", "Throwing Errors....");
                throw new IOException();
            }
        }

        File image = new File(storageDir, imgFileName);
        String imgPath = image.getAbsolutePath();
        return imgPath;
    }


    public static boolean delteFileByPath(String filePath) {
        File deleteFile = new File(filePath);
        if (deleteFile.exists()) {
            return deleteFile.delete();
        }

        return true;
    }

}
