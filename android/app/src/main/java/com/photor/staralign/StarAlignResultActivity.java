package com.photor.staralign;

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
import com.photor.R;
import com.photor.widget.TipToast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class StarAlignResultActivity extends AppCompatActivity {

    private String alignResImgPath = null;
    private ImageView alignResImageView;

    private volatile boolean isSavedAlignRes = false;  // 表示用户是否已经存储了 图片对齐的结果

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star_align_result);

        // 获取对齐结果的图片路径
        alignResImgPath = getIntent().getStringExtra(StarAlignOperator.EXTRA_ALIGN_RESULT_PATH);
        Log.d("alignResImgPath", alignResImgPath);
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
                FileUtils.delteFileByPath(alignResImgPath);
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
        if (!isSavedAlignRes) {
            FileUtils.delteFileByPath(alignResImgPath);
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
        alignResImageView = findViewById(R.id.star_align_result_iv);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(alignResImgPath);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            alignResImageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // 3. 绑定 保存 和 删除图片按钮的事件
        findViewById(R.id.save_align_res_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSavedAlignRes = true;
//                Toast.makeText(StarAlignResultActivity.this, "图片已保存", Toast.LENGTH_SHORT).show();
                final TipToast tipToast = new TipToast.Builder(StarAlignResultActivity.this)
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

        findViewById(R.id.delete_align_res_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final TipToast tipToast = new TipToast.Builder(StarAlignResultActivity.this)
                        .setMessage("删除")
                        .create();
                tipToast.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FileUtils.delteFileByPath(alignResImgPath);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tipToast.dismiss();
                                finish();
                            }
                        });
                    }
                }, 2000);
//                Toast.makeText(StarAlignResultActivity.this, "图片已删除", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
