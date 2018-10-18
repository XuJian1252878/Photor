package com.photor.home.focusstack;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.photor.album.activity.SingleMediaActivity;

import static com.example.constant.PhotoOperator.EXTRA_FOCUS_STACK_RES_PATH;
import static com.example.constant.PhotoOperator.EXTRA_PHOTO_IS_FROM_OPERATE_RESULT;

/**
 * @author htwxujian@gmail.com
 * @date 2018/10/18 15:21
 */
public class FocusStackOperator {

    public static final int REQUEST_RESULT_CODE = 455;

    public static FocusStackResultBuilder builder() {
        return new FocusStackResultBuilder();
    }

    public static class FocusStackResultBuilder {
        private Intent intent;
        private Bundle bundle;

        public FocusStackResultBuilder() {
            this.intent = new Intent();
            this.bundle = new Bundle();
        }

        public FocusStackResultBuilder setFocusStackResPath(String resBitmapPath) {
            bundle.putString(EXTRA_FOCUS_STACK_RES_PATH, resBitmapPath);
            return this;
        }

        public FocusStackResultBuilder setFocusStackResUri(Uri uri) {
            intent.setData(uri);
            return this;
        }

        public FocusStackResultBuilder setIsFromOperate(boolean isFromOperate) {
            bundle.putBoolean(EXTRA_PHOTO_IS_FROM_OPERATE_RESULT, isFromOperate);
            return this;
        }

        public void start(Activity activity) {
            intent.setClass(activity, SingleMediaActivity.class);
            intent.putExtras(bundle);
            activity.startActivityForResult(intent, REQUEST_RESULT_CODE);
        }
    }

}
