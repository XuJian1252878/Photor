package com.example.photopicker.event;

import com.example.media.image.entity.Photo;

/**
 * Created by xujian on 2018/2/5.
 */

public interface OnItemCheckListener {

    /***
     * 在图片上的选择按钮被点击的时候，判断该图片是否应该被选择，比如图片数量已经足够等等
     * @param position 所选图片的位置
     * @param path     所选的图片
     * @param selectedItemCount  已选数量
     * @return enable check
     */
    boolean onItemCheck(int position, Photo path, int selectedItemCount);
}
