package com.photor.util;

import android.content.Context;

/**
 * 随着应用启动初始化，为程序其他需要Context对象的地方提供application context
 */
public class ActivitySwitchHelper {

    public static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        ActivitySwitchHelper.context = context;
    }

}
