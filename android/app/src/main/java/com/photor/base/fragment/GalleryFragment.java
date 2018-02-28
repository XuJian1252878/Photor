package com.photor.base.fragment;

import android.os.Bundle;

/**
 * Created by xujian on 2018/2/26.
 */

public class GalleryFragment extends BaseReactFragment {

    public static GalleryFragment newInstance() {
        GalleryFragment galleryFragment = new GalleryFragment();

        Bundle args = new Bundle();
        galleryFragment.setArguments(args);

        return galleryFragment;
    }

    @Override
    public String getMainComponentName() {
        return "GalleryApp";
    }

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);
//        return rootView;
//    }
}
