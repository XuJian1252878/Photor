package com.photor.album.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class Measure {
    public static int pxToDp(int px, Context c) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        return Math.round(px * (displayMetrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
