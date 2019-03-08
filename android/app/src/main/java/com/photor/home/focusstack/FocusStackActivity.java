package com.photor.home.focusstack;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.example.focusstackinglib.FocusStackProcessing;
import com.example.photopicker.PhotoPicker;
import com.example.photopicker.PhotoPreview;
import com.example.preference.PreferenceUtil;
import com.example.theme.ThemeHelper;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.photor.R;
import com.photor.base.activity.PhotoOperateBaseActivity;
import com.photor.base.adapters.PhotoAdapter;
import com.photor.base.adapters.event.PhotoItemClickListener;
import com.photor.home.exposure.event.ExposureEnum;
import com.photor.home.exposure.task.ExposureMergeThread;
import com.photor.home.focusstack.event.FocusStackEnum;
import com.photor.util.AlertDialogsHelper;
import com.xw.repo.BubbleSeekBar;

import java.io.File;
import java.util.Arrays;

/**
 * @author htwxujian@gmail.com
 * @date 2018/10/18 14:25
 */
public class FocusStackActivity extends PhotoOperateBaseActivity {

    private String resFocusStackPath = null;
    private static final int MAX_PHOTO_COUNT = 15;

    private int bg_threshold = 70;
    private int bg_threshold_start = 0, bg_threshold_end = 255;

    private short kernels_size = 7;
    private short kernels_size_start = 3, kernels_size_end = 13;

    private float gaussian_sigma = 5.0f;
    private float gaussian_sigma_start = 2.0f, gaussian_sigma_end = 7.0f;

    private PreferenceUtil SP;  // 存储景深合成配置信息

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化景深合成的参数信息
        SP = PreferenceUtil.getInstance(this);
        bg_threshold = SP.getInt(getResources().getString(R.string.focus_stack_preference_bg_threshold), bg_threshold);
        kernels_size = (short) SP.getInt(getResources().getString(R.string.focus_stack_preference_kernels_size), (int)kernels_size);
        gaussian_sigma = SP.getFloat(getResources().getString(R.string.focus_stack_preference_gaussian_sigma), gaussian_sigma);
        initUI();
    }

    private void initUI() {
        // 0. 设置图片选择的step view
        stepView = findViewById(R.id.photo_operate_step_view);
        stepView.setSteps(Arrays.asList(getResources().getStringArray(R.array.focus_stack_steps)));

        // 1. 初始化显示选择图片的RecyclerView
        recyclerView = findViewById(R.id.photo_operate_rv);
        photoAdapter = new PhotoAdapter(selectedPhotos, this);
        photoAdapter.setMaxPhotoCount(MAX_PHOTO_COUNT);

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
                                    .setPhotoCount(MAX_PHOTO_COUNT)
                                    .start(FocusStackActivity.this);
                        } else {
                            PhotoPreview.builder()
                                    .setPhotos(selectedPhotos)
                                    .setCurrentItem(position)
                                    .start(FocusStackActivity.this);
                        }
                    }
                }));

        // 2. 设置 选择图片/进行图片景深合成 操作的按钮
        operateBtn = findViewById(R.id.photo_operate_btn);
        updateStepInfo(FocusStackEnum.FOCUS_STACK_SELECT_PHOTO.getCode());  // 设置操作步骤信息
        operateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentStep = stepView.getCurrentStep();
                if (currentStep == FocusStackEnum.FOCUS_STACK_SELECT_PHOTO.getCode()) {
                    // 1. 第一步：选择景深合成的原始照片
                    PhotoPicker.builder()
                            .setGridColumnCount(PHOTO_PICKER_SPAN_COUNT)
                            .setPhotoCount(MAX_PHOTO_COUNT)
                            .start(FocusStackActivity.this);
                } else if (currentStep == FocusStackEnum.FOCUS_STACK_RESULT.getCode()) {
                    // 2. 第二步：景深合成
                    if (selectedPhotos.size() < 2) {
                        Toast.makeText(FocusStackActivity.this,
                                getResources().getString(R.string.focus_stack_photo_count_not_enough),
                                Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        // 开始景深合成操作
                        new FocusStackTask().execute();
                    }
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

        if (currentStep == FocusStackEnum.FOCUS_STACK_SELECT_PHOTO.getCode()) {
            operateBtn.setText(R.string.focus_stack_btn_select_label);
        } else if (currentStep == FocusStackEnum.FOCUS_STACK_RESULT.getCode()) {
            operateBtn.setText(R.string.focus_stack_btn_operate_label);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.focus_stack_menu, menu);
        return true;  // true 表示显示该菜单
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.focus_stack_setting:
                getFocusStackSettingDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 显示景深合成的参数设置信息
     */
    private void getFocusStackSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog_Light);

        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_focus_stack, null);

        int bg_threshold_before = bg_threshold;
        short kernels_size_before = kernels_size;
        float gaussian_sigma_before = gaussian_sigma;

        BubbleSeekBar bgThresholdSeekBar = dialogLayout.findViewById(R.id.bg_threshold_seek_bar);
        bgThresholdSeekBar.getConfigBuilder()
                .min(bg_threshold_start)
                .max(bg_threshold_end)
//                .sectionCount(1)
                .showSectionText()
                .sectionTextColor(getResources().getColor(R.color.icongrey))
//                .sectionTextPosition(BubbleSeekBar.TextPosition.BELOW_SECTION_MARK)
                .build();
        bgThresholdSeekBar.setProgress(bg_threshold);
        bgThresholdSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                bg_threshold = progress;
            }
            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }
            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });

        BubbleSeekBar kernelsSizeSeekBar = dialogLayout.findViewById(R.id.kernels_size_seek_bar);
        kernelsSizeSeekBar.getConfigBuilder()
                .min(kernels_size_start)
                .max(kernels_size_end)
