package com.photor.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

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

}
