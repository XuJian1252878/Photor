package com.photor.base.fragment.util;

import com.photor.R;

/**
 * Created by xujian on 2018/2/26.
 */

public enum  BottomNavigationEnum {

    HOME(0, R.id.menu_main_bottom_tab_home, "HOME_MAIN_BOTTOM_NAVIGATION"),
    GALLERY(1, R.id.menu_main_bottom_tab_gallery, "GALLERY_MAIN_BOTTOM_NAVIGATION"),
    RESOURCE(2, R.id.menu_main_bottom_tab_resource, "RESOURCE_MAIN_BOTTOM_NAVIGATION"),
    CAMERA(3, R.id.menu_main_bottom_tab_camera, "CAMERA_MAIN_BOTTOM_NAVIGATION");

    private int navItemIndex;  // 底部导航栏的下标
    private int navItemId;  // 底部导航栏的资源id
    private String tag;  // 底部导航栏的tag

    BottomNavigationEnum(int index, int id, String tag) {

        this.navItemIndex = index;
        this.navItemId = id;
        this.tag = tag;
    }

    public int getNavItemIndex() {
        return navItemIndex;
    }

    public int getNavItemId() {
        return navItemId;
    }

    public String getTag() {
        return tag;
    }

    public static int findNavIndexById(int id) {
        for(BottomNavigationEnum bne: BottomNavigationEnum.values()) {
            if (bne.getNavItemId() == id) {
                return bne.getNavItemIndex();
            }
        }
        return -1;
    }
}
