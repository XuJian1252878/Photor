package com.photor.util;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.photor.R;
import com.photor.album.entity.Album;

import java.util.TreeMap;

public class AlertDialogsHelper {

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

}
