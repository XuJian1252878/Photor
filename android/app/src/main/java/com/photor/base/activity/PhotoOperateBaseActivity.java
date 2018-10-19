package com.photor.base.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.example.photopicker.PhotoPicker;
import com.example.photopicker.PhotoPreview;
import com.photor.R;
import com.photor.base.adapters.PhotoAdapter;
import com.shuhart.stepview.StepView;

import java.util.ArrayList;
import java.util.List;

/**
 * 作为图像分割、曝光合成、景深合成的公共Activity父类
 */

public class PhotoOperateBaseActivity extends BaseActivity {


    protected PhotoAdapter photoAdapter;
    protected ArrayList<String> selectedPhotos = new ArrayList<>();

    protected Button operateBtn;
    protected RecyclerView recyclerView;
    protected StepView stepView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_operate_base);

        initUI();
    }

    private void initUI() {
        // -1. 初始化action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK &&
                (PhotoPicker.REQUEST_CODE == requestCode || PhotoPreview.REQUEST_CODE == requestCode)) {
            // 说明是选择图片或者是预览图片产生的结果
            List<String> photos = null;

            if (data != null) {
                photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
            }
            selectedPhotos.clear();
            if (photos != null) {
                selectedPhotos.addAll(photos);
            }
            photoAdapter.notifyDataSetChanged();
        }
    }
}
