package com.photor.base.activity;

import android.Manifest;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.photor.MainApplication;
import com.photor.album.entity.Album;
import com.photor.album.entity.HandlingAlbums;
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

    /**
     * 如果当前Album数量不为0，那么获取当前选择的或者默认第一个的album信息；否则，返回一个空的album（Setting信息为默认）
     * @return
     */
    public Album getAlbum() {
        return ((MainApplication) getApplicationContext()).getAlbum();
    }

    /**
     * 用于获取全部的相册信息
     * @return
     */
    public HandlingAlbums getAlbums() {
        return ((MainApplication) getApplicationContext()).getAlbums();
    }
}
