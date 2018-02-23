package com.example.photopicker.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * Created by xujian on 2018/1/8.
 */

public class AndroidLifecycleUtils {

    // 如果传入的activity 或者 fragment 为null，
    // 或者 context 不为activity
    // 或者 activity 已经destroy，那么可以load image

    public static boolean canLoadImage(Fragment fragment) {
        if (fragment == null) {
            return true;
        }

        FragmentActivity activity = fragment.getActivity();
        return canLoadImage(activity);
    }

    public static boolean canLoadImage(Context context) {
        if (context == null) {
            return true;
        }

        if (!(context instanceof Activity)) {
            return true;
        }

        Activity activity = (Activity)context;
        return canLoadImage(activity);
    }

    /**
     * 判断一个activity是否在运行，如果activity在运行，那么可以load image
     * 判断activity是否在运行 activity == null || activity.isDestroyed() || activity.isFinishing()
     * @param activity
     * @return
     */
    private static boolean canLoadImage(Activity activity) {
        if (activity == null) { //
            return true;
        }

        /** Returns true if the final onDestroy() call has been made on the Activity, so this
         * instance is now dead.isDestroyed() activity已经被销毁的时候，onDestroy() 已经被调用，
         * 并且已经执行完成，isDestroyed() 是android 4.2之后加入的方法，api level 17*/
        boolean destroyed = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                activity.isDestroyed();

        /**
         * The final call you receive before your activity is destroyed. This can happen either
         * because the activity is finishing (someone called finish() on it, or because the system
         * is temporarily destroying this instance of the activity to save space. You can distinguish
         * between these two scenarios with the isFinishing() method.
         *
         *
         * Check to see whether this activity is in the process of finishing, either because you
         * called finish() on it or someone else has requested that it finished. This is often used
         * in onPause() to determine whether the activity is simply pausing or completely finishing.
         *
         * isFinishing() activity正在被销毁的时候
         */
        if (destroyed || activity.isFinishing()) {
            return false;
        }
        return true;
    }
}
