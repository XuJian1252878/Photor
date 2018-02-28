package com.photor.base.fragment.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.photor.base.fragment.GalleryFragment;
import com.photor.base.fragment.HomeFragment;
import com.photor.base.fragment.ResourceFragment;

import java.util.Arrays;
import java.util.List;

/**
 * Created by xujian on 2018/2/26.
 */

public class FragmentDataGenerator {

    /**
     * 获取主页面底部导航栏的fragment
     * @return
     */
    public static List<Fragment> getMainFragments(FragmentManager fragmentManager) {
        Fragment[] fragments = new Fragment[3];

        // 利用tag首先查找fragment，利于后来fragment的状态保存。tag相当于fragment的id信息。
        HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(BottomNavigationEnum.HOME.getTag());
        if (homeFragment == null) {
            homeFragment = HomeFragment.newInstance();
        }

        GalleryFragment galleryFragment = (GalleryFragment) fragmentManager.findFragmentByTag(BottomNavigationEnum.GALLERY.getTag());
        if (galleryFragment == null) {
            galleryFragment = GalleryFragment.newInstance();
        }

        ResourceFragment resourceFragment = (ResourceFragment) fragmentManager.findFragmentByTag(BottomNavigationEnum.RESOURCE.getTag());
        if (resourceFragment == null) {
            resourceFragment = ResourceFragment.newInstance();
        }

        fragments[BottomNavigationEnum.HOME.getNavItemIndex()] = homeFragment;
        fragments[BottomNavigationEnum.GALLERY.getNavItemIndex()] = galleryFragment;
        fragments[BottomNavigationEnum.RESOURCE.getNavItemIndex()] = resourceFragment;

        List<Fragment> fragmentList = Arrays.asList(fragments);
        return fragmentList;
    }


}
