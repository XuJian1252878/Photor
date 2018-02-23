package com.example.photopicker.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.photopicker.PhotoPickerActivity;
import com.example.photopicker.R;
import com.example.photopicker.adapter.PhotoPagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xujian on 2018/2/6.
 */

public class ImagePagerFragment extends Fragment {

    public final static String ARG_PATH = "PATHS";
    public final static String ARG_CURRENT_ITEM = "ARG_CURRENT_ITEM";

    private ArrayList<String> paths;
    private ViewPager mViewPager;
    private PhotoPagerAdapter mPagerAdapter;

    private int currentItem = 0;

    public static ImagePagerFragment newInstance(List<String> paths, int currentItem) {
        ImagePagerFragment f = new ImagePagerFragment();

        Bundle args = new Bundle();
        args.putStringArray(ARG_PATH, paths.toArray(new String[paths.size()]));
        args.putInt(ARG_CURRENT_ITEM, currentItem);

        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        paths = new ArrayList<>();
        Bundle bundle = getArguments();
        if (bundle != null) {
            String[] pathArr = bundle.getStringArray(ARG_PATH);
            paths.clear();
            if (pathArr != null) {
                paths = new ArrayList<>(Arrays.asList(pathArr));
            }
            currentItem = bundle.getInt(ARG_CURRENT_ITEM);
        }

        // 创建PagerAdapter的实例
        mPagerAdapter = new PhotoPagerAdapter(paths, Glide.with(this));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.__picker_fragment_image_pager, container, false);

        mViewPager = rootView.findViewById(R.id.vp_photos);
        mViewPager.setAdapter(mPagerAdapter);
        // 设置当前选择的照片在其文件夹下的下标 Set the currently selected page.
        mViewPager.setCurrentItem(currentItem);
        // Set the number of pages that should be retained to either side of the current page in
        // the view hierarchy in an idle state. Pages beyond this limit will be recreated from the adapter when needed.
        // 设置预先加载的ViewPager的图片，使得ViewPager的图片切换的时候不会卡顿
        mViewPager.setOffscreenPageLimit(5);

        return rootView;
    }

    // 更新activity中的标题信息

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof PhotoPickerActivity) {
            PhotoPickerActivity photoPickerActivity = (PhotoPickerActivity) getActivity();
            photoPickerActivity.updateTitleDoneItem();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        paths.clear();
        paths = null;

        if (mViewPager != null) {
            mViewPager.setAdapter(null);
        }
    }

    public ArrayList<String> getPaths() {
        return paths;
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    // 返回一个list，其中只有一个元素，即为当前选择的图片路径
    // 因为不论是选一张还是选择多张，都会以一个路径list的方式提供给调用者
    public ArrayList<String> getCurrentPath() {
        ArrayList<String> list = new ArrayList<>();
        int position = mViewPager.getCurrentItem();
        if (paths != null && paths.size() > position) {
            list.add(paths.get(position));
        }
        return list;
    }

    // 返回当前在ViewPager中选择的图片 在图片文件夹中的下标信息（与之前的currentItem下标可能不一样，因为用户可能滑动过）
    public int getCurrentItem() {
        return mViewPager.getCurrentItem();
    }

    public void setPhotos(List<String> paths, int currentItem) {
        this.paths.clear();
        this.paths.addAll(paths);
        this.currentItem = currentItem;

        mViewPager.setCurrentItem(currentItem);
        mViewPager.getAdapter().notifyDataSetChanged();
    }
}
