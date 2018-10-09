package com.photor.util;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

import com.photor.R;

public class ThemeHelper {


    public static int getColor(Context context, @ColorRes int color) {
        return ContextCompat.getColor(context, color);
    }

    public static int getCardBackgroundColor(Context context) {
        return ContextCompat.getColor(context, R.color.md_light_cards);
    }

    public static int getPrimaryColor(Context context) {
        return ContextCompat.getColor(context, R.color.md_light_blue_500);
    }

    public static int getTextColor(Context context) {
        return Color.parseColor("#2b2b2b" );
    }


    public static int getAccentColor(Context context) {
        return ContextCompat.getColor(context, R.color.md_light_blue_500);
    }

}
