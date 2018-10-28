package com.xinlan.imageeditlibrary.editimage.fragment;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.xinlan.imageeditlibrary.BaseActivity;
import com.xinlan.imageeditlibrary.R;
import com.xinlan.imageeditlibrary.editimage.EditImageActivity;
import com.xinlan.imageeditlibrary.editimage.ModuleConfig;
import com.xinlan.imageeditlibrary.editimage.adapter.EditorRecyclerAdapter;
import com.xinlan.imageeditlibrary.editimage.fliter.PhotoProcessing;
import com.xinlan.imageeditlibrary.editimage.task.ImageProcessingTask;
import com.xinlan.imageeditlibrary.editimage.view.imagezoom.ImageViewTouchBase;

import java.util.ArrayList;


/**
 * 滤镜列表fragment
 * xujian 2018/10/14
 *
 */
public class FilterListFragment extends BaseEditFragment {
    public static final int INDEX = ModuleConfig.INDEX_FILTER;
    public static final String TAG = FilterListFragment.class.getName();
    private View mainView;
    private View backBtn;// 返回主菜单按钮

    private Bitmap fliterBit;// 滤镜处理后的bitmap

    private RecyclerView mFilterGroup;

    private Bitmap currentBitmap;// 标记变量
    private static ArrayList<Bitmap> filterThumbs;  // 滤镜结果预览信息
    private EditorRecyclerAdapter adapter;

    private int bmWidth = -1, bmHeight = -1;

    private View filterCancelBtn, filterApplyBtn;
    private SeekBar filterSeerbar;

    private ViewFlipper flipper;

    private int CURRENT_FILTER_TYPE = 0; // 设置当前滤镜的类别

    public static FilterListFragment newInstance() {
        FilterListFragment fragment = new FilterListFragment();
        return fragment;
    }

    /**
     * 解决重新进入图片编辑界面时，图片滤镜缩略图不变的bug
     */
    public static void initFilterThumbs() {
        filterThumbs = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_edit_image_fliter, null);
        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        backBtn = mainView.findViewById(R.id.back_to_main);
        mFilterGroup = mainView.findViewById(R.id.filter_group);

        flipper = mainView.findViewById(R.id.flipper);

