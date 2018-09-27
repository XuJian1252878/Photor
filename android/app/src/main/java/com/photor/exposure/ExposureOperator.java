package com.photor.exposure;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.photor.base.activity.PhotoOperateResultActivity;

import static com.photor.base.activity.util.PhotoOperator.EXTRA_PHOTO_OPERATE_RESULT_PATH;

public class ExposureOperator {

    public static final int REQUEST_RESULT_CODE = 233;

    public static ExposureResultBuilder resultBuilder() {
        return new ExposureResultBuilder();
    }

    public static class ExposureResultBuilder {
        private Bundle bundle;
        private Intent intent;

        public ExposureResultBuilder() {
            this.bundle = new Bundle();
            this.intent = new Intent();
        }

        public ExposureResultBuilder setExposureResPath(String exposureResPath) {
            bundle.putString(EXTRA_PHOTO_OPERATE_RESULT_PATH, exposureResPath);
            return this;
        }

        public void start(Activity activity) {
            intent.setClass(activity, PhotoOperateResultActivity.class);
            intent.putExtras(bundle);
            activity.startActivityForResult(intent, REQUEST_RESULT_CODE);
        }
    }

}
