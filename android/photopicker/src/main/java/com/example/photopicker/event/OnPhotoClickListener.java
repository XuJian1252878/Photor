package com.example.photopicker.event;

import android.view.View;

/**
 * Created by xujian on 2018/2/5.
 */

public interface OnPhotoClickListener {
    /**
     * 图片被点击之后的动作
     * @param v
     * @param position
     * @param showCamera
     */
    void onClick(View v, int position, boolean showCamera);
}
