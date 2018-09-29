package com.photor.camera.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.contrarywind.adapter.WheelAdapter;
import com.contrarywind.listener.OnItemSelectedListener;
import com.contrarywind.view.WheelView;
import com.orhanobut.logger.Logger;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Flash;
import com.otaliastudios.cameraview.Grid;
import com.photor.R;
import com.photor.base.fragment.CameraFragment;
import com.photor.camera.event.setting.FlashOnEnum;
import com.photor.camera.event.setting.GridEnum;

import java.util.ArrayList;
import java.util.List;

public class CameraSettingPopupView extends LinearLayout {

    private CameraFragment cameraFragment;
    private View fragmentRootView;

    public CameraSettingPopupView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    // 传入参数为 context(Context), fragmentRootView
    public CameraSettingPopupView(Context context, final CameraFragment cameraFragment, View fragmentRootView) {
        super(context);
        this.setOrientation(LinearLayout.VERTICAL);
        inflate(context, R.layout.camera_setting_popup_view, this);

        this.cameraFragment = cameraFragment;
        this.fragmentRootView = fragmentRootView;
        final CameraView camera = cameraFragment.getCameraView();

        cameraFlashOnSetting(camera);
        //1。 设置 闪光灯按钮信息
        for (FlashOnEnum flashOnEnum: FlashOnEnum.values()) {
            final FlashOnEnum foe = flashOnEnum;
            ImageButton ib = findViewById(foe.getRid());
            ib.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Flash curFlash = foe.getFalsh();
                    Toast.makeText(cameraFragment.getContext(), getResources().getString(foe.getMessageId()), Toast.LENGTH_SHORT).show();
                    camera.setFlash(curFlash);
                    cameraFlashOnSetting(camera);
                }
            });
        }

        //2。 设置相机的网格信息
        cameraGridSetting(camera);
    }

    // 设置当前相机的闪光灯按钮
    private void cameraFlashOnSetting(CameraView cameraView) {
        ImageButton ib;
        Flash curFlash = cameraView.getFlash();
        for (FlashOnEnum foe: FlashOnEnum.values()) {
            ib = findViewById(foe.getRid());
            if (foe.getFalsh() == curFlash) {
                ib.setBackgroundColor(Color.argb(180, 63, 63, 63));
            } else {
                ib.setBackgroundColor(Color.argb(63, 63, 63, 63));
            }
        }
    }

    private void cameraGridSetting(final CameraView camera) {
        WheelView cameraGridSelector = findViewById(R.id.camera_grid_selector);
        final List<String> gridOptions = new ArrayList<>();
        for (GridEnum ge: GridEnum.values()) {
            gridOptions.add(getResources().getString(ge.getMessageId()));
        }
        cameraGridSelector.setAdapter(new WheelAdapter() {
            @Override
            public int getItemsCount() {
                return gridOptions.size();
            }

            @Override
            public Object getItem(int index) {
                return gridOptions.get(index);
            }

            @Override
            public int indexOf(Object o) {
                return gridOptions.indexOf(o);
            }
        });

        cameraGridSelector.setTextSize(12);
        cameraGridSelector.setCurrentItem(0);
        cameraGridSelector.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                camera.setGrid(GridEnum.getGridByIndex(index));
                Toast.makeText(cameraFragment.getContext(),
                        getResources().getString(GridEnum.getMessageIdByIndex(index)),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}
