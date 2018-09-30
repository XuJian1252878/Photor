package com.photor.base.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.orhanobut.logger.Logger;
import com.photor.base.fragment.util.BottomNavigationEnum;
import com.photor.base.fragment.util.FragmentDataGenerator;

import java.util.List;

/**
 * Created by xujian on 2018/2/26.
 */

public class MainViewPagerAdapter extends FragmentPagerAdapter {

    private FragmentManager fragmentManager;

    public MainViewPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fragmentManager = fm;
    }

    @Override
    public int getCount() {
        return FragmentDataGenerator.MAIN_FRAGMENT_PARTS;
    }

    @Override
    public Fragment getItem(int position) {
        Logger.d("MainViewPagerAdapter: " + position);
//        return fragments.get(position);
        return FragmentDataGenerator.createFragmentForViewPagerGetItem(fragmentManager, position);
    }

//    @Override
//    public Object instantiateItem(ViewGroup container, int position) {
//        Fragment fragment = (Fragment) super.instantiateItem(container, position);
//        FragmentDataGenerator.collectFragmentForViewPager(fragment, position);
//        return fragment;
//    }
}
