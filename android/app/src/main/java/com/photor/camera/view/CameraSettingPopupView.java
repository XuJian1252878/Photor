package com.photor.camera.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.otaliastudios.cameraview.CameraView;
import com.photor.R;
import com.photor.base.MainActivity;
import com.photor.base.fragment.CameraFragment;

public class CameraSettingPopupView extends LinearLayout {

    private CameraFragment cameraFragment;

    public CameraSettingPopupView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    // 传入参数为 context(Context), cameraFragment
    public CameraSettingPopupView(Context context, CameraFragment cameraFragment) {
        super(context);
        this.setOrientation(LinearLayout.VERTICAL);

        inflate(context, R.layout.camera_setting_popup_view, this);

        this.cameraFragment = cameraFragment;
        final CameraView camera = cameraFragment.getCameraView();
        // 获得当前相机的闪光灯设置
    }

    private void addButtonOptionsToPopup() {

    }
}
