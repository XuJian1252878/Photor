package com.example.theme;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.widget.EditText;
import android.widget.TextView;

import com.example.common.R;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

import java.lang.reflect.Field;

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

    public static void setCursorDrawableColor(EditText editText, int color) {
        try {
            Field fCursorDrawableRes =
                    TextView.class.getDeclaredField("mCursorDrawableRes");
            fCursorDrawableRes.setAccessible(true);
            int mCursorDrawableRes = fCursorDrawableRes.getInt(editText);
            Field fEditor = TextView.class.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            Object editor = fEditor.get(editText);
            Class<?> clazz = editor.getClass();
            Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
            fCursorDrawable.setAccessible(true);

            Drawable[] drawables = new Drawable[2];
            drawables[0] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
            drawables[1] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
            drawables[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            drawables[1].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            fCursorDrawable.set(editor, drawables);
        } catch (final Throwable ignored) {  }
    }

    public static int getBackgroundColor(Context context) {
        int color = getColor(context, R.color.md_light_background);
        return color;
    }

    public static IconicsDrawable getToolbarIcon(Context context, IIcon icon) {
        return new IconicsDrawable(context).icon(icon).color(Color.WHITE).sizeDp(18);
    }

    public static int getPopupToolbarStyle(Context context) {
        return R.style.LightActionBarMenu;
    }

}
