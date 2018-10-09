package com.photor.util;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.photor.R;

public class SnackBarHandler {

    public static final int INDEFINITE = Snackbar.LENGTH_INDEFINITE;
    public static final int LONG = Snackbar.LENGTH_LONG;
    public static final int SHORT = Snackbar.LENGTH_SHORT;

    /**
     * 带有撤销操作的SnackBar，需要外部自己调用 show显示
     * @param view
     * @param text
     * @param bottomMargin
     * @param duration
     * @return
     */
    public static Snackbar showWithBottomMargin2(View view, String text, int bottomMargin, int duration) {
        final Snackbar snackbar = Snackbar.make(view, text, duration);
        View sbView = snackbar.getView();
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) sbView.getLayoutParams();
        params.setMargins(params.leftMargin,
                params.topMargin,
                params.rightMargin,
                params.bottomMargin + bottomMargin);
        sbView.setLayoutParams(params);

        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id
                .snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(12);
        snackbar.setActionTextColor(ThemeHelper.getColor(ActivitySwitchHelper.getContext(), R.color.blue_light));
        return snackbar;
    }

    /**
     * 不带有撤销操作的SnackBar
     * @param view
     * @param text
     * @param bottomMargin
     * @param duration
     */
    public static void showWithBottomMargin(View view, String text,int bottomMargin, int duration) {
        final Snackbar snackbar = Snackbar.make(view, text, duration);
        View sbView = snackbar.getView();
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) sbView.getLayoutParams();
        params.setMargins(params.leftMargin,
                params.topMargin,
                params.rightMargin,
                params.bottomMargin + bottomMargin);
        sbView.setLayoutParams(params);

        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id
                .snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(12);
        snackbar.setAction(R.string.ok_action, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.setActionTextColor(ThemeHelper.getColor(ActivitySwitchHelper.getContext(), R.color.blue_light));
        snackbar.show();
    }

    public static void showWithBottomMargin(View view, String text, int bottomMargin) {
        showWithBottomMargin(view, text, bottomMargin, Snackbar.LENGTH_LONG);
    }

    public static Snackbar show(View view, String text, int duration) {
        final Snackbar snackbar = Snackbar.make(view, text, duration);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id
                .snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(12);
        snackbar.setActionTextColor(ThemeHelper.getColor(ActivitySwitchHelper.getContext(), R.color.blue_light));
        return snackbar;
    }

}
