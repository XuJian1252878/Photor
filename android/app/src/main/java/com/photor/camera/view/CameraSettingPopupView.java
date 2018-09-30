package com.photor.camera.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
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
import com.otaliastudios.cameraview.Hdr;
import com.otaliastudios.cameraview.Size;
import com.otaliastudios.cameraview.SizeSelector;
import com.otaliastudios.cameraview.SizeSelectors;
import com.otaliastudios.cameraview.VideoCodec;
import com.otaliastudios.cameraview.VideoQuality;
import com.otaliastudios.cameraview.WhiteBalance;
import com.photor.R;
import com.photor.base.fragment.CameraFragment;
import com.photor.camera.event.setting.FlashOnEnum;
import com.photor.camera.event.setting.GridEnum;
import com.photor.camera.event.setting.ResolutionEnum;
import com.photor.camera.event.setting.VideoCoderEnum;
import com.photor.camera.event.setting.VideoLengthEnum;
import com.photor.camera.event.setting.VideoQualityEnum;
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

        //2. 设置相机的网格信息
        cameraGridSetting(camera);

        //3. 相机白平衡设置
        cameraWhiteBalanceSetting(camera);

        //4. 相机hdr设置
        cameraHdrSetting(camera);

        //5. 相机声音设置
        cameraAudioSetting(camera);

        //6. 相机音效
        cameraPlaySounds(camera);

        //7. 视频质量设置
        cameraVideoQualitySetting(camera);

        //8. 视频编码设置
        cameraVideoCodecSetting(camera);

        //9. 视频时长设置
        cameraVideoLengthSetting(camera);

        //10. 设置相机的长宽比
