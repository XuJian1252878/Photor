package com.example.photopicker.event;

import com.example.photopicker.entity.Photo;

/**
 * Created by xujian on 2018/2/5.
 */

public interface Selectable {

    /**
     * Indicates if the item at position position is selected
     *
     * @param photo Photo of the item to check
     * @return true if the item is selected, false otherwise
     */
    boolean isSelected(Photo photo);


    /**
     * Toggle the selection status of the item at a given position
     *
     * 设置被选择的photo
     *
     * @param photo Photo of the item to toggle the selection status for
     */
    void toggleSelection(Photo photo);

    /**
     * Clear the selection status for all items
     * 设置被取消选择的photo
     */
    void clearSelection();


    /**
     * Count the selected items
     * 获取当前一共有多少张照片被选择了
     * @return Selected items count
     */
    int getSelectedItemCount();
}
