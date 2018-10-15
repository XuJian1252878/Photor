package com.xinlan.imageeditlibrary.editimage.fragment;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.ViewFlipper;

import com.example.color.ColorPalette;
import com.example.theme.ThemeHelper;
import com.xinlan.imageeditlibrary.BaseActivity;
import com.xinlan.imageeditlibrary.R;
import com.xinlan.imageeditlibrary.editimage.EditImageActivity;
import com.xinlan.imageeditlibrary.editimage.ModuleConfig;
import com.xinlan.imageeditlibrary.editimage.adapter.EditorRecyclerAdapter;
import com.xinlan.imageeditlibrary.editimage.task.ImageProcessingTask;
import com.xinlan.imageeditlibrary.editimage.view.imagezoom.ImageViewTouchBase;

/**
 * @author htwxujian@gmail.com
 * @date 2018/10/13 22:32
 */
public class EnhanceFragment extends BaseEditFragment {

    public static final String TAG = EnhanceFragment.class.getName();
    public static final int INDEX = ModuleConfig.INDEX_ENHANCE;

    private View mainView;
    private ViewFlipper flipper;
    private View backToMain; // 返回主菜单
    private RecyclerView enhanceTypeView; // 增强类型信息
    private View backToType; // 返回类型按钮
    private View applyEnhance; // seekBar中的确认按钮
    private SeekBar operateSeekBar; // 调整增强效果的SeekBar

    private Bitmap currentBitmap; // 保存原始图片
    private Bitmap enhanceBitmap; // 保持加强处理之后的照片

    private static int CURRENT_ENHANCE_TYPE = 0; // 当前加强类型的类别

    public static EnhanceFragment newInstance() {
        EnhanceFragment fragment = new EnhanceFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mainView = inflater.inflate(R.layout.fragment_edit_image_enhance, null);
        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 设置flipper的动画信息
        flipper = mainView.findViewById(R.id.flipper);
        flipper.setInAnimation(activity, R.anim.in_bottom_to_top);
        flipper.setOutAnimation(activity, R.anim.out_bottom_to_top);

        // 设置返回主菜单按钮
        backToMain = mainView.findViewById(R.id.back_to_main);
        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToMain();
            }
        });

        // 设置当前主页面的bitmap
        currentBitmap = enhanceBitmap = activity.getMainBit();

        // 设置返回加强类型的按钮
        backToType = mainView.findViewById(R.id.seekbar_cancel);
        backToType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipper.showPrevious();  //  返回到加强类型界面
                // 说明当前所做的加强操作不作数
                activity.mainImage.setImageBitmap(currentBitmap);
                // 重置加强类型
                enhanceBitmap = currentBitmap;
            }
        });

        // 设置加强类型的确认按钮
        applyEnhance = mainView.findViewById(R.id.seekbar_apply);
        applyEnhance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipper.showPrevious();  // 返回到加强类型界面
                // 说明当前所做的加强类型有效
                currentBitmap = enhanceBitmap;  // 记录当前的加强类型
                activity.mainImage.setImageBitmap(currentBitmap);
            }
        });

        // 设置SeekBar
        operateSeekBar = mainView.findViewById(R.id.slider);
        operateSeekBar.setMax(100);
        operateSeekBar.setProgress(0);

        // 设置加强类型RecyclerView
        enhanceTypeView = mainView.findViewById(R.id.enhance_type_list);
        enhanceTypeView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        enhanceTypeView.setLayoutManager(layoutManager);

        EditorRecyclerAdapter editorRecyclerAdapter = new EditorRecyclerAdapter(getContext(),
                enhanceTypeView,
                EditImageActivity.MODE_ENHANCE, new EditorRecyclerAdapter.OnEditorItemClickListener() {
            @Override
            public void onEditorItemClick(int position, View itemView) {
                // 当某一个增强项被点击之后
                swipToEnhanceProcess(position);
                // 设置当前的类别
                CURRENT_ENHANCE_TYPE = position;
            }
        });
        enhanceTypeView.setAdapter(editorRecyclerAdapter);

        // 设置强度进度条信息
        operateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean isFromUser) {
                if (isFromUser) {
                    // 是用户拖动导致的变化，才开始改变图片效果
                    new EnhanceImageTask(CURRENT_ENHANCE_TYPE, value).execute();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * onShow 设置当前的操作类型
     * activity.mode = EditImageActivity.MODE_ENHANCE;
     * 然后apply按钮根据相对应的类型进行操作
     */
    @Override
    public void onShow() {
        activity.mode = EditImageActivity.MODE_ENHANCE;
        // 设置主页面显示的图片信息
        activity.mainImage.setImageBitmap(activity.getMainBit());
        activity.mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        activity.mEnhanceFragment.setCurrentBitmap(activity.getMainBit());
        // 禁用手势缩放
        activity.mainImage.setScaleEnabled(false);
        activity.bannerFlipper.showNext(); // 保存 -> 应用
    }

    @Override
    public void backToMain() {
        activity.mode = EditImageActivity.MODE_NONE;
        activity.mainImage.setImageBitmap(currentBitmap);
        activity.bottomGallery.setCurrentItem(0);  // 默认选择第0项，也就是主页面菜单项（那么当前的ViewPager的Fragment将会自动消失）
        activity.bannerFlipper.showPrevious(); // 从 应用 -> 保存
    }

    /**
     * 跳转至ProcessBar的界面
     */
    private void swipToEnhanceProcess(int position) {
        flipper.showNext();
        // 重新设置进度条的进度为默认值
        setDefaultSeekBarProgress(position);
    }

    /**
     * 根据不同的加强选项设置默认初始值
     * @param position
     */
    private void setDefaultSeekBarProgress(int position) {
        switch (position) {
            case 2:
            case 6:
            case 7:
            case 8:
                operateSeekBar.setProgress(0);
                break;
            case 0:
            case 1:
            case 3:
            case 4:
            case 5:
                operateSeekBar.setProgress(50);
                break;
            default:
                operateSeekBar.setProgress(0);
                break;
        }
    }

    /**
     * 设置当前已经被增强的图片信息
     * @param bitmap
     */
    public void setCurrentBitmap(Bitmap bitmap) {
        this.currentBitmap = bitmap;
    }

    /**
     * 当主界面的应用按钮被按下的时候，放弃所有当前的操作，返回主界面
     */
    public void applyEnhanceImage() {
        if (currentBitmap == activity.getMainBit()) {
            // 原始图片，没有做任何操作
            backToMain();
        } else {
            // 经过加强处理后的图片
            activity.changeMainBitmap(enhanceBitmap, true);
            // 同时增强fragment调整至增强fragment主界面
            flipper.showPrevious();
            backToMain();
        }
    }

    /**
     * 设置当前图片加强效果的后台任务
     */
    private class EnhanceImageTask extends AsyncTask<Integer, Integer, Bitmap> {

        private Dialog dialog;
        private int enhanceType;
        private int progressValue;

        public EnhanceImageTask(int enhanceType, int progressValue) {
            this.enhanceType = enhanceType;
            this.progressValue = progressValue;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = BaseActivity.getLoadingDialog(getActivity(), R.string.handing, false);
            dialog.show();
        }

        @Override
        protected Bitmap doInBackground(Integer... integers) {
            Bitmap outBit = ImageProcessingTask.processImage(currentBitmap, enhanceType, progressValue);
            return outBit;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            dialog.dismiss();
            if (bitmap == null) {
                return; // 只可能返回原图
            } else {
                activity.mainImage.setImageBitmap(bitmap);
                // 记录当前加强处理之后的照片
                enhanceBitmap = bitmap;
            }
        }
    }

}
