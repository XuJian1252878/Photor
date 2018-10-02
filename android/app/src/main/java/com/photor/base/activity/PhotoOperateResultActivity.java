package com.photor.base.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.file.FileUtils;
import com.example.media.MediaManager;
import com.example.media.image.imagecapture.ImageCaptureManager;
import com.photor.R;
import com.photor.widget.TipToast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static com.photor.base.activity.util.PhotoOperator.EXTRA_PHOTO_OPERATE_RESULT_PATH;

public class PhotoOperateResultActivity extends AppCompatActivity {

    public final static int REQUEST_IMAGE_CROP_FILE_PATH = 789;

    public final static String EXTRA_CROP_IMG_RES_PATH = "EXTRA_CROP_IMG_RES_PATH";
    public String cropImgResPath = null;

    public static final String EXTRA_ORI_IMG_PATH = "EXTRA_ORI_IMG_PATH";
    private String resImgPath = null;
    private ImageView resImageView;


    private volatile boolean isSavedOperateRes = false;  // 表示用户是否已经存储了 图片对齐的结果
    private volatile boolean isSavedCropRes = false; // 表示用户是否已经成功进行了图片裁剪操作

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_operate_result);

        // 获取对齐结果的图片路径
        resImgPath = getIntent().getStringExtra(EXTRA_PHOTO_OPERATE_RESULT_PATH);
        Log.d("resImgPath", resImgPath);
        initUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FileUtils.deleteFileByPath(resImgPath);
                Toast.makeText(this, getText(R.string.sky_ground_align_save_failed), Toast.LENGTH_SHORT).show();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 返回按键按下的时候，如果用户没有进行 对齐结果图片的操作，那么删除图片
        if (!isSavedOperateRes) {
            FileUtils.deleteFileByPath(resImgPath);
        }
    }

    private void displayPhotoInImageView(int ivId, String imgPath) {
        ImageView iv = findViewById(ivId);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(imgPath);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            iv.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        // 1. 初始化 toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 2. 加载结果图片
        displayPhotoInImageView(R.id.operate_result_iv, resImgPath);

        // 3. 绑定 保存 和 删除图片按钮的事件
        findViewById(R.id.operate_res_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSavedOperateRes = true;
//                Toast.makeText(PhotoOperateResultActivity.this, "图片已保存", Toast.LENGTH_SHORT).show();
                final TipToast tipToast = new TipToast.Builder(PhotoOperateResultActivity.this)
                        .setMessage("保存")
                        .create();
                tipToast.show();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {

                        if (!isSavedCropRes) {
                            // 将原始图片添加入MediaStore的索引中
                            // 没有进行图片裁剪的情况下
                            MediaManager.galleryAddMedia(PhotoOperateResultActivity.this, resImgPath);
                        } else {
                            // 进行图片裁剪的情况下（裁剪的图片在裁剪的activity中已经保存）
                            // 原始的结果图片在上一个activity中也已经保存，所以需要删除原始图片文件
                            FileUtils.deleteFileByPath(resImgPath);
                            // 将裁剪之后图片添加入MediaStore的索引中
                            MediaManager.galleryAddMedia(PhotoOperateResultActivity.this, cropImgResPath);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tipToast.dismiss();
                                finish();
                            }
                        });
                    }
                });
            }
        });

        findViewById(R.id.delete_operate_res_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final TipToast tipToast = new TipToast.Builder(PhotoOperateResultActivity.this)
                        .setMessage("删除")
                        .create();
                tipToast.show();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {

                        FileUtils.deleteFileByPath(resImgPath);
                        FileUtils.deleteFileByPath(cropImgResPath);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tipToast.dismiss();
                                finish();
                            }
                        });
                    }
                });
//                Toast.makeText(PhotoOperateResultActivity.this, "图片已删除", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. 绑定裁剪按钮的响应事件
        findViewById(R.id.operate_crop_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropImgResPath = FileUtils.generateImgAbsPath();
                Intent intent = new Intent();
                intent.putExtra(EXTRA_CROP_IMG_RES_PATH, cropImgResPath);
                intent.putExtra(EXTRA_ORI_IMG_PATH, resImgPath);
                intent.setClass(PhotoOperateResultActivity.this, PhotoCropActivity.class);
                startActivityForResult(intent, REQUEST_IMAGE_CROP_FILE_PATH);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_IMAGE_CROP_FILE_PATH:
                if (resultCode == RESULT_CANCELED) {
                    isSavedCropRes = false;
                    resImgPath = data.getStringExtra(EXTRA_ORI_IMG_PATH);
                    displayPhotoInImageView(R.id.operate_result_iv, resImgPath);
                    return;  // 说明取消了裁剪操作
                } else if (resultCode == RESULT_OK) {
                    cropImgResPath = data.getStringExtra(EXTRA_CROP_IMG_RES_PATH);
                    isSavedCropRes = true;
                    // 说明已经进行了裁剪操作，将要在界面上显示裁剪之后的图片信息
                    displayPhotoInImageView(R.id.operate_result_iv, cropImgResPath);
                }
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
