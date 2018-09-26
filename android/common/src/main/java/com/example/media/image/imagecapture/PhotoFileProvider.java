package com.example.media.image.imagecapture;

import android.support.v4.content.FileProvider;

/**
 * Created by xujian on 2018/2/7.
 */

// ﻿同一个项目的范围之内，provider的名称不能出现重复，比如两段provider声明分别处于不同的模块中，但是因为名称相同，会出现报错。
// 所以这里为自己的模块单独定义一个provider类，作为这个模块peovider的名称

public class PhotoFileProvider extends FileProvider {
}
