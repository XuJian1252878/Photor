package com.photor.setting.event;

import com.photor.R;

/**
 * @author htwxujian@gmail.com
 * @date 2018/12/5 21:49
 */
public enum PdfImageHeadersEnum {

    HEADERS_NO(0, R.string.pdf_image_no),
    HEADERS_YES(1, R.string.pdf_image_yes);

    private int index;
    private int labelId;

    PdfImageHeadersEnum(int index, int labelId) {
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
