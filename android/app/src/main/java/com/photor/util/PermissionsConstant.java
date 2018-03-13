package com.photor.util;

import android.Manifest;

/**
 * Created by xujian on 2018/3/13.
 */

public class PermissionsConstant {

    public static final int REQUEST_CAMERA = 1;
    public static final int REQUEST_EXTERNAL_WRITE = 3;

    // 调用照相机
    public static final String[] PERMISSIONS_CAMERA = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String[] PERMISSIONS_EXTERNAL_WRITE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
}
