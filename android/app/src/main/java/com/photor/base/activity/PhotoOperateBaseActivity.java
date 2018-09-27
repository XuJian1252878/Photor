package com.photor.base.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

import com.photor.R;
import com.photor.base.adapters.PhotoAdapter;
import com.shuhart.stepview.StepView;

import java.util.ArrayList;

public class PhotoOperateBaseActivity extends AppCompatActivity {


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

        // 0. 设置图片选择的step view
        stepView = findViewById(R.id.photo_operate_step_view);
    }
}
