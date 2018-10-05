package com.photor.album.entity;

import android.content.Context;

import com.photor.R;
import com.photor.album.entity.comparator.AlbumsComparators;
import com.photor.album.provider.MediaStoreProvider;
import com.photor.album.utils.PreferenceUtil;

import java.util.ArrayList;
import java.util.Collections;

public class HandlingAlbums {

    public final static String TAG = "HandlingAlbums";
    private static String backupFile = "albums.dat";

    public ArrayList<Album> dispAlbums;
    private ArrayList<Album> selectedAlbums;

    private PreferenceUtil SP;

    private int current = 0;  // 当前被选中的相册信息
    private boolean hidden;

    public HandlingAlbums(Context context) {
        SP = PreferenceUtil.getInstance(context);
        dispAlbums = new ArrayList<Album>();
        selectedAlbums = new ArrayList<Album>();
    }

    public void loadAlbums(Context context, boolean hidden) {
        this.hidden = hidden;

        ArrayList<Album> list = new ArrayList<Album>();
        if (SP.getBoolean(context.getString(R.string.preference_use_alternative_provider), false)) {
//            StorageProvider p = new StorageProvider(context);
//            list = p.getAlbums(context, hidden);
        } else {
            // 获得初步的 图片文件夹信息（album），并没有加载全部的图片信息
            list.addAll(MediaStoreProvider.getAlbums(context, hidden));
        }
        dispAlbums = list;
        sortAlbums();
    }

    public void sortAlbums() {
        Collections.sort(dispAlbums, AlbumsComparators.getComparator(getSortingMode(), getSortingOrder()));
    }

    public SortingMode getSortingMode() {
        return SortingMode.fromValue(SP.getInt("albums_sorting_mode", SortingMode.DATE.getValue()));
    }

    public SortingOrder getSortingOrder() {
        return SortingOrder.fromValue(SP.getInt("albums_sorting_order", SortingOrder.DESCENDING.getValue()));
    }

    public Album getCurrentAlbum() {
        return dispAlbums.get(current);
    }
    
    public void clearSelectedAlbums() {
        for (Album dispAlbum : dispAlbums)
            dispAlbum.setSelected(false);

        selectedAlbums.clear();
    }

}
