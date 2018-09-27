package com.photor.staralign.task;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.photor.R;
import com.photor.staralign.event.StarAlignProgressListener;
import com.photor.widget.BaseDialog;

import java.util.ArrayList;

/**
 * Created by xujian on 2018/3/2.
 */

public class StarPhotoAlignThread extends Thread {

    private Activity activity;
    private ArrayList<String> starPhotos;
    private int alignBasePhotoIndex;
    private long alignResMatAddr;
    private String generateImgAbsPath;

    private String maskImgPath;

    private volatile int alignResFlag = -3; // 表示未执行完成

    private BaseDialog starAlignProgressDialog;

    private StarAlignProgressListener starAlignProgressListener;

    public StarPhotoAlignThread(Activity activity, ArrayList<String> starPhotos,
                                int alignBasePhotoIndex, long alignResMatAddr,
                                String generateImgAbsPath,
                                String maskImgPath,
                                StarAlignProgressListener starAlignProgressListener) {
        super();
        this.activity = activity;
        this.starPhotos = starPhotos;  // 选择的星空图片数据
        this.alignBasePhotoIndex = alignBasePhotoIndex;  // 作为基准的星空图片下标
        this.alignResMatAddr = alignResMatAddr;  // 图片结果的Mat地址
        this.generateImgAbsPath = generateImgAbsPath;  // 生成的对齐结果图片的路径
        this.maskImgPath = maskImgPath;  // 用户划分的mask图片的存储路径
        this.starAlignProgressListener = starAlignProgressListener;
    }

    @Override
    public void run() {
        super.run();
        // 进行图片对齐操作
        alignResFlag = alignStarPhotos(starPhotos, alignBasePhotoIndex, alignResMatAddr, maskImgPath, generateImgAbsPath);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                starAlignProgressDialog.dismiss();
                starAlignProgressListener.onStarAlignThreadFinish(alignResFlag);
            }
        });
    }

    public void startAlign() {
        // 在主线程设置弹出ProgressDialog
        starAlignProgressDialog = new BaseDialog.Builder(activity)
                .setTitle(activity.getString(R.string.star_align_progress_dialog_title))
                .setProgressBarShow(true)
                .setCancelable(false) // 设置触摸屏幕不可取消
                .setNegativeButton("取消图片对齐操作", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (StarPhotoAlignThread.this.isAlive()) {
                            StarPhotoAlignThread.this.stop();
                        }
                        starAlignProgressDialog.dismiss();
                    }
                })
                .setNegativeBtnShow(true)
                .setMessage("正在对齐图片")
                .create();
        starAlignProgressDialog.show();

        // 开始对齐操作
        this.start();
    }

    // 用户点击取消按钮的时候
    public void stopAlign() {
        if (this.isAlive()) {
            this.stop();  // 直接中断对齐过程
        }
        starAlignProgressDialog.dismiss();
    }



    // 进行图像对齐操作的 jni native function
    private native int alignStarPhotos(ArrayList<String> starPhotos, int alignBasePhotoIndex, long alignResMatAddr, String maskImgPath, String generateImgAbsPath);
}
