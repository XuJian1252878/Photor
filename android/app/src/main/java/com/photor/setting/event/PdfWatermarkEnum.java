package com.photor.setting.event;

import com.photor.R;

/**
 * @author htwxujian@gmail.com
 * @date 2018/12/5 13:11
 */
public enum PdfWatermarkEnum {
    NO_WATERMARK(0, R.string.pdf_image_watermark_no),
    YES_WATERMARK(1, R.string.pdf_image_watermark_yes);

    private int index;
    private int labelId;

    PdfWatermarkEnum(int index, int labelId) {
        this.index = index;
        this.labelId = labelId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getLabelId() {
        return labelId;
    }

    public void setLabelId(int labelId) {
        this.labelId = labelId;
    }
}
