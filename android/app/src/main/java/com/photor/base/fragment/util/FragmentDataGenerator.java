package com.photor.base.fragment.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.photor.base.fragment.AlbumFragment;
import com.photor.base.fragment.CameraFragment;
import com.photor.base.fragment.GalleryFragment;
import com.photor.base.fragment.HomeFragment;

/**
 * Created by xujian on 2018/2/26.
 */

public class FragmentDataGenerator {

    public static final int MAIN_FRAGMENT_PARTS = 4;

    public static final Fragment[] FRAGMENTS = new Fragment[MAIN_FRAGMENT_PARTS];

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
            AlbumFragment albumFragment = (AlbumFragment) fragmentManager.findFragmentByTag(BottomNavigationEnum.RESOURCE.getTag());
            if (albumFragment == null) {
                albumFragment = AlbumFragment.newInstance();
//                fragmentTransaction.add(albumFragment, BottomNavigationEnum.RESOURCE.getTag()).commit();
            }
//            fragmentManager.executePendingTransactions();
            return albumFragment;
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
