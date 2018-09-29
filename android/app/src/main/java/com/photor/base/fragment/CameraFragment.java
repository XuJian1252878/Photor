package com.photor.base.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraLogger;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.SessionType;
import com.otaliastudios.cameraview.Size;
import com.photor.R;
import com.photor.camera.activity.PicturePreviewActivity;
import com.photor.camera.activity.VideoPreviewActivity;
import com.photor.camera.event.Control;
import com.photor.camera.view.CameraSettingPopupView;
import com.photor.camera.view.ControlView;

import java.io.File;
import java.text.DecimalFormat;

public class CameraFragment extends Fragment implements View.OnClickListener, ControlView.Callback {

    private View rootView;

    private CameraView camera;
    private CameraOptions cameraOptions;

    private ViewGroup controlPanel;

    private LinearLayout bottomControlPanel;

    private boolean mCapturingPicture;
    private boolean mCapturingVideo;

    // 曝光调节控制面板
    LinearLayout slidersContainer;
    // 控制面板内的曝光进度条
    SeekBar exposureSeekbar;
    // 调出曝光调节的按钮
    ImageButton exposureImgBtn;

    private float exposureCorrectionMaxValue; // 手机相机的曝光参数上界
    private float exposureCorrectionMinValue; // 手机相机的曝光参数下界
    private float exposureCorrectionCurValue; // 手机相机当前的曝光值
    private float exposureRange; // 手机相机曝光范围

    // 相机设置界面
    private ImageView settingPopupBtn;
    private ScrollView settingPopupContainer;


    // To show stuff in the callback
    private Size mCaptureNativeSize;
    private long mCaptureTime;

    public static CameraFragment newInstance() {
        CameraFragment cameraFragment = new CameraFragment();

        Bundle bundle = new Bundle();
        cameraFragment.setArguments(bundle);

        return cameraFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        initUI(rootView);
        return rootView;
    }


