package com.example.photopicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;


import com.example.permissions.PermissionsUtils;

import java.util.ArrayList;

import static com.example.media.image.PhotoPicker.EXTRA_GRID_COLUMN;
import static com.example.media.image.PhotoPicker.EXTRA_MAX_COUNT;
import static com.example.media.image.PhotoPicker.EXTRA_ORIGINAL_PHOTOS;
import static com.example.media.image.PhotoPicker.EXTRA_PREVIEW_ENABLED;
import static com.example.media.image.PhotoPicker.EXTRA_SHOW_CAMERA;
import static com.example.media.image.PhotoPicker.EXTRA_SHOW_GIF;

/**
 * 用于方便创建 intent 的Builder类
 * Created by xujian on 2018/1/8.
 */

public class PhotoPicker {

    /**
     * 把启动一个activity需要用到的请求码，传输数据的标签（intent中的key -- 启动 和 返回）
     * intent的生成全部整合到一起。
     * */
    // 这个是启动activity的请求码，由于识别是哪一个activity返回的数据。用于startActivityForResult
    public static final int REQUEST_CODE = 233;
    // activity返回时，存放返回数据的key
    public static final String KEY_SELECTED_PHOTOS = "SELECTED_PHOTOS";

//    public static final int DEFAULT_MAX_COUNT = 9;
//    public static final int DEFAULT_COLUMN_NUMBER = 3;
//
//    // intent 需要填充的key
//    public static final String EXTRA_MAX_COUNT = "MAX_COUNT";
//    public final static String EXTRA_SHOW_CAMERA     = "SHOW_CAMERA";
//    public final static String EXTRA_SHOW_GIF        = "SHOW_GIF";
//    public final static String EXTRA_GRID_COLUMN     = "column";
//    public final static String EXTRA_ORIGINAL_PHOTOS = "ORIGINAL_PHOTOS";
//    public final static String EXTRA_PREVIEW_ENABLED = "PREVIEW_ENABLED";


    public static PhotoPickerBuilder builder() {
        return new PhotoPickerBuilder();
    }

    // PhotoPickerBuilder类
    public static class PhotoPickerBuilder {
        private Bundle mPickerOptionsBundle;
        private Intent mPickerIntent;

        public PhotoPickerBuilder() {
            mPickerOptionsBundle = new Bundle();
            mPickerIntent = new Intent();
        }

        /**
         * Send the Intent from an Activity with a custom request code
         *
         * @param activity    Activity to receive result
         * @param requestCode requestCode for result
         */
        public void start(@NonNull Activity activity, int requestCode) {
            if (PermissionsUtils.checkReadStoragePermission(activity)) {
                activity.startActivityForResult(getIntent(activity), requestCode);
            }
        }

        /**
         * Send the Intent with a custom request code
         * @param fragment    Fragment to receive result
         * @param requestCode requestCode for result
         */
        public void start(@NonNull Context context,
                          @NonNull Fragment fragment, int requestCode) {
            if (PermissionsUtils.checkReadStoragePermission(fragment.getActivity())) {
                fragment.startActivityForResult(getIntent(context), requestCode);
            }
        }

        /**
         * Send the Intent with a fixed request code
         * @param fragment    Fragment to receive result
         */
        public void start(@NonNull Context context,
                          @NonNull Fragment fragment) {
            if (PermissionsUtils.checkReadStoragePermission(fragment.getActivity())) {
                fragment.startActivityForResult(getIntent(context), REQUEST_CODE);
            }
        }

        /**
         * Send the crop Intent from an Activity
         * @param activity Activity to receive result
         */
        public void start(@NonNull Activity activity) {
            start(activity, REQUEST_CODE);
        }

        /**
         * Get Intent to start {@link PhotoPickerActivity}
         * @return Intent for {@link PhotoPickerActivity}
         */
        public Intent getIntent(@NonNull Context context) {
            mPickerIntent.setClass(context, PhotoPickerActivity.class);
            mPickerIntent.putExtras(mPickerOptionsBundle);
            return mPickerIntent;
        }

        public PhotoPickerBuilder setPhotoCount(int photoCount) {
            mPickerOptionsBundle.putInt(EXTRA_MAX_COUNT, photoCount);
            return this;
        }

        public PhotoPickerBuilder setGridColumnCount(int columnCount) {
            mPickerOptionsBundle.putInt(EXTRA_GRID_COLUMN, columnCount);
            return this;
        }

        public PhotoPickerBuilder setShowGif(boolean showGif) {
            mPickerOptionsBundle.putBoolean(EXTRA_SHOW_GIF, showGif);
            return this;
        }

        public PhotoPickerBuilder setShowCamera(boolean showCamera) {
            mPickerOptionsBundle.putBoolean(EXTRA_SHOW_CAMERA, showCamera);
            return this;
        }

        public PhotoPickerBuilder setSelected(ArrayList<String> imagesUri) {
            mPickerOptionsBundle.putStringArrayList(EXTRA_ORIGINAL_PHOTOS, imagesUri);
            return this;
        }

        public PhotoPickerBuilder setPreviewEnabled(boolean previewEnabled) {
            mPickerOptionsBundle.putBoolean(EXTRA_PREVIEW_ENABLED, previewEnabled);
            return this;
        }

    }

}
