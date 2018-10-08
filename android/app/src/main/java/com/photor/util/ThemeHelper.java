package com.photor.util;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

public class ThemeHelper {


    public static int getColor(Context context, @ColorRes int color) {
        return ContextCompat.getColor(context, color);
    }

}
