package com.photor.base.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.file.FileUtils;
import com.example.media.MediaManager;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraLogger;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.SessionType;
import com.otaliastudios.cameraview.Size;
import com.otaliastudios.cameraview.SizeSelector;
import com.photor.R;
import com.photor.camera.activity.VideoPreviewActivity;
import com.photor.camera.event.CameraOperator;
import com.photor.camera.view.CameraSettingPopupView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

public class CameraFragment extends Fragment implements View.OnClickListener {

    private View rootView;

    private CameraView camera;
    private CameraOptions cameraOptions;
    private Toolbar toolbar;  // MainActivity的导航栏信息

    private LinearLayout bottomControlPanel;

    private boolean mCapturingPicture;  // 表示正在拍照过程中
    private boolean mCapturingVideo;  // 表示正在视频录制过程中

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
    private CameraSettingPopupView cameraSettingPopupView;


    // To show stuff in the callback
    private Size mCaptureNativeSize;
    private long mCaptureTime;

    private DecimalFormat exposureValueFormat = new DecimalFormat("##0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.

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

        camera = rootView.findViewById(R.id.camera);
        cameraSettingPopupView = new CameraSettingPopupView(getActivity(),
                CameraFragment.this, rootView);

        // 初始化相机的输出照片信息（不知道为什么，只有在 onCreateView 中调用才有效）
        camera.setPictureSize(new SizeSelector() {
            @Override
            public List<Size> select(List<Size> source) {
                // Receives a list of available sizes.
                // Must return a list of acceptable sizes.
                return source;
            }
        });

        // Activity CameraView is a component bound to your activity or fragment lifecycle. This means that you must pass the lifecycle owner using setLifecycleOwner
        // Fragment use fragment.viewLifecycleOwner instead of this!
//        camera.setLifecycleOwner(this);
        camera.addCameraListener(new CameraListener() {
            public void onCameraOpened(CameraOptions options) {
                // 因为许多关于相机的设置要等到相机启动完成之后才能生效，所以将initUI的操作全部放在了 onCameraOpened 回调函数中
                onOpened(rootView);
            }
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
                // 如果是滑动屏幕方式导致的曝光值变化，那么显示当前的曝光值信息
                if (slidersContainer.getVisibility() == View.INVISIBLE) {
                    message("曝光值：" + exposureValueFormat.format(newValue) + " EV", false);
                }
            }
        });

