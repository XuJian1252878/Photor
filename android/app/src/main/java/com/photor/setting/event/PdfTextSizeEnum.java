package com.photor.setting.event;

import com.photor.R;

/**
 * @author htwxujian@gmail.com
 * @date 2018/12/6 16:32
 */
public enum PdfTextSizeEnum {

    TZ12(0, 12, R.string.pdf_image_tz_12),
    TZ14(1, 14, R.string.pdf_image_tz_14),
    TZ16(2, 16, R.string.pdf_image_tz_16),
    TZ18(3, 18, R.string.pdf_image_tz_18),
    TZ20(4, 20, R.string.pdf_image_tz_20),
    TZ25(5, 25, R.string.pdf_image_tz_25),
    TZ30(6, 30, R.string.pdf_image_tz_30),
    TZ35(7, 35, R.string.pdf_image_tz_35),
    TZ50(8, 50, R.string.pdf_image_tz_50),
    TZ70(9, 70, R.string.pdf_image_tz_70),
    TZ100(10, 100, R.string.pdf_image_tz_100),
    ;

    private int id;
    private int textSize;
    private int stringId;

    PdfTextSizeEnum(int id, int textSize, int stringId) {
        this.id = id;
        this.textSize = textSize;
        this.stringId = stringId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getStringId() {
        return stringId;
    }

    public void setStringId(int stringId) {
        this.stringId = stringId;
    }

    public static int getTextSizeById(int otherId) {
        for (PdfTextSizeEnum ptse: PdfTextSizeEnum.values()) {
            if (ptse.getId() == otherId) {
                return ptse.getTextSize();
            }
        }
        return TZ12.getTextSize();
    }

    public static int getIdByTextSize(int textSize) {
        for (PdfTextSizeEnum ptse: PdfTextSizeEnum.values()) {
            if (ptse.getTextSize() == textSize) {
                return ptse.getId();
            }
        }
        return TZ12.getId();
    }
}
