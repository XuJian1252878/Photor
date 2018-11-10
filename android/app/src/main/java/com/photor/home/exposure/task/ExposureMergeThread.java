package com.photor.home.exposure.task;

import android.app.Activity;
import android.view.View;

import com.example.theme.ThemeHelper;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.photor.R;
import com.photor.home.exposure.event.ExposureProcessFinishListener;
import com.photor.home.exposure.event.ToneMappingMethodEnum;
import com.photor.home.exposure.event.ToneMappingParamEnum;
import com.photor.home.staralign.task.StarPhotoAlignThread;
import com.photor.widget.BaseDialog;

import java.util.ArrayList;

public class ExposureMergeThread extends Thread {


    private native int exposureMergePhotos(ArrayList<String> photos, ArrayList<Float> exposureTimes, long resImgAddr, String resImgPath);

    private native int exposureMergePhotosDrago(ArrayList<String> photos,
                                                ArrayList<Float> exposureTimes,
                                                long resImgAddr,
                                                String resImgPath,
                                                float gamma_drago,
                                                float saturation_drago,
                                                float bias_drago);

    private native int exposureMergePhotosDurand(ArrayList<String> photos,
                                                 ArrayList<Float> exposureTimes,
                                                 long resImgAddr,
                                                 String resImgPath,
                                                 float gamma_durand,
                                                 float saturation_durand,
                                                 float contrast_durand,
                                                 float sigma_space_durand,
                                                 float sigma_color_durand);

    private native int exposureMergePhotosMantiuk(ArrayList<String> photos,
                                                  ArrayList<Float> exposureTimes,
                                                  long resImgAddr,
                                                  String resImgPath,
                                                  float gamma_mantiuk,
                                                  float saturation_mantiuk,
                                                  float scale_mantiuk);

    private native int exposureMergePhotosReinhard(ArrayList<String> photos,
                                                  ArrayList<Float> exposureTimes,
                                                  long resImgAddr,
                                                  String resImgPath,
                                                   float gamma_reinhard,
                                                   float color_adapt_reinhard,
                                                   float light_adapt_reinhard,
                                                   float intensity_reinhard);

    private Activity activity;
    private ArrayList<String> photos;
    private ArrayList<Float> exposureTimes;
    private ExposureProcessFinishListener processFinishListener;
    private String resImgPath;
    private int toneMappingMethodIndex;
    private long resImgAddr;

    private int expResFlag = 0;

    private SweetAlertDialog exposureProcessDialog;

    public ExposureMergeThread(Activity activity,
                               ArrayList<String> photos,
                               ArrayList<Float> exposureTimes,
                               ExposureProcessFinishListener processFinishListener,
                               String resImgPath,
                               int toneMappingMethodIndex,
                               long resImgAddr) {
        this.activity = activity;
        this.photos = photos;
        this.exposureTimes = exposureTimes;
        this.processFinishListener = processFinishListener;
        this.resImgPath = resImgPath;
        this.toneMappingMethodIndex = toneMappingMethodIndex;
        this.resImgAddr = resImgAddr;
    }

    @Override
    public void run() {
        super.run();

        if (toneMappingMethodIndex == ToneMappingMethodEnum.Drago.getMethodId()) {
            expResFlag = exposureMergePhotosDrago(this.photos, this.exposureTimes, this.resImgAddr, this.resImgPath,
                        ToneMappingParamEnum.gamma_drago.getValue(),
                        ToneMappingParamEnum.saturation_drago.getValue(),
                        ToneMappingParamEnum.bias_drago.getValue());
        } else if (toneMappingMethodIndex == ToneMappingMethodEnum.Durand.getMethodId()) {
            expResFlag = exposureMergePhotosDurand(this.photos, this.exposureTimes, this.resImgAddr, this.resImgPath,
                        ToneMappingParamEnum.gamma_durand.getValue(),
                        ToneMappingParamEnum.saturation_durand.getValue(),
                        ToneMappingParamEnum.contrast_durand.getValue(),
                        ToneMappingParamEnum.sigma_space_durand.getValue(),
                        ToneMappingParamEnum.sigma_color_durand.getValue()
                    );
        } else if (toneMappingMethodIndex == ToneMappingMethodEnum.Mantiuk.getMethodId()) {
            expResFlag = exposureMergePhotosMantiuk(this.photos, this.exposureTimes, this.resImgAddr, this.resImgPath,
                    ToneMappingParamEnum.gamma_mantiuk.getValue(),
                    ToneMappingParamEnum.saturation_mantiuk.getValue(),
                    ToneMappingParamEnum.scale_mantiuk.getValue());

        } else if (toneMappingMethodIndex == ToneMappingMethodEnum.Reinhard.getMethodId()) {
            expResFlag = exposureMergePhotosReinhard(this.photos, this.exposureTimes, this.resImgAddr, this.resImgPath,
                    ToneMappingParamEnum.gamma_reinhard.getValue(),
                    ToneMappingParamEnum.color_adapt_reinhard.getValue(),
                    ToneMappingParamEnum.light_adapt_reinhard.getValue(),
                    ToneMappingParamEnum.intensity_reinhard.getValue());
        }

//        expResFlag = exposureMergePhotos(this.photos, this.exposureTimes, this.resImgAddr, this.resImgPath);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 操作结束之后
                exposureProcessDialog.dismiss();
                processFinishListener.onExposureProcessFinish(expResFlag);
            }
        });
    }

    public void startExposureMerge() {
        exposureProcessDialog = new SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE);
        exposureProcessDialog.setTitleText(activity.getString(R.string.exposure_progress_dialog_title));  // 设置对话框title
        exposureProcessDialog.getProgressHelper().setBarColor(ThemeHelper.getPrimaryColor(activity));// 设置对话框进度条颜色
        exposureProcessDialog.setContentText(activity.getString(R.string.loading))
                .setCancelText("取消曝光合成操作")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        if (ExposureMergeThread.this.isAlive()) {
                            ExposureMergeThread.this.interrupt();
                        }
                        exposureProcessDialog.dismiss();
                    }
                })
                .show();

        this.start();
    }

}
