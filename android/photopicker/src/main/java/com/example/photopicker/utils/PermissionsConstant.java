package com.example.photopicker.utils;

import android.Manifest;

/**
 * Created by xujian on 2018/1/8.
 */

public class PermissionsConstant {

    // 申请运行权限的时候 申请结果的返回码
    public static final int REQUEST_CAMERA = 1;
    public static final int REQUEST_EXTERNAL_READ = 2;
    public static final int REQUEST_EXTERNAL_WRITE = 3;

    // 定义每一项操作所需要的权限

    // 调用照相机
    public static final String[] PERMISSIONS_CAMERA = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String[] PERMISSIONS_EXTERNAL_WRITE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String[] PERMISSIONS_EXTERNAL_READ = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

}
