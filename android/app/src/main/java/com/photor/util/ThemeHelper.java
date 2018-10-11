package com.photor.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;
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

    public static int getSubTextColor(Context context) {
        return getColor(context, R.color.md_grey_600);
    }

    public static int getIconColor(Context context) {
        return getColor(context, R.color.md_light_primary_icon);
    }

    public static IconicsDrawable getIcon(Context context, IIcon icon) {
        return new IconicsDrawable(context).icon(icon).color(getIconColor(context));
    }


    public static void setColorScrollBarDrawable(Context context, Drawable drawable) {
        drawable.setColorFilter(new PorterDuffColorFilter(getPrimaryColor(context), PorterDuff.Mode.SRC_ATOP));
    }

}
