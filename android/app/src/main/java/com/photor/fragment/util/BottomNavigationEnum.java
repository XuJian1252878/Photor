package com.photor.fragment.util;

/**
 * Created by xujian on 2018/2/26.
 */

public enum  BottomNavigationEnum {

    HOME(0, "HOME_MAIN_BOTTOM_NAVIGATION"),
    GALLERY(1, "GALLERY_MAIN_BOTTOM_NAVIGATION"),
    RESOURCE(2, "RESOURCE_MAIN_BOTTOM_NAVIGATION");

    private int navigationItemIndex;
    private String tag;

    BottomNavigationEnum(int index, String tag) {

        this.navigationItemIndex = index;
        this.tag = tag;
    }

    public int getNavigationItemIndex() {
        return navigationItemIndex;
    }

    public String getTag() {
        return tag;
    }
}
