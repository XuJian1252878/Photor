package com.photor.base.activity;

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

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static com.photor.base.activity.util.PhotoOperator.EXTRA_PHOTO_OPERATE_RESULT_PATH;

public class PhotoOperateResultActivity extends AppCompatActivity {

    private String resImgPath = null;
    private ImageView resImageView;

    private volatile boolean isSavedOperateRes = false;  // 表示用户是否已经存储了 图片对齐的结果

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

    private void initUI() {
        // 1. 初始化 toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 2. 加载结果图片
        resImageView = findViewById(R.id.operate_result_iv);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(resImgPath);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            resImageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // 3. 绑定 保存 和 删除图片按钮的事件
        findViewById(R.id.operate_res_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSavedOperateRes = true;
                // 将图片添加入MediaStore的索引中
                MediaManager.galleryAddMedia(PhotoOperateResultActivity.this, resImgPath);
//                Toast.makeText(PhotoOperateResultActivity.this, "图片已保存", Toast.LENGTH_SHORT).show();
                final TipToast tipToast = new TipToast.Builder(PhotoOperateResultActivity.this)
                        .setMessage("保存")
                        .create();
                tipToast.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tipToast.dismiss();
                                finish();
                            }
                        });
                    }
                }, 2000);
            }
        });

        findViewById(R.id.delete_operate_res_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final TipToast tipToast = new TipToast.Builder(PhotoOperateResultActivity.this)
                        .setMessage("删除")
                        .create();
                tipToast.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FileUtils.deleteFileByPath(resImgPath);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tipToast.dismiss();
                                finish();
                            }
                        });
                    }
                }, 2000);
//                Toast.makeText(PhotoOperateResultActivity.this, "图片已删除", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
