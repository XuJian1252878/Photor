package com.photor.util;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.theme.ThemeHelper;
import com.photor.R;
import com.photor.album.entity.Album;

import java.lang.reflect.Field;
import java.util.TreeMap;

public class AlertDialogsHelper {

    public static boolean check=false;

    public static AlertDialog getInsertTextDialog(final Activity activity, AlertDialog.Builder dialogBuilder , EditText editText, @StringRes int title, String link) {
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_insert_text, null);
        TextView textViewTitle = (TextView) dialogLayout.findViewById(R.id.rename_title);
        ((CardView) dialogLayout.findViewById(R.id.dialog_chose_provider_title)).setCardBackgroundColor(ThemeHelper.getCardBackgroundColor(activity));
        textViewTitle.setBackgroundColor(ThemeHelper.getPrimaryColor(activity));

        if (link != null) {
            textViewTitle.setText(Html.fromHtml(link));
            textViewTitle.setLinkTextColor(Color.WHITE);
        } else {
            textViewTitle.setText(title);
        }
        // 激活链接需要在Java代码中使用setMovementMethod()方法设置TextView为可点击。
        textViewTitle.setMovementMethod(LinkMovementMethod.getInstance());
        ThemeHelper.setCursorDrawableColor(editText, ThemeHelper.getTextColor(activity));  // 设置标题栏光标的颜色

        // 设置editText的布局
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(layoutParams);
        editText.setSingleLine(true);
        editText.getBackground().mutate().setColorFilter(ThemeHelper.getTextColor(activity), PorterDuff.Mode.SRC_IN);
        editText.setTextColor(ThemeHelper.getTextColor(activity));

