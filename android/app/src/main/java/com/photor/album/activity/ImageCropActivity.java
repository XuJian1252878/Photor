package com.photor.album.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.file.FileUtils;
import com.photor.R;
import com.photor.base.activity.BaseActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import me.pqpo.smartcropperlib.view.CropImageView;

import static com.photor.base.activity.PhotoOperateResultActivity.EXTRA_ORI_IMG_PATH;

public class ImageCropActivity extends BaseActivity {

    private CropImageView ivCrop;
    private Button btnCancel;
    private Button btnCrop;
    private File mCroppedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_crop);
        ivCrop = findViewById(R.id.iv_crop);
        btnCancel = findViewById(R.id.btn_cancel);
        btnCrop = findViewById(R.id.btn_ok);


        String oriImgPath = getIntent().getStringExtra(EXTRA_ORI_IMG_PATH);
        String cropImgPath = FileUtils.generateImgEditResPath();

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


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (ivCrop.canRightCrop()) {
                    Bitmap crop = ivCrop.crop();
                    if (crop != null) {
                        saveImage(crop, mCroppedFile);
                        FileUtils.updateMediaStore(ImageCropActivity.this, mCroppedFile, null); // 添加到媒体库中
                        Toast.makeText(ImageCropActivity.this, "图片裁剪成功", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                } else {
                    Toast.makeText(ImageCropActivity.this, "无法裁剪图片", Toast.LENGTH_SHORT).show();
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
