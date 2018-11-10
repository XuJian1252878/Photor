package com.photor.home.exposure;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.file.FileUtils;
import com.example.media.image.MediaExifHelper;
import com.example.photopicker.PhotoPicker;
import com.example.photopicker.PhotoPreview;
import com.example.preference.PreferenceUtil;
import com.example.theme.ThemeHelper;
import com.photor.R;
import com.photor.base.activity.PhotoOperateBaseActivity;
import com.photor.base.adapters.PhotoAdapter;
import com.photor.base.adapters.event.PhotoItemClickListener;
import com.photor.home.exposure.event.ExposureEnum;
import com.photor.home.exposure.event.ExposureProcessFinishListener;
import com.photor.home.exposure.event.ToneMappingMethodEnum;
import com.photor.home.exposure.event.ToneMappingParamEnum;
import com.photor.home.exposure.task.ExposureMergeThread;
import com.photor.util.AlertDialogsHelper;
import com.shawnlin.numberpicker.NumberPicker;
import com.xw.repo.BubbleSeekBar;

import org.opencv.core.Mat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExposureBaseActivity extends PhotoOperateBaseActivity {

    // 曝光合成结果
    private Mat expoResMat = new Mat();

    // 当前默认的色调映射函数
    private int toneMappingMethodIndex = 0;  // 默认是Drago

    private PreferenceUtil SP;  // 存储景深合成配置信息

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
        SP = PreferenceUtil.getInstance(this);
        // 初始化色调映射各个参数的初始值
        for (ToneMappingParamEnum tmpe: ToneMappingParamEnum.values()) {
            String paramName = getString(tmpe.getNameResId());
            float value = SP.getFloat(paramName, tmpe.getValue());
            tmpe.setValue(value);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.expose_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.exposure_setting:
                getExposeSettingDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // 获取曝光合成的设置对话框
    private void getExposeSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog_Light);
        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_exposure_setting, null);

        // 设置CardView的背景色
        dialogLayout.findViewById(R.id.exposure_card).setBackgroundColor(ThemeHelper.getCardBackgroundColor(this));
        dialogLayout.findViewById(R.id.text_dialog_title).setBackgroundColor(ThemeHelper.getPrimaryColor(this));


        // 色调映射方法的选择
        NumberPicker toneMappingPicker = dialogLayout.findViewById(R.id.tone_mapping_method_picker);
        List<String> toneMappingMethods = new ArrayList<>();
        for (ToneMappingMethodEnum tme: ToneMappingMethodEnum.values()) {
            String toneMappingMethod = getString(tme.getMethodNameResId());
            toneMappingMethods.add(toneMappingMethod);
        }
        String[] toneMappingMethodArray = toneMappingMethods.toArray(new String[toneMappingMethods.size()]);
        toneMappingPicker.setMinValue(1);
        toneMappingPicker.setMaxValue(toneMappingMethodArray.length);
        toneMappingPicker.setDisplayedValues(toneMappingMethodArray);
        toneMappingPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // 设置参数控制面板的可见性
                showToneMappingPanel(dialogLayout, newVal - 1);
                toneMappingMethodIndex = newVal - 1;
            }
        });
        toneMappingPicker.setValue(toneMappingMethodIndex + 1);
        showToneMappingPanel(dialogLayout, toneMappingMethodIndex);


        // 初始化参数调节相关SeekBar
        for (ToneMappingParamEnum tmpe: ToneMappingParamEnum.values()) {
            initToneMappingSeekBar(dialogLayout, tmpe);
        }

        /**
         * You must correct the offsets by setting ScrollListener when BubbleSeekBar's parent view is
         * scrollable (such as ScrollView, except ViewPager), otherwise, the appearing position of
         * the bubble may be wrong. For example:
         */
        ScrollView focusStackParamsContainer = dialogLayout.findViewById(R.id.exposure_params_container);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            focusStackParamsContainer.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                    for (ToneMappingParamEnum tmpe: ToneMappingParamEnum.values()) {
                        BubbleSeekBar seekBar = (BubbleSeekBar)dialogLayout.findViewById(tmpe.getSeekBarResId());
                        seekBar.correctOffsetWhenContainerOnScrolling();
                    }
                }
            });
        }

        builder.setView(dialogLayout);  // builder设置布局

        // 在builder中设置取消按钮
        builder.setNegativeButton(R.string.cancel_action, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (ToneMappingParamEnum tmpe: ToneMappingParamEnum.values()) {
                    tmpe.setTmpValue(tmpe.getValue());
                }
            }
        });

        // builder 设置确定按钮
        builder.setPositiveButton(R.string.ok_action, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 存储当前设置的参数值
                for (ToneMappingParamEnum tmpe: ToneMappingParamEnum.values()) {
                    if (toneMappingMethodIndex == tmpe.getMethodId()) {
                        tmpe.setValue(tmpe.getTmpValue());
                        // 存储入设置数据
                        String paramName = ExposureBaseActivity.this.getString(tmpe.getNameResId());
                        SP.putFloat(paramName, tmpe.getValue());
                    }
                }

                dialogInterface.dismiss();  // 关闭AlertDialog的最好方式
            }
        });


        // 获取dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        AlertDialogsHelper.setButtonTextColor(new int[]{DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE},
                ThemeHelper.getAccentColor(this), dialog);

    }

    private void initUI() {

        // 0. 设置图片选择的step view
        stepView = findViewById(R.id.photo_operate_step_view);
        stepView.setSteps(Arrays.asList(getResources().getStringArray(R.array.exposure_steps)));

        // 1. 初始化显示选择图片的RecyclerView
        recyclerView = findViewById(R.id.photo_operate_rv);
        photoAdapter = new PhotoAdapter(selectedPhotos, this);

        // 设置recyclerView的总列数
        if (selectedPhotos.size() <= 0) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, OrientationHelper.VERTICAL));
        } else {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(PHOTO_PICKER_SPAN_COUNT, OrientationHelper.VERTICAL));
        }
        recyclerView.setAdapter(photoAdapter);

        recyclerView.addOnItemTouchListener(new PhotoItemClickListener(this,
                new PhotoItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (photoAdapter.getItemViewType(position) == PhotoAdapter.TYPE_ADD) {
                            PhotoPicker.builder()
                                    .setSelected(selectedPhotos)
                                    .setPhotoCount(PhotoAdapter.MAX_PHOTO_COUNT)
                                    .start(ExposureBaseActivity.this);
                        } else {
                            PhotoPreview.builder()
                                    .setPhotos(selectedPhotos)
                                    .setCurrentItem(position)
                                    .start(ExposureBaseActivity.this);
                        }
                    }
                }));

        // 2. 设置 选择图片/进行图片曝光合成 操作的按钮
        operateBtn = findViewById(R.id.photo_operate_btn);
        updateStepInfo(ExposureEnum.EXPOSURE_SELECT_PHOTOS.getCode());  // 初始化操作步骤信息（包括stepview和btn文字两个部分）
        operateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentStep = stepView.getCurrentStep();
                if (currentStep == ExposureEnum.EXPOSURE_SELECT_PHOTOS.getCode()) {
                    PhotoPicker.builder()
                            .setGridColumnCount(PHOTO_PICKER_SPAN_COUNT)
                            .setPhotoCount(PhotoAdapter.MAX_PHOTO_COUNT)
                            .start(ExposureBaseActivity.this);
                } else if (currentStep == ExposureEnum.EXPOSURE_RESULT.getCode()) {
                    // 开始曝光合成操作
                    if (selectedPhotos.size() < 2) {
                        Toast.makeText(ExposureBaseActivity.this, getResources().getString(R.string.exposure_photo_count_not_enough), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // 获取所选择图片的曝光时间
                    List<Float> exposureTimes = new ArrayList<>();
                    for (String photoPath: selectedPhotos) {
                        float exposureTime = MediaExifHelper.getExposureTime(photoPath);
                        exposureTimes.add(exposureTime);
                    }

                    final String resImgPath = FileUtils.generateImgAbsPath();  // 曝光合成结果图片的路径
                    ExposureMergeThread exposureMergeThread = new ExposureMergeThread(ExposureBaseActivity.this,
                            selectedPhotos, (ArrayList<Float>) exposureTimes,
                            new ExposureProcessFinishListener() {
                        @Override
                        public void onExposureProcessFinish(int expResCode) {
                            // 查看曝光合成操作是否成功
                            if (expResCode == ExposureEnum.EXPOSURE_MERGE_SUCCESS.getCode()) {
                                System.out.println(resImgPath);
                                // 更新MediaStore的信息
                                FileUtils.updateMediaStore(getApplicationContext(), new File(resImgPath), false, null);
                                ExposureOperator.resultBuilder()
                                        .setExposureResPath(resImgPath)
                                        .setExposureResultUri(Uri.fromFile(new File(resImgPath)))
                                        .setIsFromOperate(true)
                                        .start(ExposureBaseActivity.this);
                            } else {
                                Toast.makeText(ExposureBaseActivity.this,
                                        ExposureEnum.EXPOSURE_MERGE_FAILED.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                            resImgPath,
                            toneMappingMethodIndex,
                            expoResMat.getNativeObjAddr());
                    exposureMergeThread.startExposureMerge();
                }
            }
        });
    }


    // 根据情况显示step view
    private void updateStepInfo(int currentStep) {
        // 首先更新step view的信息
        if (currentStep < stepView.getStepCount()) {
            stepView.go(currentStep, true);
        }

        // 然后更新图片对齐按钮的信息
        if (currentStep == ExposureEnum.EXPOSURE_SELECT_PHOTOS.getCode()) {
            operateBtn.setText(R.string.exposure_btn_select_label);
        } else if (currentStep == ExposureEnum.EXPOSURE_RESULT.getCode()) {
            operateBtn.setText(R.string.exposure_operate_btn_label);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK &&
                (PhotoPicker.REQUEST_CODE == requestCode || PhotoPreview.REQUEST_CODE == requestCode)) {
            if (selectedPhotos.size() <= 0) {
                updateStepInfo(ExposureEnum.EXPOSURE_SELECT_PHOTOS.getCode());
            } else {
                updateStepInfo(ExposureEnum.EXPOSURE_RESULT.getCode());
            }
        }
    }

    /**
     * 设置当前tone mappping的参数设置面板的可见性
     * @param dialogLayout
     * @param showIndex
     */
    private void showToneMappingPanel(View dialogLayout, int showIndex) {
        if (showIndex < 0 || showIndex >= ToneMappingMethodEnum.values().length) {
            return;
        }

        for (ToneMappingMethodEnum tme: ToneMappingMethodEnum.values()) {
            int panelResId = tme.getToneMappingPanelResId();
            int methodId = tme.getMethodId();

            if (showIndex == methodId) {
                dialogLayout.findViewById(panelResId).setVisibility(View.VISIBLE);
            } else {
                dialogLayout.findViewById(panelResId).setVisibility(View.GONE);
            }
        }
    }


    /**
     * 设置关于色调映射函数的SeekBar
     * @param dialogLayout
     * @param tmpe
     */
    private void initToneMappingSeekBar(View dialogLayout, ToneMappingParamEnum tmpe) {
        BubbleSeekBar bgThresholdSeekBar = dialogLayout.findViewById(tmpe.getSeekBarResId());
        bgThresholdSeekBar.getConfigBuilder()
                .min(tmpe.getMinValue())
                .max(tmpe.getMaxValue())
                .showSectionText()
                .sectionTextColor(getResources().getColor(R.color.icongrey))
                .build();
        bgThresholdSeekBar.setProgress(tmpe.getValue());
        bgThresholdSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                tmpe.setTmpValue(progressFloat);
            }
            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }
            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });
    }

}
