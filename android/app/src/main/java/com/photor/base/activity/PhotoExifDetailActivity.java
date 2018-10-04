package com.photor.base.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.allen.library.SuperTextView;
import com.example.media.image.MediaExifHelper;
import com.photor.R;

import static com.photor.base.activity.PhotoOperateResultActivity.EXTRA_CROP_IMG_RES_PATH;
import static com.photor.base.activity.PhotoOperateResultActivity.EXTRA_IS_SAVED_CROP_RES;
import static com.photor.base.activity.PhotoOperateResultActivity.EXTRA_ORI_IMG_PATH;
import static com.photor.base.activity.PhotoOperateResultActivity.REQUEST_IMAGE_EXIF_INFO;

public class PhotoExifDetailActivity extends AppCompatActivity {

    private SuperTextView exifDateTv;
    private SuperTextView exifLocationTv;

    private String oriImgPath;
    private String cropImgResPath;

    private boolean isSavedCropRes = false;
    private String targetImgPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exif_detail);

        // -1. 初始化action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent data = getIntent();
        oriImgPath = data.getStringExtra(EXTRA_ORI_IMG_PATH);
        cropImgResPath = data.getStringExtra(EXTRA_CROP_IMG_RES_PATH);
        isSavedCropRes = data.getBooleanExtra(EXTRA_IS_SAVED_CROP_RES, false);
        targetImgPath = !isSavedCropRes ? oriImgPath : cropImgResPath;


        // 设置时间
        exifDateTv = findViewById(R.id.image_exif_date_tv);
        exifDateTv.setLeftTextIsBold(true);
        exifDateTv.setLeftBottomString(MediaExifHelper.getPhotoTokenDate(targetImgPath));

        // 设置地点
        exifLocationTv = findViewById(R.id.image_exif_location_tv);
        exifLocationTv.setLeftTextIsBold(true);
        exifLocationTv.setLeftBottomString(MediaExifHelper.getExifLocation(targetImgPath) == null
                ? "无" : MediaExifHelper.getExifLocation(targetImgPath));

        // 设置其他的相信exif信息
        SuperTextView exifInfoTv = findViewById(R.id.image_exif_detail_tv);
        exifInfoTv.setLeftTextIsBold(true);
        exifInfoTv.setLeftBottomString(MediaExifHelper.getExifInfo(targetImgPath));

    }


    @Override
    public void onBackPressed() {
        Intent intent = generateBackIntent();
        setResult(REQUEST_IMAGE_EXIF_INFO, intent);
        finish();
        super.onBackPressed();
    }


    private Intent generateBackIntent() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ORI_IMG_PATH, oriImgPath);
        intent.putExtra(EXTRA_CROP_IMG_RES_PATH, cropImgResPath);
        return intent;
    }
}
