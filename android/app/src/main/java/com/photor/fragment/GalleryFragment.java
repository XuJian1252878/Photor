package com.photor.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by xujian on 2018/2/26.
 */

public class GalleryFragment extends Fragment {

    public static GalleryFragment newInstance() {
        GalleryFragment galleryFragment = new GalleryFragment();

        Bundle args = new Bundle();
        galleryFragment.setArguments(args);

        return galleryFragment;
    }

}
