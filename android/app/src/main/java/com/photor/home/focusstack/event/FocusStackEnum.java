package com.photor.home.focusstack.event;

/**
 * @author htwxujian@gmail.com
 * @date 2018/10/18 14:54
 */
public enum FocusStackEnum {
    FOCUS_STACK_SUCCESS(1, "景深合成成功"),
    FOCUS_STACK_FAILED(-1, "景深合成失败"),
    FOCUS_STACK_SELECT_PHOTO(0, "选择照片"),
    FOCUS_STACK_RESULT(1, "景深合成");

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    FocusStackEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