//                .sectionCount(1)
                .showSectionText()
                .sectionTextColor(getResources().getColor(R.color.icongrey))
//                .sectionTextPosition(BubbleSeekBar.TextPosition.BELOW_SECTION_MARK)
                .build();
        kernelsSizeSeekBar.setProgress(kernels_size);
        kernelsSizeSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                kernels_size = (short)progress;
            }
            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }
            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });

        BubbleSeekBar gaussianSigmaSeekBar = dialogLayout.findViewById(R.id.gaussian_sigma_seek_bar);
        gaussianSigmaSeekBar.getConfigBuilder()
                .min(gaussian_sigma_start)
                .max(gaussian_sigma_end)
//                .sectionCount(1)
                .showSectionText()
                .sectionTextColor(getResources().getColor(R.color.icongrey))
//                .sectionTextPosition(BubbleSeekBar.TextPosition.BELOW_SECTION_MARK)
                .build();
        gaussianSigmaSeekBar.setProgress(gaussian_sigma);
        gaussianSigmaSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                gaussian_sigma = progressFloat;
            }
            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }
            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });


        /**
         * You must correct the offsets by setting ScrollListener when BubbleSeekBar's parent view is
         * scrollable (such as ScrollView, except ViewPager), otherwise, the appearing position of
         * the bubble may be wrong. For example:
         */
        ScrollView focusStackParamsContainer = dialogLayout.findViewById(R.id.focus_stack_params_container);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            focusStackParamsContainer.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                    bgThresholdSeekBar.correctOffsetWhenContainerOnScrolling();
                    kernelsSizeSeekBar.correctOffsetWhenContainerOnScrolling();
                    gaussianSigmaSeekBar.correctOffsetWhenContainerOnScrolling();
                }
            });
        }

        // 设置CardView的背景色
        dialogLayout.findViewById(R.id.focus_stack_card).setBackgroundColor(ThemeHelper.getCardBackgroundColor(this));
        dialogLayout.findViewById(R.id.text_dialog_title).setBackgroundColor(ThemeHelper.getPrimaryColor(this));
        builder.setView(dialogLayout);  // builder设置布局


        // 在builder中设置取消按钮
        builder.setNegativeButton(R.string.cancel_action, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 恢复原来的设置值
                bg_threshold = bg_threshold_before;
                kernels_size = kernels_size_before;
                gaussian_sigma = gaussian_sigma_before;
            }
        });

        // builder 设置确定按钮
        builder.setPositiveButton(R.string.ok_action, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 存储当前设置的参数值
                SP.putInt(getResources().getString(R.string.focus_stack_preference_bg_threshold), bg_threshold);
                SP.putInt(getResources().getString(R.string.focus_stack_preference_kernels_size), kernels_size);
                SP.putFloat(getResources().getString(R.string.focus_stack_preference_gaussian_sigma), gaussian_sigma);
                dialogInterface.dismiss();  // 关闭AlertDialog的最好方式
            }
        });

        // 获取dialog
        AlertDialog dialog = builder.create();

        dialog.show();
        AlertDialogsHelper.setButtonTextColor(new int[]{DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE},
                ThemeHelper.getAccentColor(this), dialog);
    }


    /**
     * 进行景深合成的任务
     */
    private class FocusStackTask extends AsyncTask<Void, Void, Bitmap> {
//        private Dialog dialog;
        private SweetAlertDialog focusStackProcessDialog;
        boolean isCancel = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            resFocusStackPath = FileUtils.generateImgAbsPath();
//            dialog = AlertDialogsHelper.getLoadingDialog(FocusStackActivity.this,
//                    getResources().getString(R.string.loading), false);
//            dialog.show();
            focusStackProcessDialog = new SweetAlertDialog(FocusStackActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            focusStackProcessDialog.setTitleText(FocusStackActivity.this.getString(R.string.focus_stack_dialog_title));  // 设置对话框title
            focusStackProcessDialog.getProgressHelper().setBarColor(ThemeHelper.getPrimaryColor(FocusStackActivity.this));// 设置对话框进度条颜色
            focusStackProcessDialog.setContentText(FocusStackActivity.this.getString(R.string.loading))
                    .setCancelText("取消景深合成操作")
                    .showCancelButton(true)
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            isCancel = true;
                            focusStackProcessDialog.dismiss();
                        }
                    })
                    .show();
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            if (selectedPhotos == null || selectedPhotos.size() <= 0) {
                return null;
            }
            Bitmap bitmap = FocusStackProcessing.processImage(selectedPhotos, bg_threshold, kernels_size, gaussian_sigma, resFocusStackPath);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null && !isCancel) {
//                FileUtils.saveImgBitmap(resFocusStackPath, bitmap);  // native代码中已经存储了景深合成的图像，所以不需要存储第二次
                focusStackProcessDialog.dismiss();
                // 开启显示结果图片的Activity
                FocusStackOperator.builder()
                        .setFocusStackResPath(resFocusStackPath)
                        .setFocusStackResUri(Uri.fromFile(new File(resFocusStackPath)))
                        .setIsFromOperate(true)
                        .start(FocusStackActivity.this);

            } else {
                focusStackProcessDialog.dismiss();
                Toast.makeText(FocusStackActivity.this,
                        ExposureEnum.EXPOSURE_MERGE_FAILED.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
