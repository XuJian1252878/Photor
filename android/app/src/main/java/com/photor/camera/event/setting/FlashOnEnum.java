package com.photor.camera.event.setting;

import com.otaliastudios.cameraview.Flash;
import com.photor.R;

/**
 * 相机的闪光灯设置
 */
public enum FlashOnEnum {

    FLASH_OFF(0, Flash.OFF, R.id.camera_flash_off_btn, R.drawable.popup_flash_off, R.string.camera_flash_off_label),
    FLASH_ON(1, Flash.ON, R.id.camera_flash_on_btn, R.drawable.popup_flash_on, R.string.camera_flash_on_label),
    FLASH_AUTO(2, Flash.AUTO, R.id.camera_flash_auto_btn, R.drawable.popup_flash_auto, R.string.camera_flash_auto_label),
    FLASH_TORCH(3, Flash.TORCH, R.id.camera_flash_torch_btn, R.drawable.popup_flash_torch, R.string.camera_flash_torch_label);

    private int index;
    private Flash flash;  // camera view 的对应功能flag
    private int rid; // 按钮的资源id
    private int srcid; // 按钮的srcid
    private int messageId; // 提示信息

    FlashOnEnum(int index, Flash flash, int rid, int srcid,int messageId) {
        this.index = index;
        this.flash = flash;
        this.rid = rid;
        this.srcid = srcid;
        this.messageId = messageId;
    }

    public Flash getFlash() {
        return flash;
    }

    public int getRid() {
        return this.rid;
    }

    public int getMessageId() {
        return this.messageId;
    }

    public static int getRidByFlash(Flash flash) {
        for (FlashOnEnum foe: FlashOnEnum.values()) {
            if (foe.flash == flash) {
                return foe.rid;
            }
        }
        return -1;
    }
}
