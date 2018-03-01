package com.photor.staralign.task;

import android.content.Context;
import android.os.AsyncTask;

import com.photor.R;
import com.photor.widget.BaseDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xujian on 2018/3/1.
 */

public class StarPhotoAlignTask extends AsyncTask<Void, Integer, Integer> {

    private Context context;
    private ArrayList<String> starPhotos;
    private int alignBasePhotoIndex;
    private long alignResMatAddr;

    private BaseDialog starAlignProgressDialog;

    public StarPhotoAlignTask(Context context, ArrayList<String> starPhotos, int alignBasePhotoIndex, long alignResMatAddr) {
        this.context = context;
        this.starPhotos = starPhotos;
        this.alignBasePhotoIndex = alignBasePhotoIndex;
        this.alignResMatAddr = alignResMatAddr;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // 在主线程设置弹出ProgressDialog
        starAlignProgressDialog = new BaseDialog.Builder(context)
                .setTitle(context.getString(R.string.star_align_progress_dialog_title))
                .setProgressBarShow(true)
                .setCancelable(false) // 设置触摸屏幕不可取消
                .create();

        starAlignProgressDialog.show();
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        starAlignProgressDialog.dismiss();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int res = alignStarPhotos(starPhotos, alignBasePhotoIndex, alignResMatAddr);
        return null;
    }

    // 进行图像对齐操作的 jni native function
    private native int alignStarPhotos(ArrayList<String> starPhotos, int alignBasePhotoIndex, long alignResMatAddr);
}