    private void initUI(View rootView) {

        bottomControlPanel = rootView.findViewById(R.id.camera_fragment_bottom_control_panel);

        // 1. 初始化相机曝光信息
        initCameraExposureUIInfo(rootView);

        // 2. 初始化相机的拍照录像功能
        rootView.findViewById(R.id.edit).setOnClickListener(this);
        rootView.findViewById(R.id.capturePhoto).setOnClickListener(this);
        rootView.findViewById(R.id.captureVideo).setOnClickListener(this);
        rootView.findViewById(R.id.toggleCamera).setOnClickListener(this);

        controlPanel = rootView.findViewById(R.id.controls);
        ViewGroup group = (ViewGroup) controlPanel.getChildAt(0);
        Control[] controls = Control.values();
        for (Control control : controls) {
            ControlView view = new ControlView(this.getContext(), control, this);
            group.addView(view, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        controlPanel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                BottomSheetBehavior b = BottomSheetBehavior.from(controlPanel);
                b.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        exposureImgBtn = rootView.findViewById(R.id.exposure);
        exposureImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slidersContainer.getVisibility() == View.VISIBLE) {
                    slidersContainer.setVisibility(View.INVISIBLE);
                } else {
                    slidersContainer.setVisibility(View.VISIBLE);
                }
                if (settingPopupContainer != null && settingPopupContainer.getVisibility() == View.VISIBLE) {
                    settingPopupContainer.setVisibility(View.INVISIBLE);
                }
            }
        });

        // 3. 初始化相机的浮动设置窗口信息
        settingPopupBtn = rootView.findViewById(R.id.camera_setting_popup_btn);
        settingPopupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 控制 相机的浮动设置窗口 可见性
                if (settingPopupContainer.getVisibility() == View.VISIBLE) {
                    settingPopupContainer.setVisibility(View.INVISIBLE);
                    bottomControlPanel.setVisibility(View.VISIBLE);
                } else {
                    settingPopupContainer.setVisibility(View.VISIBLE);
                    bottomControlPanel.setVisibility(View.INVISIBLE);
                }
                // 控制 曝光调节窗口可视性
                if (slidersContainer != null && slidersContainer.getVisibility() == View.VISIBLE) {
                    slidersContainer.setVisibility(View.INVISIBLE);
                }
            }
        });
        settingPopupContainer = rootView.findViewById(R.id.camera_setting_popup_container);
        settingPopupContainer.setVisibility(View.INVISIBLE);
        // prevent popup being transparent
        settingPopupContainer.setBackgroundColor(Color.BLACK);
        settingPopupContainer.setAlpha(0.9f);
        // 绑定Setting 模版
        CameraSettingPopupView cameraSettingPopupView = new CameraSettingPopupView(getActivity(),
                CameraFragment.this, rootView);
        settingPopupContainer.addView(cameraSettingPopupView);
    }

    private void initCameraExposureUIInfo(View rootView) {

        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE);
        // 首先曝光滑动设置为false
        slidersContainer = rootView.findViewById(R.id.sliders_container);
        slidersContainer.setVisibility(View.INVISIBLE);

        camera = rootView.findViewById(R.id.camera);
        camera.start();  // 启动camera参数，以便于获取手机相机参数
        while (!camera.isStarted()) {
            // camera 启动需要时间，不这样会导致后面取不到 曝光的范围值
        }

        camera.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 当点击不在 slidersContainer 上的时候，隐藏 slidersContainer 界面
                if (slidersContainer != null) {
                    slidersContainer.setVisibility(View.INVISIBLE);
                }
                if (settingPopupContainer != null) {
                    settingPopupContainer.setVisibility(View.INVISIBLE);
                }
                if (bottomControlPanel.getVisibility() == View.INVISIBLE) {
                    bottomControlPanel.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        cameraOptions = camera.getCameraOptions();

        exposureSeekbar = rootView.findViewById(R.id.exposure_seekbar);

        // 获得当前相机的曝光参数
        exposureCorrectionMaxValue = cameraOptions.getExposureCorrectionMaxValue();
        exposureCorrectionMinValue = cameraOptions.getExposureCorrectionMinValue();
        exposureCorrectionCurValue = camera.getExposureCorrection();
        exposureRange = exposureCorrectionMaxValue - exposureCorrectionMinValue;

        // 设置曝光进度条的初始值
        exposureSeekbar.setMax((int)((exposureCorrectionMaxValue - exposureCorrectionMinValue) * 10));
        exposureSeekbar.setProgress((int)((exposureCorrectionCurValue - exposureCorrectionMinValue) * 10));
        exposureSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int nextEv = progress / 10;
                    if (nextEv < exposureCorrectionMinValue) {
                        exposureCorrectionCurValue = exposureCorrectionMinValue;
                    } else if (nextEv > exposureCorrectionMaxValue) {
                        exposureCorrectionCurValue = exposureCorrectionMaxValue;
                    } else {
                        exposureCorrectionCurValue = nextEv;
                    }
                    camera.setExposureCorrection(exposureCorrectionCurValue);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // 设置曝光值的按钮（位于seekbar上）
        final DecimalFormat decimalFormat = new DecimalFormat("##0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        ImageButton increaseZoomBtn = rootView.findViewById(R.id.increase_zoom);
        increaseZoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float nextEv = exposureCorrectionCurValue + exposureRange / 10;
                exposureCorrectionCurValue = nextEv > exposureCorrectionMaxValue ? exposureCorrectionMaxValue : nextEv;
                // 每次增加10%的曝光
                exposureSeekbar.setProgress((int)((exposureCorrectionCurValue - exposureCorrectionMinValue) * 10));
                camera.setExposureCorrection(exposureCorrectionCurValue);
                Toast.makeText(getActivity(), "当前曝光值为 " + decimalFormat.format(exposureCorrectionCurValue) + " EV", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton decreaseZoomBtn = rootView.findViewById(R.id.decrease_zoom);
        decreaseZoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float nextEv = exposureCorrectionCurValue - exposureRange / 10;
                exposureCorrectionCurValue = nextEv < exposureCorrectionMinValue ? exposureCorrectionMinValue : nextEv;
                // 每次减少 10% 的曝光
                exposureSeekbar.setProgress((int)((exposureCorrectionCurValue - exposureCorrectionMinValue) * 10));
                camera.setExposureCorrection(exposureCorrectionCurValue);
                Toast.makeText(getActivity(), "当前曝光值为 " + decimalFormat.format(exposureCorrectionCurValue) + "EV", Toast.LENGTH_SHORT).show();
            }
        });

        // Activity CameraView is a component bound to your activity or fragment lifecycle. This means that you must pass the lifecycle owner using setLifecycleOwner
        // Fragment use fragment.viewLifecycleOwner instead of this!
