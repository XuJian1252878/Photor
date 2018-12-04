package com.photor.setting;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.preference.PreferenceUtil;
import com.example.theme.ThemeHelper;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;
import com.photor.R;
import com.photor.base.activity.BaseActivity;
import com.photor.util.AlertDialogsHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author htwxujian@gmail.com
 * @date 2018/12/4 14:37
 */
public class SettingActivity extends BaseActivity {

    private PreferenceUtil SP;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.settingAct_scrollView)
    ScrollView settingActScrollerView;

    @BindView(R.id.general_setting_title)
    TextView generalTextView;

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        SP = PreferenceUtil.getInstance(this);

        /**
         * 相册界面显示设置
         */
        findViewById(R.id.ll_n_columns).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                multiColumnsDialog();
            }
        });

        /**
         * 主题背景设置
         */
        setTheme();

    }

    private void setTheme() {
        // 设置背景颜色信息
        findViewById(R.id.setting_background).setBackgroundColor(ThemeHelper.getBackgroundColor(this));

        // 设置各个设置选项背景模块的颜色信息
        int color = ThemeHelper.getCardBackgroundColor(this);
        findViewById(R.id.general_setting_card).setBackgroundColor(color);

        // 设置toolbar信息
        toolbar.setBackgroundColor(ThemeHelper.getPrimaryColor(this));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_arrow_left)
                .color(Color.WHITE)
                .sizeDp(19));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // 设置ScrollView滑动条的颜色
        ThemeHelper.setScrollViewColor(this, settingActScrollerView);

        /** Icons **/
        color = ThemeHelper.getIconColor(this);
        ((IconicsImageView) findViewById(R.id.n_columns_icon)).setColor(color);

        /** TextViews **/
        color = ThemeHelper.getTextColor(this);
        ((TextView) findViewById(R.id.n_columns_Item_Title)).setTextColor(color);

        /** Sub Text Views**/
        color = ThemeHelper.getSubTextColor(this);
        ((TextView) findViewById(R.id.n_columns_Item_Title_Sub)).setTextColor(color);

        /**
         * 设置设置选项颜色信息
         */
        generalTextView.setTextColor(ThemeHelper.getPrimaryColor(this));
    }

    /**
     * 相册、图片显示列数的设置函数
     */
    private void multiColumnsDialog() {
        AlertDialog.Builder multiColumnsDialogBuilder = new AlertDialog.Builder(SettingActivity.this, ThemeHelper.getDialogStyle());
        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_multi_column, null);

        // 设置列数文本label 颜色信息
        ((TextView) dialogLayout.findViewById(R.id.text_view_portrait)).setTextColor(ThemeHelper.getTextColor(this));
        ((TextView) dialogLayout.findViewById(R.id.text_view_landscape)).setTextColor(ThemeHelper.getTextColor(this));
        ((TextView) dialogLayout.findViewById(R.id.folders_title)).setTextColor(ThemeHelper.getTextColor(this));
        ((TextView) dialogLayout.findViewById(R.id.media_title)).setTextColor(ThemeHelper.getTextColor(this));
        ((TextView) dialogLayout.findViewById(R.id.folders_title_landscape)).setTextColor(ThemeHelper.getTextColor(this));
        ((TextView) dialogLayout.findViewById(R.id.media_title_landscape)).setTextColor(ThemeHelper.getTextColor(this));
        ((CardView) dialogLayout.findViewById(R.id.multi_column_card)).setCardBackgroundColor(ThemeHelper.getCardBackgroundColor(this));

        // 设置列数数量的 文本颜色信息
        dialogLayout.findViewById(R.id.multi_column_title).setBackgroundColor(ThemeHelper.getPrimaryColor(this));
        final TextView nColFolders = (TextView) dialogLayout.findViewById(R.id.n_columns_folders);
        final TextView nColMedia = (TextView) dialogLayout.findViewById(R.id.n_columns_media);
        final TextView nColFoldersL = (TextView) dialogLayout.findViewById(R.id.n_columns_folders_landscape);
        final TextView nColMediaL = (TextView) dialogLayout.findViewById(R.id.n_columns_media_landscape);

        nColFolders.setTextColor(ThemeHelper.getSubTextColor(this));
        nColMedia.setTextColor(ThemeHelper.getSubTextColor(this));
        nColFoldersL.setTextColor(ThemeHelper.getSubTextColor(this));
        nColMediaL.setTextColor(ThemeHelper.getSubTextColor(this));

        // 设置对应的SeekBar信息
        SeekBar barFolders = (SeekBar) dialogLayout.findViewById(R.id.seek_bar_n_columns_folders);
        SeekBar barMedia = (SeekBar) dialogLayout.findViewById(R.id.seek_bar_n_columns_media);
        SeekBar barFoldersL = (SeekBar) dialogLayout.findViewById(R.id.seek_bar_n_columns_folders_landscape);
        SeekBar barMediaL = (SeekBar) dialogLayout.findViewById(R.id.seek_bar_n_columns_media_landscape);

        ThemeHelper.themeSeekBar(this, barFolders); ThemeHelper.themeSeekBar(this, barMedia);
        ThemeHelper.themeSeekBar(this, barFoldersL); ThemeHelper.themeSeekBar(this, barMediaL);

        // 设置竖屏状态下的相册、图片列数信息
        nColFolders.setText(String.valueOf(SP.getInt(getString(R.string.n_columns_folders), 2)));
        nColMedia.setText(String.valueOf(SP.getInt(getString(R.string.n_columns_media), 3)));
        barFolders.setProgress(SP.getInt("n_columns_folders", 2) - 1);
        barMedia.setProgress(SP.getInt("n_columns_media", 3) - 1);

        barFolders.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                nColFolders.setText(String.valueOf(i + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        barMedia.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                nColMedia.setText(String.valueOf(i + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        // 横屏设置
        nColFoldersL.setText(String.valueOf(SP.getInt(getString(R.string.n_columns_folders_landscape), 3)));
        nColMediaL.setText(String.valueOf(SP.getInt(getString(R.string.n_columns_media_landscape), 4)));
        barFoldersL.setProgress(SP.getInt(getString(R.string.n_columns_folders_landscape), 3) - 2);
        barMediaL.setProgress(SP.getInt(getString(R.string.n_columns_media_landscape), 4) - 3);
        barFoldersL.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                nColFoldersL.setText(String.valueOf(i+2));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        barMediaL.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                nColMediaL.setText(String.valueOf(i+3));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        multiColumnsDialogBuilder.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int nFolders = Integer.parseInt(nColFolders.getText().toString());
                int nMedia = Integer.parseInt(nColMedia.getText().toString());
                int nFoldersL = Integer.parseInt(nColFoldersL.getText().toString());
                int nMediaL = Integer.parseInt(nColMediaL.getText().toString());

                SP.putInt(getString(R.string.n_columns_folders), nFolders);
                SP.putInt(getString(R.string.n_columns_media), nMedia);
                SP.putInt(getString(R.string.n_columns_folders_landscape), nFoldersL);
                SP.putInt(getString(R.string.n_columns_media_landscape), nMediaL);
            }
        });

        multiColumnsDialogBuilder.setNegativeButton(getString(R.string.cancel_action), null);
        multiColumnsDialogBuilder.setView(dialogLayout);
        AlertDialog alertDialog = multiColumnsDialogBuilder.create();
        alertDialog.show();
        AlertDialogsHelper.setButtonTextColor(new int[]{DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE},
                ThemeHelper.getAccentColor(this), alertDialog);
    }



}
