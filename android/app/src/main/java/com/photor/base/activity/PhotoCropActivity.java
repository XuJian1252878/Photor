package com.photor.base.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.photor.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import me.pqpo.smartcropperlib.view.CropImageView;

import static com.example.constant.PhotoOperator.EXTRA_CROP_IMG_RES_PATH;
import static com.example.constant.PhotoOperator.EXTRA_ORI_IMG_PATH;


public class PhotoCropActivity extends AppCompatActivity {


    CropImageView ivCrop;
    Button btnCancel;
    Button btnOk;

    File mCroppedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_crop);
        ivCrop = findViewById(R.id.iv_crop);
        btnCancel = findViewById(R.id.btn_cancel);
        btnOk = findViewById(R.id.btn_ok);


        String oriImgPath = getIntent().getStringExtra(EXTRA_ORI_IMG_PATH);
        String cropImgPath = getIntent().getStringExtra(EXTRA_CROP_IMG_RES_PATH);
        mCroppedFile = new File(cropImgPath);  // 设置保存裁剪结果的图片文件

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(oriImgPath, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateSampleSize(options);
        Bitmap selectedBitmap = BitmapFactory.decodeFile(oriImgPath, options);

        if (selectedBitmap != null) {
            // 开始裁剪操作
            ivCrop.setImageToCrop(selectedBitmap);
        }


        final Intent resIntent = new Intent();
        resIntent.putExtra(EXTRA_ORI_IMG_PATH, oriImgPath);
        resIntent.putExtra(EXTRA_CROP_IMG_RES_PATH, cropImgPath);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, resIntent);
                finish();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (ivCrop.canRightCrop()) {
                    Bitmap crop = ivCrop.crop();
                    if (crop != null) {
                        saveImage(crop, mCroppedFile);
                        setResult(RESULT_OK, resIntent);
                    } else {
                        setResult(RESULT_CANCELED, resIntent);
                    }
                    finish();
                } else {
                    Toast.makeText(PhotoCropActivity.this, "cannot crop correctly", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    private void saveImage(Bitmap bitmap, File saveFile) {
        try {
            FileOutputStream fos = new FileOutputStream(saveFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int calculateSampleSize(BitmapFactory.Options options) {
        int outHeight = options.outHeight;
        int outWidth = options.outWidth;
        int sampleSize = 1;
        int destHeight = 1000;
        int destWidth = 1000;
        if (outHeight > destHeight || outWidth > destHeight) {
            if (outHeight > outWidth) {
                sampleSize = outHeight / destHeight;
            } else {
                sampleSize = outWidth / destWidth;
            }
        }
        if (sampleSize < 1) {
            sampleSize = 1;
        }
        return sampleSize;
    }


}
