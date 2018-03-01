package com.photor.staralign;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by xujian on 2018/3/1.
 */

public class StarAlignSetting {

    public static final int REQUEST_CODE = 123; // 用于启动StarAlignResultActivity的Request Code。

    // 关于一系列启动activity的参数
    public static final String EXTRA_PHOTO_COUNT = "extra_photo_count";
    public static final int DEFAULT_PHOTO_COUNT = 9; // 最多有九张图片的限制

    public static final String EXTRA_PHOTOS = "extra_photos"; // 选择的图片路径列表
    public static final String EXTRA_BASE_PHOTO_INDEX = "extra_base_photo_index";
    public static final int DEFAULT_BASE_PHOTO_INDEX = 0;  // 默认以第几张图片作为配准


    public static final String EXTRA_ALIGN_RESULT_PATH = "extra_align_result_path"; // 对齐结果的图片路径


    public static StarAlignSettingBuilder builder() {
        return new StarAlignSettingBuilder();
    }


    private static class StarAlignSettingBuilder {
        private Bundle bundle;
        private Intent intent;

        public StarAlignSettingBuilder() {
        }

        public StarAlignSettingBuilder setPhotoCount(int photoCount) {
            bundle.putInt(EXTRA_PHOTO_COUNT, photoCount);
            return this;
        }

        public StarAlignSettingBuilder setPhotos(ArrayList<String> photos) {
            bundle.putStringArrayList(EXTRA_PHOTOS, photos);
            return this;
        }

        public StarAlignSettingBuilder setBasePhotoIndex(int basePhotoIndex) {
            bundle.putInt(EXTRA_BASE_PHOTO_INDEX, basePhotoIndex);
            return this;
        }

        public StarAlignSettingBuilder setAlignResultPath(String alignResultPath) {
            bundle.putString(EXTRA_ALIGN_RESULT_PATH, alignResultPath);
            return this;
        }

        public void start(Activity activity) {
            intent.setClass(activity, StarAlignResultActivity.class);
            intent.putExtras(bundle);
            activity.startActivityForResult(intent, REQUEST_CODE);
        }
    }

}
