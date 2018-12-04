package com.photor.setting.event;

import com.photor.R;

/**
 * @author htwxujian@gmail.com
 * @date 2018/12/4 23:45
 */
public enum PdfImageDisplayEnum {

    CENTER_PARENT(0, R.string.pdf_image_display_one_page_center_label),
    FILL_PARENT(1, R.string.pdf_image_display_one_page_fill_label);

    private int index;
    private int labelId;

    PdfImageDisplayEnum(int index, int labelId) {
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
