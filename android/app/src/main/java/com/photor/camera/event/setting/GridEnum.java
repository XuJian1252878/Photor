package com.photor.camera.event.setting;

import com.otaliastudios.cameraview.Grid;
import com.photor.R;

public enum GridEnum {

    GRID_OFF(0, Grid.OFF, R.string.camera_grid_off_label),
    GRID_3_3(1, Grid.DRAW_3X3, R.string.camera_grid_3_3_label),
    GRID_4_4(2, Grid.DRAW_4X4, R.string.camera_grid_4_4_label),
    GRID_PHI(3, Grid.DRAW_PHI, R.string.camera_grid_phi_label);

    private int index;
    private Grid grid;
    private int messageId;

    public int getIndex() {
        return this.index;
    }

    public Grid getGrid() {
        return this.grid;
    }

    public int getMessageId() {
        return this.messageId;
    }

    public static Grid getGridByIndex(int index) {
        for (GridEnum ge: GridEnum.values()) {
            if (ge.index == index) {
                return ge.grid;
            }
        }
        return null;
    }

    public static int getMessageIdByIndex(int index) {
        for (GridEnum ge: GridEnum.values()) {
            if (ge.index == index) {
                return ge.messageId;
            }
        }

        return -1;
    }

    GridEnum(int index, Grid grid, int messageId) {
        this.index = index;
        this.grid = grid;
        this.messageId = messageId;
    }
}
