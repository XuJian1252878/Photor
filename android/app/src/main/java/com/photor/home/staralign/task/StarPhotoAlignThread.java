package com.photor.home.staralign.task;

import android.app.Activity;
import android.graphics.Bitmap;

import com.example.theme.ThemeHelper;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.photor.R;
import com.photor.home.staralign.event.StarAlignProgressListener;
import com.photor.util.ImageUtils;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

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
    private SweetAlertDialog starAlignProgressDialog;
    private StarAlignProgressListener starAlignProgressListener;

    private List<Mat> matList = new ArrayList<>();

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

        for (int index = 0; index < starPhotos.size(); index ++) {
            matList.add(new Mat());
        }
    }

    @Override
    public void run() {
        super.run();
        // 第一种方式：进行图片对齐操作（在C++层面做了采样率压缩）
//        alignResFlag = alignStarPhotos(starPhotos, alignBasePhotoIndex, alignResMatAddr, maskImgPath, generateImgAbsPath);

        // 如果原始图片过大，那么对原始图片进行压缩
        List<Bitmap> bitmapList = ImageUtils.getCompressedImage(starPhotos, 5);
        ArrayList<Long> matNativeAddrList = new ArrayList<>();
        for (int index = 0; index < bitmapList.size(); index ++) {
            Bitmap bitmap = bitmapList.get(index);
            Mat inputMat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC3);

            inputMat.copyTo(matList.get(index));
            Utils.bitmapToMat(bitmap, matList.get(index));

            matNativeAddrList.add(matList.get(index).getNativeObjAddr());
        }

        // 2. 第二种方式
        alignResFlag =  alignStarPhotosCompress(
                matNativeAddrList,
                alignBasePhotoIndex,
                alignResMatAddr,
                maskImgPath,
                generateImgAbsPath
        );

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                starAlignProgressDialog.dismiss();
                starAlignProgressListener.onStarAlignThreadFinish(alignResFlag);

                // 销毁生成的Mat信息
                for (Mat mat: matList) {
                    mat.release();
                }
            }
        });
    }

    public void startAlign() {
        // 在主线程设置弹出ProgressDialog
        starAlignProgressDialog = new SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE);
        starAlignProgressDialog.setTitleText(activity.getString(R.string.star_align_progress_dialog_title));  // 设置对话框title
        starAlignProgressDialog.getProgressHelper().setBarColor(ThemeHelper.getPrimaryColor(activity));// 设置对话框进度条颜色
        starAlignProgressDialog.setContentText(activity.getString(R.string.loading))
                .setCancelText("取消图片对齐操作")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        if (StarPhotoAlignThread.this.isAlive()) {
                            StarPhotoAlignThread.this.interrupt();
                        }
                        starAlignProgressDialog.dismiss();
                    }
                })
                .show();

        // 设置取消按钮颜色的操作
//        starAlignProgressDialog.findViewById(R.id.cancel_button).setBackgroundColor(activity.getResources().getColor(R.color.accent_red));

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

    // 进行图像对齐操作的 jni native function （图像太大会照成OOM，后台对图像进行的是改变图像缩放的操作）
    private native int alignStarPhotos(ArrayList<String> starPhotos, int alignBasePhotoIndex, long alignResMatAddr, String maskImgPath, String generateImgAbsPath);

    // 选择图像对齐的操作，首先对图像进行压缩
    private native int alignStarPhotosCompress(ArrayList<Long> starMats,
                                               int alignBasePhotoIndex,
                                               long alignResMatAddr,
                                               String maskImgPath,
                                               String generateImgAbsPath);
}
