package com.photor.exposure.event;

public enum ExposureEnum {

    EXPOSURE_SELECT_PHOTOS(0, "选择曝光图片组"),
    EXPOSURE_RESULT(1, "曝光合成");

    private int code;
    private String message;

    ExposureEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public static String getMessageByCode(int code) {
        for (ExposureEnum ee: ExposureEnum.values()) {
            if (ee.code == code) {
                return ee.message;
            }
        }

        return null;
    }

}