        filterCancelBtn = mainView.findViewById(R.id.seekbar_cancel);
        filterCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                flipper.showPrevious();
                // 说明取消了当前的滤镜结果
                fliterBit = currentBitmap;
                activity.mainImage.setImageBitmap(currentBitmap);
                activity.setBottomGalleryHeight(R.dimen.editor_filter_mid_row_size);
            }
        });

        filterApplyBtn = mainView.findViewById(R.id.seekbar_apply);
        filterApplyBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                flipper.showPrevious();
                // 说明采用了当前的滤镜结果
                currentBitmap = fliterBit;
                activity.mainImage.setImageBitmap(currentBitmap);
                activity.setBottomGalleryHeight(R.dimen.editor_filter_mid_row_size);
            }
        });

        // 设置seekbar
        filterSeerbar = mainView.findViewById(R.id.slider);
        filterSeerbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                if (fromUser) {
                    new FilterImageTask(CURRENT_FILTER_TYPE, value).execute();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        backBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMain();
            }
        });
        setUpFliters();
    }

    @Override
    public void onShow() {
        activity.setBottomGalleryHeight(R.dimen.editor_filter_mid_row_size);  // 设置滤镜预览的高度信息
        activity.mode = EditImageActivity.MODE_FILTER;
        activity.mFilterListFragment.setCurrentBitmap(activity.getMainBit());
        activity.mainImage.setImageBitmap(activity.getMainBit());
        activity.mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        activity.mainImage.setScaleEnabled(false);
        activity.bannerFlipper.showNext();
        // 加载全部滤镜效果的缩略图
        getFilterThumbs();
    }

    /**
     * 返回主菜单
     */
    @Override
    public void backToMain() {
        activity.setBottomGalleryHeight(R.dimen.bottom_banner_height);
        currentBitmap = activity.getMainBit();
        fliterBit = null;
        activity.mainImage.setImageBitmap(activity.getMainBit());// 返回原图
        activity.mode = EditImageActivity.MODE_NONE;
        activity.bottomGallery.setCurrentItem(0);
        activity.mainImage.setScaleEnabled(true);
        activity.bannerFlipper.showPrevious();
    }

    private void setDefaultSeekBarProgress(int position) {
        switch (position) {
            default:
                filterSeerbar.setProgress(100);
                break;
        }
    }

    /**
     * 保存滤镜处理后的图片
     */
    public void applyFilterImage() {
        // System.out.println("保存滤镜处理后的图片");
        if (currentBitmap == activity.getMainBit()) {// 原始图片
            // System.out.println("原始图片");
            backToMain();
            return;
        } else {// 经滤镜处理后的图片
            // System.out.println("滤镜图片");
            activity.changeMainBitmap(fliterBit,true);
            backToMain();
        }// end if
    }

    /**
     * 跳转至滤镜滑动调节界面
     */
    private void swipToFilterProgress(int position) {
        flipper.showNext();
        setDefaultSeekBarProgress(position);
    }

    /**
     * 装载滤镜
     */
    private void setUpFliters() {
        String[] filterTitles = getResources().getStringArray(R.array.filter_titles);
        if (filterTitles == null)
            return;

        adapter = new EditorRecyclerAdapter(getContext(), mFilterGroup, EditImageActivity.MODE_FILTER, new EditorRecyclerAdapter.OnEditorItemClickListener() {
            @Override
            public void onEditorItemClick(int position, View itemView) {
                CURRENT_FILTER_TYPE = position;  // 记录当前的滤镜类别
                swipToFilterProgress(position);  // 显示滑动条按钮
                // 设置被点击时的滤镜效果（初始被点击时为全滤镜效果）
                new FilterImageTask(position, 100).execute();
                activity.setBottomGalleryHeight(R.dimen.bottom_banner_height);
            }
        });
        adapter.setFilterThumbs(filterThumbs);  // 设置缩略图信息
        mFilterGroup.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);  // 不设置布局，recyclerView不会显示
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFilterGroup.setLayoutManager(layoutManager);
        mFilterGroup.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        if (fliterBit != null && (!fliterBit.isRecycled())) {
            fliterBit.recycle();
        }
        super.onDestroy();
    }

    public Bitmap getCurrentBitmap() {
        return currentBitmap;
    }

    public void setCurrentBitmap(Bitmap currentBitmap) {
        this.currentBitmap = currentBitmap;
    }


    public void getFilterThumbs() {
        if (currentBitmap != null) {
            GetFilterThumbsTask getFilterThumbsTask = new GetFilterThumbsTask();
            getFilterThumbsTask.execute();
        }
    }

    private Bitmap getResizeBitmap(Bitmap bitmap, int divisor) {
        float scale = 1 / (float)divisor;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        if (bmWidth < 0) bmWidth = bitmap.getWidth();
        if (bmHeight < 0) bmHeight = bitmap.getHeight();
        // 把图片缩小到只剩原来的1/25
        return Bitmap.createBitmap(bitmap, 0, 0, bmWidth, bmHeight, matrix, false);
    }

    /**
     * 图像滤镜缩略图处理任务
     */
    private class GetFilterThumbsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            TypedArray titleList = getContext().getResources().obtainTypedArray(R.array.filter_titles);

            if (filterThumbs == null) {
                filterThumbs = new ArrayList<>();
                bmWidth = currentBitmap.getWidth();
                bmHeight = currentBitmap.getHeight();
                int length = titleList != null ? titleList.length() : 0;

                for (int i = 0; i < length; i ++) {
                    // 获取全部的滤镜缩略图信息
                    filterThumbs.add(ImageProcessingTask.processImage(
                            getResizeBitmap(currentBitmap, 5),
                            i + 100 * EditImageActivity.MODE_FILTER,
                            100));
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (filterThumbs == null) {
                return;
            }
            // 设置RecyclerView数据变化
            adapter.setFilterThumbs(filterThumbs);  // 重新设置filterThumbs数据
            mFilterGroup.getAdapter().notifyDataSetChanged();
        }
    }


    /**
     * 图像滤镜处理任务
     */
    private class FilterImageTask extends AsyncTask<Void, Integer, Bitmap> {

        private int filterType;
        private Dialog dialog;
        private int value;

        public FilterImageTask(int filterType, int value) {
            this.filterType = filterType;
            this.value = value;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = BaseActivity.getLoadingDialog(getContext(), R.string.handing, false);
            dialog.show();
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap outBit = ImageProcessingTask.processImage(currentBitmap, filterType + 100 * EditImageActivity.MODE_FILTER, value);
            return outBit;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            dialog.dismiss();
            if (bitmap == null) {
                return;
            } else {
                activity.mainImage.setImageBitmap(bitmap);
                fliterBit = bitmap;
            }
        }
    }
}// end class
