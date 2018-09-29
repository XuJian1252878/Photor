package com.photor.camera.event.setting;

import com.otaliastudios.cameraview.WhiteBalance;
import com.photor.R;

public enum WhiteBalanceEnum {
    AUTO(0, WhiteBalance.AUTO, R.string.camera_white_balance_auto),
    INCANDESCENT(1, WhiteBalance.INCANDESCENT, R.string.camera_white_balance_incandescent),
    FLUORESCENT(2, WhiteBalance.FLUORESCENT, R.string.camera_white_balance_fluorescent),
    DAYLIGHT(3, WhiteBalance.DAYLIGHT, R.string.camera_white_balance_daylight),
    CLOUDY(4, WhiteBalance.CLOUDY, R.string.camera_white_balance_cloudy);

    private int index;
    private WhiteBalance whiteBalance;
    private int messageId;

    public int getMessageId() {
        return this.messageId;
    }

    public static int getIndexByWhiteBalance(WhiteBalance whiteBalance) {
        for (WhiteBalanceEnum wbe: WhiteBalanceEnum.values()) {
            if (wbe.whiteBalance == whiteBalance) {
                return wbe.index;
            }
        }
        return -1;
    }

    public static WhiteBalance getWhiteBalanceByIndex(int index) {
        for (WhiteBalanceEnum wbe: WhiteBalanceEnum.values()) {
            if (wbe.index == index) {
                return wbe.whiteBalance;
            }
        }
        return null;
    }

    WhiteBalanceEnum(int index, WhiteBalance whiteBalance, int messageId) {
        this.index = index;
        this.whiteBalance = whiteBalance;
        this.messageId = messageId;
    }
}
