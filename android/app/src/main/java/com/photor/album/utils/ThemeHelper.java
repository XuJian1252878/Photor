package com.photor.album.utils;

import android.content.Context;
import android.graphics.Color;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

public class ThemeHelper {

    private Context context;

    public ThemeHelper(Context context) {
        this.context = context;
    }

    public IconicsDrawable getToolbarIcon(IIcon icon) {
        return new IconicsDrawable(context).icon(icon).color(Color.WHITE).sizeDp(18);
    }

}
