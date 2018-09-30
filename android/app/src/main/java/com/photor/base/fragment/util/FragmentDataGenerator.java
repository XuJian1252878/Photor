package com.photor.base.fragment.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.orhanobut.logger.Logger;
import com.photor.base.fragment.CameraFragment;
import com.photor.base.fragment.GalleryFragment;
import com.photor.base.fragment.HomeFragment;
import com.photor.base.fragment.ResourceFragment;

import java.util.Arrays;
import java.util.List;

/**
 * Created by xujian on 2018/2/26.
 */

public class FragmentDataGenerator {

    private static final int MAIN_FRAGMENT_PARTS = 4;

    /**
     * 获取主页面底部导航栏的fragment
     * @return
     */
    public static List<Fragment> getMainFragments(FragmentManager fragmentManager) {
        Fragment[] fragments = new Fragment[MAIN_FRAGMENT_PARTS];

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // 利用tag首先查找fragment，利于后来fragment的状态保存。tag相当于fragment的id信息。
        HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(BottomNavigationEnum.HOME.getTag());
        if (homeFragment == null) {
            homeFragment = HomeFragment.newInstance();
            // 添加fragmentmanager信息
            fragmentTransaction.add(homeFragment, BottomNavigationEnum.HOME.getTag());
        }

        GalleryFragment galleryFragment = (GalleryFragment) fragmentManager.findFragmentByTag(BottomNavigationEnum.GALLERY.getTag());
        if (galleryFragment == null) {
            galleryFragment = GalleryFragment.newInstance();
            fragmentTransaction.add(galleryFragment, BottomNavigationEnum.GALLERY.getTag());
        }

        ResourceFragment resourceFragment = (ResourceFragment) fragmentManager.findFragmentByTag(BottomNavigationEnum.RESOURCE.getTag());
        if (resourceFragment == null) {
            resourceFragment = ResourceFragment.newInstance();
            fragmentTransaction.add(resourceFragment, BottomNavigationEnum.RESOURCE.getTag());
        }

        CameraFragment cameraFragment = (CameraFragment) fragmentManager.findFragmentByTag(BottomNavigationEnum.CAMERA.getTag());
        if (cameraFragment == null) {
            cameraFragment = CameraFragment.newInstance();
            fragmentTransaction.add(cameraFragment, BottomNavigationEnum.CAMERA.getTag());
        }

        fragmentManager.executePendingTransactions();

        fragments[BottomNavigationEnum.HOME.getNavItemIndex()] = homeFragment;
        fragments[BottomNavigationEnum.GALLERY.getNavItemIndex()] = galleryFragment;
        fragments[BottomNavigationEnum.RESOURCE.getNavItemIndex()] = resourceFragment;
        fragments[BottomNavigationEnum.CAMERA.getNavItemIndex()] = cameraFragment;

        List<Fragment> fragmentList = Arrays.asList(fragments);
        return fragmentList;
    }


}