        // ...
        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(editText, null);
        } catch (Exception ignored) { }

        ((RelativeLayout) dialogLayout.findViewById(R.id.container_edit_text)).addView(editText);
        dialogBuilder.setView(dialogLayout);
        return dialogBuilder.create();
    }

    public static AlertDialog getTextDialog(final Activity activity, AlertDialog.Builder textDialogBuilder, @StringRes int title, @StringRes int Message, String msg){
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_text, null);

        TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.text_dialog_title);
        TextView dialogMessage = (TextView) dialogLayout.findViewById(R.id.text_dialog_message);

        ((CardView) dialogLayout.findViewById(R.id.message_card)).setCardBackgroundColor(ThemeHelper.getCardBackgroundColor(activity));
        dialogTitle.setBackgroundColor(ThemeHelper.getPrimaryColor(activity));
        dialogTitle.setText(title);
        if (msg != null) dialogMessage.setText(msg);
        else dialogMessage.setText(Message);
        dialogMessage.setTextColor(ThemeHelper.getTextColor(activity));
        textDialogBuilder.setView(dialogLayout);
        return textDialogBuilder.create();
    }

    public static AlertDialog getTextCheckboxDialog(final Activity activity, AlertDialog.Builder
            textDialogBuilder, @StringRes int title, @StringRes int Message, String msg, String checkboxmessage,
                                                    final int colorId){
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_checkbox, null);
        TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.text_dialog_title);
        TextView dialogMessage = (TextView) dialogLayout.findViewById(R.id.text_dialog_message);
        TextView checkboxmessg = (TextView) dialogLayout.findViewById(R.id.checkbox_text_dialog);
        final CheckBox checkBox = (CheckBox) dialogLayout.findViewById(R.id.checkbox_text_dialog_cb);
        if(checkBox.isChecked()){
            check = true;
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    check=true;

                }else{
                    check=false;
                }
            }
        });
        ((CardView) dialogLayout.findViewById(R.id.message_card)).setCardBackgroundColor(ThemeHelper.getCardBackgroundColor(activity));
        dialogTitle.setBackgroundColor(ThemeHelper.getPrimaryColor(activity));
        dialogTitle.setText(title);
        checkboxmessg.setText(checkboxmessage);
        checkboxmessg.setTextColor(ThemeHelper.getTextColor(activity));
        if (msg != null) dialogMessage.setText(msg);
        else dialogMessage.setText(Message);
        dialogMessage.setTextColor(ThemeHelper.getTextColor(activity));
        textDialogBuilder.setView(dialogLayout);
        checkBox.setButtonTintList(ColorStateList.valueOf(colorId));
        return textDialogBuilder.create();
    }

    public static AlertDialog getAlbumDetailsDialog(Activity activity, AlertDialog.Builder detailsDialogBuilder,
                                                    final Album f) {
        TreeMap<String, String> mainDetails = f.getAlbumDetails(activity.getApplicationContext());
        View dialogLayout = activity.getLayoutInflater().inflate(R.layout.dialog_album_detail, null);
        dialogLayout.findViewById(R.id.album_details_title).setBackgroundColor(ThemeHelper.getColor(activity, R.color.md_light_blue_500));
        ((CardView) dialogLayout.findViewById(R.id.album_details_card)).setCardBackgroundColor(ThemeHelper.getColor(activity, R.color.md_light_cards));
        detailsDialogBuilder.setView(dialogLayout);
        loadDetails(dialogLayout, activity, mainDetails);
        return detailsDialogBuilder.create();
    }


    private static void loadDetails(View dialogLayout, Activity activity, TreeMap<String, String> metadata) {
        int textColor = Color.parseColor("#2b2b2b" );
        ((ImageView)dialogLayout.findViewById(R.id.icon_folder)).setColorFilter(ThemeHelper.getColor(activity, R.color.md_light_blue_500));
        TextView name = (TextView) dialogLayout.findViewById(R.id.album_details_name);
        name.setText(metadata.get(activity.getString(R.string.folder_name)));
        name.setTextColor(textColor);
        TextView type = (TextView) dialogLayout.findViewById(R.id.album_details_type);
        type.setText(R.string.folder);
        type.setTextColor(textColor);
        TextView path = (TextView) dialogLayout.findViewById(R.id.album_details_path);
        path.setText(metadata.get(activity.getString(R.string.folder_path)));
        path.setTextColor(textColor);
        TextView parent = (TextView) dialogLayout.findViewById(R.id.album_details_parent);
        parent.setText(metadata.get(activity.getString(R.string.parent_path)));
        parent.setTextColor(textColor);
        TextView total = (TextView) dialogLayout.findViewById(R.id.album_details_total);
        total.setText(metadata.get(activity.getString(R.string.total_photos)));
        total.setTextColor(textColor);
        TextView size = (TextView) dialogLayout.findViewById(R.id.album_details_size);
        size.setText(metadata.get(activity.getString(R.string.size_folder)));
        size.setTextColor(textColor);
        TextView modified = (TextView) dialogLayout.findViewById(R.id.album_details_last_modified);
        modified.setText(metadata.get(activity.getString(R.string.modified)));
        modified.setTextColor(textColor);
        TextView readable = (TextView) dialogLayout.findViewById(R.id.album_details_readable);
        readable.setText(metadata.get(activity.getString(R.string.readable)));
        readable.setTextColor(textColor);
        TextView writable = (TextView) dialogLayout.findViewById(R.id.album_details_writable);
        writable.setText(metadata.get(activity.getString(R.string.writable)));
        writable.setTextColor(textColor);

        // 设置label文字颜色
        ((TextView)dialogLayout.findViewById(R.id.label_type)).setTextColor(textColor);
        ((TextView)dialogLayout.findViewById(R.id.label_path)).setTextColor(textColor);
        ((TextView)dialogLayout.findViewById(R.id.label_parent)).setTextColor(textColor);
        ((TextView)dialogLayout.findViewById(R.id.label_total_photos)).setTextColor(textColor);
        ((TextView)dialogLayout.findViewById(R.id.label_size)).setTextColor(textColor);
        ((TextView)dialogLayout.findViewById(R.id.label_last_modified)).setTextColor(textColor);
        ((TextView)dialogLayout.findViewById(R.id.label_readable)).setTextColor(textColor);
        ((TextView)dialogLayout.findViewById(R.id.label_writable)).setTextColor(textColor);
    }

    /**
     * 设置提示框上按钮的文字信息
     * @param buttons
     * @param color
     * @param alertDialog
     */
    public static void setButtonTextColor(int[] buttons, int color, AlertDialog alertDialog) {
        for (int button: buttons) {
            alertDialog.getButton(button).setTextColor(color);
        }
    }


    /**
     * 获得处理进度条信息
     * @param context
     * @param title
     * @param canCancel
     * @return
     */
    public static Dialog getLoadingDialog(Context context, String title,
                                          boolean canCancel) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCancelable(canCancel);
        dialog.setMessage(title);
        return dialog;
    }

}
