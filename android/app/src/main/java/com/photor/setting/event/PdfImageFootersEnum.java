package com.photor.setting.event;

import com.photor.R;

/**
 * @author htwxujian@gmail.com
 * @date 2018/12/5 21:51
 */
public enum PdfImageFootersEnum {

    FOOTER_NO(0, R.string.pdf_image_no),
    FOOTER_YES(1, R.string.pdf_image_yes);

    private int index;
    private int labelId;

    PdfImageFootersEnum(int index, int labelId) {
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