        return rootView;
    }

    private void initUI(View rootView) {

        bottomControlPanel = rootView.findViewById(R.id.camera_fragment_bottom_control_panel);

        // 1. 初始化相机曝光信息
        initCameraExposureUIInfo(rootView);

        // 2. 初始化相机的拍照录像功能
        rootView.findViewById(R.id.capturePhoto).setOnClickListener(this);
        rootView.findViewById(R.id.captureVideo).setOnClickListener(this);
        rootView.findViewById(R.id.toggleCamera).setOnClickListener(this);

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
        cameraSettingPopupView = new CameraSettingPopupView(getActivity(),
                CameraFragment.this, rootView);
        if (settingPopupContainer.getChildCount() <= 0) {
            // ScrollerView 只能包含一个子View，在屏幕旋转或者由其他Activity跳转回来的时候，
            // Fragment的View重建过程可能会导致 ScrollerView 添加超过一个的子View
            settingPopupContainer.addView(cameraSettingPopupView);
        }
    }

    private void initCameraExposureUIInfo(View rootView) {

        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE);
        // 首先曝光滑动设置为false
        slidersContainer = rootView.findViewById(R.id.sliders_container);
        slidersContainer.setVisibility(View.INVISIBLE);

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
        ImageButton increaseZoomBtn = rootView.findViewById(R.id.increase_zoom);
        increaseZoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float nextEv = exposureCorrectionCurValue + exposureRange / 10;
                exposureCorrectionCurValue = nextEv > exposureCorrectionMaxValue ? exposureCorrectionMaxValue : nextEv;
                // 每次增加10%的曝光
                exposureSeekbar.setProgress((int)((exposureCorrectionCurValue - exposureCorrectionMinValue) * 10));
                camera.setExposureCorrection(exposureCorrectionCurValue);
                Toast.makeText(getActivity(), "当前曝光值为 " + exposureValueFormat.format(exposureCorrectionCurValue) + " EV", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "当前曝光值为 " + exposureValueFormat.format(exposureCorrectionCurValue) + "EV", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void message(String content, boolean important) {
        int length = important ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast.makeText(this.getContext(), content, length).show();
    }

    /**
     * 相机初始化完成时候 -- 进行所有相关的UI操作
     * @param rootView
     */
    private void onOpened(View rootView) {
        initUI(rootView);
    }

    /**
     * 拍照操作结束时候的操作
     * @param jpeg
     */
    private void onPicture(final byte[] jpeg) {
        mCapturingPicture = false;
        long callbackTime = System.currentTimeMillis();
        if (mCapturingVideo) {
            message(getResources().getString(R.string.take_photo_while_record_video), false);
            return;
        }

        // This can happen if picture was taken with a gesture.
        if (mCaptureTime == 0) mCaptureTime = callbackTime - 300;
        if (mCaptureNativeSize == null) mCaptureNativeSize = camera.getPictureSize();

        int nativeWidth = mCaptureNativeSize != null ? mCaptureNativeSize.getWidth() : 1000;
        int nativeHeight = mCaptureNativeSize != null ?  mCaptureNativeSize.getHeight() : 1000;

        mCaptureTime = 0;
        mCaptureNativeSize = null;

        // 存储获得的照片信息
        final String resImgPath = FileUtils.generateImgAbsPath();
        CameraUtils.decodeBitmap(jpeg, nativeWidth, nativeHeight, new CameraUtils.BitmapCallback() {
            @Override
            public void onBitmapReady(Bitmap bitmap) {
                // 存储Bitmap信息到手机内存
                FileUtils.saveFileByByte(resImgPath, jpeg);
                CameraOperator.builder()
                        .setCameraResImgPath(resImgPath)
                        .setFileUri(Uri.fromFile(new File(resImgPath)))
                        .setIsFromCamera(true)
                        .start(CameraFragment.this.getActivity());
            }
        });
    }

    private void onVideo(File video) {
        mCapturingVideo = false;  // 说明视频已经录制完成
        MediaManager.galleryAddMedia(getContext(), video.getAbsolutePath());

        // android 录像按钮设置
        ImageButton videoIbv = rootView.findViewById(R.id.captureVideo);
        videoIbv.setImageResource(R.drawable.ic_video_on_white);

        // 计时器设置
        stopVideoRecording();

        Intent intent = new Intent(getActivity(), VideoPreviewActivity.class);
        intent.putExtra("video", Uri.fromFile(video));
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
        super.onDestroyView();
    }

    private void capturePhoto() {
        if (mCapturingPicture) return;  // 当前拍照操作正在处理的情况

        // 如果正在录视频，那么无法进行拍照操作
        if (mCapturingVideo) {
            message(getResources().getString(R.string.take_photo_while_record_video), false);
            return;
        }

        mCapturingPicture = true;
        mCaptureTime = System.currentTimeMillis();
        mCaptureNativeSize = camera.getPictureSize();

        // 根据当前相机界面的大小设置照片的大小
        camera.capturePicture();
        message(getResources().getString(R.string.capturing_picture), false);
    }

    private void captureVideo() {

        if (mCapturingPicture) {
            // 当前正处于拍照流程，正在处理照片
            message(getResources().getString(R.string.camera_video_on_pic), false);
            return;
        }

        // 设置为视频录制状态
        if (camera.getSessionType() != SessionType.VIDEO) {
            camera.setSessionType(SessionType.VIDEO);
        }

        ImageButton videoIbv = rootView.findViewById(R.id.captureVideo);
        if (mCapturingVideo) {
            // 说明正在录制视频，再次点击的时候应该关闭录制
            message(getResources().getString(R.string.camera_stop_record_video), false);
            camera.stopCapturingVideo();
        } else {
            // 说明正要开始录制视频
            mCapturingVideo = true;
            // 显示计时控件

            // 为什么这样子转化就不行
//            (ImageButton)(rootView.findViewById(R.id.captureVideo)).setImageResource(R.drawable.ic_video_on_red);
            videoIbv.setImageResource(R.drawable.ic_video_on_red);
            message(getResources().getString(R.string.camera_start_record_video), false);
            File file = FileUtils.generateVideoFile();
            startVideoRecording(); // 计时器设置
            camera.startCapturingVideo(file);
        }
    }

    private void toggleCamera() {
        if (mCapturingPicture) return;
        switch (camera.toggleFacing()) {
            case BACK:
                message(getResources().getString(R.string.switch_to_back_camera), false);
                break;

            case FRONT:
                message(getResources().getString(R.string.switch_to_front_camera), false);
                break;
        }
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

    private void startVideoRecording() {
        rootView.findViewById(R.id.video_record_timer_container).setVisibility(View.VISIBLE);
        Chronometer timer = rootView.findViewById(R.id.video_record_timer);
        // 计时器清零
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();
    }

    private void stopVideoRecording() {
        rootView.findViewById(R.id.video_record_timer_container).setVisibility(View.GONE);
        Chronometer timer = rootView.findViewById(R.id.video_record_timer);
        timer.stop();
    }

    public CameraView getCameraView() {
        return this.camera;
    }
}
