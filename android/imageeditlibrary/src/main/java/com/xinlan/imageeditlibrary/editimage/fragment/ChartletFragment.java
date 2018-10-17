package com.xinlan.imageeditlibrary.editimage.fragment;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

import com.xinlan.imageeditlibrary.R;
import com.xinlan.imageeditlibrary.editimage.EditImageActivity;
import com.xinlan.imageeditlibrary.editimage.ModuleConfig;
import com.xinlan.imageeditlibrary.editimage.task.StickerTask;
import com.xinlan.imageeditlibrary.editimage.view.StickerItem;
import com.xinlan.imageeditlibrary.editimage.view.StickerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author htwxujian@gmail.com
 * @date 2018/10/17 11:24
 */
public class ChartletFragment extends BaseEditFragment {

    public static final int INDEX = ModuleConfig.INDEX_CHARTLET;
    public static final String TAG = ChartletFragment.class.getName();

    private View mainView;
    private ViewFlipper flipper;
    private View backToMainBtn;
    private RecyclerView chartletRv;

    private StickerView mChartletView;  // 照片贴图控件显示
    private List<Bitmap> thumbsBitmap; // 显示照片的缩略图信息
    private List<String> selectedImgPaths; // 当前照片贴图的路径信息

    private SaveChartletTask saveChartletTask;  // 保存贴图的任务

    public static ChartletFragment newInstance() {
        ChartletFragment chartletFragment = new ChartletFragment();
        return chartletFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_edit_image_chartlet, null);
        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        flipper = mainView.findViewById(R.id.flipper);
        flipper.setInAnimation(activity, R.anim.in_bottom_to_top);
        flipper.setOutAnimation(activity, R.anim.out_bottom_to_top);

        backToMainBtn = mainView.findViewById(R.id.back_to_main);  // 返回主操作面板的按钮
        backToMainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToMain();
            }
        });
        this.mChartletView = activity.mChartletView;

        chartletRv = mainView.findViewById(R.id.chatlet_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        chartletRv.setLayoutManager(layoutManager);

        ChartletAdapter chartletAdapter = new ChartletAdapter();
        chartletRv.setAdapter(chartletAdapter);
    }

    @Override
    public void onShow() {
        activity.setBottomGalleryHeight(R.dimen.editor_frame_mid_row_size);
        activity.mode = EditImageActivity.MODE_CHARTLET;
        // 设置贴图页面可见
        activity.mChartletFragment.getmChartletView().setVisibility(View.VISIBLE);
        activity.bannerFlipper.showNext(); // 显示下一个操作页面
        this.selectedImgPaths = activity.selectedImgPaths;
        // 进行加载缩略图的任务
        new AsyncThumbs().execute();
    }

    @Override
    public void backToMain() {
        activity.setBottomGalleryHeight(R.dimen.bottom_banner_height);
        activity.mode = EditImageActivity.MODE_NONE;
        activity.bottomGallery.setCurrentItem(0);
        mChartletView.setVisibility(View.GONE);  // 隐藏贴图按钮
        activity.bannerFlipper.showPrevious();
    }

    /**
     * 获得缩小一定倍数的bitmap
     * @param bitmap
     * @param divisor
     * @return
     */
    private Bitmap getResizeBitmap(Bitmap bitmap, int divisor) {
        float scale = 1 / (float)divisor;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        int bmWidth = bitmap.getWidth();
        int bmHeight = bitmap.getHeight();
        // 把图片缩小到只剩原来的1/25
        return Bitmap.createBitmap(bitmap, 0, 0, bmWidth, bmHeight, matrix, false);
    }

    /**
     * 照片加载的缩略图任务
     */
    private class AsyncThumbs extends AsyncTask<Void, Void, Void> {

        private Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = activity.getLoadingDialog(getContext(), R.string.handing, false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (thumbsBitmap == null) {
                thumbsBitmap = new ArrayList<>();
                int length = selectedImgPaths == null ? 0 : selectedImgPaths.size();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_4444;
                for (int i = 0; i < length; i ++) {
                    Bitmap bm = BitmapFactory.decodeFile(selectedImgPaths.get(i), options);
                    thumbsBitmap.add(getResizeBitmap(bm, 5));
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (thumbsBitmap == null) {
                return;
            }
            // 通知Adapter更新界面信息
            chartletRv.getAdapter().notifyDataSetChanged();
            dialog.dismiss();
        }
    }

    public StickerView getmChartletView() {
        return mChartletView;
    }

    /**
     * 照片贴图Adapter
     */
    private class ChartletAdapter extends RecyclerView.Adapter<ChartletAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.frames, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
            if (thumbsBitmap == null || thumbsBitmap.size() <= 0
                    || selectedImgPaths == null || selectedImgPaths.size() <= 0) {
                return;
            }
            // 设置ImageView的布局信息
            int frameImageSize = (int)getResources().getDimension(R.dimen.icon_item_image_size_frame_preview);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(frameImageSize, frameImageSize);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            holder.thumbImg.setLayoutParams(layoutParams);
            holder.thumbImg.setImageBitmap(thumbsBitmap.get(position));
            holder.thumbImg.setTag(selectedImgPaths.get(position));  // 设置当前选择照片的路径信息
            // 设置thumbImg的点击事件
            holder.thumbImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new LoadOriginChartlatTask().execute(selectedImgPaths.get(position));
                }
            });
        }

        @Override
        public int getItemCount() {
            return selectedImgPaths == null ? 0 : selectedImgPaths.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private ImageView thumbImg;

            public ViewHolder(View itemView) {
                super(itemView);
                thumbImg = itemView.findViewById(R.id.frame_image);
            }
        }
    }

    /**
     * 当点击某一个缩略图的时候加载对应原图的操作
     */
    private class LoadOriginChartlatTask extends AsyncTask<String, Void, Void> {

        private Dialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = activity.getLoadingDialog(getContext(), R.string.handing, false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            String selectedImgPath = params[0];
            selectedChartletItem(selectedImgPath);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
        }
    }

    /**
     * 当选中某一个缩略图的时候，加载对应的原图信息
     * @param selectedImgPath
     */
    private void selectedChartletItem(String selectedImgPath) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmap = BitmapFactory.decodeFile(selectedImgPath);
        mChartletView.addBitImage(bitmap);
    }

    /**
     * 保存合成的相片贴图的任务
     */
    private class SaveChartletTask extends StickerTask {

        public SaveChartletTask(EditImageActivity activity) {
            super(activity);
        }

        @Override
        public void handleImage(Canvas canvas, Matrix m) {
            LinkedHashMap<Integer, StickerItem> addItems = mChartletView.getBank();
            for (Integer id: addItems.keySet()) {
                StickerItem item = addItems.get(id);
                item.matrix.postConcat(m);  // 乘以底部图片变化矩阵
                canvas.drawBitmap(item.bitmap, item.matrix, null);
            }
        }

        @Override
        public void onPostResult(Bitmap result) {
            mChartletView.clear();
            activity.changeMainBitmap(result, true);
            backToMain();
        }
    }

    /**
     * 保存贴图，合成一张图片
     */
    public void applyChartlet() {
        if (saveChartletTask != null) {
            saveChartletTask.cancel(true);
        }
        saveChartletTask = new SaveChartletTask((EditImageActivity) getActivity());
        saveChartletTask.execute(activity.getMainBit());
    }

}
