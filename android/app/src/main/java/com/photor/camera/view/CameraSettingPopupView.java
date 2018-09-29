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
import com.otaliastudios.cameraview.Audio;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Flash;
import com.otaliastudios.cameraview.Grid;
import com.otaliastudios.cameraview.Hdr;
import com.otaliastudios.cameraview.WhiteBalance;
import com.photor.R;
import com.photor.base.fragment.CameraFragment;
import com.photor.camera.event.setting.FlashOnEnum;
import com.photor.camera.event.setting.GridEnum;
import com.photor.camera.event.setting.WhiteBalanceEnum;
import com.shawnlin.numberpicker.NumberPicker;

import java.util.ArrayList;
import java.util.List;

public class CameraSettingPopupView extends LinearLayout {

    private CameraFragment cameraFragment;
    private View fragmentRootView;

    private boolean isPlaySounds = true;

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

        //3. 相机白平衡设置
        cameraWhiteBalanceSetting(camera);

        //4. 相机hdr设置
        cameraHdrSetting(camera);

        //5. 相机声音设置
        cameraAudioSetting(camera);

        //6. 相机音效
        cameraPlaySounds(camera);
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

    // 相机网格设置信息
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

        cameraGridSelector.setTextSize(13);
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

    // 相机白平衡设置
    private void cameraWhiteBalanceSetting(final CameraView camera) {
        final NumberPicker whiteBalanceSelector = findViewById(R.id.camera_white_balance_selector);
        List<String> whiteBalances = new ArrayList<>();
        for (WhiteBalanceEnum wbe: WhiteBalanceEnum.values()) {
            whiteBalances.add(getResources().getString(wbe.getMessageId()));
        }
        String[] whiteBalancesArray = whiteBalances.toArray(new String[whiteBalances.size()]);
        whiteBalanceSelector.setMinValue(1);
        whiteBalanceSelector.setMaxValue(whiteBalancesArray.length);
        whiteBalanceSelector.setDisplayedValues(whiteBalancesArray);
        // 设置当前的白平衡信息
        WhiteBalance curWhiteBalance = camera.getWhiteBalance();
        whiteBalanceSelector.setValue(WhiteBalanceEnum.getIndexByWhiteBalance(curWhiteBalance) + 1);
        // 设置2白平衡变化事件
        whiteBalanceSelector.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                WhiteBalance curWhiteBalance = WhiteBalanceEnum.getWhiteBalanceByIndex(newVal - 1);
                camera.setWhiteBalance(curWhiteBalance);
                whiteBalanceSelector.setValue(newVal);
            }
        });
    }

    // 相机hdr设置
    private void cameraHdrSetting(final CameraView camera) {
        Hdr curHdr = camera.getHdr();
        final ImageButton hdrBtn = findViewById(R.id.camera_hdr_btn);
        if (curHdr == Hdr.ON) {
            hdrBtn.setBackgroundColor(Color.argb(180, 63, 63, 63));
        } else {
            hdrBtn.setBackgroundColor(Color.argb(63, 63, 63, 63));
        }
        hdrBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Hdr curHdr = camera.getHdr();
                if (curHdr == Hdr.ON) {
                    hdrBtn.setBackgroundColor(Color.argb(63, 63, 63, 63));
                    camera.setHdr(Hdr.OFF);
                    Toast.makeText(cameraFragment.getContext(),
                            getResources().getString(R.string.camera_hdr_setting_off),
                            Toast.LENGTH_SHORT).show();
                } else {
                    hdrBtn.setBackgroundColor(Color.argb(180, 63, 63, 63));
                    camera.setHdr(Hdr.ON);
                    Toast.makeText(cameraFragment.getContext(),
                            getResources().getString(R.string.camera_hdr_setting_on),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 相机音频设置
    private void cameraAudioSetting(final CameraView camera) {
        Audio curAudio = camera.getAudio();
        final ImageButton audioBtn = findViewById(R.id.camera_audio_btn);
        if (curAudio == Audio.ON) {
            audioBtn.setBackgroundColor(Color.argb(180, 63, 63, 63));
        } else {
            audioBtn.setBackgroundColor(Color.argb(63, 63, 63, 63));
        }
        audioBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Audio curAudio = camera.getAudio();
                if (curAudio == Audio.ON) {
                    camera.setAudio(Audio.OFF);
                    audioBtn.setBackgroundColor(Color.argb(63, 63, 63, 63));
                    Toast.makeText(cameraFragment.getContext(),
                            getResources().getString(R.string.camera_audio_setting_off),
                            Toast.LENGTH_SHORT).show();
                } else {
                    camera.setAudio(Audio.ON);
                    audioBtn.setBackgroundColor(Color.argb(180, 63, 63, 63));
                    Toast.makeText(cameraFragment.getContext(),
                            getResources().getString(R.string.camera_audio_setting_on),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 相机提示音设置
    private void cameraPlaySounds(final CameraView camera) {
        final ImageButton soundsBtn = findViewById(R.id.camera_sounds_btn);
        if (isPlaySounds) {
            soundsBtn.setBackgroundColor(Color.argb(63, 63, 63, 63));
        } else {
            soundsBtn.setBackgroundColor(Color.argb(180, 63, 63, 63));
        }
        soundsBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaySounds) {
                    isPlaySounds = false;
                    soundsBtn.setBackgroundColor(Color.argb(180, 63, 63, 63));
                    Toast.makeText(cameraFragment.getContext(),
                            getResources().getString(R.string.camera_sounds_setting_off),
                            Toast.LENGTH_SHORT).show();
                } else {
                    isPlaySounds = true;
                    soundsBtn.setBackgroundColor(Color.argb(63, 63, 63, 63));
                    Toast.makeText(cameraFragment.getContext(),
                            getResources().getString(R.string.camera_sounds_setting_on),
                            Toast.LENGTH_SHORT).show();
                }

                camera.setPlaySounds(isPlaySounds);
            }
        });
    }

}
