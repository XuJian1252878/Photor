package com.photor.base.activity;

import android.Manifest;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(newBase);
        // 对 activity 添加图标支持
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }
}
