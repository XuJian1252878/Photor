package com.photor.home.exposure.task;

import android.app.Activity;
import android.view.View;

import com.example.theme.ThemeHelper;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.photor.R;
import com.photor.home.exposure.event.ExposureProcessFinishListener;
import com.photor.home.staralign.task.StarPhotoAlignThread;
import com.photor.widget.BaseDialog;

import java.util.ArrayList;

public class ExposureMergeThread extends Thread {


    private native int exposureMergePhotos(ArrayList<String> photos, ArrayList<Float> exposureTimes, long resImgAddr, String resImgPath);

    private Activity activity;
    private ArrayList<String> photos;
    private ArrayList<Float> exposureTimes;
    private ExposureProcessFinishListener processFinishListener;
    private String resImgPath;
    private long resImgAddr;

    private int expResFlag = 0;

    private SweetAlertDialog exposureProcessDialog;

    public ExposureMergeThread(Activity activity,
                               ArrayList<String> photos,
                               ArrayList<Float> exposureTimes,
                               ExposureProcessFinishListener processFinishListener,
                               String resImgPath,
                               long resImgAddr) {
        this.activity = activity;
        this.photos = photos;
        this.exposureTimes = exposureTimes;
        this.processFinishListener = processFinishListener;
        this.resImgPath = resImgPath;
        this.resImgAddr = resImgAddr;
    }

    @Override
    public void run() {
        super.run();
        expResFlag = exposureMergePhotos(this.photos, this.exposureTimes, this.resImgAddr, this.resImgPath);
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
