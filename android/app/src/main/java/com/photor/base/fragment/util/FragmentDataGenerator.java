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

    public static final int MAIN_FRAGMENT_PARTS = 4;

    private static Fragment[] FRAGMENTS = new Fragment[MAIN_FRAGMENT_PARTS];

    public static void collectFragmentForViewPager(Fragment fragment, int position) {
        FRAGMENTS[position] = fragment;
    }

    /**
     * 获取主页面底部导航栏的fragment
     * @return
     */
    public static Fragment createFragmentForViewPagerGetItem(FragmentManager fragmentManager, int position) {
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // 利用tag首先查找fragment，利于后来fragment的状态保存。tag相当于fragment的id信息。
        if (position == BottomNavigationEnum.HOME.getNavItemIndex()) {
            HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentByTag(BottomNavigationEnum.HOME.getTag());
            if (homeFragment == null) {
                homeFragment = HomeFragment.newInstance();
                // 添加fragmentmanager信息
                // FragmentPagerAdapter 会自动给之中的fragment加 tag，把fragment加入到 FragmentManager中
                // 所以开发者没有必要自己去做 fragmentTransaction.add tag的操作
//                fragmentTransaction.add(homeFragment, BottomNavigationEnum.HOME.getTag()).commit();
            }
//            fragmentManager.executePendingTransactions();
            return homeFragment;
        }


        if (position == BottomNavigationEnum.GALLERY.getNavItemIndex()) {
            GalleryFragment galleryFragment = (GalleryFragment) fragmentManager.findFragmentByTag(BottomNavigationEnum.GALLERY.getTag());
            if (galleryFragment == null) {
                galleryFragment = GalleryFragment.newInstance();
//                fragmentTransaction.add(galleryFragment, BottomNavigationEnum.GALLERY.getTag()).commit();
            }
//            fragmentManager.executePendingTransactions();
            return galleryFragment;
        }


        if (position == BottomNavigationEnum.RESOURCE.getNavItemIndex()) {
            ResourceFragment resourceFragment = (ResourceFragment) fragmentManager.findFragmentByTag(BottomNavigationEnum.RESOURCE.getTag());
            if (resourceFragment == null) {
                resourceFragment = ResourceFragment.newInstance();
//                fragmentTransaction.add(resourceFragment, BottomNavigationEnum.RESOURCE.getTag()).commit();
            }
//            fragmentManager.executePendingTransactions();
            return resourceFragment;
        }

        if (position == BottomNavigationEnum.CAMERA.getNavItemIndex()) {
            CameraFragment cameraFragment = (CameraFragment) fragmentManager.findFragmentByTag(BottomNavigationEnum.CAMERA.getTag());
            if (cameraFragment == null) {
                cameraFragment = CameraFragment.newInstance();
//                fragmentTransaction.add(cameraFragment, BottomNavigationEnum.CAMERA.getTag()).commit();
            }
//            fragmentManager.executePendingTransactions();
            return cameraFragment;
        }

        return null;
    }


}
