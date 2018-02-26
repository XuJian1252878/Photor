package com.photor.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.photor.R;

/**
 * Created by xujian on 2018/2/26.
 */

public class ResourceFragment extends Fragment {

    public static ResourceFragment newInstance() {
        ResourceFragment resourceFragment = new ResourceFragment();

        Bundle args = new Bundle();
        resourceFragment.setArguments(args);

        return resourceFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_resource, container, false);
        return rootView;
    }
}