//        camera.setCropOutput(true);
        cameraWidthHeightSetting(camera);

        //11. 设置照片的分辨率信息
        cameraResolutionSetting(camera);
    }

    //11. 设置相机的分辨率信息
    public void cameraResolutionSetting(final CameraView camera) {

        NumberPicker cameraResolutionSelector = findViewById(R.id.camera_resolution_selector);
        List<String> resolutions = new ArrayList<>();
        for (ResolutionEnum re: ResolutionEnum.values()) {
            resolutions.add(re.getMessage());
            // 同时设置允许输出的照片的尺寸信息
        }

        String[] resolutionsArray = resolutions.toArray(new String[resolutions.size()]);

        cameraResolutionSelector.setMinValue(1);
        cameraResolutionSelector.setMaxValue(resolutionsArray.length);
        cameraResolutionSelector.setDisplayedValues(resolutionsArray);
        cameraResolutionSelector.setValue(ResolutionEnum.MP4032_3024.getIndex() + 1);

        cameraResolutionSelector.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                ResolutionEnum resolution = ResolutionEnum.getResolutionEnumByIndex(newVal - 1);
                setCameraPictureOutputSize(camera, resolution);
            }
        });
    }

    // 设置当前相机输出的照片分辨率
    private void setCameraPictureOutputSize(CameraView camera, ResolutionEnum resolutionEnum) {

        int picWidth = resolutionEnum.getWidth();
        int picHeight = resolutionEnum.getHeight();

        SizeSelector width = SizeSelectors.minWidth(picWidth);
        SizeSelector height = SizeSelectors.minHeight(picHeight);
        SizeSelector dimensions = SizeSelectors.and(width, height); // Matches sizes bigger than 1000x2000.

        SizeSelector result = SizeSelectors.or(
                dimensions, // Try to match both constraints
                SizeSelectors.biggest() // If none is found, take the biggest
        );
        camera.setPictureSize(result);
        // https://github.com/natario1/CameraView/issues/271  设置分辨率的时候，这个CameraView有一个bug，现在只能用以下的方法解决。
        camera.stop();
        camera.start();
        Size preSize = camera.getPreviewSize();
        Size picSize = camera.getPictureSize();
        Logger.d("setCameraPicture OutputSizeWidth: " + preSize.getWidth() + " -- " + "Height: " + preSize.getHeight());
        Logger.d("setCameraPicture OutputSizeWidth: " + picSize.getWidth() + " -- " + "Height: " + picSize.getHeight());
    }

    //10. 设置相机的宽信息
    public void cameraWidthHeightSetting(CameraView camera) {
        View cameraParentView = (View)camera.getParent();
        int cameraWidthBoundary = cameraParentView.getWidth();
        int cameraHeightBoundary = cameraParentView.getHeight();

        int cameraWidthStep = cameraWidthBoundary / 10;
        int cameraHeightStep = cameraHeightBoundary / 10;

        List<Integer> cameraWidthSteps = new ArrayList<>();
        List<Integer> cameraHeightSteps = new ArrayList<>();

        for (int i = cameraWidthStep; i < cameraWidthBoundary; i += cameraWidthStep) {
            cameraWidthSteps.add(i);
        }
        cameraWidthSteps.add(ViewGroup.LayoutParams.MATCH_PARENT);

        for (int i = cameraHeightStep; i < cameraHeightBoundary; i += cameraHeightStep) {
            cameraHeightSteps.add(i);
        }
        cameraHeightSteps.add(ViewGroup.LayoutParams.MATCH_PARENT);

        initCameraLayoutInfo(camera, R.id.camera_layout_width_selector, cameraWidthSteps, true);
        initCameraLayoutInfo(camera, R.id.camera_layout_height_selector, cameraHeightSteps, false);
    }

    /**
     *
     * @param viewId
     * @param steps
     * @param widthOrHeight true: Width false: Height
     */
    private void initCameraLayoutInfo(final CameraView camera, int viewId, final List<Integer> steps, final boolean widthOrHeight) {
        final NumberPicker layoutSelector = findViewById(viewId);
        List<String> stepsList = new ArrayList<>();
        for (int index = 0; index < steps.size() - 1; index ++) {
            stepsList.add(steps.get(index).toString());
        }
        stepsList.add("最大");
        String[] stepsArray = stepsList.toArray(new String[stepsList.size()]);
        layoutSelector.setMinValue(1);
        layoutSelector.setMaxValue(stepsArray.length);
        layoutSelector.setValue(stepsArray.length);
        layoutSelector.setDisplayedValues(stepsArray);

        layoutSelector.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                int layoutValue = newVal == steps.size() ? ViewGroup.LayoutParams.MATCH_PARENT : steps.get(newVal - 1);
                if (widthOrHeight) {
                    // 宽度
                    camera.getLayoutParams().width = layoutValue;
                } else {
                    camera.getLayoutParams().height = layoutValue;
                }
                camera.setLayoutParams(camera.getLayoutParams());
                layoutSelector.setValue(newVal);
            }
        });
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
            soundsBtn.setBackgroundColor(Color.argb(180, 63, 63, 63));
        } else {
            soundsBtn.setBackgroundColor(Color.argb(63, 63, 63, 63));
        }
        soundsBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaySounds) {
                    isPlaySounds = false;
                    soundsBtn.setBackgroundColor(Color.argb(63, 63, 63, 63));
                    Toast.makeText(cameraFragment.getContext(),
                            getResources().getString(R.string.camera_sounds_setting_off),
                            Toast.LENGTH_SHORT).show();
                } else {
                    isPlaySounds = true;
                    soundsBtn.setBackgroundColor(Color.argb(180, 63, 63, 63));
                    Toast.makeText(cameraFragment.getContext(),
                            getResources().getString(R.string.camera_sounds_setting_on),
                            Toast.LENGTH_SHORT).show();
                }

                camera.setPlaySounds(isPlaySounds);
            }
        });
    }

    // 视频质量设置
    private void cameraVideoQualitySetting(final CameraView camera) {
        final NumberPicker videoQualitySelector = findViewById(R.id.camera_video_quality_selector);
        List<String> videoQualities = new ArrayList<>();
        for (VideoQualityEnum vqe: VideoQualityEnum.values()) {
            videoQualities.add(getResources().getString(vqe.getMessageId()));
        }
        String[] videoQualitiesArray = videoQualities.toArray(new String[videoQualities.size()]);
        videoQualitySelector.setMinValue(1);
        videoQualitySelector.setMaxValue(videoQualitiesArray.length);
        videoQualitySelector.setDisplayedValues(videoQualitiesArray);

        VideoQuality curVideQuality = camera.getVideoQuality();
        videoQualitySelector.setValue(VideoQualityEnum.getIndexByVideoQuality(curVideQuality) + 1);

        videoQualitySelector.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                VideoQuality curVideoQuality = VideoQualityEnum.getVideoQualityByIndex(newVal - 1);
                camera.setVideoQuality(curVideoQuality);
                videoQualitySelector.setValue(newVal);
            }
        });
    }

    // 视频编码设置
    private void cameraVideoCodecSetting(final CameraView camera) {
        final NumberPicker videoCodecSelector = findViewById(R.id.camera_video_codec_selector);
        List<String> codecs = new ArrayList<>();
        for (VideoCoderEnum vee: VideoCoderEnum.values()) {
            codecs.add(getResources().getString(vee.getMessageId()));
        }
        String[] codecsArray = codecs.toArray(new String[codecs.size()]);
        videoCodecSelector.setMinValue(1);
        videoCodecSelector.setMaxValue(codecsArray.length);
        videoCodecSelector.setDisplayedValues(codecsArray);

        VideoCodec curVideoCodec = camera.getVideoCodec();
        videoCodecSelector.setValue(VideoCoderEnum.getIndexByVideoCodec(curVideoCodec) + 1);

        videoCodecSelector.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                VideoCodec curVideoCodec = VideoCoderEnum.getVideoCodecByIndex(newVal - 1);
                camera.setVideoCodec(curVideoCodec);
                videoCodecSelector.setValue(newVal);
            }
        });
    }

    // 视频时长设置
    private void cameraVideoLengthSetting(final CameraView camera) {
        final ImageButton videoLengthBtn = findViewById(R.id.camera_video_length_btn);
        final NumberPicker videoLengthSelector = findViewById(R.id.camera_video_length_selector);

        List<String> lengths = new ArrayList<>();
        for (VideoLengthEnum vle: VideoLengthEnum.values()) {
            lengths.add(vle.getTimeInfo());
        }
        String[] lengthsArray = lengths.toArray(new String[lengths.size()]);
        videoLengthSelector.setMinValue(1);
        videoLengthSelector.setMaxValue(lengthsArray.length);
        videoLengthSelector.setDisplayedValues(lengthsArray);

        int curVideoLength = camera.getVideoMaxDuration();
        // 设置当前视频限制时长的刻度
        videoLengthSelector.setValue(
                VideoLengthEnum.getIndexByMilliseconds(
                        VideoLengthEnum.getMillisecondsAround(curVideoLength)
                ) + 1
        );

        if (curVideoLength <= 0) {
            videoLengthBtn.setBackgroundColor(Color.argb(63, 63, 63, 63));
            videoLengthSelector.setVisibility(GONE);
        } else {
            videoLengthBtn.setBackgroundColor(Color.argb(180, 63, 63, 63));
            videoLengthSelector.setVisibility(VISIBLE);
        }

        videoLengthBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int curVideoLength = VideoLengthEnum.getMillisecondsAround(camera.getVideoMaxDuration());
                if (curVideoLength <= 0) {
                    videoLengthBtn.setBackgroundColor(Color.argb(180, 63, 63, 63));
                    camera.setVideoMaxDuration(VideoLengthEnum.MINUTE_1.getMilliseconds());
                    videoLengthSelector.setValue(VideoLengthEnum.MINUTE_1.getIndex() + 1);
                    videoLengthSelector.setVisibility(VISIBLE);
                } else {
                    videoLengthBtn.setBackgroundColor(Color.argb(63, 63, 63, 63));
                    camera.setVideoMaxDuration(VideoLengthEnum.INFINITE.getMilliseconds());
                    videoLengthSelector.setValue(VideoLengthEnum.INFINITE.getIndex() + 1);
                    videoLengthSelector.setVisibility(GONE);
                }
            }
        });

        videoLengthSelector.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                int curVideoLength = VideoLengthEnum.getMillisecondsByIndex(newVal - 1);
                if (curVideoLength <= 0) {
                    videoLengthBtn.setBackgroundColor(Color.argb(63, 63, 63, 63));
                    videoLengthSelector.setVisibility(GONE);
                } else {
                    videoLengthBtn.setBackgroundColor(Color.argb(180, 63, 63, 63));
                    videoLengthSelector.setVisibility(VISIBLE);
                }
                camera.setVideoMaxDuration(curVideoLength);
            }
        });
    }

}
