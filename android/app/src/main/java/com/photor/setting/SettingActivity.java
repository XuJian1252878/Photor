package com.photor.setting;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.preference.PreferenceUtil;
import com.example.theme.ThemeHelper;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;
import com.photor.R;
import com.photor.base.activity.BaseActivity;
import com.photor.setting.event.PdfImageDisplayEnum;
import com.photor.setting.event.PdfWatermarkEnum;
import com.photor.util.AlertDialogsHelper;
import com.shawnlin.numberpicker.NumberPicker;

import java.util.ArrayList;
import java.util.List;

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

    private int pdfDisplayOnePageMode = 0;  // image 在pdf文件中的显示格式
    private int pdfWatermark = 0;  // 控制pdf文件是否有水印，0没有 1有

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
         * 导出pdf文件 图片分辨率设置
         */
        findViewById(R.id.pdf_resolution).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfExportResolutionDialog();
            }
        });

        /**
         * 主题背景设置
         */
        setTheme();

        /**
         * 初始化设置参数
         */
        pdfDisplayOnePageMode = SP.getInt(getString(R.string.pdf_image_display_one_page), 0);  // pdf显示设置
        pdfWatermark = SP.getInt(getString(R.string.pdf_watermark_switch), PdfWatermarkEnum.YES_WATERMARK.getIndex()); // 设置水印

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
        ((IconicsImageView) findViewById(R.id.pdf_resolution_icon)).setColor(color);

        /** TextViews **/
        color = ThemeHelper.getTextColor(this);
        ((TextView) findViewById(R.id.n_columns_Item_Title)).setTextColor(color);
        ((TextView) findViewById(R.id.pdf_resolution_Title)).setTextColor(color);

        /** Sub Text Views**/
        color = ThemeHelper.getSubTextColor(this);
        ((TextView) findViewById(R.id.n_columns_Item_Title_Sub)).setTextColor(color);
        ((TextView) findViewById(R.id.pdf_resolution_Title_Sub)).setTextColor(color);

        /**
         * 设置设置选项颜色信息
         */
        generalTextView.setTextColor(ThemeHelper.getPrimaryColor(this));
    }

    /**
     * 设置图片导出为pdf的选项
     */
    private void pdfExportResolutionDialog() {
        AlertDialog.Builder pdfDialogBuilder = new AlertDialog.Builder(SettingActivity.this, ThemeHelper.getDialogStyle());
        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_pdf_export_setting, null);

        // 设置当前对话框卡片的背景颜色
        ((CardView)(dialogLayout.findViewById(R.id.pdf_export_card))).setCardBackgroundColor(ThemeHelper.getCardBackgroundColor(this));

        // 设置文本相关信息
        ((TextView)(dialogLayout.findViewById(R.id.pdf_export_title))).setBackgroundColor(ThemeHelper.getPrimaryColor(this));

        // pdf 水印设置
        pdfWatermark = SP.getInt(getString(R.string.pdf_watermark_switch), PdfWatermarkEnum.YES_WATERMARK.getIndex());

        Switch pdfWatermarkSwitch = dialogLayout.findViewById(R.id.pdf_watermark_switch);
        EditText pdfWatermarkEditText = dialogLayout.findViewById(R.id.pdf_image_watermark_content);

        pdfWatermarkSwitch.setTextOff(getString(R.string.pdf_image_watermark_no));
        pdfWatermarkSwitch.setTextOn(getString(R.string.pdf_image_watermark_yes));
        pdfWatermarkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    // 允许输入
                    pdfWatermark = PdfWatermarkEnum.YES_WATERMARK.getIndex();
                    pdfWatermarkEditText.setCursorVisible(true);
                    pdfWatermarkEditText.setFocusableInTouchMode(true);
                    pdfWatermarkEditText.requestFocus();
                    SP.putInt(SettingActivity.this.getString(R.string.pdf_watermark_switch), 1);
                } else {
                    // 不允许输入
                    pdfWatermark = PdfWatermarkEnum.NO_WATERMARK.getIndex();
                    pdfWatermarkEditText.setCursorVisible(false);
                    pdfWatermarkEditText.setFocusableInTouchMode(false);
                    pdfWatermarkEditText.clearFocus();
                    SP.putInt(SettingActivity.this.getString(R.string.pdf_watermark_switch), 0);
                }
            }
        });
        pdfWatermarkSwitch.setChecked(pdfWatermark != 0);

        // 设置水印内容的输入框事件
        pdfWatermarkEditText.addTextChangedListener(new TextWatcher() {
            String waterMark = null;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                waterMark = SP.getString(SettingActivity.this.getString(R.string.pdf_image_watermark_content), "");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                waterMark = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                SP.putString(SettingActivity.this.getString(R.string.pdf_image_watermark_content), waterMark);
            }
        });
        pdfWatermarkEditText.setText(SP.getString(SettingActivity.this.getString(R.string.pdf_image_watermark_content), ""));

        // 设置图片显示选项
        NumberPicker pdfDisplayOnePagePicker = dialogLayout.findViewById(R.id.pdf_image_display_one_page_picker);
        List<String> pdfDisplayList = new ArrayList<>();
        for (PdfImageDisplayEnum pde: PdfImageDisplayEnum.values()) {
            pdfDisplayList.add(getString(pde.getLabelId()));
        }
        String[] pdfDisplayArray = pdfDisplayList.toArray(new String[pdfDisplayList.size()]);
        pdfDisplayOnePagePicker.setMinValue(1);
        pdfDisplayOnePagePicker.setMaxValue(pdfDisplayArray.length);
        pdfDisplayOnePagePicker.setDisplayedValues(pdfDisplayArray);
        pdfDisplayOnePagePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                pdfDisplayOnePageMode = newVal - 1;
            }
        });
        pdfDisplayOnePageMode = SP.getInt(getString(R.string.pdf_image_display_one_page), 0);
        pdfDisplayOnePagePicker.setValue(pdfDisplayOnePageMode + 1);
        pdfDisplayOnePagePicker.setDividerColorResource(R.color.blue_light);
        pdfDisplayOnePagePicker.setDividerThickness(1);
        pdfDisplayOnePagePicker.setTextSize(R.dimen.size_15dp);
        pdfDisplayOnePagePicker.setSelectedTextSize(R.dimen.size_20dp);

        // 设置对话框按钮
        pdfDialogBuilder.setPositiveButton(getString(R.string.ok_action), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SP.putInt(getString(R.string.pdf_image_display_one_page), pdfDisplayOnePageMode);
            }
        });

        // 设置取消对话框按钮
        pdfDialogBuilder.setNegativeButton(getString(R.string.cancel_action), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        pdfDialogBuilder.setView(dialogLayout);
        AlertDialog pdfDialog = pdfDialogBuilder.create();
        pdfDialog.show();
        AlertDialogsHelper.setButtonTextColor(new int[]{DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE},
                ThemeHelper.getAccentColor(this), pdfDialog);
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
