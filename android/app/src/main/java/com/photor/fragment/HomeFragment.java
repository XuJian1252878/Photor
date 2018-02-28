package com.photor.fragment;

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

import com.photor.R;
import com.photor.activity.test.OpencvTestActivity;

/**
 * Created by xujian on 2018/2/26.
 */

public class HomeFragment extends Fragment {

    private static final int REQUEST_PERMISSION = 233;

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

        Button opencvTestButton = rootView.findViewById(R.id.opencv_test);
        opencvTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission(Manifest.permission.CAMERA,REQUEST_PERMISSION))
                    init();
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
            default:
                getActivity().finish();
                break;
        }
    }

    private void showHint(String hint){
        Toast.makeText(getContext(),hint , Toast.LENGTH_LONG).show();
    }
}
