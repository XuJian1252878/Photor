package com.photor.exposure.task;

import android.app.Activity;
import android.view.View;

import com.photor.R;
import com.photor.exposure.event.ExposureProcessFinishListener;
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

    private BaseDialog exposureProcessDialog;

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
        exposureProcessDialog = new BaseDialog.Builder(activity)
                .setTitle(activity.getString(R.string.exposure_progress_dialog_title))
                .setProgressBarShow(true)
                .setCancelable(false)
                .setNegativeButton("取消曝光合成操作", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ExposureMergeThread.this.isAlive()) {
                            ExposureMergeThread.this.interrupt();
                        }
                        exposureProcessDialog.dismiss();
                    }
                })
                .setNegativeBtnShow(true)
                .setMessage("正在曝光合成")
                .create();
        exposureProcessDialog.show();

        this.start();
    }

}
