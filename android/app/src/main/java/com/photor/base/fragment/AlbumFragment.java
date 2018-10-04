package com.photor.base.fragment;

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

public class AlbumFragment extends Fragment {

    public static AlbumFragment newInstance() {
        AlbumFragment albumFragment = new AlbumFragment();

        Bundle args = new Bundle();
        albumFragment.setArguments(args);

        return albumFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_album, container, false);
        return rootView;
    }
}
