package com.photor.base.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.permissions.PermissionsUtils;
import com.photor.R;
import com.photor.base.activity.test.OpencvTestActivity;
import com.photor.home.dofcalculator.activity.DofCalcMainActivity;
import com.photor.home.exposure.ExposureBaseActivity;
import com.photor.home.scanner.ImageScannerActivity;
import com.photor.home.staralign.StarAlignBaseActivity;

/**
 * Created by xujian on 2018/2/26.
 */

public class HomeFragment extends Fragment {

    private static final int REQUEST_PERMISSION = 233;
    private static final int REQUEST_CAMERA_PERMISSION_STAR_ALIGN = 234;
    private static final int REQUEST_CAMERA_PERMISSION_EXPOSURE = 235;

    public static HomeFragment newInstance() {
        HomeFragment homeFragment = new HomeFragment();

        Bundle args = new Bundle();
        homeFragment.setArguments(args);

        return homeFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // opencv android sdk 测试函数
        Button opencvTestButton = rootView.findViewById(R.id.opencv_test);
        opencvTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission(Manifest.permission.CAMERA,REQUEST_PERMISSION))
                    init();
            }
        });

        // 星空图片测试
        rootView.findViewById(R.id.star_align_enter_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 不动态申请权限的话，之后的写文件操作会失败。
                if (PermissionsUtils.checkCameraPermission(HomeFragment.this, REQUEST_CAMERA_PERMISSION_STAR_ALIGN)) {
                    startStarAlign();
                }
            }
        });

        // 景深计算
        rootView.findViewById(R.id.dof_calc_enter_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDofCalc();
            }
        });

        // 曝光合成
        rootView.findViewById(R.id.exposure_enter_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionsUtils.checkCameraPermission(HomeFragment.this, REQUEST_CAMERA_PERMISSION_EXPOSURE)) {
                    startExposure();
                }
            }
        });

        // 图片信息扫描
        rootView.findViewById(R.id.image_scanner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startImageScanner();
            }
        });

        return rootView;
    }


    private void init(){
        startActivity(new Intent(getActivity(), OpencvTestActivity.class));
        getActivity().finish();
    }

    private boolean checkPermission(String permission,int requestCode){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
                    showHint("Camera and SDCard access is required, please grant the permission in settings.");
                    getActivity().finish();
                } else {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{
                                    permission,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            },
                            requestCode);
                }
                return false;
            }else return true;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                }else {
                    showHint("Camera and SDCard access is required, please grant the permission in settings.");
                    getActivity().finish();
                }
                break;
            case REQUEST_CAMERA_PERMISSION_STAR_ALIGN:  // 申请星空对齐需要的权限信息
                startStarAlign();
                break;
            default:
                getActivity().finish();
                break;
        }
    }

    private void showHint(String hint){
        Toast.makeText(getContext(),hint , Toast.LENGTH_LONG).show();
    }

    // 启动图片对齐的操作
    private void startStarAlign() {
        startActivity(new Intent(getActivity(), StarAlignBaseActivity.class));
    }

    // 启动景深计算器的操作
    private void startDofCalc() {
        startActivity(new Intent(getActivity(), DofCalcMainActivity.class));
    }

    // 启动曝光合成的操作
    private void startExposure() {
        startActivity(new Intent(getActivity(), ExposureBaseActivity.class));
    }

    // 启动图片扫描器操作
    private void startImageScanner() {
        startActivity(new Intent(getActivity(), ImageScannerActivity.class));
    }
}
