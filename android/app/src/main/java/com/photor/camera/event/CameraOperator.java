package com.photor.camera.event;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.photor.base.activity.PhotoOperateResultActivity;

import static com.photor.base.activity.util.PhotoOperator.EXTRA_PHOTO_OPERATE_RESULT_PATH;

public class CameraOperator {

    private static final int REQUEST_RESULT_CODE = 678;

    public static CameraOperatorBuilder builder() {
        return new CameraOperatorBuilder();
    }

    public static class CameraOperatorBuilder {

        private Bundle bundle;
        private Intent intent;

        public CameraOperatorBuilder() {
            this.bundle = new Bundle();
            this.intent = new Intent();
        }

        public CameraOperatorBuilder setCameraResImgPath(String imgResPath) {
            this.bundle.putString(EXTRA_PHOTO_OPERATE_RESULT_PATH, imgResPath);
            return this;
        }


        public void start(Activity activity) {
            intent.setClass(activity, PhotoOperateResultActivity.class);
            intent.putExtras(bundle);
            activity.startActivityForResult(intent, REQUEST_RESULT_CODE);
        }
    }

}
