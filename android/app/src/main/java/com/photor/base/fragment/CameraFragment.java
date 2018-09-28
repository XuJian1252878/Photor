package com.photor.base.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.photor.R;

public class CameraFragment extends Fragment {

    public static CameraFragment newInstance() {
        CameraFragment cameraFragment = new CameraFragment();

        Bundle bundle = new Bundle();
        cameraFragment.setArguments(bundle);

        return cameraFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        return rootView;
    }
}
