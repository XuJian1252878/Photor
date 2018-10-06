package com.photor.album.entity;

import android.content.Context;
import android.support.annotation.Nullable;

import com.photor.album.utils.CustomAlbumsHelper;

public class AlbumSettings {


    private String path;
    private String coverPath;
    private int sortingMode;
    private int sortingOrder;
    private boolean pinned;

    private FilterMode filterMode = FilterMode.ALL;

    public static AlbumSettings getSettings(Context context, Album album) {
        CustomAlbumsHelper h = CustomAlbumsHelper.getInstance(context);
        // 从数据库获得对应album的封面图片路径，排序模式、升序降序等信息
        return h.getSettings(album.getPath());
    }

    static AlbumSettings getDefaults() {
        return new AlbumSettings(null, null, SortingMode.DATE.getValue(), SortingOrder.DESCENDING.getValue(), 0);
    }


    public AlbumSettings(String path, String cover, int sortingMode, int sortingOrder, int pinned) {
        this.path = path;
        this.coverPath = cover;  // 相册对应的相册封面信息
        this.sortingMode = sortingMode;
        this.sortingOrder = sortingOrder;
        this.pinned = pinned == 1;
    }

    FilterMode getFilterMode() {
        return filterMode;
    }

    void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
    }

    String getCoverPath() {
        return coverPath;
    }

    public SortingMode getSortingMode() {
        return SortingMode.fromValue(sortingMode);
    }

    public SortingOrder getSortingOrder() {
        return SortingOrder.fromValue(sortingOrder);
    }

    void changeSortingMode(Context context, SortingMode sortingMode) {
        this.sortingMode = sortingMode.getValue();
        CustomAlbumsHelper h = CustomAlbumsHelper.getInstance(context);
        h.setAlbumSortingMode(path, sortingMode.getValue());
    }

    void changeSortingOrder(Context context, SortingOrder sortingOrder) {
        this.sortingOrder = sortingOrder.getValue();
        CustomAlbumsHelper h = CustomAlbumsHelper.getInstance(context);
        h.setAlbumSortingOrder(path, sortingOrder.getValue());
    }

    public void changeCoverPath(Context context, @Nullable String coverPath) {
        this.coverPath = coverPath;
        CustomAlbumsHelper h = CustomAlbumsHelper.getInstance(context);
        h.setAlbumPhotoPreview(path, coverPath);
    }

    boolean isPinned() {
        return pinned;
    }

    public void togglePin(Context context) {
        this.pinned = !pinned;
        CustomAlbumsHelper h = CustomAlbumsHelper.getInstance(context);
        h.pinAlbum(path, pinned);
    }

}
