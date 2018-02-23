package com.example.photopicker.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by xujian on 2018/2/6.
 */

public class ImageCaptureManager {

    private Context mContext;
    private String mCurrentPhotoPath;

    private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
    public static final int REQUEST_TAKE_PHOTO = 1;

    public ImageCaptureManager(Context mContext) {
        this.mContext = mContext;
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.e("TAG", "Throwing Errors....");
                throw new IOException();
            }
        }

        File image = new File(storageDir, imageFileName);

        // 获取当前照相机 存储图片 的绝对路径
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public Intent dispatchTakePictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // resolveActivity 确保有activity 能处理 关于照相机的intent
        // which returns the first activity component that can handle the intent. Performing this
        // check is important because if you call startActivityForResult() using an intent that no
        // app can handle, your app will crash.
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            // 打开照相机程序前 创建即将存储相片的文件
            File file = createImageFile();
            Uri photoFile;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // provider 后缀随意是什么都可以，不一定是provider
                String authority = mContext.getApplicationInfo().packageName + ".provider";
                // returns a content:// URI
                photoFile = PhotoFileProvider.getUriForFile(mContext.getApplicationContext(), authority, file);
            } else {
                // returns file:// URI
                photoFile = Uri.fromFile(file);
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile);
            }
        }
        return takePictureIntent;
    }

    // invoke the system's media scanner to add your photo to the Media Provider's database,
    // making it available in the Android Gallery application and to other apps.
    // 将图片加入MediaStore的索引中，下一次扫描MediaStore的时候就会更新加入刚刚拍的照片
    public void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        if (TextUtils.isEmpty(mCurrentPhotoPath)) {
            return;
        }

        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        mContext.sendBroadcast(mediaScanIntent);
    }


    public String getCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && mCurrentPhotoPath != null) {
            savedInstanceState.putString(CAPTURED_PHOTO_PATH_KEY, mCurrentPhotoPath);
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(CAPTURED_PHOTO_PATH_KEY)) {
            mCurrentPhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH_KEY);
        }
    }
}
