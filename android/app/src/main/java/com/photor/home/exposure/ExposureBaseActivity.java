package com.photor.home.exposure;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.Toast;

import com.example.file.FileUtils;
import com.example.media.image.MediaExifHelper;
import com.example.photopicker.PhotoPicker;
import com.example.photopicker.PhotoPreview;
import com.photor.R;
import com.photor.base.activity.PhotoOperateBaseActivity;
import com.photor.base.adapters.PhotoAdapter;
import com.photor.base.adapters.event.PhotoItemClickListener;
import com.photor.home.exposure.event.ExposureEnum;
import com.photor.home.exposure.event.ExposureProcessFinishListener;
import com.photor.home.exposure.task.ExposureMergeThread;

import org.opencv.core.Mat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExposureBaseActivity extends PhotoOperateBaseActivity {

    private Mat expoResMat = new Mat();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }


    private void initUI() {

        // 0. 设置图片选择的step view
        stepView = findViewById(R.id.photo_operate_step_view);
        stepView.setSteps(Arrays.asList(getResources().getStringArray(R.array.exposure_steps)));

        // 1. 初始化显示选择图片的RecyclerView
        recyclerView = findViewById(R.id.photo_operate_rv);
        photoAdapter = new PhotoAdapter(selectedPhotos, this);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL));
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
                            .setGridColumnCount(4)
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
                    }, resImgPath, expoResMat.getNativeObjAddr());
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
}
