package com.photor.album.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class Measure {

    public static int pxToDp(int px, Context c) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        return Math.round(px * (displayMetrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * 获取系统默认的状态栏的高度信息
     * @param r
     * @return
     */
    public static int getStatusBarHeight(Resources r) {
        // 第一个参数为ID名，第二个为资源属性是ID或者是Drawable，第三个为包名。 如果找到了，返回资源Id，如果找不到，返回0 。
        int resourceId = r.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            return r.getDimensionPixelSize(resourceId);

        return 0;
    }
}