//        camera.setLifecycleOwner(this);
        camera.addCameraListener(new CameraListener() {
            public void onCameraOpened(CameraOptions options) { onOpened(); }
            public void onPictureTaken(byte[] jpeg) { onPicture(jpeg); }

            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
                onVideo(video);
            }

            @Override
            public void onExposureCorrectionChanged(float newValue, float[] bounds, PointF[] fingers) {
                super.onExposureCorrectionChanged(newValue, bounds, fingers);
                exposureCorrectionCurValue = newValue;
                // 设置曝光值
                exposureSeekbar.setProgress((int)(newValue - exposureCorrectionMinValue) * 10);
            }
        });
    }


    private void message(String content, boolean important) {
        int length = important ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast.makeText(this.getContext(), content, length).show();
    }

    private void onOpened() {
        ViewGroup group = (ViewGroup) controlPanel.getChildAt(0);
        for (int i = 0; i < group.getChildCount(); i++) {
            ControlView view = (ControlView) group.getChildAt(i);
            view.onCameraOpened(camera);
        }
    }

    private void onPicture(byte[] jpeg) {
        mCapturingPicture = false;
        long callbackTime = System.currentTimeMillis();
        if (mCapturingVideo) {
            message("Captured while taking video. Size="+mCaptureNativeSize, false);
            return;
        }

        // This can happen if picture was taken with a gesture.
        if (mCaptureTime == 0) mCaptureTime = callbackTime - 300;
        if (mCaptureNativeSize == null) mCaptureNativeSize = camera.getPictureSize();

        PicturePreviewActivity.setImage(jpeg);
        Intent intent = new Intent(getActivity(), PicturePreviewActivity.class);
        intent.putExtra("delay", callbackTime - mCaptureTime);
        intent.putExtra("nativeWidth", mCaptureNativeSize.getWidth());
        intent.putExtra("nativeHeight", mCaptureNativeSize.getHeight());
        startActivity(intent);

        mCaptureTime = 0;
        mCaptureNativeSize = null;
    }

    private void onVideo(File video) {
        mCapturingVideo = false;
        Intent intent = new Intent(getActivity(), VideoPreviewActivity.class);
        intent.putExtra("video", Uri.fromFile(video));
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.edit: edit(); break;
            case R.id.capturePhoto: capturePhoto(); break;
            case R.id.captureVideo: captureVideo(); break;
            case R.id.toggleCamera: toggleCamera(); break;
        }

        // 当点击区域不在 曝光调节面板的时候，关闭曝光调节面板
        if (slidersContainer != null) {
            slidersContainer.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onDestroyView() {
        BottomSheetBehavior b = BottomSheetBehavior.from(controlPanel);
        if (b.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            b.setState(BottomSheetBehavior.STATE_HIDDEN);
            return;
        }
        super.onDestroyView();
    }

//    @Override
//    public void onBackPressed() {
//        BottomSheetBehavior b = BottomSheetBehavior.from(controlPanel);
//        if (b.getState() != BottomSheetBehavior.STATE_HIDDEN) {
//            b.setState(BottomSheetBehavior.STATE_HIDDEN);
//            return;
//        }
//        super.onBackPressed();
//    }

    private void edit() {
        BottomSheetBehavior b = BottomSheetBehavior.from(controlPanel);
        b.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void capturePhoto() {
        if (mCapturingPicture) return;
        mCapturingPicture = true;
        mCaptureTime = System.currentTimeMillis();
        mCaptureNativeSize = camera.getPictureSize();
        message("Capturing picture...", false);
        camera.capturePicture();
    }

    private void captureVideo() {
        if (camera.getSessionType() != SessionType.VIDEO) {
            message("Can't record video while session type is 'picture'.", false);
            return;
        }
        if (mCapturingPicture || mCapturingVideo) return;
        mCapturingVideo = true;
        message("Recording for 8 seconds...", true);
        camera.startCapturingVideo(null, 8000);
    }

    private void toggleCamera() {
        if (mCapturingPicture) return;
        switch (camera.toggleFacing()) {
            case BACK:
                message("Switched to back camera!", false);
                break;

            case FRONT:
                message("Switched to front camera!", false);
                break;
        }
    }

    @Override
    public boolean onValueChanged(Control control, Object value, String name) {
        if (!camera.isHardwareAccelerated() && (control == Control.WIDTH || control == Control.HEIGHT)) {
            if ((Integer) value > 0) {
                message("This device does not support hardware acceleration. " +
                        "In this case you can not change width or height. " +
                        "The view will act as WRAP_CONTENT by default.", true);
                return false;
            }
        }
        control.applyValue(camera, value);
        BottomSheetBehavior b = BottomSheetBehavior.from(controlPanel);
        b.setState(BottomSheetBehavior.STATE_HIDDEN);
        message("Changed " + control.getName() + " to " + name, false);
        return true;
    }

    //region Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean valid = true;
        for (int grantResult : grantResults) {
            valid = valid && grantResult == PackageManager.PERMISSION_GRANTED;
        }
        if (valid && !camera.isStarted()) {
            camera.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        camera.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        camera.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        camera.destroy();
    }

    public CameraView getCameraView() {
        return this.camera;
    }
}
