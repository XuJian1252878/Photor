package com.xinlan.imageeditlibrary.editimage.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xinlan.imageeditlibrary.R;
import com.xinlan.imageeditlibrary.editimage.ModuleConfig;


/**
 * 工具栏主菜单
 *
 * @author panyi
 * xujian 2018/10/13
 */
public class MainMenuFragment extends BaseEditFragment implements View.OnClickListener {
    public static final int INDEX = ModuleConfig.INDEX_MAIN;

    public static final String TAG = MainMenuFragment.class.getName();
    private View mainView;

    private View stickerBtn;// 贴图按钮
    private View fliterBtn;// 滤镜按钮
    private View cropBtn;// 剪裁按钮
    private View rotateBtn;// 旋转按钮
    private View mTextBtn;//文字型贴图添加
    private View mPaintBtn;//编辑按钮
    private View mBeautyBtn;//美颜按钮
    private View enhanceBtn; //增强按钮
    private View frameBtn; // 相框按钮
    private View chartletBtn; // 相册贴图按钮

    public static MainMenuFragment newInstance() {
        MainMenuFragment fragment = new MainMenuFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_edit_image_main_menu,
                null);
        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        stickerBtn = mainView.findViewById(R.id.btn_stickers);
        fliterBtn = mainView.findViewById(R.id.btn_filter);
        cropBtn = mainView.findViewById(R.id.btn_crop);
        rotateBtn = mainView.findViewById(R.id.btn_rotate);
        mTextBtn = mainView.findViewById(R.id.btn_text);
        mPaintBtn = mainView.findViewById(R.id.btn_paint);
        mBeautyBtn = mainView.findViewById(R.id.btn_beauty);
        enhanceBtn = mainView.findViewById(R.id.btn_enhance);
        frameBtn = mainView.findViewById(R.id.btn_frame);
        chartletBtn = mainView.findViewById(R.id.btn_chartlet);

        stickerBtn.setOnClickListener(this);
        fliterBtn.setOnClickListener(this);
        cropBtn.setOnClickListener(this);
        rotateBtn.setOnClickListener(this);
        mTextBtn.setOnClickListener(this);
        mPaintBtn.setOnClickListener(this);
        mBeautyBtn.setOnClickListener(this);
        enhanceBtn.setOnClickListener(this);
        frameBtn.setOnClickListener(this);
        chartletBtn.setOnClickListener(this);

        if (activity.chartletMode) {
            mainView.findViewById(R.id.chartlet_layout).setVisibility(View.VISIBLE);
        } else {
            mainView.findViewById(R.id.chartlet_layout).setVisibility(View.GONE);
        }

    }

    @Override
    public void onShow() {
        // do nothing
    }

    @Override
    public void backToMain() {
        //do nothing
    }

    @Override
    public void onClick(View v) {
        if (v == stickerBtn) {
            onStickClick();
        } else if (v == fliterBtn) {
            onFilterClick();
        } else if (v == cropBtn) {
            onCropClick();
        } else if (v == rotateBtn) {
            onRotateClick();
        } else if (v == mTextBtn) {
            onAddTextClick();
        } else if (v == mPaintBtn) {
            onPaintClick();
        } else if (v == mBeautyBtn) {
            onBeautyClick();
        } else if (v == enhanceBtn) {
            onEnhanceClick();
        } else if (v == frameBtn) {
            onFrameClick();
        } else if (v == chartletBtn) {
            onChartletClick();
        }
    }

    /**
     * 贴图模式
     *
     * @author panyi
     */
    private void onStickClick() {
        activity.bottomGallery.setCurrentItem(StickerFragment.INDEX);
        activity.mStickerFragment.onShow();
    }

    /**
     * 滤镜模式
     *
     * @author panyi
     */
    private void onFilterClick() {
        activity.bottomGallery.setCurrentItem(FilterListFragment.INDEX);
        activity.mFilterListFragment.onShow();
    }

    /**
     * 裁剪模式
     *
     * @author panyi
     */
    private void onCropClick() {
        activity.bottomGallery.setCurrentItem(CropFragment.INDEX);
        activity.mCropFragment.onShow();
    }

    /**
     * 图片旋转模式
     *
     * @author panyi
     */
    private void onRotateClick() {
        activity.bottomGallery.setCurrentItem(RotateFragment.INDEX);
        activity.mRotateFragment.onShow();
    }

    /**
     * 插入文字模式
     *
     * @author panyi
     */
    private void onAddTextClick() {
        activity.bottomGallery.setCurrentItem(AddTextFragment.INDEX);
        activity.mAddTextFragment.onShow();
    }

    /**
     * 自由绘制模式
     */
    private void onPaintClick() {
        activity.bottomGallery.setCurrentItem(PaintFragment.INDEX);
        activity.mPaintFragment.onShow();
    }

    /**
     * 美颜模式
     */
    private void onBeautyClick() {
        activity.bottomGallery.setCurrentItem(BeautyFragment.INDEX);
        activity.mBeautyFragment.onShow();
    }

    /**
     * 增强模式
     * @author xujian 2018/10/14
     */
    private void onEnhanceClick() {
        activity.bottomGallery.setCurrentItem(EnhanceFragment.INDEX);
        activity.mEnhanceFragment.onShow();
    }

    /**
     * 相框模式
     * @author xujian 2018/10/15
     */
    private void onFrameClick() {
        activity.bottomGallery.setCurrentItem(FrameFragment.INDEX);
        activity.mFrameFragment.onShow();
    }

    /**
     * 相册贴图模式
     * @author xujian 2018/10/17
     */
    private void onChartletClick() {
        activity.bottomGallery.setCurrentItem(ChartletFragment.INDEX);
        activity.mChartletFragment.onShow();
    }

}// end class
