package com.photor.staralign.event;

/**
 * Created by xujian on 2018/3/2.
 */

public enum StarAlignEnum {

    STAR_ALIGN_RESLUT_SUCCESS(1, "对齐操作成功"),
    STAR_ALIGN_NOT_ENOUGH(-1, "没有足够的图片进行对齐"),
    STAR_ALIGN_NOT_SELECTED(-2, "没有选择要对齐的星空图片"),
    STAR_ALIGN_RESULT_SUSPEND(-3, "对齐操作被中断"),

    STAR_ALIGN_SELECT_PHOTOS(0, "选择图片"),
    STAR_ALIGN_BOUNDARY(1, "划定分界线"),
    STAR_ALIGN_RESULT(2, "图片对齐结果");

    private int code;
    private String resIllustrate;

    StarAlignEnum(int resCode, String resIllustrate) {
        this.code = resCode;
        this.resIllustrate = resIllustrate;
    }

    public int getCode() {
        return code;
    }

    public String getResIllustrate() {
        return resIllustrate;
    }

    public static String getIllusstrateByCode(int resCode) {
        for (StarAlignEnum sae: StarAlignEnum.values()) {
            if (sae.code == resCode) {
                return sae.resIllustrate;
            }
        }

        return null;
    }
}
