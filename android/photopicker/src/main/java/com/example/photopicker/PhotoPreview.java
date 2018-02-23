package com.example.photopicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

/**
 * 为打开预览图片的activity 构建intent （builder类构建）
 * Created by xujian on 2018/1/9.
 */

public class PhotoPreview {

    // 调用方 startActivityForResult的 request code
    public static final int REQUEST_CODE = 666;

    // startActivityForResult 传入的数据传输key
    public final static String EXTRA_CURRENT_ITEM = "current_item";
    public final static String EXTRA_PHOTOS = "photos";
    public final static String EXTRA_SHOW_DELETE = "show_delete";

    public final static PhotoPreviewBuilder builder() {
        return new PhotoPreviewBuilder();
    }


    public static class PhotoPreviewBuilder {

        private Bundle mPreviewOptionsBundle;
        private Intent mPreviewIntent;

        public PhotoPreviewBuilder() {
            mPreviewIntent = new Intent();
            mPreviewOptionsBundle = new Bundle();
        }


        /**
         * 用一个自定义的request code来启动ativity
         * Send the Intent from an Activity with a custom request code
         * @param activity    Activity to receive result
         * @param requestCode requestCode for result
         */
        public void start(@NonNull Activity activity, int requestCode) {
            activity.startActivityForResult(getIntent(activity), requestCode);
        }

        /**
         * Send the Intent with a custom request code
         * @param context   Fragment 对应的activity获取的context（Fragment也可以直接获取）
         * @param fragment    Fragment to receive result
         * @param requestCode requestCode for result
         */
        public void start(@NonNull Context context, @NonNull Fragment fragment, int requestCode) {
            fragment.startActivityForResult(getIntent(context), requestCode);
        }

        /**
         *
         * @param context
         * @param fragment
         */
        public void start(@NonNull Context context, @NonNull Fragment fragment) {
            fragment.startActivityForResult(getIntent(context), REQUEST_CODE);
        }

        /**
         * Send the crop Intent from an Activity
         *
         * @param activity Activity to receive result
         */
        public void start(@NonNull Activity activity) {
            start(activity, REQUEST_CODE);
        }

        /**
         * Get Intent to start {@link PhotoPickerActivity}
         *
         * @return Intent for {@link PhotoPickerActivity}
         */
        public Intent getIntent(@NonNull Context context) {
            mPreviewIntent.setClass(context, PhotoPagerActivity.class);
            mPreviewIntent.putExtras(mPreviewOptionsBundle);
            return mPreviewIntent;
        }

        public PhotoPreviewBuilder setPhotos(ArrayList<String> photos) {
            mPreviewOptionsBundle.putStringArrayList(EXTRA_PHOTOS, photos);
            return this;
        }

        public PhotoPreviewBuilder setCurrentItem(int currentItem) {
            mPreviewOptionsBundle.putInt(EXTRA_CURRENT_ITEM, currentItem);
            return this;
        }

        public PhotoPreviewBuilder setShowDeleteButton(boolean showDeleteButton) {
            mPreviewOptionsBundle.putBoolean(EXTRA_SHOW_DELETE, showDeleteButton);
            return this;
        }
    }

}
